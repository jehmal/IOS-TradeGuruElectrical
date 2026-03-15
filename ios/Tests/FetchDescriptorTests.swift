import Testing
@testable import Tradeguruelectrical

#if canImport(SwiftData)
import SwiftData

@Suite struct FetchDescriptorTests {
    @Test func sortDescriptorCompiles() {
        let sort = SortDescriptor(\Conversation.updatedAt, order: .reverse)
        #expect(sort.order == .reverse)
    }

    @Test func fetchDescriptorWithSortCompiles() {
        var descriptor = FetchDescriptor<Conversation>(
            sortBy: [SortDescriptor(\.updatedAt, order: .reverse)]
        )
        descriptor.fetchLimit = 50
        #expect(descriptor.fetchLimit == 50)
    }

    @Test func predicateCompiles() {
        let query = "test"
        let predicate = #Predicate<Conversation> { $0.title.localizedStandardContains(query) }
        let descriptor = FetchDescriptor<Conversation>(
            predicate: predicate,
            sortBy: [SortDescriptor(\.updatedAt, order: .reverse)]
        )
        #expect(descriptor.sortBy.count == 1)
    }
}
#endif
