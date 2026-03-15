import SwiftUI

struct ContentView: View {
    @AppStorage("hasCompletedOnboarding") private var hasCompletedOnboarding = false
    @State private var hasAcceptedDisclaimer = false

    var body: some View {
        Group {
            if !hasCompletedOnboarding {
                OnboardingView()
                    .transition(.opacity)
            } else if !hasAcceptedDisclaimer {
                SafetyDisclaimerView {
                    hasAcceptedDisclaimer = true
                }
                .transition(.opacity)
            } else {
                ChatView()
                    .transition(.opacity)
            }
        }
        .animation(.easeInOut(duration: 0.3), value: hasCompletedOnboarding)
        .animation(.easeInOut(duration: 0.3), value: hasAcceptedDisclaimer)
    }
}

#Preview {
    ContentView()
}
