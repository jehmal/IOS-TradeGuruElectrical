import Foundation

nonisolated struct AuthUser: Codable, Sendable {
    var id: String
    var email: String
    var name: String?
    var pictureURL: String?
}
