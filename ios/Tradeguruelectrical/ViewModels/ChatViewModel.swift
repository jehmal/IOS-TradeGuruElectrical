import Foundation

@Observable
@MainActor
class ChatViewModel {
    var conversations: [Conversation] = []
    var activeConversation: Conversation?
    var isStreaming = false
    var streamingBlocks: [ContentBlock] = []
    var selectedMode: ThinkingMode = .faultFinder

    init() {
        conversations = MockData.allConversations
        activeConversation = conversations.first
    }

    func send(_ text: String, mode: ThinkingMode, attachments: [MessageAttachment]? = nil) {
        guard !text.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty else { return }
        guard var conversation = activeConversation else {
            var newConvo = Conversation(title: String(text.prefix(40)), mode: mode)
            let userMessage = ChatMessage(role: .user, blocks: [ContentBlock(type: .text, content: text)], mode: mode, attachments: attachments)
            newConvo.messages.append(userMessage)
            newConvo.updatedAt = Date()
            conversations.insert(newConvo, at: 0)
            activeConversation = newConvo
            simulateResponse(for: newConvo, mode: mode)
            return
        }

        let userMessage = ChatMessage(role: .user, blocks: [ContentBlock(type: .text, content: text)], mode: mode, attachments: attachments)
        conversation.messages.append(userMessage)
        conversation.updatedAt = Date()
        updateConversation(conversation)
        simulateResponse(for: conversation, mode: mode)
    }

    func newConversation(mode: ThinkingMode) {
        let conversation = Conversation(title: "New Conversation", mode: mode)
        conversations.insert(conversation, at: 0)
        activeConversation = conversations.first
    }

    func selectConversation(_ conversation: Conversation) {
        activeConversation = conversation
    }

    private func simulateResponse(for conversation: Conversation, mode: ThinkingMode) {
        let mockResponses = mockBlocksForMode(mode)
        isStreaming = true
        streamingBlocks = []

        Task {
            var updatedConversation = conversation
            for block in mockResponses {
                try? await Task.sleep(for: .milliseconds(300))
                streamingBlocks.append(block)
            }

            try? await Task.sleep(for: .milliseconds(200))

            let assistantMessage = ChatMessage(role: .assistant, blocks: streamingBlocks, mode: mode)
            updatedConversation.messages.append(assistantMessage)
            updatedConversation.updatedAt = Date()
            updateConversation(updatedConversation)

            streamingBlocks = []
            isStreaming = false
        }
    }

    private func updateConversation(_ conversation: Conversation) {
        if let index = conversations.firstIndex(where: { $0.id == conversation.id }) {
            conversations[index] = conversation
        }
        if activeConversation?.id == conversation.id {
            activeConversation = conversation
        }
    }

    private func mockBlocksForMode(_ mode: ThinkingMode) -> [ContentBlock] {
        switch mode {
        case .faultFinder:
            return [
                ContentBlock(type: .text, content: "Based on your description, this could indicate an insulation breakdown or moisture ingress. Let me walk you through the diagnosis."),
                ContentBlock(
                    type: .stepList,
                    title: "Recommended Checks",
                    steps: [
                        "Isolate the affected circuit at the switchboard.",
                        "Perform an insulation resistance test at 500V DC.",
                        "Check all accessible terminations for signs of damage or moisture.",
                        "Re-test with the RCD after repairs."
                    ]
                ),
                ContentBlock(type: .warning, content: "Ensure the circuit is de-energised and locked out before performing any insulation resistance testing.")
            ]
        case .learn:
            return [
                ContentBlock(type: .heading, content: "Key Concepts", level: 2),
                ContentBlock(type: .text, content: "Understanding the fundamental principles will help you apply the correct standards and sizing methods in practice."),
                ContentBlock(
                    type: .callout,
                    content: "Always refer to the current edition of AS/NZS 3008.1 for the most up-to-date current-carrying capacity tables. Older editions may have different derating factors.",
                    style: "tip"
                )
            ]
        case .research:
            return [
                ContentBlock(type: .heading, content: "Technical Specifications", level: 2),
                ContentBlock(type: .text, content: "Here are the relevant specifications and regulatory references for your query."),
                ContentBlock(
                    type: .regulation,
                    content: "Installation requirements as specified in the current Wiring Rules.",
                    code: "AS/NZS 3000:2018",
                    clause: "Applicable clause",
                    summary: "Refer to the specific section of the Wiring Rules for detailed compliance requirements."
                )
            ]
        }
    }
}
