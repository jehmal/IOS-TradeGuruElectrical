import SwiftUI

struct CodeBlockView: View {
    let content: String
    let language: String?

    var body: some View {
        VStack(alignment: .trailing, spacing: 0) {
            if let language {
                Text(language)
                    .font(.system(size: 11))
                    .foregroundStyle(Color.tradeTextSecondary)
                    .padding(.trailing, 12)
                    .padding(.top, 8)
            }

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
