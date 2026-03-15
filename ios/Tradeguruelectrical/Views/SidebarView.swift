import SwiftUI

struct SidebarView: View {
    let conversations: [Conversation]
    let onSelect: (Conversation) -> Void
    let onDelete: (Conversation) -> Void
    let onNewChat: () -> Void
    let onClose: () -> Void
    @State private var searchText = ""

    private var filteredConversations: [Conversation] {
        if searchText.isEmpty { return conversations }
        return conversations.filter { $0.title.localizedStandardContains(searchText) }
    }

    var body: some View {
        ZStack(alignment: .leading) {
            Color.black.opacity(0.3)
                .ignoresSafeArea()
                .onTapGesture { onClose() }

            sidebarContent
                .frame(width: 300)
                .background(Color.tradeBg)
                .transition(.move(edge: .leading))
        }
    }

    private var sidebarContent: some View {
        VStack(spacing: 0) {
            header
                .padding(.horizontal, 16)
                .padding(.vertical, 12)

            Color.tradeBorder.frame(height: 1)

            HStack(spacing: 8) {
                Image(systemName: "magnifyingglass")
                    .font(.system(size: 14))
                    .foregroundStyle(Color.tradeTextSecondary)
                TextField("Search conversations", text: $searchText)
                    .font(.system(size: 14))
                if !searchText.isEmpty {
                    Button {
                        searchText = ""
                    } label: {
                        Image(systemName: "xmark.circle.fill")
                            .font(.system(size: 14))
                            .foregroundStyle(Color.tradeTextSecondary)
                    }
                    .accessibilityLabel("Clear search")
                }
            }
            .padding(8)
            .background(Color.tradeInput)
            .clipShape(.rect(cornerRadius: 10))
            .padding(.horizontal, 16)
            .padding(.vertical, 8)

            List {
                ForEach(filteredConversations) { conversation in
                    Button {
                        onSelect(conversation)
                    } label: {
                        conversationRow(conversation)
                    }
                    .accessibilityLabel(conversation.title)
                    .listRowInsets(EdgeInsets())
                    .listRowBackground(Color.clear)
                }
                .onDelete { indexSet in
                    for index in indexSet {
                        let conversation = filteredConversations[index]
                        onDelete(conversation)
                    }
                }
            }
            .listStyle(.plain)

            Color.tradeBorder.frame(height: 1)

            Button {
                onNewChat()
            } label: {
                Text("New Conversation")
                    .font(.system(size: 15, weight: .semibold))
                    .foregroundStyle(.white)
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 12)
                    .background(Color.tradeGreen)
                    .clipShape(.rect(cornerRadius: 12))
            }
            .accessibilityLabel("Start new conversation")
            .padding(16)
        }
    }

    private var header: some View {
        HStack {
            Text("Conversations")
                .font(.system(size: 20, weight: .semibold))
                .foregroundStyle(Color.tradeText)

            Spacer()

            Button {
                onClose()
            } label: {
                Image(systemName: "xmark")
                    .font(.system(size: 14, weight: .semibold))
                    .foregroundStyle(Color.tradeTextSecondary)
                    .frame(width: 44, height: 44)
            }
            .accessibilityLabel("Close menu")
        }
    }

    private func conversationRow(_ conversation: Conversation) -> some View {
        HStack(spacing: 10) {
            Image(systemName: conversation.mode.icon)
                .font(.system(size: 14))
                .foregroundStyle(conversation.mode.color)

            VStack(alignment: .leading, spacing: 2) {
                Text(conversation.title)
                    .font(.system(size: 15, weight: .semibold))
                    .foregroundStyle(Color.tradeText)
                    .lineLimit(1)

                Text(conversation.updatedAt, style: .date)
                    .font(.system(size: 12))
                    .foregroundStyle(Color.tradeTextSecondary)

                Text("\(conversation.messages.count) messages")
                    .font(.system(size: 12))
                    .foregroundStyle(Color.tradeTextSecondary)
            }

            Spacer()
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 10)
        .frame(minHeight: 44)
        .contentShape(Rectangle())
    }
}
