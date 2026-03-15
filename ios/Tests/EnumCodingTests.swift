import Testing
@testable import Tradeguruelectrical

@Suite struct EnumCodingTests {
    @Test func contentBlockTypeRawValues() {
        #expect(ContentBlockType.stepList.rawValue == "step_list")
        #expect(ContentBlockType.partsList.rawValue == "parts_list")
        #expect(ContentBlockType.diagramRef.rawValue == "diagram_ref")
        #expect(ContentBlockType.toolCall.rawValue == "tool_call")
        #expect(ContentBlockType.text.rawValue == "text")
        #expect(ContentBlockType.table.rawValue == "table")
    }

    @Test func contentBlockTypeFromRawValue() {
        #expect(ContentBlockType(rawValue: "step_list") == .stepList)
        #expect(ContentBlockType(rawValue: "parts_list") == .partsList)
        #expect(ContentBlockType(rawValue: "diagram_ref") == .diagramRef)
        #expect(ContentBlockType(rawValue: "tool_call") == .toolCall)
        #expect(ContentBlockType(rawValue: "invalid") == nil)
    }

    @Test func messageRoleRoundTrip() {
        #expect(MessageRole(rawValue: "user") == .user)
        #expect(MessageRole(rawValue: "assistant") == .assistant)
        #expect(MessageRole.user.rawValue == "user")
    }

    @Test func attachmentTypeRoundTrip() {
        #expect(AttachmentType(rawValue: "image") == .image)
        #expect(AttachmentType(rawValue: "video") == .video)
        #expect(AttachmentType(rawValue: "document") == .document)
    }

    @Test func thinkingModeRoundTrip() {
        #expect(ThinkingMode(rawValue: "fault_finder") == .faultFinder)
        #expect(ThinkingMode(rawValue: "learn") == .learn)
        #expect(ThinkingMode(rawValue: "research") == .research)
    }

    @Test func contentBlockTypeCaseIterable() {
        #expect(ContentBlockType.allCases.count == 12)
    }
}
