import SwiftUI
import PhotosUI

struct ChatInputBar: View {
    @Binding var text: String
    @Binding var selectedMode: ThinkingMode
    var onSend: ((String, [MessageAttachment]?) -> Void)?
    var onInputFocus: (() -> Void)?
    @State private var attachmentActive = false
    @State private var selectedItem: PhotosPickerItem?
    @FocusState private var inputFocused: Bool

    var body: some View {
        VStack(spacing: 0) {
            Color.tradeBorder.frame(height: 1)

            VStack(spacing: 10) {
                ModeSelector(selectedMode: $selectedMode)
                    .padding(.horizontal, 12)
                    .padding(.top, 10)

                HStack(spacing: 0) {
                    if attachmentActive {
                        Button {
                            withAnimation(.spring(duration: 0.3)) {
                                attachmentActive = false
                                selectedItem = nil
                            }
                        } label: {
                            Image(systemName: "plus")
                                .font(.system(size: 16, weight: .medium))
                                .foregroundStyle(Color.tradeText)
                                .rotationEffect(.degrees(45))
                                .frame(width: 30, height: 30)
                                .background(Color.tradeInput)
                                .clipShape(Circle())
                        }
                        .padding(.leading, 12)
                    } else {
                        PhotosPicker(selection: $selectedItem, matching: .any(of: [.images, .videos])) {
                            Image(systemName: "plus")
                                .font(.system(size: 16, weight: .medium))
                                .foregroundStyle(Color.tradeText)
                                .frame(width: 30, height: 30)
                                .background(Color.tradeInput)
                                .clipShape(Circle())
                        }
                        .padding(.leading, 12)
                        .onChange(of: selectedItem) { _, newItem in
                            if newItem != nil {
                                withAnimation(.spring(duration: 0.3)) {
                                    attachmentActive = true
                                }
                            }
                        }
                    }

                    TextField("Ask TradeGuru", text: $text, axis: .vertical)
                        .focused($inputFocused)
                        .font(.system(size: 16))
                        .lineLimit(1...5)
                        .padding(10)
                        .background(Color.tradeLight)
                        .overlay(
                            RoundedRectangle(cornerRadius: 20)
                                .stroke(Color.tradeBorder, lineWidth: 0.5)
                        )
                        .clipShape(.rect(cornerRadius: 20))
                        .padding(.horizontal, 10)
                        .onChange(of: inputFocused) { _, focused in
                            if focused { onInputFocus?() }
                        }

                    if !text.isEmpty {
                        Button {
                            onSend?(text, nil)
                            text = ""
                        } label: {
                            Image(systemName: "arrow.up.circle.fill")
                                .font(.system(size: 28))
                                .foregroundStyle(Color.tradeGreen)
                        }
                        .padding(.trailing, 12)
                        .transition(.scale.combined(with: .opacity))
                    }
                }
                .padding(.bottom, 10)
            }
            .background(.ultraThinMaterial)
        }
    }
}

#Preview {
    ChatInputBar(text: .constant(""), selectedMode: .constant(.faultFinder))
}
