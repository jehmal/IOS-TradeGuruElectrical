import Foundation

extension ThinkingMode: Codable {}

nonisolated enum MessageRole: String, Codable {
    case user
    case assistant
}

nonisolated struct ChatMessage: Codable, Identifiable {
    let id: UUID
    let role: MessageRole
    let blocks: [ContentBlock]
    let timestamp: Date
    let mode: ThinkingMode
    var attachments: [MessageAttachment]?

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

nonisolated struct MessageAttachment: Codable, Identifiable {
    let id: UUID
    let type: AttachmentType
    let fileName: String
    let fileSize: Int?
    let thumbnailData: Data?

    init(
        id: UUID = UUID(),
        type: AttachmentType,
        fileName: String,
        fileSize: Int? = nil,
        thumbnailData: Data? = nil
    ) {
        self.id = id
        self.type = type
        self.fileName = fileName
        self.fileSize = fileSize
        self.thumbnailData = thumbnailData
    }
}

nonisolated enum AttachmentType: String, Codable {
    case image
    case video
    case document
}
