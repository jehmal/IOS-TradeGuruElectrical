import Foundation
@preconcurrency import AVFoundation
import SwiftData

@Observable
@MainActor
class ChatViewModel {
    var conversations: [Conversation] = []
    var activeConversation: Conversation?
    var isStreaming = false
    var streamingBlocks: [ContentBlock] = []
    var selectedMode: ThinkingMode = .faultFinder
    var error: String?
    var lastResponseId: String?
    var pipelineStage: PipelineStage = .idle
    private var lastSendMode: ThinkingMode = .faultFinder
    private var lastSendAttachments: [MessageAttachment]?
    private var lastSendType: SendType = .text

    private enum SendType { case text, vision, document }

    private var deviceId: String
    private var audioPlayer: AVAudioPlayer?
    private var modelContext: ModelContext

    private var currentJWT: String? {
        AuthManager.shared.tokens?.accessToken
    }

    init(modelContext: ModelContext) {
        self.modelContext = modelContext
        deviceId = DeviceManager.deviceIdOrFallback()
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
        Task { await registerDeviceIfNeeded() }
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

        if let conversation = activeConversation {
            conversation.messages.append(userMessage)
            conversation.updatedAt = Date()
        } else {
            let newConvo = Conversation(title: String(displayText.prefix(40)), mode: mode)
            newConvo.messages.append(userMessage)
            modelContext.insert(newConvo)
            refreshConversations()
            activeConversation = newConvo
        }

        lastSendMode = mode
        lastSendAttachments = nil
        lastSendType = .text
        Task { await fetchResponse(mode: mode) }
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

        if let conversation = activeConversation {
            conversation.messages.append(userMessage)
            conversation.updatedAt = Date()
        } else {
            let newConvo = Conversation(title: String(displayText.prefix(40)), mode: mode)
            newConvo.messages.append(userMessage)
            modelContext.insert(newConvo)
            refreshConversations()
            activeConversation = newConvo
        }

        lastSendMode = mode
        lastSendAttachments = attachments
        lastSendType = .vision
        Task { await fetchVisionResponse(mode: mode, attachments: attachments) }
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

        if let conversation = activeConversation {
            conversation.messages.append(userMessage)
            conversation.updatedAt = Date()
        } else {
            let newConvo = Conversation(title: String(displayText.prefix(40)), mode: mode)
            newConvo.messages.append(userMessage)
            modelContext.insert(newConvo)
            refreshConversations()
            activeConversation = newConvo
        }

        lastSendMode = mode
        lastSendAttachments = attachments
        lastSendType = .document
        Task { await uploadDocumentAndChat(mode: mode, attachments: attachments) }
    }

    func retryLastRequest() {
        error = nil
        switch lastSendType {
        case .text:
            Task { await fetchResponse(mode: lastSendMode) }
        case .vision:
            Task { await fetchVisionResponse(mode: lastSendMode, attachments: lastSendAttachments) }
        case .document:
            Task { await uploadDocumentAndChat(mode: lastSendMode, attachments: lastSendAttachments) }
        }
    }

    func newConversation(mode: ThinkingMode) {
        let conversation = Conversation(title: "New Conversation", mode: mode)
        modelContext.insert(conversation)
        refreshConversations()
        activeConversation = conversation
        streamingBlocks = []
        isStreaming = false
        error = nil
    }

    func selectConversation(_ conversation: Conversation) {
        activeConversation = conversation
        streamingBlocks = []
        isStreaming = false
        error = nil
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

    func dismissError() {
        error = nil
    }

    // MARK: - API Calls

    private func fetchResponse(mode: ThinkingMode) async {
        guard let conversation = activeConversation else { return }

        #if DEBUG
        if APIConfig.useMockData {
            await mockResponse(mode: mode)
            return
        }
        #endif

        let apiMessages = conversation.messages.map { msg -> [String: String] in
            let content = msg.blocks.compactMap(\.content).joined(separator: "\n")
            return ["role": msg.role.rawValue, "content": content]
        }

        isStreaming = true
        streamingBlocks = []
        error = nil

        for await result in TradeGuruAPI.chat(messages: apiMessages, mode: mode, deviceId: deviceId, jwt: currentJWT) {
            switch result {
            case .block(let block):
                streamingBlocks.append(block)

            case .status(let payload):
                if let stage = PipelineStage(rawValue: payload.stage) {
                    pipelineStage = stage
                }

            case .done(let payload):
                lastResponseId = payload.responseId
                let assistantMessage = ChatMessage(role: .assistant, blocks: streamingBlocks, mode: mode)
                conversation.messages.append(assistantMessage)
                conversation.updatedAt = Date()
                if conversation.messages.count == 2 {
                    let firstUserText = conversation.messages.first?.blocks.first?.content ?? "Chat"
                    conversation.title = String(firstUserText.prefix(40))
                }
                activeConversation = conversation
                streamingBlocks = []
                isStreaming = false
                pipelineStage = .idle

            case .error(let payload):
                error = payload.message
                if payload.partial == true, !streamingBlocks.isEmpty {
                    let partialMessage = ChatMessage(role: .assistant, blocks: streamingBlocks, mode: mode)
                    conversation.messages.append(partialMessage)
                    activeConversation = conversation
                }
                streamingBlocks = []
                isStreaming = false
                pipelineStage = .idle
            }
        }

        if isStreaming {
            isStreaming = false
        }
    }

    private func fetchVisionResponse(mode: ThinkingMode, attachments: [MessageAttachment]?) async {
        guard let conversation = activeConversation else { return }

        #if DEBUG
        if APIConfig.useMockData {
            await mockResponse(mode: mode)
            return
        }
        #endif

        let lastUserMsg = conversation.messages.last { $0.role == .user }
        let messageText = lastUserMsg?.blocks.compactMap(\.content).joined(separator: "\n") ?? ""

        var imageDataUri = ""
        if let atts = attachments {
            for att in atts {
                if att.type == .image, let data = att.thumbnailData {
                    imageDataUri = "data:image/jpeg;base64,\(data.base64EncodedString())"
                    break
                }
            }
        }

        isStreaming = true
        streamingBlocks = []
        error = nil

        for await result in TradeGuruAPI.chatVision(message: messageText, image: imageDataUri, mode: mode, deviceId: deviceId, jwt: currentJWT) {
            switch result {
            case .block(let block):
                streamingBlocks.append(block)

            case .status(let payload):
                if let stage = PipelineStage(rawValue: payload.stage) {
                    pipelineStage = stage
                }

            case .done(let payload):
                lastResponseId = payload.responseId
                let assistantMessage = ChatMessage(role: .assistant, blocks: streamingBlocks, mode: mode)
                conversation.messages.append(assistantMessage)
                conversation.updatedAt = Date()
                if conversation.messages.count == 2 {
                    let firstUserText = conversation.messages.first?.blocks.first?.content ?? "Chat"
                    conversation.title = String(firstUserText.prefix(40))
                }
                activeConversation = conversation
                streamingBlocks = []
                isStreaming = false
                pipelineStage = .idle

            case .error(let payload):
                error = payload.message
                if payload.partial == true, !streamingBlocks.isEmpty {
                    let partialMessage = ChatMessage(role: .assistant, blocks: streamingBlocks, mode: mode)
                    conversation.messages.append(partialMessage)
                    activeConversation = conversation
                }
                streamingBlocks = []
                isStreaming = false
                pipelineStage = .idle
            }
        }

        if isStreaming {
            isStreaming = false
        }
    }

    private func uploadDocumentAndChat(mode: ThinkingMode, attachments: [MessageAttachment]?) async {
        guard let conversation = activeConversation else { return }

        #if DEBUG
        if APIConfig.useMockData {
            await mockResponse(mode: mode)
            return
        }
        #endif

        if let doc = attachments?.first(where: { $0.type == .document }), let data = doc.thumbnailData {
            let mimeType: String
            if doc.fileName.hasSuffix(".pdf") {
                mimeType = "application/pdf"
            } else if doc.fileName.hasSuffix(".txt") {
                mimeType = "text/plain"
            } else {
                mimeType = "application/octet-stream"
            }
            do {
                let uploaded = try await TradeGuruAPI.uploadFile(
                    fileData: data,
                    fileName: doc.fileName,
                    mimeType: mimeType,
                    deviceId: deviceId,
                    jwt: currentJWT
                )
                let lastUserText = conversation.messages.last(where: { $0.role == .user })?.blocks.compactMap(\.content).joined(separator: "\n") ?? ""
                let messageWithFile = lastUserText.isEmpty
                    ? "[Uploaded: \(uploaded.filename)]"
                    : "\(lastUserText)\n\n[Attached file: \(uploaded.filename) (id: \(uploaded.id))]"

                let apiMessages: [[String: String]] = conversation.messages.map { msg in
                    var content = msg.blocks.compactMap(\.content).joined(separator: "\n")
                    if msg.id == conversation.messages.last?.id && msg.role == .user {
                        content = messageWithFile
                    }
                    return ["role": msg.role.rawValue, "content": content]
                }

                isStreaming = true
                streamingBlocks = []
                error = nil

                for await result in TradeGuruAPI.chat(messages: apiMessages, mode: mode, deviceId: deviceId, jwt: currentJWT) {
                    switch result {
                    case .block(let block):
                        streamingBlocks.append(block)
                    case .status(let payload):
                        if let stage = PipelineStage(rawValue: payload.stage) {
                            pipelineStage = stage
                        }
                    case .done(let payload):
                        lastResponseId = payload.responseId
                        let assistantMessage = ChatMessage(role: .assistant, blocks: streamingBlocks, mode: mode)
                        conversation.messages.append(assistantMessage)
                        conversation.updatedAt = Date()
                        if conversation.messages.count == 2 {
                            let firstUserText = conversation.messages.first?.blocks.first?.content ?? "Chat"
                            conversation.title = String(firstUserText.prefix(40))
                        }
                        activeConversation = conversation
                        streamingBlocks = []
                        isStreaming = false
                        pipelineStage = .idle
                    case .error(let payload):
                        error = payload.message
                        streamingBlocks = []
                        isStreaming = false
                        pipelineStage = .idle
                    }
                }
                if isStreaming { isStreaming = false }
            } catch {
                self.error = "File upload failed: \(error.localizedDescription)"
            }
        } else {
            await fetchResponse(mode: mode)
        }
    }

    #if DEBUG
    private func mockResponse(mode: ThinkingMode) async {
        isStreaming = true
        streamingBlocks = []
        error = nil

        let mockConversation = MockData.allConversations.first { $0.mode == mode }
        let mockBlocks = mockConversation?.messages.last(where: { $0.role == .assistant })?.blocks ?? [
            ContentBlock(type: .text, content: "This is a mock response for \(mode.name) mode.")
        ]

        try? await Task.sleep(for: .milliseconds(500))

        for block in mockBlocks {
            streamingBlocks.append(block)
            try? await Task.sleep(for: .milliseconds(100))
        }

        guard let conversation = activeConversation else {
            isStreaming = false
            return
        }

        let assistantMessage = ChatMessage(role: .assistant, blocks: streamingBlocks, mode: mode)
        conversation.messages.append(assistantMessage)
        conversation.updatedAt = Date()
        if conversation.messages.count == 2 {
            let firstUserText = conversation.messages.first?.blocks.first?.content ?? "Chat"
            conversation.title = String(firstUserText.prefix(40))
        }
        activeConversation = conversation
        streamingBlocks = []
        isStreaming = false
    }
    #endif

    private func registerDeviceIfNeeded() async {
        if deviceId.isEmpty || deviceId == "pending" {
            do {
                let registeredId = try await TradeGuruAPI.registerDevice()
                DeviceManager.save(registeredId)
                deviceId = registeredId
            } catch {
                error = "Device registration failed. Retrying on next launch."
            }
        }
    }

    // MARK: - Rating

    func rateLastResponse(stars: Int, comment: String? = nil) async {
        guard let responseId = lastResponseId else { return }
        try? await TradeGuruAPI.rate(
            responseId: responseId,
            stars: stars,
            mode: selectedMode,
            comment: comment,
            deviceId: deviceId,
            jwt: currentJWT
        )
    }

    // MARK: - Audio Transcription

    func transcribeAudio(_ audioData: Data) async -> String? {
        do {
            return try await TradeGuruAPI.transcribe(audioData: audioData, mimeType: "audio/m4a", deviceId: deviceId, jwt: currentJWT)
        } catch {
            self.error = "Transcription failed"
            return nil
        }
    }

    // MARK: - Text-to-Speech

    func speakText(_ text: String) async {
        do {
            let audioData = try await TradeGuruAPI.speak(text: text, deviceId: deviceId, jwt: currentJWT)
            let player = try AVAudioPlayer(data: audioData)
            player.prepareToPlay()
            player.play()
            audioPlayer = player
        } catch {
            self.error = "Speech playback failed"
        }
    }

    // MARK: - Helpers

    private func refreshConversations() {
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

    private func safeSave() {
        do {
            if modelContext.hasChanges {
                try modelContext.save()
            }
        } catch {
        }
    }
}
