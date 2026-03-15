import SwiftUI
import SwiftData

struct ChatView: View {
    @Environment(\.modelContext) private var modelContext
    @State private var viewModel: ChatViewModel?
    @State private var selectedMode: ThinkingMode = .faultFinder
    @State private var inputText = ""
    @State private var showModeCard = true
    @State private var showSidebar = false
    @State private var showSettings = false
    @State private var userDismissedCard = false

    var body: some View {
        Group {
            if let viewModel {
                chatContent(viewModel: viewModel)
            } else {
                Color.tradeBg.ignoresSafeArea()
            }
        }
        .onAppear {
            if viewModel == nil {
                viewModel = ChatViewModel(modelContext: modelContext)
            }
        }
    }

    private func chatContent(viewModel: ChatViewModel) -> some View {
        VStack(spacing: 0) {
            navBar(viewModel: viewModel)
                .padding(.horizontal, 16)
                .padding(.vertical, 8)
                .background(Color.tradeBg)

            conversationArea(viewModel: viewModel)

            errorBanner(viewModel: viewModel)

            inputBar(viewModel: viewModel)
        }
        .sheet(isPresented: $showSettings) {
            SettingsView()
        }
        .onChange(of: inputText) { _, newValue in
            if !newValue.isEmpty {
                dismissModeCard()
            }
        }
        .overlay {
            if showSidebar {
                sidebarOverlay(viewModel: viewModel)
            }
        }
        .onChange(of: selectedMode) { _, newMode in
            userDismissedCard = false
            viewModel.selectedMode = newMode
            showModeCard = true
        }
    }

    // MARK: - Conversation Area

    @ViewBuilder
    private func conversationArea(viewModel: ChatViewModel) -> some View {
        ZStack {
            Color.tradeBg.ignoresSafeArea()

            VStack {
                if showModeCard {
                    ModeInfoCard(mode: selectedMode) {
                        dismissModeCard()
                    }
                    .onTapGesture {
                        dismissModeCard()
                    }
                    .transition(.opacity)
                    .padding(.top, 16)
                }

                if let conversation = viewModel.activeConversation,
                   !conversation.messages.isEmpty || !viewModel.streamingBlocks.isEmpty {
                    messageList(viewModel: viewModel, conversation: conversation)
                } else {
                    Spacer()
                    emptyStateLogo
                    Spacer()
                }
            }
        }
    }

    private var emptyStateLogo: some View {
        Image("TradeGuruLogo")
            .renderingMode(.template)
            .resizable()
            .scaledToFit()
            .frame(width: 180, height: 180)
            .foregroundStyle(Color.tradeText)
            .opacity(0.08)
    }

    // MARK: - Message List

    private func messageList(viewModel: ChatViewModel, conversation: Conversation) -> some View {
        ScrollViewReader { proxy in
            ScrollView {
                LazyVStack(spacing: 16) {
                    ForEach(conversation.messages) { message in
                        messageBubbleRow(viewModel: viewModel, message: message, conversation: conversation)
                    }

                    if viewModel.isStreaming && !viewModel.streamingBlocks.isEmpty {
                        streamingBubble(viewModel: viewModel)
                    } else if viewModel.isStreaming && viewModel.streamingBlocks.isEmpty {
                        typingIndicator(viewModel: viewModel)
                    }

                    Color.clear.frame(height: 1).id("bottom")
                }
                .padding(.horizontal, 16)
                .padding(.vertical, 12)
            }
            .onChange(of: viewModel.activeConversation?.messages.count) { _, _ in
                proxy.scrollTo("bottom", anchor: .bottom)
            }
            .onChange(of: viewModel.streamingBlocks.count) { _, _ in
                proxy.scrollTo("bottom", anchor: .bottom)
            }
        }
    }

    private func messageBubbleRow(viewModel: ChatViewModel, message: ChatMessage, conversation: Conversation) -> some View {
        MessageBubble(
            message: message,
            isLastAssistantMessage: message.id == conversation.messages.last?.id && message.role == .assistant,
            onRate: { stars in
                Task { await viewModel.rateLastResponse(stars: stars) }
            },
            onFlag: { reason in
                Task { try? await TradeGuruAPI.feedback(responseId: viewModel.lastResponseId ?? "", reason: reason, mode: viewModel.selectedMode, deviceId: DeviceManager.deviceIdOrFallback(), jwt: AuthManager.shared.tokens?.accessToken) }
            },
            onSpeak: { text in
                Task { await viewModel.speakText(text) }
            }
        )
    }

    // MARK: - Error Banner

    @ViewBuilder
    private func errorBanner(viewModel: ChatViewModel) -> some View {
        if let errorMessage = viewModel.error {
            HStack(spacing: 8) {
                Image(systemName: "exclamationmark.triangle.fill")
                    .font(.system(size: 14))
                    .foregroundStyle(.red)

                Text(errorMessage)
                    .font(.system(size: 13))
                    .foregroundStyle(.red)
                    .lineLimit(2)

                Spacer()

                Button {
                    viewModel.retryLastRequest()
                } label: {
                    Text("Retry")
                        .font(.system(size: 13, weight: .semibold))
                        .foregroundStyle(.red)
                }
                .frame(height: 44)

                Button {
                    viewModel.dismissError()
                } label: {
                    Image(systemName: "xmark")
                        .font(.system(size: 12, weight: .medium))
                        .foregroundStyle(.red)
                }
                .frame(width: 44, height: 44)
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 8)
            .background(Color.red.opacity(0.1))
        }
    }

    // MARK: - Input Bar

    private func inputBar(viewModel: ChatViewModel) -> some View {
        ChatInputBar(
            text: $inputText,
            selectedMode: $selectedMode,
            onSend: { text, attachments in
                let hasImageAttachment = attachments?.contains(where: { $0.type == .image && $0.thumbnailData != nil }) ?? false
                let hasDocumentAttachment = attachments?.contains(where: { $0.type == .document }) ?? false
                if hasDocumentAttachment {
                    viewModel.sendWithDocument(text, mode: selectedMode, attachments: attachments)
                } else if hasImageAttachment {
                    viewModel.sendWithVision(text, mode: selectedMode, attachments: attachments)
                } else {
                    viewModel.send(text, mode: selectedMode, attachments: attachments)
                }
                inputText = ""
            },
            onInputFocus: {
                dismissModeCard()
            },
            onVoiceInput: { transcribedText in
                inputText = transcribedText
            },
            onAudioRecorded: { audioData in
                Task {
                    if let text = await viewModel.transcribeAudio(audioData) {
                        inputText = text
                    }
                }
            }
        )
    }

    // MARK: - Sidebar Overlay

    private func sidebarOverlay(viewModel: ChatViewModel) -> some View {
        SidebarView(
            conversations: viewModel.conversations,
            onSelect: { conversation in
                viewModel.selectConversation(conversation)
                withAnimation(.easeInOut(duration: 0.25)) { showSidebar = false }
            },
            onDelete: { conversation in
                viewModel.deleteConversation(conversation)
            },
            onNewChat: {
                viewModel.newConversation(mode: selectedMode)
                withAnimation(.easeInOut(duration: 0.25)) { showSidebar = false }
            },
            onClose: {
                withAnimation(.easeInOut(duration: 0.25)) { showSidebar = false }
            }
        )
    }

    // MARK: - Helpers

    private func dismissModeCard() {
        withAnimation(.easeOut(duration: 0.15)) {
            showModeCard = false
            userDismissedCard = true
        }
    }

    private func streamingBubble(viewModel: ChatViewModel) -> some View {
        VStack(alignment: .leading, spacing: 12) {
            ForEach(viewModel.streamingBlocks) { block in
                streamingBlockView(for: block)
            }
        }
        .padding(14)
        .background(Color.tradeSurface)
        .clipShape(.rect(cornerRadius: 16))
        .frame(maxWidth: 330, alignment: .leading)
        .frame(maxWidth: .infinity, alignment: .leading)
    }

    @ViewBuilder
    private func streamingBlockView(for block: ContentBlock) -> some View {
        switch block.type {
        case .text:
            TextBlockView(content: block.content ?? "")
        case .heading:
            Text(block.content ?? "")
                .font(.system(size: headingSize(for: block.level), weight: .bold))
                .foregroundStyle(Color.tradeText)
                .frame(maxWidth: .infinity, alignment: .leading)
        case .stepList:
            StepListView(title: block.title, steps: block.steps ?? [])
        case .warning:
            WarningCardView(content: block.content ?? "")
        case .code:
            CodeBlockView(content: block.content ?? "", language: block.language)
        case .partsList:
            PartsListView(items: block.items ?? [])
        case .regulation:
            RegulationView(code: block.code, clause: block.clause, summary: block.summary)
        case .table:
            TableBlockView(headers: block.headers, rows: block.rows ?? [])
        case .callout:
            CalloutView(content: block.content ?? "", style: block.style)
        case .diagramRef:
            Text("Diagram: \(block.content ?? "reference")")
                .font(.system(size: 13))
                .foregroundStyle(Color.tradeTextSecondary)
                .italic()
        case .toolCall:
            Text("Tool: \(block.content ?? "processing")")
                .font(.system(size: 13))
                .foregroundStyle(Color.tradeTextSecondary)
                .italic()
        case .link:
            if let urlString = block.url, let url = URL(string: urlString) {
                Link(block.content ?? urlString, destination: url)
                    .font(.system(size: 14))
                    .foregroundStyle(Color.modeLearn)
            } else {
                Text(block.content ?? "Link")
                    .font(.system(size: 14))
                    .foregroundStyle(Color.modeLearn)
                    .underline()
            }
        }
    }

    private func headingSize(for level: Int?) -> CGFloat {
        switch level {
        case 1: 20
        case 2: 18
        case 3: 16
        default: 18
        }
    }

    private func typingIndicator(viewModel: ChatViewModel) -> some View {
        HStack(spacing: 6) {
            ForEach(0..<3, id: \.self) { index in
                Circle()
                    .fill(Color.tradeTextSecondary)
                    .frame(width: 8, height: 8)
                    .opacity(0.4)
                    .animation(
                        .easeInOut(duration: 0.6)
                            .repeatForever(autoreverses: true)
                            .delay(Double(index) * 0.2),
                        value: viewModel.isStreaming
                    )
            }
        }
        .padding(14)
        .background(Color.tradeSurface)
        .clipShape(.rect(cornerRadius: 16))
        .frame(maxWidth: 80, alignment: .leading)
        .frame(maxWidth: .infinity, alignment: .leading)
    }

    private func navBar(viewModel: ChatViewModel) -> some View {
        HStack(spacing: 12) {
            Button {
                withAnimation(.easeInOut(duration: 0.25)) {
                    showSidebar = true
                }
            } label: {
                Image(systemName: "line.3.horizontal")
                    .font(.system(size: 18, weight: .medium))
                    .foregroundStyle(Color.tradeText)
            }
            .frame(width: 44, height: 44)
            .accessibilityLabel("Menu")

            Spacer()

            Button {
                showSettings = true
            } label: {
                Image(systemName: "gearshape")
                    .font(.system(size: 18))
                    .foregroundStyle(Color.tradeText)
            }
            .frame(width: 44, height: 44)
            .accessibilityLabel("Settings")

            Button {
                viewModel.newConversation(mode: selectedMode)
            } label: {
                Image(systemName: "square.and.pencil")
                    .font(.system(size: 20))
                    .foregroundStyle(Color.tradeText)
            }
            .frame(width: 44, height: 44)
            .accessibilityLabel("New conversation")
        }
    }
}

#Preview {
    ChatView()
        .modelContainer(for: Conversation.self, inMemory: true)
}
