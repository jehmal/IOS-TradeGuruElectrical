import SwiftUI

nonisolated enum ThinkingMode: String, CaseIterable, Identifiable, Codable {
    case faultFinder = "fault_finder"
    case learn = "learn"
    case research = "research"

    nonisolated var id: String { rawValue }

    nonisolated var name: String {
        switch self {
        case .faultFinder: "Fault Finder"
        case .learn: "Learn"
        case .research: "Research"
        }
    }

    nonisolated var shortDescription: String {
        switch self {
        case .faultFinder: "Get it fixed"
        case .learn: "Show me how"
        case .research: "Look it up"
        }
    }

    nonisolated var icon: String {
        switch self {
        case .faultFinder: "bolt.fill"
        case .learn: "book.fill"
        case .research: "magnifyingglass"
        }
    }

    var color: Color {
        switch self {
        case .faultFinder: .modeFaultFinder
        case .learn: .modeLearn
        case .research: .modeResearch
        }
    }

    nonisolated var fullDescription: String {
        switch self {
        case .faultFinder:
            "Diagnose electrical faults, trace circuits, and identify issues with AI-assisted troubleshooting."
        case .learn:
            "Study electrical theory, code compliance, and best practices with interactive AI tutoring."
        case .research:
            "Research products, specifications, regulations, and technical documentation."
        }
    }
}
