import SwiftUI

struct CalloutView: View {
    let content: String
    let style: String?

    private var resolvedStyle: CalloutStyle {
        switch style {
        case "tip": .tip
        case "important": .important
        default: .info
        }
    }

    var body: some View {
        HStack(spacing: 0) {
            Rectangle()
                .fill(resolvedStyle.tintColor)
                .frame(width: 4)

            HStack(alignment: .top, spacing: 10) {
                Image(systemName: resolvedStyle.iconName)
                    .font(.system(size: 16))
                    .foregroundStyle(resolvedStyle.tintColor)

                Text(content)
                    .font(.system(size: 14))
                    .foregroundStyle(Color.tradeText)
                    .frame(maxWidth: .infinity, alignment: .leading)
            }
            .padding(12)
        }
        .background(Color.tradeSurface)
        .clipShape(.rect(cornerRadius: 12))
    }
}

private enum CalloutStyle {
    case tip
    case info
    case important

    var iconName: String {
        switch self {
        case .tip: "lightbulb.fill"
        case .info: "info.circle.fill"
        case .important: "exclamationmark.circle.fill"
        }
    }

    var tintColor: Color {
        switch self {
        case .tip: .tradeGreen
        case .info: .modeLearn
        case .important: .modeFaultFinder
        }
    }
}
