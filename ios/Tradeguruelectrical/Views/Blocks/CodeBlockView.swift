import SwiftUI
import UIKit

struct CodeBlockView: View {
    let content: String
    let language: String?

    @State private var showCopied = false

    var body: some View {
        VStack(alignment: .trailing, spacing: 0) {
            HStack {
                Spacer()

                if let language {
                    Text(language)
                        .font(.system(size: 11))
                        .foregroundStyle(Color.tradeTextSecondary)
                }

                Button {
                    UIPasteboard.general.string = content
                    showCopied = true
                    Task {
                        try? await Task.sleep(for: .seconds(1.5))
                        showCopied = false
                    }
                } label: {
                    if showCopied {
                        Text("Copied")
                            .font(.system(size: 11))
                            .foregroundStyle(Color.tradeGreen)
                    } else {
                        Image(systemName: "doc.on.doc")
                            .font(.system(size: 12))
                            .foregroundStyle(Color.tradeTextSecondary)
                    }
                }
                .buttonStyle(.plain)
                .accessibilityLabel(Text(verbatim: "Copy code"))
            }
            .padding(.horizontal, 12)
            .padding(.top, 8)

            ScrollView(.horizontal, showsIndicators: false) {
                Text(content)
                    .font(.system(size: 13, design: .monospaced))
                    .foregroundStyle(Color.tradeText)
                    .padding(12)
            }
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(Color.tradeSurface)
        .clipShape(.rect(cornerRadius: 8))
    }
}
