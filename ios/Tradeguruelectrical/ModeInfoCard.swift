import SwiftUI

struct ModeInfoCard: View {
    let mode: ThinkingMode
    let onDismiss: () -> Void

    var body: some View {
        HStack(alignment: .top, spacing: 12) {
            Image(systemName: mode.icon)
                .font(.system(size: 16, weight: .semibold))
                .foregroundStyle(.white)
                .frame(width: 32, height: 32)
                .background(mode.color)
                .clipShape(.rect(cornerRadius: 8))

            VStack(alignment: .leading, spacing: 4) {
                Text(mode.name)
                    .font(.system(size: 16, weight: .semibold))
                    .foregroundStyle(mode.color)

                Text(mode.fullDescription)
                    .font(.system(size: 14))
                    .foregroundStyle(Color.tradeTextSecondary)
                    .lineLimit(3)
            }

            Spacer()

            Button(action: onDismiss) {
                Image(systemName: "xmark")
                    .font(.system(size: 12, weight: .semibold))
                    .foregroundStyle(Color.tradeTextSecondary)
                    .frame(width: 24, height: 24)
            }
            .accessibilityLabel(Text(verbatim: "Dismiss mode info"))
        }
        .padding(12)
        .background(Color.tradeSurface)
        .overlay(
            RoundedRectangle(cornerRadius: 12)
                .stroke(Color.tradeBorder, lineWidth: 1)
        )
        .clipShape(.rect(cornerRadius: 12))
        .padding(.horizontal, 16)
    }
}

#Preview {
    ModeInfoCard(mode: .faultFinder, onDismiss: {})
}
