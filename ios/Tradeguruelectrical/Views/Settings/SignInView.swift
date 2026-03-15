import SwiftUI

struct SignInView: View {
    let onGoogleSignIn: () -> Void
    let onAppleSignIn: () -> Void
    let onEmailSignIn: () -> Void
    @Environment(\.dismiss) private var dismiss

    var body: some View {
        NavigationStack {
            VStack(spacing: 12) {
                Button {
                    onGoogleSignIn()
                } label: {
                    Text("Continue with Google")
                        .font(.system(size: 17, weight: .semibold))
                        .foregroundStyle(Color(hex: 0x242026))
                        .frame(maxWidth: .infinity)
                        .frame(height: 50)
                        .background(.white)
                        .clipShape(.rect(cornerRadius: 12))
                        .overlay(
                            RoundedRectangle(cornerRadius: 12)
                                .stroke(Color.tradeBorder, lineWidth: 1)
                        )
                }
                .accessibilityLabel("Sign in with Google")

                Button {
                    onAppleSignIn()
                } label: {
                    Text("Continue with Apple")
                        .font(.system(size: 17, weight: .semibold))
                        .foregroundStyle(.white)
                        .frame(maxWidth: .infinity)
                        .frame(height: 50)
                        .background(.black)
                        .clipShape(.rect(cornerRadius: 12))
                }
                .accessibilityLabel("Sign in with Apple")

                Button {
                    onEmailSignIn()
                } label: {
                    Text("Continue with Email")
                        .font(.system(size: 17, weight: .semibold))
                        .foregroundStyle(.white)
                        .frame(maxWidth: .infinity)
                        .frame(height: 50)
                        .background(Color.tradeGreen)
                        .clipShape(.rect(cornerRadius: 12))
                }
                .accessibilityLabel("Sign in with email")
            }
            .padding(24)
            .navigationTitle("Sign In")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .topBarTrailing) {
                    Button {
                        dismiss()
                    } label: {
                        Image(systemName: "xmark")
                            .font(.system(size: 14, weight: .semibold))
                            .foregroundStyle(Color.tradeTextSecondary)
                    }
                }
            }
        }
    }
}

#Preview {
    SignInView(
        onGoogleSignIn: {},
        onAppleSignIn: {},
        onEmailSignIn: {}
    )
}
