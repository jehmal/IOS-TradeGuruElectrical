import Foundation
import SwiftData

enum DataMigrator {
    private static let migrationKey = "swiftdata_migrated"
    private static let fileName = "conversations.json"

    static func migrateIfNeeded(context: ModelContext) {
        guard !UserDefaults.standard.bool(forKey: migrationKey) else { return }

        let fileURL = FileManager.default.urls(for: .documentDirectory, in: .userDomainMask)[0]
            .appendingPathComponent(fileName)

        guard FileManager.default.fileExists(atPath: fileURL.path) else {
            UserDefaults.standard.set(true, forKey: migrationKey)
            return
        }

        do {
            let data = try Data(contentsOf: fileURL)
            let decoder = JSONDecoder()
            let legacyConversations = try decoder.decode([LegacyConversation].self, from: data)

            for legacy in legacyConversations {
                let conversation = Conversation(
                    id: legacy.id,
                    title: legacy.title,
                    messages: legacy.messages.map { convertMessage($0) },
                    mode: legacy.mode,
                    createdAt: legacy.createdAt,
                    updatedAt: legacy.updatedAt
                )
                context.insert(conversation)
            }

            try context.save()
            try FileManager.default.removeItem(at: fileURL)
        } catch {
        }

        UserDefaults.standard.set(true, forKey: migrationKey)
    }

    private static func convertMessage(_ legacy: LegacyMessage) -> ChatMessage {
        ChatMessage(
            id: legacy.id,
            role: legacy.role,
            blocks: legacy.blocks.map { convertBlock($0) },
            timestamp: legacy.timestamp,
            mode: legacy.mode,
            attachments: legacy.attachments?.map { convertAttachment($0) }
        )
    }

    private static func convertBlock(_ legacy: LegacyContentBlock) -> ContentBlock {
        ContentBlock(
            id: legacy.id,
            type: legacy.type,
            content: legacy.content,
            title: legacy.title,
            steps: legacy.steps,
            items: legacy.items?.map { convertPartsItem($0) },
            language: legacy.language,
            code: legacy.code,
            clause: legacy.clause,
            summary: legacy.summary,
            url: legacy.url,
            rows: legacy.rows,
            headers: legacy.headers,
            level: legacy.level,
            style: legacy.style
        )
    }

    private static func convertAttachment(_ legacy: LegacyAttachment) -> MessageAttachment {
        MessageAttachment(
            id: legacy.id,
            type: legacy.type,
            fileName: legacy.fileName,
            fileSize: legacy.fileSize,
            thumbnailData: legacy.thumbnailData
        )
    }

    private static func convertPartsItem(_ legacy: LegacyPartsItem) -> PartsItem {
        PartsItem(
            id: legacy.id,
            name: legacy.name,
            spec: legacy.spec,
            qty: legacy.qty
        )
    }
}

private nonisolated struct LegacyConversation: Codable, Sendable {
    let id: UUID
    var title: String
    var messages: [LegacyMessage]
    var mode: ThinkingMode
    let createdAt: Date
    var updatedAt: Date
}

private nonisolated struct LegacyMessage: Codable, Sendable {
    let id: UUID
    let role: MessageRole
    let blocks: [LegacyContentBlock]
    let timestamp: Date
    let mode: ThinkingMode
    var attachments: [LegacyAttachment]?
}

private nonisolated struct LegacyContentBlock: Codable, Sendable {
    let id: UUID
    let type: ContentBlockType
    var content: String?
    var title: String?
    var steps: [String]?
    var items: [LegacyPartsItem]?
    var language: String?
    var code: String?
    var clause: String?
    var summary: String?
    var url: String?
    var rows: [[String]]?
    var headers: [String]?
    var level: Int?
    var style: String?
}

private nonisolated struct LegacyAttachment: Codable, Sendable {
    let id: UUID
    let type: AttachmentType
    let fileName: String
    let fileSize: Int?
    let thumbnailData: Data?
}

private nonisolated struct LegacyPartsItem: Codable, Sendable {
    let id: UUID
    let name: String
    let spec: String
    let qty: Int
}
