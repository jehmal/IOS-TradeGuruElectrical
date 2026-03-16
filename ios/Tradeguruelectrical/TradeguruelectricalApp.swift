import SwiftUI
import SwiftData

@main
struct TradeguruelectricalApp: App {
    @Environment(\.scenePhase) private var scenePhase
    let container: ModelContainer

    init() {
        let useInMemory = Self.shouldUseInMemoryStore()

        if !useInMemory {
            let appSupport = FileManager.default.urls(for: .applicationSupportDirectory, in: .userDomainMask).first
            if let appSupport, !FileManager.default.fileExists(atPath: appSupport.path) {
                try? FileManager.default.createDirectory(at: appSupport, withIntermediateDirectories: true)
            }
        }

        if useInMemory {
            do {
                let inMemory = try ModelContainer(
                    for: Conversation.self,
                    configurations: ModelConfiguration(isStoredInMemoryOnly: true)
                )
                self.container = inMemory
            } catch {
                self.container = Self.lastResortContainer()
            }
        } else {
            do {
                let container = try ModelContainer(for: Conversation.self)
                DataMigrator.migrateIfNeeded(context: container.mainContext)
                self.container = container
            } catch {
                do {
                    let fallback = try ModelContainer(
                        for: Conversation.self,
                        configurations: ModelConfiguration(isStoredInMemoryOnly: true)
                    )
                    self.container = fallback
                } catch {
                    self.container = Self.lastResortContainer()
                }
            }
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

    private static func shouldUseInMemoryStore() -> Bool {
        if !canWriteToAppSupport() {
            return true
        }
        return false
    }

    private static func lastResortContainer() -> ModelContainer {
        do {
            return try ModelContainer(
                for: Conversation.self,
                configurations: ModelConfiguration(isStoredInMemoryOnly: true)
            )
        } catch {
            return try! ModelContainer(for: Conversation.self)
        }
    }

    private static func canWriteToAppSupport() -> Bool {
        guard let appSupport = FileManager.default.urls(for: .applicationSupportDirectory, in: .userDomainMask).first else {
            return false
        }
        if !FileManager.default.fileExists(atPath: appSupport.path) {
            do {
                try FileManager.default.createDirectory(at: appSupport, withIntermediateDirectories: true)
            } catch {
                return false
            }
        }
        let testFile = appSupport.appendingPathComponent(".tg_write_test_\(ProcessInfo.processInfo.processIdentifier)")
        let testData = Data("test".utf8)
        do {
            try testData.write(to: testFile, options: .atomic)
            try FileManager.default.removeItem(at: testFile)
            return true
        } catch {
            return false
        }
    }
}
