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
            ChatNavBar(
                onMenuTap: { withAnimation(.easeInOut(duration: 0.25)) { showSidebar = true } },
                onSettingsTap: { showSettings = true },
                onNewChat: { viewModel.newConversation(mode: selectedMode) }
            )
            .padding(.horizontal, 16)
            .padding(.vertical, 8)
            .background(Color.tradeBg)

            conversationArea(viewModel: viewModel)

            if let error = viewModel.error {
                ChatErrorBanner(
                    errorMessage: error,
                    onRetry: { viewModel.retryLastRequest() },
                    onDismiss: { viewModel.dismissError() }
                )
            }

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
                    ChatMessageList(conversation: conversation, viewModel: viewModel)
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

    private func dismissModeCard() {
        withAnimation(.easeOut(duration: 0.15)) {
            showModeCard = false
            userDismissedCard = true
        }
    }
}

#Preview {
    ChatView()
        .modelContainer(for: Conversation.self, inMemory: true)
}
