import SwiftUI

struct OnboardingView: View {
    @AppStorage("hasCompletedOnboarding") private var hasCompletedOnboarding = false
    @State private var currentPage = 0

    private var pages: [(icon: String, title: String, description: String, color: Color)] {
        ThinkingMode.allCases.map { ($0.icon, $0.name, $0.fullDescription, $0.color) }
    }

    var body: some View {
        ZStack {
            Color.tradeBg.ignoresSafeArea()

            VStack(spacing: 0) {
                HStack {
                    Spacer()
                    if currentPage < pages.count {
                        Button("Skip") {
                            hasCompletedOnboarding = true
                        }
                        .font(.system(size: 14))
                        .foregroundStyle(Color.tradeTextSecondary)
                        .padding(.trailing, 20)
                        .padding(.top, 8)
                        .accessibilityLabel(Text(verbatim: "Skip onboarding"))
                    }
                }

                TabView(selection: $currentPage) {
                    ForEach(Array(pages.enumerated()), id: \.offset) { index, page in
                        OnboardingPageView(
                            icon: page.icon,
                            title: page.title,
                            description: page.description,
                            color: page.color
                        )
                        .tag(index)
                    }

                    OnboardingFinalPageView(
                        onGetStarted: { hasCompletedOnboarding = true },
                        onSignIn: { hasCompletedOnboarding = true }
                    )
                    .tag(pages.count)
                }
                .tabViewStyle(.page(indexDisplayMode: .always))
            }
        }
    }
}

#Preview {
    OnboardingView()
}
