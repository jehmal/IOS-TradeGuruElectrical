import SwiftUI

struct OnboardingFinalPageView: View {
    let onGetStarted: () -> Void
    let onSignIn: () -> Void

    var body: some View {
        VStack(spacing: 0) {
            Spacer()

            Image("TradeGuruLogo")
                .renderingMode(.template)
                .resizable()
                .scaledToFit()
                .frame(width: 120, height: 120)
                .foregroundStyle(Color.tradeGreen)

            Text("Ready to start?")
                .font(.system(size: 28, weight: .bold))
                .foregroundStyle(Color.tradeText)
                .padding(.top, 32)

            Text("Your AI-powered electrical assistant. Ask anything about wiring, faults, standards, or theory.")
                .font(.system(size: 16))
                .foregroundStyle(Color.tradeTextSecondary)
                .multilineTextAlignment(.center)
                .padding(.top, 12)
                .padding(.horizontal, 40)

            Spacer()

            Button(action: onGetStarted) {
                Text("Get Started")
                    .font(.system(size: 17, weight: .semibold))
                    .foregroundStyle(.white)
                    .frame(maxWidth: .infinity)
                    .frame(height: 50)
                    .background(Color.tradeGreen)
                    .clipShape(.rect(cornerRadius: 14))
            }
            .accessibilityLabel("Get started with TradeGuru")
            .padding(.horizontal, 40)

            Button(action: onSignIn) {
                Text("Already have an account? Sign In")
                    .font(.system(size: 14))
                    .foregroundStyle(Color.tradeTextSecondary)
            }
            .accessibilityLabel("Sign in to existing account")
            .padding(.top, 12)
            .padding(.bottom, 16)
        }
    }
}

#Preview {
    OnboardingFinalPageView(onGetStarted: {}, onSignIn: {})
}
