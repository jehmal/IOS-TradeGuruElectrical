import SwiftUI

struct RegulationView: View {
    let code: String?
    let clause: String?
    let summary: String?

    var body: some View {
        HStack(spacing: 0) {
            Rectangle()
                .fill(Color.modeResearch)
                .frame(width: 4)

            VStack(alignment: .leading, spacing: 6) {
                if let code {
                    Text(code)
                        .font(.system(size: 15, weight: .bold))
                        .foregroundStyle(Color.tradeText)
                }

                if let clause {
                    Text(clause)
                        .font(.system(size: 13))
                        .foregroundStyle(Color.tradeTextSecondary)
                }

                if let summary {
                    Text(summary)
                        .font(.system(size: 14))
                        .foregroundStyle(Color.tradeText)
                }
            }
            .padding(12)
            .frame(maxWidth: .infinity, alignment: .leading)
        }
        .background(Color.tradeSurface)
        .clipShape(.rect(cornerRadius: 12))
    }
}
