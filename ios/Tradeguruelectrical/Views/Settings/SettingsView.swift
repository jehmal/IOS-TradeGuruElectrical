import SwiftUI

struct SettingsView: View {
    @Environment(\.dismiss) private var dismiss
    @State private var showSignIn = false
    @State private var showClearConfirmation = false

    private var auth: AuthManager { AuthManager.shared }

    private var appVersion: String {
        let version = Bundle.main.infoDictionary?["CFBundleShortVersionString"] as? String ?? "1.0"
        let build = Bundle.main.infoDictionary?["CFBundleVersion"] as? String ?? "1"
        return "\(version) (\(build))"
    }

    var body: some View {
        NavigationStack {
            List {
                accountSection
                dataSection
                aboutSection
            }
            .navigationTitle("Settings")
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
            .sheet(isPresented: $showSignIn) {
                SignInView(
                    onGoogleSignIn: {
                        showSignIn = false
                        Task { await auth.signIn(provider: "GoogleOAuth") }
                    },
                    onAppleSignIn: {
                        showSignIn = false
                        Task { await auth.signIn(provider: "AppleOAuth") }
                    },
                    onEmailSignIn: {
                        showSignIn = false
                        Task { await auth.signIn(provider: nil) }
                    }
                )
            }
            .confirmationDialog(
                "Clear All Conversations",
                isPresented: $showClearConfirmation,
                titleVisibility: .visible
            ) {
                Button("Clear All", role: .destructive) {}
                Button("Cancel", role: .cancel) {}
            } message: {
                Text("This will permanently delete all your conversations. This action cannot be undone.")
            }
        }
    }

    @ViewBuilder
    private var accountSection: some View {
        Section("Account") {
            if auth.isAuthenticated, let user = auth.currentUser {
                HStack(spacing: 12) {
                    if let pictureURL = user.pictureURL, let url = URL(string: pictureURL) {
                        AsyncImage(url: url) { image in
                            image
                                .resizable()
                                .scaledToFill()
                        } placeholder: {
                            Image(systemName: "person.circle.fill")
                                .resizable()
                                .foregroundStyle(Color.tradeTextSecondary)
                        }
                        .frame(width: 60, height: 60)
                        .clipShape(.circle)
                    } else {
                        Image(systemName: "person.circle.fill")
                            .resizable()
                            .foregroundStyle(Color.tradeTextSecondary)
                            .frame(width: 60, height: 60)
                    }

                    VStack(alignment: .leading, spacing: 4) {
                        if let name = user.name {
                            Text(name)
                                .font(.system(size: 17, weight: .semibold))
                                .foregroundStyle(Color.tradeText)
                        }
                        Text(user.email)
                            .font(.system(size: 14))
                            .foregroundStyle(Color.tradeTextSecondary)
                        TierBadgeView(tier: auth.tier)
                    }
                }
                .padding(.vertical, 4)

                Button(role: .destructive) {
                    auth.signOut()
                } label: {
                    Text("Sign Out")
                }
                .accessibilityLabel("Sign out")
            } else {
                VStack(alignment: .leading, spacing: 8) {
                    Text("Sign In to TradeGuru")
                        .font(.system(size: 15))
                        .foregroundStyle(Color.tradeText)
                    Text("Access Pro features and sync across devices")
                        .font(.system(size: 13))
                        .foregroundStyle(Color.tradeTextSecondary)
                }
                .padding(.vertical, 4)

                Button {
                    showSignIn = true
                } label: {
                    Text("Sign In")
                        .foregroundStyle(Color.tradeGreen)
                }
                .accessibilityLabel("Sign in to TradeGuru")
            }
        }
    }

    @ViewBuilder
    private var dataSection: some View {
        Section("Data") {
            Button(role: .destructive) {
                showClearConfirmation = true
            } label: {
                Text("Clear All Conversations")
            }
            .accessibilityLabel("Clear all conversations")
        }
    }

    @ViewBuilder
    private var aboutSection: some View {
        Section("About") {
            HStack {
                Text("TradeGuru Electrical")
                    .foregroundStyle(Color.tradeText)
                Spacer()
                Text(appVersion)
                    .foregroundStyle(Color.tradeTextSecondary)
            }

            if let termsURL = URL(string: "https://tradeguru.com.au/terms") {
                Link("Terms of Service", destination: termsURL)
            }

            if let privacyURL = URL(string: "https://tradeguru.com.au/privacy") {
                Link("Privacy Policy", destination: privacyURL)
            }
        }
    }
}

#Preview("Anonymous") {
    SettingsView()
}

#Preview("Authenticated") {
    SettingsView()
}
