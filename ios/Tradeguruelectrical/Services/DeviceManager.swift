import Foundation
import Security

nonisolated struct DeviceManager: Sendable {
    private static let service = "com.tradeguru.electrical"
    private static let account = "device_id"
    private static let userDefaultsKey = "tradeguru_device_id_fallback"

    static func getOrCreateDeviceId() -> String {
        if let existing = readFromKeychain() {
            return existing
        }
        let newId = UUID().uuidString
        saveToKeychain(newId)
        return newId
    }

    static func deviceIdOrFallback() -> String {
        if let existing = readFromKeychain() {
            return existing
        }
        if let fallback = UserDefaults.standard.string(forKey: userDefaultsKey), !fallback.isEmpty {
            return fallback
        }
        let newId = UUID().uuidString
        saveToKeychain(newId)
        UserDefaults.standard.set(newId, forKey: userDefaultsKey)
        return newId
    }

    static func save(_ deviceId: String) {
        saveToKeychain(deviceId)
        UserDefaults.standard.set(deviceId, forKey: userDefaultsKey)
    }

    private static func readFromKeychain() -> String? {
        let query: [String: Any] = [
            kSecClass as String: kSecClassGenericPassword,
            kSecAttrService as String: service,
            kSecAttrAccount as String: account,
            kSecReturnData as String: true,
            kSecMatchLimit as String: kSecMatchLimitOne
        ]

        var result: AnyObject?
        let status = SecItemCopyMatching(query as CFDictionary, &result)

        guard status == errSecSuccess,
              let data = result as? Data,
              let value = String(data: data, encoding: .utf8) else {
            return nil
        }
        return value
    }

    private static func saveToKeychain(_ value: String) {
        guard let data = value.data(using: .utf8) else { return }

        let query: [String: Any] = [
            kSecClass as String: kSecClassGenericPassword,
            kSecAttrService as String: service,
            kSecAttrAccount as String: account,
            kSecValueData as String: data,
            kSecAttrAccessible as String: kSecAttrAccessibleAfterFirstUnlock
        ]

        let status = SecItemAdd(query as CFDictionary, nil)

        if status == errSecDuplicateItem {
            let updateQuery: [String: Any] = [
                kSecClass as String: kSecClassGenericPassword,
                kSecAttrService as String: service,
                kSecAttrAccount as String: account
            ]
            let updateAttributes: [String: Any] = [
                kSecValueData as String: data
            ]
            SecItemUpdate(updateQuery as CFDictionary, updateAttributes as CFDictionary)
        }
    }
}
