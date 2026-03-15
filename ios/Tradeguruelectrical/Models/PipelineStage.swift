import Foundation

nonisolated enum PipelineStage: String, Codable {
    case idle
    case searching
    case synthesizing
    case streaming
    case error
}
