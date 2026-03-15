import SwiftUI
import SwiftData

@main
struct TradeguruelectricalApp: App {
    @Environment(\.scenePhase) private var scenePhase
    let container: ModelContainer

    init() {
        do {
            let container = try ModelContainer(for: Conversation.self)
            DataMigrator.migrateIfNeeded(context: container.mainContext)
            self.container = container
        } catch {
            let fallback = try! ModelContainer(
                for: Conversation.self,
                configurations: ModelConfiguration(isStoredInMemoryOnly: true)
            )
            self.container = fallback
        }
        Task { await AuthManager.shared.restoreSession() }
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
        .modelContainer(container)
        .onChange(of: scenePhase) { _, phase in
            if phase == .active {
                Task { await AuthManager.shared.refreshTokenIfNeeded() }
            }
        }
    }
}
