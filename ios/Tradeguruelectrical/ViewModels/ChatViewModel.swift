import Foundation
import SwiftData

@Observable
@MainActor
class ChatViewModel {
    let engine: ChatEngine
    let conversationManager: ConversationManager
    var selectedMode: ThinkingMode = .faultFinder

    var conversations: [Conversation] { conversationManager.conversations }

    var activeConversation: Conversation? {
        get { conversationManager.activeConversation }
        set { conversationManager.activeConversation = newValue }
    }

    var isStreaming: Bool { engine.isStreaming }
    var streamingBlocks: [ContentBlock] { engine.streamingBlocks }

    var error: String? {
        get { engine.error }
        set { engine.error = newValue }
    }

    var lastResponseId: String? { engine.lastResponseId }
    var pipelineStage: PipelineStage { engine.pipelineStage }

    init(modelContext: ModelContext) {
        engine = ChatEngine()
        conversationManager = ConversationManager(modelContext: modelContext)
    }

    func send(_ text: String, mode: ThinkingMode, attachments: [MessageAttachment]? = nil) {
        let trimmed = text.trimmingCharacters(in: .whitespacesAndNewlines)
        let hasAttachments = !(attachments?.isEmpty ?? true)
        guard !trimmed.isEmpty || hasAttachments else { return }
        let displayText = trimmed.isEmpty ? "[Photo]" : text

        let userMessage = ChatMessage(
            role: .user,
            blocks: [ContentBlock(type: .text, content: displayText)],
            mode: mode,
            attachments: attachments
        )

        let conversation = conversationManager.ensureConversation(for: displayText, mode: mode)
        conversation.messages.append(userMessage)
        conversation.updatedAt = Date()

        Task { await engine.fetchResponse(mode: mode, conversation: conversation) }
    }

    func sendWithVision(_ text: String, mode: ThinkingMode, attachments: [MessageAttachment]?) {
        let trimmed = text.trimmingCharacters(in: .whitespacesAndNewlines)
        let hasAttachments = !(attachments?.isEmpty ?? true)
        guard !trimmed.isEmpty || hasAttachments else { return }
        let displayText = trimmed.isEmpty ? "[Photo]" : text

        let userMessage = ChatMessage(
            role: .user,
            blocks: [ContentBlock(type: .text, content: displayText)],
            mode: mode,
            attachments: attachments
        )

        let conversation = conversationManager.ensureConversation(for: displayText, mode: mode)
        conversation.messages.append(userMessage)
        conversation.updatedAt = Date()

        Task { await engine.fetchVisionResponse(mode: mode, attachments: attachments, conversation: conversation) }
    }

    func sendWithDocument(_ text: String, mode: ThinkingMode, attachments: [MessageAttachment]?) {
        let trimmed = text.trimmingCharacters(in: .whitespacesAndNewlines)
        let hasAttachments = !(attachments?.isEmpty ?? true)
        guard !trimmed.isEmpty || hasAttachments else { return }

        let docAttachment = attachments?.first(where: { $0.type == .document })
        let displayText = trimmed.isEmpty ? "[File: \(docAttachment?.fileName ?? "document")]" : text

        let userMessage = ChatMessage(
            role: .user,
            blocks: [ContentBlock(type: .text, content: displayText)],
            mode: mode,
            attachments: attachments
        )

        let conversation = conversationManager.ensureConversation(for: displayText, mode: mode)
        conversation.messages.append(userMessage)
        conversation.updatedAt = Date()

        Task { await engine.uploadDocumentAndChat(mode: mode, attachments: attachments, conversation: conversation) }
    }

    func retryLastRequest() {
        guard let conversation = activeConversation else { return }
        engine.retryLastRequest(conversation: conversation)
    }

    func newConversation(mode: ThinkingMode) {
        conversationManager.newConversation(mode: mode)
        engine.streamingBlocks = []
        engine.isStreaming = false
        engine.error = nil
    }

    func selectConversation(_ conversation: Conversation) {
        conversationManager.selectConversation(conversation)
        engine.streamingBlocks = []
        engine.isStreaming = false
        engine.error = nil
    }

    func deleteConversation(_ conversation: Conversation) {
        conversationManager.deleteConversation(conversation)
    }

    func searchConversations(_ query: String) -> [Conversation] {
        conversationManager.searchConversations(query)
    }

    func dismissError() {
        engine.error = nil
    }

    func rateLastResponse(stars: Int, comment: String? = nil) async {
        await engine.rateLastResponse(stars: stars, comment: comment, mode: selectedMode)
    }

    func transcribeAudio(_ audioData: Data) async -> String? {
        await engine.transcribeAudio(audioData)
    }

    func speakText(_ text: String) async {
        await engine.speakText(text)
    }
}
