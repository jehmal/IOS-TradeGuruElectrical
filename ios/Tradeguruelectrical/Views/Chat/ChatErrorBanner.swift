import SwiftUI

struct ChatErrorBanner: View {
    let errorMessage: String
    let onRetry: () -> Void
    let onDismiss: () -> Void

    var body: some View {
        HStack(spacing: 8) {
            Image(systemName: "exclamationmark.triangle.fill")
                .font(.system(size: 14))
                .foregroundStyle(.red)

            Text(errorMessage)
                .font(.system(size: 13))
                .foregroundStyle(.red)
                .lineLimit(2)

            Spacer()

            Button {
                onRetry()
            } label: {
                Text("Retry")
                    .font(.system(size: 13, weight: .semibold))
                    .foregroundStyle(.red)
            }
            .frame(height: 44)

            Button {
                onDismiss()
            } label: {
                Image(systemName: "xmark")
                    .font(.system(size: 12, weight: .medium))
                    .foregroundStyle(.red)
            }
            .frame(width: 44, height: 44)
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 8)
        .background(Color.red.opacity(0.1))
    }
}
