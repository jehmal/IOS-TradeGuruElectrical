import Foundation
import SwiftData

enum TradeGuruSchemaV1: VersionedSchema {
    static var versionIdentifier: Schema.Version { Schema.Version(1, 0, 0) }

    static var models: [any PersistentModel.Type] {
        [Conversation.self, ChatMessage.self, ContentBlock.self, PartsItem.self, MessageAttachment.self]
    }
}

enum TradeGuruMigrationPlan: SchemaMigrationPlan {
    static var schemas: [any VersionedSchema.Type] {
        [TradeGuruSchemaV1.self]
    }

    static var stages: [MigrationStage] {
        []
    }
}
