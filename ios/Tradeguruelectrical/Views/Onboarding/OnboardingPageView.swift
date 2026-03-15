import SwiftUI

struct OnboardingPageView: View {
    let icon: String
    let title: String
    let description: String
    let color: Color

    var body: some View {
        VStack(spacing: 0) {
            Spacer()

            Image(systemName: icon)
                .font(.system(size: 80, weight: .semibold))
                .foregroundStyle(color)
                .frame(width: 120, height: 120)
                .background(color.opacity(0.12))
                .clipShape(Circle())

            Text(title)
                .font(.system(size: 28, weight: .bold))
                .foregroundStyle(Color.tradeText)
                .padding(.top, 32)

            Text(description)
                .font(.system(size: 16))
                .foregroundStyle(Color.tradeTextSecondary)
                .multilineTextAlignment(.center)
                .lineLimit(4)
                .padding(.top, 12)
                .padding(.horizontal, 40)

            Spacer()
        }
        .accessibilityElement(children: .combine)
    }
}

#Preview {
    OnboardingPageView(
        icon: "bolt.fill",
        title: "Fault Finder",
        description: "Diagnose electrical faults, trace circuits, and identify issues with AI-assisted troubleshooting.",
        color: .modeFaultFinder
    )
}
