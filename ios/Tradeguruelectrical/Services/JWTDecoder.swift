import Foundation

nonisolated enum JWTDecoder: Sendable {
    static func decode(_ jwt: String) -> (user: AuthUser, expiresAt: Date)? {
        let segments = jwt.split(separator: ".")
        guard segments.count == 3 else { return nil }

        var base64 = String(segments[1])
            .replacingOccurrences(of: "-", with: "+")
            .replacingOccurrences(of: "_", with: "/")

        let remainder = base64.count % 4
        if remainder > 0 {
            base64 += String(repeating: "=", count: 4 - remainder)
        }

        guard let data = Data(base64Encoded: base64) else { return nil }

        guard let json = try? JSONSerialization.jsonObject(with: data) as? [String: Any] else { return nil }

        guard let sub = json["sub"] as? String,
              let email = json["email"] as? String,
              let exp = json["exp"] as? TimeInterval else { return nil }

        let user = AuthUser(
            id: sub,
            email: email,
            name: json["name"] as? String,
            pictureURL: json["picture"] as? String
        )

        let expiresAt = Date(timeIntervalSince1970: exp)

        return (user: user, expiresAt: expiresAt)
    }
}
