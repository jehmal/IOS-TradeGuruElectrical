import Foundation
import SwiftData

@Observable
@MainActor
class ConversationManager {
    var conversations: [Conversation] = []
    var activeConversation: Conversation?
    private var modelContext: ModelContext

    init(modelContext: ModelContext) {
        self.modelContext = modelContext
        #if DEBUG
        if APIConfig.useMockData {
            conversations = MockData.allConversations
            activeConversation = conversations.first
        } else {
            refreshConversations()
            activeConversation = conversations.first
        }
        #else
        refreshConversations()
        activeConversation = conversations.first
        #endif
    }

    func newConversation(mode: ThinkingMode) {
        let conversation = Conversation(title: "New Conversation", mode: mode)
        modelContext.insert(conversation)
        refreshConversations()
        activeConversation = conversation
    }

    func selectConversation(_ conversation: Conversation) {
        activeConversation = conversation
    }

    func deleteConversation(_ conversation: Conversation) {
        modelContext.delete(conversation)
        if activeConversation?.id == conversation.id {
            activeConversation = nil
        }
        refreshConversations()
        activeConversation = activeConversation ?? conversations.first
    }

    func searchConversations(_ query: String) -> [Conversation] {
        let lowered = query.lowercased()
        return conversations.filter { $0.title.lowercased().contains(lowered) }
    }

    func ensureConversation(for text: String, mode: ThinkingMode) -> Conversation {
        if let conversation = activeConversation {
            return conversation
        }
        let newConvo = Conversation(title: String(text.prefix(40)), mode: mode)
        modelContext.insert(newConvo)
        refreshConversations()
        activeConversation = newConvo
        return newConvo
    }

    func refreshConversations() {
        do {
            var descriptor = FetchDescriptor<Conversation>(
                sortBy: [SortDescriptor(\.updatedAt, order: .reverse)]
            )
            descriptor.fetchLimit = 50
            conversations = try modelContext.fetch(descriptor)
        } catch {
            conversations = []
        }
    }

    func safeSave() {
        do {
            if modelContext.hasChanges {
                try modelContext.save()
            }
        } catch {
        }
    }
}
