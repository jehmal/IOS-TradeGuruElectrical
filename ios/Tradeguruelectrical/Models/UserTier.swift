import Foundation
import SwiftUI

nonisolated enum UserTier: String, Codable {
    case free
    case pro
    case unlimited

    var displayName: String {
        switch self {
        case .free: "Free"
        case .pro: "Pro"
        case .unlimited: "Unlimited"
        }
    }

    @MainActor var color: Color {
        switch self {
        case .free: .tradeTextSecondary
        case .pro: .modeLearn
        case .unlimited: .tradeGreen
        }
    }
}
