import SwiftUI

struct PipelineStatusView: View {
    @Binding var stage: PipelineStage

    var body: some View {
        Group {
            if stage != .idle && stage != .error {
                HStack(spacing: 10) {
                    Image(systemName: iconName)
                        .font(.system(size: 16))
                        .foregroundStyle(Color.tradeTextSecondary)

                    Text(statusText)
                        .font(.system(size: 14))
                        .foregroundStyle(Color.tradeTextSecondary)

                    PipelineStatusDots()
                }
                .accessibilityElement(children: .combine)
                .accessibilityLabel(statusText)
                .padding(.horizontal, 16)
                .padding(.vertical, 10)
                .background(Color.tradeSurface)
                .clipShape(.rect(cornerRadius: 20))
                .transition(.opacity.combined(with: .scale(scale: 0.95)))
            }
        }
        .animation(.easeInOut(duration: 0.3), value: stage)
    }

    private var iconName: String {
        switch stage {
        case .searching:
            return "magnifyingglass.circle"
        case .synthesizing:
            return "brain.head.profile"
        case .streaming:
            return "text.bubble"
        default:
            return ""
        }
    }

    private var statusText: String {
        switch stage {
        case .searching:
            return "Searching knowledge base..."
        case .synthesizing:
            return "Synthesizing response..."
        case .streaming:
            return "Streaming..."
        default:
            return ""
        }
    }
}
