import SwiftUI

struct TextBlockView: View {
    let content: String

    var body: some View {
        Text(content)
            .font(.system(size: 15))
            .foregroundStyle(Color.tradeText)
            .frame(maxWidth: .infinity, alignment: .leading)
    }
}
