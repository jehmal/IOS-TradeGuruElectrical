import Foundation

nonisolated enum PipelineStage: String, Codable, Sendable {
    case idle
    case searching
    case synthesizing
    case streaming
    case error
}
