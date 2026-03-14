### Task
Code-scribe agent #3: Wiring existing views, SidebarView, and HTML component viewer.

### Steps taken
1. Read all reference files: CLAUDE.md, frontend-components-build.md, all existing Swift files, chatmock.md, inventory.md, backend spec, existing chat.html
2. Verified Agent #1 models (ContentBlock, ChatMessage, Conversation) are already created with correct types
3. Verified Agent #2 MessageBubble view already exists with correct block switch statement
4. Created new SidebarView.swift with slide-in drawer, conversation list, close/select/new-chat closures
5. Updated ChatView.swift: added viewModel state, showSidebar state, wired hamburger button, wired new chat button, replaced empty state with ScrollView+MessageBubble rendering, added sidebar overlay, wired ChatInputBar onSend, synced selectedMode with viewModel
6. Updated ChatInputBar.swift: added onSend parameter, wired send button action, changed placeholder to "Ask TradeGuru"
7. Rewrote preview/chat.html as full component viewer with device picker (iPhone/iPad/Watch), component picker (14 options), dark/light toggle, all mock data matching Swift MockData, embedded logo base64, interactive sidebar, mode selector cycling
8. Verified no .foregroundColor(), .cornerRadius() anti-patterns in any Swift file

### Important notes
- ChatView.swift references ChatViewModel which does not exist yet (Agent #1 is expected to create it). The viewModel must have: `conversations`, `activeConversation`, `selectedMode`, `send(_:mode:attachments:)`, `newConversation(mode:)`, `selectConversation(_:)`.
- SidebarView does NOT use @State for data owned by parent - all data passed via closures and let parameters.
- ChatInputBar.onSend is optional to preserve backward compatibility with existing #Preview.
- The HTML mock data uses the same conversations, blocks, and Australian electrical content (RCD testing, cable sizing, switchboard requirements) specified in the build prompt.
- HTML is fully self-contained with zero external dependencies.

### File paths
- `/mnt/c/users/jehma/desktop/Tradeguru-swft/ios/Tradeguruelectrical/Views/SidebarView.swift` (NEW)
- `/mnt/c/users/jehma/desktop/Tradeguru-swft/ios/Tradeguruelectrical/ChatView.swift` (UPDATED)
- `/mnt/c/users/jehma/desktop/Tradeguru-swft/ios/Tradeguruelectrical/ChatInputBar.swift` (UPDATED)
- `/mnt/c/users/jehma/desktop/Tradeguru-swft/preview/chat.html` (REWRITTEN)
