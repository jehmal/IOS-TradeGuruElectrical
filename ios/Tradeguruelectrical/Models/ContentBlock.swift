import Foundation
import SwiftData

nonisolated enum ContentBlockType: String, Codable, CaseIterable {
    case text
    case heading
    case stepList = "step_list"
    case warning
    case code
    case partsList = "parts_list"
    case regulation
    case diagramRef = "diagram_ref"
    case toolCall = "tool_call"
    case table
    case callout
    case link
}

@Model
class ContentBlock {
    var id: UUID
    var type: ContentBlockType
    var content: String?
    var title: String?
    var steps: [String]?
    @Relationship(deleteRule: .cascade, inverse: \PartsItem.contentBlock) var items: [PartsItem]?
    var language: String?
    var code: String?
    var clause: String?
    var summary: String?
    var url: String?
    var rows: [[String]]?
    var headers: [String]?
    var level: Int?
    var style: String?
    var message: ChatMessage?

    init(
        id: UUID = UUID(),
        type: ContentBlockType,
        content: String? = nil,
        title: String? = nil,
        steps: [String]? = nil,
        items: [PartsItem]? = nil,
        language: String? = nil,
        code: String? = nil,
        clause: String? = nil,
        summary: String? = nil,
        url: String? = nil,
        rows: [[String]]? = nil,
        headers: [String]? = nil,
        level: Int? = nil,
        style: String? = nil
    ) {
        self.id = id
        self.type = type
        self.content = content
        self.title = title
        self.steps = steps
        self.items = items
        self.language = language
        self.code = code
        self.clause = clause
        self.summary = summary
        self.url = url
        self.rows = rows
        self.headers = headers
        self.level = level
        self.style = style
    }
}
