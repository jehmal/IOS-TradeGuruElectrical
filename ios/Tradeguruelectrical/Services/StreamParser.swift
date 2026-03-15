import Foundation

nonisolated struct SSEEvent: Sendable {
    let event: String
    let data: String
}

nonisolated struct StreamDonePayload: Codable, Sendable {
    let responseId: String
    let usage: TokenUsage?
    let cached: Bool?
    let category: String?

    enum CodingKeys: String, CodingKey {
        case responseId = "response_id"
        case usage
        case cached
        case category
    }
}

nonisolated struct TokenUsage: Codable, Sendable {
    let inputTokens: Int
    let outputTokens: Int

    enum CodingKeys: String, CodingKey {
        case inputTokens = "input_tokens"
        case outputTokens = "output_tokens"
    }
}

nonisolated struct StreamErrorPayload: Codable, Sendable {
    let code: String
    let message: String
    let partial: Bool?
}

nonisolated struct StatusPayload: Codable, Sendable {
    let stage: String
    let detail: String?
}

enum StreamParser {

    static func parse(lines: AsyncLineSequence<URLSession.AsyncBytes>) -> AsyncStream<StreamResult> {
        AsyncStream { continuation in
            Task {
                var currentEvent = ""
                var currentData = ""

                for try await line in lines {
                    if line.hasPrefix("event: ") {
                        currentEvent = String(line.dropFirst(7))
                    } else if line.hasPrefix("data: ") {
                        currentData = String(line.dropFirst(6))
                    } else if line.isEmpty, !currentEvent.isEmpty {
                        let result = processEvent(event: currentEvent, data: currentData)
                        if let result {
                            continuation.yield(result)
                        }
                        currentEvent = ""
                        currentData = ""
                    }
                }

                continuation.finish()
            }
        }
    }

    private static func processEvent(event: String, data: String) -> StreamResult? {
        guard let jsonData = data.data(using: .utf8) else { return nil }
        let decoder = JSONDecoder()

        switch event {
        case "block":
            guard let block = try? decoder.decode(ContentBlock.self, from: jsonData) else { return nil }
            return .block(block)

        case "status":
            guard let payload = try? decoder.decode(StatusPayload.self, from: jsonData) else { return nil }
            return .status(payload)

        case "done":
            guard let payload = try? decoder.decode(StreamDonePayload.self, from: jsonData) else { return nil }
            return .done(payload)

        case "error":
            guard let payload = try? decoder.decode(StreamErrorPayload.self, from: jsonData) else { return nil }
            return .error(payload)

        default:
            return nil
        }
    }
}

nonisolated enum StreamResult: @unchecked Sendable {
    case block(ContentBlock)
    case status(StatusPayload)
    case done(StreamDonePayload)
    case error(StreamErrorPayload)
}
