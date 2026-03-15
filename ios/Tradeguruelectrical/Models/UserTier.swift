import Foundation
import SwiftUI

enum UserTier: String, Codable, Sendable {
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

    var color: Color {
        switch self {
        case .free: .tradeTextSecondary
        case .pro: .modeLearn
        case .unlimited: .tradeGreen
        }
    }
}
