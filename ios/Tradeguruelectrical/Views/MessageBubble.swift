import SwiftUI

struct MessageBubble: View {
    let message: ChatMessage
    var isLastAssistantMessage: Bool = false
    var onRate: ((Int) -> Void)?
    var onFlag: ((String) -> Void)?
    var onSpeak: ((String) -> Void)?

    @State private var userRating: Int = 0

    var body: some View {
        VStack(alignment: message.role == .user ? .trailing : .leading, spacing: 4) {
            if message.role == .user {
                userBubble
            } else {
                assistantBubble
            }

            HStack(spacing: 4) {
                if message.role == .assistant {
                    Image(systemName: message.mode.icon)
                        .font(.system(size: 14))
                        .foregroundStyle(message.mode.color)
                }

                Text(message.timestamp, style: .time)
                    .font(.system(size: 11))
                    .foregroundStyle(Color.tradeTextSecondary)
            }

            if message.role == .assistant && isLastAssistantMessage {
                actionRow
            }
        }
        .frame(maxWidth: .infinity, alignment: message.role == .user ? .trailing : .leading)
    }

    private var userBubble: some View {
        let textContent = message.blocks
            .compactMap { $0.content }
            .joined(separator: "\n")

        return Text(textContent)
            .font(.system(size: 15))
            .foregroundStyle(.white)
            .padding(.horizontal, 14)
            .padding(.vertical, 10)
            .background(Color.tradeGreen)
            .clipShape(.rect(cornerRadius: 16))
            .frame(maxWidth: 280, alignment: .trailing)
    }

    private var assistantBubble: some View {
        VStack(alignment: .leading, spacing: 12) {
            ForEach(message.blocks) { block in
                blockView(for: block)
            }
        }
        .padding(14)
        .background(Color.tradeSurface)
        .clipShape(.rect(cornerRadius: 16))
        .frame(maxWidth: 330, alignment: .leading)
    }

    private var actionRow: some View {
        HStack(spacing: 8) {
            ForEach(1...5, id: \.self) { star in
                Button {
                    userRating = star
                    onRate?(star)
                } label: {
                    Image(systemName: star <= userRating ? "star.fill" : "star")
                        .font(.system(size: 14))
                        .foregroundStyle(star <= userRating ? Color.tradeGreen : Color.tradeTextSecondary)
                }
                .buttonStyle(.plain)
                .accessibilityLabel("Rate \(star) stars")
            }

            Button {
                onFlag?("inappropriate")
            } label: {
                Image(systemName: "flag")
                    .font(.system(size: 14))
                    .foregroundStyle(Color.tradeTextSecondary)
            }
            .buttonStyle(.plain)
            .accessibilityLabel("Report response")

            Button {
                let text = message.blocks
                    .compactMap { $0.content }
                    .joined(separator: "\n")
                onSpeak?(text)
            } label: {
                Image(systemName: "speaker.wave.2")
                    .font(.system(size: 14))
                    .foregroundStyle(Color.tradeTextSecondary)
            }
            .buttonStyle(.plain)
            .accessibilityLabel("Read aloud")
        }
        .frame(height: 44)
    }

    @ViewBuilder
    private func blockView(for block: ContentBlock) -> some View {
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
}
