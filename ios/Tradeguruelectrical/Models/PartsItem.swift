import Foundation
import SwiftData

@Model
class PartsItem {
    var id: UUID
    var name: String
    var spec: String
    var qty: Int
    var contentBlock: ContentBlock?

    init(id: UUID = UUID(), name: String, spec: String, qty: Int) {
        self.id = id
        self.name = name
        self.spec = spec
        self.qty = qty
    }
}

extension PartsItem: Decodable {
    enum CodingKeys: String, CodingKey {
        case id, name, spec, qty
    }

    required convenience init(from decoder: Decoder) throws {
        let c = try decoder.container(keyedBy: CodingKeys.self)
        self.init(
            id: (try? c.decode(UUID.self, forKey: .id)) ?? UUID(),
            name: try c.decode(String.self, forKey: .name),
            spec: try c.decode(String.self, forKey: .spec),
            qty: try c.decode(Int.self, forKey: .qty)
        )
    }
}
