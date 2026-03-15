import Foundation
import SwiftData

nonisolated enum MessageRole: String, Codable {
    case user
    case assistant
}

@Model
class ChatMessage {
    var id: UUID
    var role: MessageRole
    @Relationship(deleteRule: .cascade, inverse: \ContentBlock.message) var blocks: [ContentBlock]
    var timestamp: Date
    var mode: ThinkingMode
    @Relationship(deleteRule: .cascade, inverse: \MessageAttachment.message) var attachments: [MessageAttachment]?
    var conversation: Conversation?

    init(
        id: UUID = UUID(),
        role: MessageRole,
        blocks: [ContentBlock],
        timestamp: Date = Date(),
        mode: ThinkingMode,
        attachments: [MessageAttachment]? = nil
    ) {
        self.id = id
        self.role = role
        self.blocks = blocks
        self.timestamp = timestamp
        self.mode = mode
        self.attachments = attachments
    }
}
