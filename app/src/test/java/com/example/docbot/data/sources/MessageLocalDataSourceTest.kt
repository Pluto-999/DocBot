package com.example.docbot.data.sources

import com.example.docbot.data.models.Conversation
import com.example.docbot.data.models.Message
import com.example.docbot.data.models.MessageType
import io.objectbox.Box
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

class MessageLocalDataSourceTest: ObjectBoxTest() {

    lateinit var localDataSource: MessageLocalDataSource

    lateinit var messageBox: Box<Message>
    lateinit var conversationBox: Box<Conversation>

    @Before
    fun setUp() {
        messageBox = store.boxFor(Message::class.java)
        conversationBox = store.boxFor(Conversation::class.java)

        localDataSource = MessageLocalDataSource(
            messageBox, conversationBox
        )
    }


    /****/

    @Test
    fun testInsertMessage() {
        val conversation = Conversation()
        val conversationId = conversationBox.put(conversation)

        localDataSource.insertMessage(
            conversationId,
            "Test",
            MessageType.PROMPT
        )

        // message has been created
        assertEquals(1, messageBox.count())

        val conversationMessages = conversation.messages.toList()
        val newMessage = conversationMessages[0]

        // message is associated to the correct conversation
        assertEquals(conversationId, newMessage.conversation.targetId)

        // conversation has the message
        val updatedConversation = conversationBox.get(conversationId)
        assertEquals(1, updatedConversation.messages.size)

        // new message has correct contents and type
        assertEquals(newMessage.contents, "Test")
        assertEquals(newMessage.messageType, MessageType.PROMPT)

        // new message and conversation have same timestamp value
        assertEquals(
            newMessage.timestamp.truncatedTo(ChronoUnit.SECONDS),
            conversation.latestMessage.truncatedTo(ChronoUnit.SECONDS)
        )
    }

    @Test
    fun testInsertMessageDoesNotProceedWithInvalidConversationId() {
        localDataSource.insertMessage(
            999,
            "Test",
            MessageType.PROMPT
        )

        assertEquals(0, messageBox.count())
    }


    /****/

    @Test
    fun testGetMessages() = runTest {
        val messageOne = Message(contents = "1", timestamp = LocalDateTime.now().minusDays(1))
        val messageTwo = Message(contents = "2", timestamp = LocalDateTime.now().minusDays(2))
        messageBox.put(messageOne, messageTwo)

        val conversation = Conversation()
        conversation.messages.add(messageOne)
        conversation.messages.add(messageTwo)
        conversationBox.put(conversation)

        val flow = localDataSource.getMessages(conversation.id)
        val resultList = flow.first()

        assertEquals(2, resultList.size)
        assertEquals(messageTwo.id, resultList[0].id)
        assertEquals(messageOne.id, resultList[1].id)
    }


    /****/

    @Test
    fun testGetRecentMessages() {
        val messageOne = Message(contents = "1", messageType = MessageType.PROMPT)
        val messageTwo = Message(contents = "2", messageType = MessageType.PROMPT)
        val messageThree = Message(contents = "3", messageType = MessageType.RESPONSE)
        messageBox.put(messageOne, messageTwo, messageThree)

        val conversation = Conversation()
        conversation.messages.add(messageOne)
        conversation.messages.add(messageTwo)
        conversation.messages.add(messageThree)
        conversationBox.put(conversation)

        val messagesList = localDataSource.getRecentMessages(conversation.id)

        assertEquals(2, messagesList.size)
        assertEquals(messageTwo.id, messagesList[0].id)
        assertEquals(messageThree.id, messagesList[1].id)
    }

}