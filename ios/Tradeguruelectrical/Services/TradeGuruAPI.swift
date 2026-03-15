import Foundation

enum TradeGuruAPIError: Error {
    case noDeviceId
    case httpError(Int, String)
    case decodingError(String)
    case streamError(StreamErrorPayload)
    case networkError(Error)
}

enum TradeGuruAPI {

    // MARK: - Device Registration

    static func registerDevice() async throws -> String {
        var req = APIConfig.request("device/register", deviceId: "pending")
        let body: [String: String] = [
            "platform": APIConfig.platform,
            "app_version": APIConfig.appVersion,
            "locale": Locale.current.identifier,
            "timezone": TimeZone.current.identifier,
        ]
        req.httpBody = try JSONEncoder().encode(body)
        req.setValue("application/json", forHTTPHeaderField: "Content-Type")
        req.allHTTPHeaderFields?.removeValue(forKey: "X-Device-ID")

        let (data, response) = try await URLSession.shared.data(for: req)
        try validateResponse(response, data: data)

        let result = try JSONDecoder().decode(DeviceRegisterResponse.self, from: data)
        return result.deviceId
    }

    // MARK: - Chat (SSE Streaming)

    static func chat(
        messages: [[String: String]],
        mode: ThinkingMode,
        deviceId: String,
        jwt: String? = nil
    ) -> AsyncStream<StreamResult> {
        AsyncStream { continuation in
            Task {
                do {
                    var req = APIConfig.request("chat", deviceId: deviceId, jwt: jwt)
                    let body: [String: Any] = [
                        "messages": messages,
                        "mode": mode.rawValue,
                        "device_id": deviceId,
                        "platform": APIConfig.platform,
                    ]
                    req.httpBody = try JSONSerialization.data(withJSONObject: body)

                    let (bytes, response) = try await URLSession.shared.bytes(for: req)

                    guard let http = response as? HTTPURLResponse else {
                        continuation.yield(.error(StreamErrorPayload(code: "NO_RESPONSE", message: "No HTTP response", partial: nil)))
                        continuation.finish()
                        return
                    }

                    guard http.statusCode == 200 else {
                        let errorBody = await collectErrorBody(bytes)
                        continuation.yield(.error(StreamErrorPayload(
                            code: "HTTP_\(http.statusCode)",
                            message: errorBody,
                            partial: nil
                        )))
                        continuation.finish()
                        return
                    }

                    for await result in StreamParser.parse(lines: bytes.lines) {
                        continuation.yield(result)
                    }

                    continuation.finish()
                } catch {
                    continuation.yield(.error(StreamErrorPayload(
                        code: "NETWORK_ERROR",
                        message: error.localizedDescription,
                        partial: nil
                    )))
                    continuation.finish()
                }
            }
        }
    }

    // MARK: - Vision (Photo Analysis)

    static func chatVision(
        message: String,
        image: String,
        mode: ThinkingMode,
        deviceId: String,
        jwt: String? = nil
    ) -> AsyncStream<StreamResult> {
        AsyncStream { continuation in
            Task {
                do {
                    var req = APIConfig.request("chat/vision", deviceId: deviceId, jwt: jwt)
                    let body: [String: Any] = [
                        "message": message,
                        "image": image,
                        "mode": mode.rawValue,
                        "device_id": deviceId,
                        "platform": APIConfig.platform,
                    ]
                    req.httpBody = try JSONSerialization.data(withJSONObject: body)

                    let (bytes, response) = try await URLSession.shared.bytes(for: req)

                    guard let http = response as? HTTPURLResponse, http.statusCode == 200 else {
                        continuation.yield(.error(StreamErrorPayload(code: "HTTP_ERROR", message: "Vision request failed", partial: nil)))
                        continuation.finish()
                        return
                    }

                    for await result in StreamParser.parse(lines: bytes.lines) {
                        continuation.yield(result)
                    }

                    continuation.finish()
                } catch {
                    continuation.yield(.error(StreamErrorPayload(code: "NETWORK_ERROR", message: error.localizedDescription, partial: nil)))
                    continuation.finish()
                }
            }
        }
    }

    // MARK: - Audio

    static func transcribe(audioData: Data, mimeType: String, deviceId: String, jwt: String? = nil) async throws -> String {
        let boundary = "TradeGuru-\(UUID().uuidString)"
        var req = APIConfig.request("audio/transcribe", deviceId: deviceId, jwt: jwt)
        req.setValue("multipart/form-data; boundary=\(boundary)", forHTTPHeaderField: "Content-Type")

        var body = Data()
        let ext = mimeType.contains("wav") ? "wav" : "m4a"
        body.append("--\(boundary)\r\nContent-Disposition: form-data; name=\"audio\"; filename=\"recording.\(ext)\"\r\nContent-Type: \(mimeType)\r\n\r\n".data(using: .utf8) ?? Data())
        body.append(audioData)
        body.append("\r\n--\(boundary)--\r\n".data(using: .utf8) ?? Data())
        req.httpBody = body

        let (data, response) = try await URLSession.shared.data(for: req)
        try validateResponse(response, data: data)

        let result = try JSONDecoder().decode(TranscribeResponse.self, from: data)
        return result.text
    }

    static func speak(text: String, voice: String = "nova", deviceId: String, jwt: String? = nil) async throws -> Data {
        var req = APIConfig.request("audio/speech", deviceId: deviceId, jwt: jwt)
        let body = ["text": text, "voice": voice]
        req.httpBody = try JSONEncoder().encode(body)

        let (data, response) = try await URLSession.shared.data(for: req)
        try validateResponse(response, data: data)
        return data
    }

    // MARK: - File Upload

    static func uploadFile(fileData: Data, fileName: String, mimeType: String, deviceId: String, jwt: String? = nil) async throws -> FileUploadResponse {
        let boundary = "TradeGuru-\(UUID().uuidString)"
        var req = APIConfig.request("files/upload", deviceId: deviceId, jwt: jwt)
        req.setValue("multipart/form-data; boundary=\(boundary)", forHTTPHeaderField: "Content-Type")

        var body = Data()
        body.append("--\(boundary)\r\nContent-Disposition: form-data; name=\"file\"; filename=\"\(fileName)\"\r\nContent-Type: \(mimeType)\r\n\r\n".data(using: .utf8) ?? Data())
        body.append(fileData)
        body.append("\r\n--\(boundary)--\r\n".data(using: .utf8) ?? Data())
        req.httpBody = body

        let (data, response) = try await URLSession.shared.data(for: req)
        try validateResponse(response, data: data)
        return try JSONDecoder().decode(FileUploadResponse.self, from: data)
    }

    // MARK: - Rating & Feedback

    static func rate(responseId: String, stars: Int, mode: ThinkingMode, comment: String? = nil, deviceId: String, jwt: String? = nil) async throws {
        var req = APIConfig.request("rating", deviceId: deviceId, jwt: jwt)
        var body: [String: Any] = [
            "response_id": responseId,
            "stars": stars,
            "mode": mode.rawValue,
        ]
        if let comment { body["comment"] = comment }
        req.httpBody = try JSONSerialization.data(withJSONObject: body)

        let (data, response) = try await URLSession.shared.data(for: req)
        try validateResponse(response, data: data)
    }

    static func feedback(responseId: String, reason: String, detail: String? = nil, mode: ThinkingMode, deviceId: String, jwt: String? = nil) async throws {
        var req = APIConfig.request("feedback", deviceId: deviceId, jwt: jwt)
        var body: [String: Any] = [
            "response_id": responseId,
            "reason": reason,
            "mode": mode.rawValue,
        ]
        if let detail { body["detail"] = detail }
        req.httpBody = try JSONSerialization.data(withJSONObject: body)

        let (data, response) = try await URLSession.shared.data(for: req)
        try validateResponse(response, data: data)
    }

    // MARK: - Helpers

    private static func validateResponse(_ response: URLResponse, data: Data) throws {
        guard let http = response as? HTTPURLResponse else {
            throw TradeGuruAPIError.httpError(0, "No HTTP response")
        }
        guard (200...299).contains(http.statusCode) else {
            let body = String(data: data, encoding: .utf8) ?? "Unknown error"
            throw TradeGuruAPIError.httpError(http.statusCode, body)
        }
    }

    private static func collectErrorBody(_ bytes: URLSession.AsyncBytes) async -> String {
        var collected = ""
        for await line in bytes.lines {
            collected += line
            if collected.count > 500 { break }
        }
        return collected.isEmpty ? "Unknown error" : collected
    }
}

// MARK: - Response Types

nonisolated struct DeviceRegisterResponse: Codable {
    let deviceId: String
    enum CodingKeys: String, CodingKey {
        case deviceId = "device_id"
    }
}

nonisolated struct TranscribeResponse: Codable {
    let text: String
}

nonisolated struct FileUploadResponse: Codable {
    let id: String
    let filename: String
}
