import Testing
@testable import Tradeguruelectrical

@Suite struct RelationshipTests {
    @Test func conversationContainsMessages() {
        let block = ContentBlock(type: .text, content: "Hello")
        let message = ChatMessage(role: .user, blocks: [block], mode: .faultFinder)
        let convo = Conversation(title: "Test", messages: [message], mode: .faultFinder)
        #expect(convo.messages.count == 1)
        #expect(convo.messages.first?.role == .user)
    }

    @Test func messageContainsBlocks() {
        let block1 = ContentBlock(type: .text, content: "First")
        let block2 = ContentBlock(type: .warning, content: "Danger")
        let message = ChatMessage(role: .assistant, blocks: [block1, block2], mode: .learn)
        #expect(message.blocks.count == 2)
        #expect(message.blocks[0].type == .text)
        #expect(message.blocks[1].type == .warning)
    }

    @Test func messageContainsAttachments() {
        let att = MessageAttachment(type: .image, fileName: "photo.jpg")
        let block = ContentBlock(type: .text, content: "See photo")
        let message = ChatMessage(role: .user, blocks: [block], mode: .faultFinder, attachments: [att])
        #expect(message.attachments?.count == 1)
        #expect(message.attachments?.first?.fileName == "photo.jpg")
    }

    @Test func contentBlockContainsPartsItems() {
        let item1 = PartsItem(name: "Wire", spec: "14 AWG", qty: 1)
        let item2 = PartsItem(name: "Breaker", spec: "20A", qty: 2)
        let block = ContentBlock(type: .partsList, items: [item1, item2])
        #expect(block.items?.count == 2)
        #expect(block.items?[0].name == "Wire")
        #expect(block.items?[1].qty == 2)
    }

    @Test func conversationAppendMessage() {
        let convo = Conversation(title: "Test", mode: .research)
        let block = ContentBlock(type: .text, content: "New message")
        let message = ChatMessage(role: .user, blocks: [block], mode: .research)
        convo.messages.append(message)
        #expect(convo.messages.count == 1)
    }
}
