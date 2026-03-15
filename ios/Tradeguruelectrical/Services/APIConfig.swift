import Foundation

nonisolated enum APIConfig: Sendable {
    #if DEBUG
    nonisolated(unsafe) static var useMockData = false
    #endif
    static let baseURL = "https://tradeguru.com.au/api/v1"
    static let platform = "ios"
    static let appVersion = "1.0.0"
    static let workosClientId = "client_01JWQK8QD9RJVTCTMR8ACE9CKB"
    static let authProxyURL = "https://tradeguru.com.au/api/workos-auth-proxy"
    static let oauthRedirectURI = "tradeguru://auth-callback"

    static func url(_ path: String) -> URL {
        let clean = path.hasPrefix("/") ? String(path.dropFirst()) : path
        guard let url = URL(string: "\(baseURL)/\(clean)") else {
            fatalError("Invalid API URL: \(baseURL)/\(clean)")
        }
        return url
    }

    static func headers(deviceId: String, jwt: String? = nil) -> [String: String] {
        var h = [
            "Content-Type": "application/json",
            "X-Device-ID": deviceId,
        ]
        if let jwt {
            h["Authorization"] = "Bearer \(jwt)"
        }
        return h
    }

    static func request(_ path: String, method: String = "POST", deviceId: String, jwt: String? = nil) -> URLRequest {
        var req = URLRequest(url: url(path))
        req.httpMethod = method
        req.timeoutInterval = 120
        for (key, value) in headers(deviceId: deviceId, jwt: jwt) {
            req.setValue(value, forHTTPHeaderField: key)
        }
        return req
    }
}
