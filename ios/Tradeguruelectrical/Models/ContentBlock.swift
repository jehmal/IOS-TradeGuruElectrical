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

extension ContentBlock: Decodable {
    enum CodingKeys: String, CodingKey {
        case id, type, content, title, steps, items, language, code, clause, summary, url, rows, headers, level, style
    }

    convenience init(from decoder: Decoder) throws {
        let c = try decoder.container(keyedBy: CodingKeys.self)
        self.init(
            id: (try? c.decode(UUID.self, forKey: .id)) ?? UUID(),
            type: try c.decode(ContentBlockType.self, forKey: .type),
            content: try? c.decode(String.self, forKey: .content),
            title: try? c.decode(String.self, forKey: .title),
            steps: try? c.decode([String].self, forKey: .steps),
            items: try? c.decode([PartsItem].self, forKey: .items),
            language: try? c.decode(String.self, forKey: .language),
            code: try? c.decode(String.self, forKey: .code),
            clause: try? c.decode(String.self, forKey: .clause),
            summary: try? c.decode(String.self, forKey: .summary),
            url: try? c.decode(String.self, forKey: .url),
            rows: try? c.decode([[String]].self, forKey: .rows),
            headers: try? c.decode([String].self, forKey: .headers),
            level: try? c.decode(Int.self, forKey: .level),
            style: try? c.decode(String.self, forKey: .style)
        )
    }
}
