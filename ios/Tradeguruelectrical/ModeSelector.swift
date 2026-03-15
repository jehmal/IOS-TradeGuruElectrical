import SwiftUI

struct ModeSelector: View {
    @Binding var selectedMode: ThinkingMode

    var body: some View {
        HStack(spacing: 6) {
            ForEach(ThinkingMode.allCases) { mode in
                Button {
                    withAnimation(.easeInOut(duration: 0.2)) {
                        selectedMode = mode
                    }
                } label: {
                    VStack(spacing: 2) {
                        HStack(spacing: 4) {
                            Image(systemName: mode.icon)
                                .font(.system(size: 11))
                            Text(mode.name)
                                .font(.system(size: 12, weight: .semibold))
                        }
                        Text(mode.shortDescription)
                            .font(.system(size: 10))
                    }
                    .frame(maxWidth: .infinity)
                    .padding(.horizontal, 10)
                    .padding(.vertical, 8)
                    .foregroundStyle(selectedMode == mode ? mode.color : Color.tradeText)
                    .background(selectedMode == mode ? mode.color.opacity(0.12) : Color.tradeInput)
                    .overlay(
                        RoundedRectangle(cornerRadius: 10)
                            .stroke(selectedMode == mode ? mode.color : Color.tradeBorder, lineWidth: 1)
                    )
                    .clipShape(.rect(cornerRadius: 10))
                }
                .accessibilityLabel("Select \(mode.name) mode")
            }
        }
    }
}

#Preview {
    ModeSelector(selectedMode: .constant(.faultFinder))
        .padding()
}
