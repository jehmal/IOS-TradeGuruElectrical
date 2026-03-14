import SwiftUI

struct TableBlockView: View {
    let headers: [String]?
    let rows: [[String]]

    private var columnCount: Int {
        headers?.count ?? rows.first?.count ?? 0
    }

    var body: some View {
        ScrollView(.horizontal, showsIndicators: false) {
            VStack(spacing: 0) {
                if let headers {
                    HStack(spacing: 0) {
                        ForEach(Array(headers.enumerated()), id: \.offset) { _, header in
                            Text(header)
                                .font(.system(size: 13, weight: .semibold))
                                .foregroundStyle(Color.tradeText)
                                .frame(minWidth: 80, alignment: .leading)
                                .padding(.horizontal, 12)
                                .padding(.vertical, 8)
                        }
                    }
                    .background(Color.tradeSurface)

                    Divider()
                        .background(Color.tradeBorder)
                }

                ForEach(Array(rows.enumerated()), id: \.offset) { rowIndex, row in
                    HStack(spacing: 0) {
                        ForEach(Array(row.enumerated()), id: \.offset) { _, cell in
                            Text(cell)
                                .font(.system(size: 13))
                                .foregroundStyle(Color.tradeText)
                                .frame(minWidth: 80, alignment: .leading)
                                .padding(.horizontal, 12)
                                .padding(.vertical, 8)
                        }
                    }

                    if rowIndex < rows.count - 1 {
                        Rectangle()
                            .fill(Color.tradeBorder)
                            .frame(height: 0.5)
                    }
                }
            }
        }
        .clipShape(.rect(cornerRadius: 12))
        .overlay(
            RoundedRectangle(cornerRadius: 12)
                .stroke(Color.tradeBorder, lineWidth: 1)
        )
    }
}
