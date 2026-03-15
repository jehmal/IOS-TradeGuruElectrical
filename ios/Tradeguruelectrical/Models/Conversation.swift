import Foundation
import SwiftData

@Model
class Conversation {
    var id: UUID
    var title: String
    @Relationship(deleteRule: .cascade, inverse: \ChatMessage.conversation) var messages: [ChatMessage]
    var mode: ThinkingMode
    var createdAt: Date
    var updatedAt: Date

    init(
        id: UUID = UUID(),
        title: String,
        messages: [ChatMessage] = [],
        mode: ThinkingMode,
        createdAt: Date = Date(),
        updatedAt: Date = Date()
    ) {
        self.id = id
        self.title = title
        self.messages = messages
        self.mode = mode
        self.createdAt = createdAt
        self.updatedAt = updatedAt
    }
}
