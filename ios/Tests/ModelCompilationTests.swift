import Testing
@testable import Tradeguruelectrical

#if canImport(SwiftData)
import SwiftData
#endif

@Suite struct ModelCompilationTests {
    @Test func partsItemInit() {
        let item = PartsItem(name: "14/2 NM-B", spec: "AWG 14, 2-conductor", qty: 3)
        #expect(item.name == "14/2 NM-B")
        #expect(item.spec == "AWG 14, 2-conductor")
        #expect(item.qty == 3)
    }

    @Test func messageAttachmentInit() {
        let attachment = MessageAttachment(type: .image, fileName: "photo.jpg")
        #expect(attachment.type == .image)
        #expect(attachment.fileName == "photo.jpg")
        #expect(attachment.fileSize == nil)
        #expect(attachment.thumbnailData == nil)
    }

    @Test func contentBlockInit() {
        let block = ContentBlock(type: .text, content: "Hello")
        #expect(block.type == .text)
        #expect(block.content == "Hello")
        #expect(block.items == nil)
    }

    @Test func chatMessageInit() {
        let block = ContentBlock(type: .text, content: "Test")
        let message = ChatMessage(role: .user, blocks: [block], mode: .faultFinder)
        #expect(message.role == .user)
        #expect(message.blocks.count == 1)
        #expect(message.mode == .faultFinder)
    }

    @Test func conversationInit() {
        let convo = Conversation(title: "Test Chat", mode: .learn)
        #expect(convo.title == "Test Chat")
        #expect(convo.messages.isEmpty)
        #expect(convo.mode == .learn)
    }
}
