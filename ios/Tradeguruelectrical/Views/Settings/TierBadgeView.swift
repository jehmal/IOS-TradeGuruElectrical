import SwiftUI

struct TierBadgeView: View {
    let tier: UserTier

    var body: some View {
        Text(tier.displayName)
            .font(.system(size: 12, weight: .semibold))
            .foregroundStyle(tier.color)
            .padding(.horizontal, 10)
            .padding(.vertical, 4)
            .background(tier.color.opacity(0.15))
            .clipShape(.rect(cornerRadius: 8))
            .accessibilityLabel(Text(verbatim: "\(tier.displayName) plan"))
    }
}

#Preview {
    VStack(spacing: 12) {
        TierBadgeView(tier: .free)
        TierBadgeView(tier: .pro)
        TierBadgeView(tier: .unlimited)
    }
    .padding()
}
