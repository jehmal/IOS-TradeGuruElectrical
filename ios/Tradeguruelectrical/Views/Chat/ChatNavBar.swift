import SwiftUI

struct ChatNavBar: View {
    let onMenuTap: () -> Void
    let onSettingsTap: () -> Void
    let onNewChat: () -> Void

    var body: some View {
        HStack(spacing: 12) {
            Button {
                onMenuTap()
            } label: {
                Image(systemName: "line.3.horizontal")
                    .font(.system(size: 18, weight: .medium))
                    .foregroundStyle(Color.tradeText)
            }
            .frame(width: 44, height: 44)
            .accessibilityLabel("Menu")

            Spacer()

            Button {
                onSettingsTap()
            } label: {
                Image(systemName: "gearshape")
                    .font(.system(size: 18))
                    .foregroundStyle(Color.tradeText)
            }
            .frame(width: 44, height: 44)
            .accessibilityLabel("Settings")

            Button {
                onNewChat()
            } label: {
                Image(systemName: "square.and.pencil")
                    .font(.system(size: 20))
                    .foregroundStyle(Color.tradeText)
            }
            .frame(width: 44, height: 44)
            .accessibilityLabel("New conversation")
        }
    }
}
