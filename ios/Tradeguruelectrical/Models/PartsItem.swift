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
