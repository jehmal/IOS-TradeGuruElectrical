import Foundation
@preconcurrency import AVFoundation

@Observable
@MainActor
class ChatEngine {
    var isStreaming = false
    var streamingBlocks: [ContentBlock] = []
    var error: String?
    var lastResponseId: String?
    var pipelineStage: PipelineStage = .idle

    private var lastSendMode: ThinkingMode = .faultFinder
    private var lastSendAttachments: [MessageAttachment]?
    private var lastSendType: SendType = .text
    private enum SendType { case text, vision, document }

    private var deviceId: String
    private var audioPlayer: AVAudioPlayer?

    private var currentJWT: String? {
        AuthManager.shared.tokens?.accessToken
    }

    init() {
        deviceId = DeviceManager.deviceIdOrFallback()
        Task { await registerDeviceIfNeeded() }
    }

    func fetchResponse(mode: ThinkingMode, conversation: Conversation) async {
        #if DEBUG
        if APIConfig.useMockData {
            await mockResponse(mode: mode, conversation: conversation)
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
        lastSendMode = mode
        lastSendAttachments = nil
        lastSendType = .text

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
                finalizeResponse(mode: mode, conversation: conversation)
            case .error(let payload):
                handleStreamError(payload: payload, mode: mode, conversation: conversation)
            }
        }

        if isStreaming { isStreaming = false }
    }

    func fetchVisionResponse(mode: ThinkingMode, attachments: [MessageAttachment]?, conversation: Conversation) async {
        #if DEBUG
        if APIConfig.useMockData {
            await mockResponse(mode: mode, conversation: conversation)
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
        lastSendMode = mode
        lastSendAttachments = attachments
        lastSendType = .vision

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
                finalizeResponse(mode: mode, conversation: conversation)
            case .error(let payload):
                handleStreamError(payload: payload, mode: mode, conversation: conversation)
            }
        }

        if isStreaming { isStreaming = false }
    }

    func uploadDocumentAndChat(mode: ThinkingMode, attachments: [MessageAttachment]?, conversation: Conversation) async {
        #if DEBUG
        if APIConfig.useMockData {
            await mockResponse(mode: mode, conversation: conversation)
            return
        }
        #endif

        lastSendMode = mode
        lastSendAttachments = attachments
        lastSendType = .document

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
                        finalizeResponse(mode: mode, conversation: conversation)
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
            await fetchResponse(mode: mode, conversation: conversation)
        }
    }

    func retryLastRequest(conversation: Conversation) {
        error = nil
        switch lastSendType {
        case .text:
            Task { await fetchResponse(mode: lastSendMode, conversation: conversation) }
        case .vision:
            Task { await fetchVisionResponse(mode: lastSendMode, attachments: lastSendAttachments, conversation: conversation) }
        case .document:
            Task { await uploadDocumentAndChat(mode: lastSendMode, attachments: lastSendAttachments, conversation: conversation) }
        }
    }

    func rateLastResponse(stars: Int, comment: String? = nil, mode: ThinkingMode) async {
        guard let responseId = lastResponseId else { return }
        try? await TradeGuruAPI.rate(
            responseId: responseId,
            stars: stars,
            mode: mode,
            comment: comment,
            deviceId: deviceId,
            jwt: currentJWT
        )
    }

    func transcribeAudio(_ audioData: Data) async -> String? {
        do {
            return try await TradeGuruAPI.transcribe(audioData: audioData, mimeType: "audio/m4a", deviceId: deviceId, jwt: currentJWT)
        } catch {
            self.error = "Transcription failed"
            return nil
        }
    }

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

    private func finalizeResponse(mode: ThinkingMode, conversation: Conversation) {
        let assistantMessage = ChatMessage(role: .assistant, blocks: streamingBlocks, mode: mode)
        conversation.messages.append(assistantMessage)
        conversation.updatedAt = Date()
        if conversation.messages.count == 2 {
            let firstUserText = conversation.messages.first?.blocks.first?.content ?? "Chat"
            conversation.title = String(firstUserText.prefix(40))
        }
        streamingBlocks = []
        isStreaming = false
        pipelineStage = .idle
    }

    private func handleStreamError(payload: StreamErrorPayload, mode: ThinkingMode, conversation: Conversation) {
        error = payload.message
        if payload.partial == true, !streamingBlocks.isEmpty {
            let partialMessage = ChatMessage(role: .assistant, blocks: streamingBlocks, mode: mode)
            conversation.messages.append(partialMessage)
        }
        streamingBlocks = []
        isStreaming = false
        pipelineStage = .idle
    }

    private func registerDeviceIfNeeded() async {
        if deviceId.isEmpty || deviceId == "pending" {
            do {
                let registeredId = try await TradeGuruAPI.registerDevice()
                DeviceManager.save(registeredId)
                deviceId = registeredId
            } catch {
                self.error = "Device registration failed. Retrying on next launch."
            }
        }
    }

    #if DEBUG
    private func mockResponse(mode: ThinkingMode, conversation: Conversation) async {
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

        finalizeResponse(mode: mode, conversation: conversation)
    }
    #endif
}
