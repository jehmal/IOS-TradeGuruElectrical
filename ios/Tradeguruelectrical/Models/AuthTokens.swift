import Foundation

nonisolated struct AuthTokens: Codable {
    var accessToken: String
    var refreshToken: String
    var expiresAt: Date
}
