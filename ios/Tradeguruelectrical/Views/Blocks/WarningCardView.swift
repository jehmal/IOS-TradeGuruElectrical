import SwiftUI

struct WarningCardView: View {
    let content: String

    var body: some View {
        HStack(alignment: .top, spacing: 10) {
            Image(systemName: "exclamationmark.triangle.fill")
                .font(.system(size: 18))
                .foregroundStyle(Color.modeFaultFinder)

            VStack(alignment: .leading, spacing: 4) {
                Text("Warning")
                    .font(.system(size: 15, weight: .bold))
                    .foregroundStyle(Color.modeFaultFinder)

                Text(content)
                    .font(.system(size: 14))
                    .foregroundStyle(Color.tradeText)
            }
            .frame(maxWidth: .infinity, alignment: .leading)
        }
        .padding(12)
        .background(Color.modeFaultFinder.opacity(0.1))
        .clipShape(.rect(cornerRadius: 12))
        .overlay(
            RoundedRectangle(cornerRadius: 12)
                .stroke(Color.modeFaultFinder, lineWidth: 1)
        )
    }
}
