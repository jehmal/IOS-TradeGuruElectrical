import Foundation
import SwiftData

@Model
class MessageAttachment {
    var id: UUID
    var type: AttachmentType
    var fileName: String
    var fileSize: Int?
    var thumbnailData: Data?
    var message: ChatMessage?

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
