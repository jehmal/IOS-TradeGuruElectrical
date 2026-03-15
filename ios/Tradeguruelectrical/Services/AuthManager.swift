import Foundation
import AuthenticationServices

@Observable
@MainActor
class AuthManager {
    static let shared = AuthManager()

    var authState: AuthState = .anonymous
    var tier: UserTier = .free
    var tokens: AuthTokens?

    var currentUser: AuthUser? {
        if case .authenticated(let user) = authState { return user }
        return nil
    }

    var isAuthenticated: Bool {
        currentUser != nil
    }

    private var pendingVerifier: String?

    private init() {}

    func signIn(provider: String?) async {
        let verifier = PKCEHelper.generateVerifier()
        let challenge = PKCEHelper.generateChallenge(from: verifier)
        pendingVerifier = verifier

        guard var components = URLComponents(string: "https://api.workos.com/user_management/authorize") else { return }
        components.queryItems = [
            URLQueryItem(name: "client_id", value: APIConfig.workosClientId),
            URLQueryItem(name: "redirect_uri", value: APIConfig.oauthRedirectURI),
            URLQueryItem(name: "response_type", value: "code"),
            URLQueryItem(name: "state", value: UUID().uuidString),
            URLQueryItem(name: "code_challenge", value: challenge),
            URLQueryItem(name: "code_challenge_method", value: "S256"),
        ]
        if let provider {
            components.queryItems?.append(URLQueryItem(name: "provider", value: provider))
        }

        guard let url = components.url else { return }

        let code: String? = await withCheckedContinuation { continuation in
            let session = ASWebAuthenticationSession(
                url: url,
                callbackURLScheme: "tradeguru"
            ) { callbackURL, error in
                guard error == nil,
                      let callbackURL,
                      let comps = URLComponents(url: callbackURL, resolvingAgainstBaseURL: false),
                      let codeValue = comps.queryItems?.first(where: { $0.name == "code" })?.value
                else {
                    continuation.resume(returning: nil)
                    return
                }
                continuation.resume(returning: codeValue)
            }
            session.prefersEphemeralWebBrowserSession = false
            session.presentationContextProvider = AuthPresentationContext.shared
            session.start()
        }

        guard let code, let verifier = pendingVerifier else {
            pendingVerifier = nil
            return
        }
        pendingVerifier = nil

        do {
            let tokens = try await exchangeCode(code, verifier: verifier)
            guard let decoded = JWTDecoder.decode(tokens.accessToken) else { return }

            let authTokens = AuthTokens(
                accessToken: tokens.accessToken,
                refreshToken: tokens.refreshToken,
                expiresAt: decoded.expiresAt
            )
            self.tokens = authTokens
            KeychainHelper.save(authTokens, forKey: "tokens")
            KeychainHelper.save(decoded.user, forKey: "user")
            authState = .authenticated(user: decoded.user)

            await linkDevice()
        } catch {}
    }

    func signOut() {
        KeychainHelper.delete(forKey: "tokens")
        KeychainHelper.delete(forKey: "user")
        tokens = nil
        authState = .anonymous
        tier = .free
    }

    func linkDevice() async {
        guard let jwt = tokens?.accessToken else { return }

        let deviceId = DeviceManager.deviceIdOrFallback()
        var req = APIConfig.request("device/link", deviceId: deviceId, jwt: jwt)
        let body: [String: String] = ["device_id": deviceId]
        req.httpBody = try? JSONEncoder().encode(body)

        guard let (data, response) = try? await URLSession.shared.data(for: req),
              let http = response as? HTTPURLResponse,
              (200...299).contains(http.statusCode),
              let json = try? JSONSerialization.jsonObject(with: data) as? [String: Any],
              let tierString = json["tier"] as? String,
              let newTier = UserTier(rawValue: tierString)
        else { return }

        tier = newTier
    }

    func restoreSession() async {
        let savedTokens: AuthTokens? = KeychainHelper.load(forKey: "tokens")
        let savedUser: AuthUser? = KeychainHelper.load(forKey: "user")

        guard let savedTokens, let savedUser else { return }

        if savedTokens.expiresAt > Date() {
            tokens = savedTokens
            authState = .authenticated(user: savedUser)
            await linkDevice()
        } else {
            await refreshToken(savedTokens)
        }
    }

    func refreshTokenIfNeeded() async {
        guard let current = tokens else { return }
        let fiveMinutes: TimeInterval = 5 * 60
        guard current.expiresAt.timeIntervalSinceNow < fiveMinutes else { return }

        await refreshToken(current)
    }

    private func refreshToken(_ current: AuthTokens) async {
        do {
            let refreshed = try await exchangeRefresh(current.refreshToken)
            guard let decoded = JWTDecoder.decode(refreshed.accessToken) else {
                signOut()
                return
            }

            let authTokens = AuthTokens(
                accessToken: refreshed.accessToken,
                refreshToken: refreshed.refreshToken,
                expiresAt: decoded.expiresAt
            )
            tokens = authTokens
            KeychainHelper.save(authTokens, forKey: "tokens")
            KeychainHelper.save(decoded.user, forKey: "user")
            authState = .authenticated(user: decoded.user)
        } catch {
            signOut()
        }
    }

    private func exchangeCode(_ code: String, verifier: String) async throws -> TokenResponse {
        guard let url = URL(string: "\(APIConfig.authProxyURL)/user_management/authenticate") else {
            throw URLError(.badURL)
        }
        var req = URLRequest(url: url)
        req.httpMethod = "POST"
        req.setValue("application/json", forHTTPHeaderField: "Content-Type")

        let body: [String: String] = [
            "grant_type": "authorization_code",
            "client_id": APIConfig.workosClientId,
            "code": code,
            "code_verifier": verifier,
        ]
        req.httpBody = try JSONEncoder().encode(body)

        let (data, _) = try await URLSession.shared.data(for: req)
        return try JSONDecoder().decode(TokenResponse.self, from: data)
    }

    private func exchangeRefresh(_ refreshToken: String) async throws -> TokenResponse {
        guard let url = URL(string: "\(APIConfig.authProxyURL)/user_management/authenticate") else {
            throw URLError(.badURL)
        }
        var req = URLRequest(url: url)
        req.httpMethod = "POST"
        req.setValue("application/json", forHTTPHeaderField: "Content-Type")

        let body: [String: String] = [
            "grant_type": "refresh_token",
            "client_id": APIConfig.workosClientId,
            "refresh_token": refreshToken,
        ]
        req.httpBody = try JSONEncoder().encode(body)

        let (data, _) = try await URLSession.shared.data(for: req)
        return try JSONDecoder().decode(TokenResponse.self, from: data)
    }
}

private nonisolated struct TokenResponse: Codable, Sendable {
    let accessToken: String
    let refreshToken: String

    enum CodingKeys: String, CodingKey {
        case accessToken = "access_token"
        case refreshToken = "refresh_token"
    }
}

@MainActor
private final class AuthPresentationContext: NSObject, ASWebAuthenticationPresentationContextProviding {
    static let shared = AuthPresentationContext()

    func presentationAnchor(for session: ASWebAuthenticationSession) -> ASPresentationAnchor {
        guard let scene = UIApplication.shared.connectedScenes.first as? UIWindowScene,
              let window = scene.windows.first
        else {
            return ASPresentationAnchor()
        }
        return window
    }
}
