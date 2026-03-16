import SwiftUI

struct ChatMessageList: View {
    let conversation: Conversation
    let viewModel: ChatViewModel

    var body: some View {
        ScrollViewReader { proxy in
            ScrollView {
                LazyVStack(spacing: 16) {
                    ForEach(conversation.messages) { message in
                        messageBubbleRow(message: message)
                    }

                    if viewModel.isStreaming && !viewModel.streamingBlocks.isEmpty {
                        streamingBubble
                    } else if viewModel.isStreaming && viewModel.streamingBlocks.isEmpty {
                        typingIndicator
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

    private func messageBubbleRow(message: ChatMessage) -> some View {
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

    private var streamingBubble: some View {
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

    private var typingIndicator: some View {
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
}
