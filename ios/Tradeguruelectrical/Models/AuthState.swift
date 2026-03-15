import Foundation

nonisolated enum AuthState: Sendable {
    case anonymous
    case authenticated(user: AuthUser)
}
