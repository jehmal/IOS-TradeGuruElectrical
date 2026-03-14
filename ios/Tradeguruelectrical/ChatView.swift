import SwiftUI

struct ChatView: View {
    @State private var viewModel = ChatViewModel()
    @State private var selectedMode: ThinkingMode = .faultFinder
    @State private var inputText = ""
    @State private var showModeCard = true
    @State private var showSidebar = false
    @State private var userDismissedCard = false

    var body: some View {
        VStack(spacing: 0) {
            navBar
                .padding(.horizontal, 16)
                .padding(.vertical, 8)
                .background(Color.tradeBg)

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
                        .transition(.opacity.combined(with: .move(edge: .top)))
                        .padding(.top, 16)
                    }

                    if let conversation = viewModel.activeConversation,
                       !conversation.messages.isEmpty {
                        ScrollView {
                            LazyVStack(spacing: 16) {
                                ForEach(conversation.messages) { message in
                                    MessageBubble(message: message)
                                }
                            }
                            .padding(.horizontal, 16)
                            .padding(.vertical, 12)
                        }
                    } else {
                        Spacer()

                        Image("TradeGuruLogo")
                            .renderingMode(.template)
                            .resizable()
                            .scaledToFit()
                            .frame(width: 180, height: 180)
                            .foregroundStyle(Color.tradeText)
                            .opacity(0.08)

                        Spacer()
                    }
                }
            }

            ChatInputBar(
                text: $inputText,
                selectedMode: $selectedMode,
                onSend: { text, attachments in
                    viewModel.send(text, mode: selectedMode, attachments: attachments)
                    inputText = ""
                },
                onInputFocus: {
                    dismissModeCard()
                }
            )
        }
        .onChange(of: inputText) { _, newValue in
            if !newValue.isEmpty {
                dismissModeCard()
            }
        }
        .overlay {
            if showSidebar {
                SidebarView(
                    conversations: viewModel.conversations,
                    onSelect: { conversation in
                        viewModel.selectConversation(conversation)
                        withAnimation(.easeInOut(duration: 0.25)) { showSidebar = false }
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
        }
        .onChange(of: selectedMode) { _, newMode in
            userDismissedCard = false
            withAnimation(.easeInOut(duration: 0.2)) {
                showModeCard = true
            }
            viewModel.selectedMode = newMode
        }
    }

    private func dismissModeCard() {
        withAnimation(.easeOut(duration: 0.15)) {
            showModeCard = false
            userDismissedCard = true
        }
    }

    private var navBar: some View {
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

            Spacer()

            Button {
                viewModel.newConversation(mode: selectedMode)
            } label: {
                Image(systemName: "square.and.pencil")
                    .font(.system(size: 20))
                    .foregroundStyle(Color.tradeText)
            }
            .frame(width: 44, height: 44)
        }
    }
}

#Preview {
    ChatView()
}
