import SwiftUI
import SwiftData

@main
struct TradeguruelectricalApp: App {
    @Environment(\.scenePhase) private var scenePhase
    let container: ModelContainer

    init() {
        Self.installCrashHandlers()
        NSLog("[TG] App init starting — useInMemory: \(Self.shouldUseInMemoryStore())")
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
                    migrationPlan: TradeGuruMigrationPlan.self,
                    configurations: ModelConfiguration(isStoredInMemoryOnly: true)
                )
                self.container = inMemory
            } catch {
                self.container = Self.lastResortContainer()
            }
        } else {
            do {
                let container = try ModelContainer(for: Conversation.self, migrationPlan: TradeGuruMigrationPlan.self)
                DataMigrator.migrateIfNeeded(context: container.mainContext)
                self.container = container
            } catch {
                do {
                    let fallback = try ModelContainer(
                        for: Conversation.self,
                        migrationPlan: TradeGuruMigrationPlan.self,
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
                migrationPlan: TradeGuruMigrationPlan.self,
                configurations: ModelConfiguration(isStoredInMemoryOnly: true)
            )
        } catch {
            return try! ModelContainer(for: Conversation.self, migrationPlan: TradeGuruMigrationPlan.self)
        }
    }

    private static func installCrashHandlers() {
        NSSetUncaughtExceptionHandler { exception in
            NSLog("[TG-CRASH] Uncaught exception: \(exception.name.rawValue)")
            NSLog("[TG-CRASH] Reason: \(exception.reason ?? "nil")")
            NSLog("[TG-CRASH] Stack: \(exception.callStackSymbols.prefix(10).joined(separator: "\n"))")
        }
        signal(SIGABRT) { _ in NSLog("[TG-CRASH] SIGABRT received — likely ICU abort or assertion failure") }
        signal(SIGSEGV) { _ in NSLog("[TG-CRASH] SIGSEGV received — memory access violation") }
        signal(SIGBUS) { _ in NSLog("[TG-CRASH] SIGBUS received — bus error") }
        signal(SIGTRAP) { _ in NSLog("[TG-CRASH] SIGTRAP received — Swift precondition/fatal error") }
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
