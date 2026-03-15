import Foundation

nonisolated struct AuthUser: Codable {
    var id: String
    var email: String
    var name: String?
    var pictureURL: String?
}
