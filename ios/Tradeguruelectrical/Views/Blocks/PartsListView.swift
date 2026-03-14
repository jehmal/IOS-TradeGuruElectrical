import SwiftUI

struct PartsListView: View {
    let items: [PartsItem]

    var body: some View {
        VStack(spacing: 0) {
            HStack(spacing: 0) {
                Text("Item")
                    .frame(maxWidth: .infinity, alignment: .leading)
                Text("Specification")
                    .frame(maxWidth: .infinity, alignment: .leading)
                Text("Qty")
                    .frame(width: 44, alignment: .center)
            }
            .font(.system(size: 13, weight: .semibold))
            .foregroundStyle(Color.tradeText)
            .padding(.horizontal, 12)
            .padding(.vertical, 8)
            .background(Color.tradeSurface)

            ForEach(Array(items.enumerated()), id: \.element.id) { index, item in
                HStack(spacing: 0) {
                    Text(item.name)
                        .frame(maxWidth: .infinity, alignment: .leading)
                    Text(item.spec)
                        .frame(maxWidth: .infinity, alignment: .leading)
                    Text("\(item.qty)")
                        .frame(width: 44, alignment: .center)
                }
                .font(.system(size: 13))
                .foregroundStyle(Color.tradeText)
                .padding(.horizontal, 12)
                .padding(.vertical, 8)
                .background(index.isMultiple(of: 2) ? Color.tradeBg : Color.tradeSurface)
            }
        }
        .clipShape(.rect(cornerRadius: 12))
        .overlay(
            RoundedRectangle(cornerRadius: 12)
                .stroke(Color.tradeBorder, lineWidth: 1)
        )
    }
}
