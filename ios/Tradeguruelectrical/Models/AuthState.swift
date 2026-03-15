import Foundation

nonisolated enum AuthState {
    case anonymous
    case authenticated(user: AuthUser)
}
