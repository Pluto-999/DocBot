package com.example.docbot.data.sources

import com.example.docbot.data.conversation.ConversationLocalDataSource
import com.example.docbot.data.models.Conversation
import com.example.docbot.data.models.Document
import com.example.docbot.data.models.DocumentChunk
import com.example.docbot.data.models.Message
import com.example.docbot.ui.screens.home.ConversationFilter
import com.example.docbot.ui.screens.home.ConversationOrder
import io.objectbox.Box
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime

class ConversationLocalDataSourceTest: ObjectBoxTest() {

    lateinit var localDataSource: ConversationLocalDataSource

    lateinit var conversationBox: Box<Conversation>
    lateinit var messageBox: Box<Message>
    lateinit var documentBox: Box<Document>
    lateinit var documentChunkBox: Box<DocumentChunk>

    @Before
    fun setUp() {
        conversationBox = store.boxFor(Conversation::class.java)
        messageBox = store.boxFor(Message::class.java)
        documentBox = store.boxFor(Document::class.java)
        documentChunkBox = store.boxFor(DocumentChunk::class.java)

        localDataSource = ConversationLocalDataSource(
            conversationBox,
            messageBox,
            documentBox,
            documentChunkBox
        )
    }


    /****/

    @Test
    fun testInsertConversation() {
        val conversationId = localDataSource.insertConversation()

        val conversation = conversationBox.get(conversationId)

        assertNotNull(conversation)
        assertEquals(conversation.id, conversationId)
        assertEquals(1, conversationBox.count())
    }

    @Test
    fun testMultipleInserts() {
        localDataSource.insertConversation()
        localDataSource.insertConversation()
        localDataSource.insertConversation()

        assertEquals(3, conversationBox.count())
    }


    /****/

    private fun setupGetConversations(): List<Long> {
        val conversationIdOne = localDataSource.insertConversation()
        val conversationOne = conversationBox.get(conversationIdOne)
        conversationOne.latestMessage = LocalDateTime.now().minusDays(2)
        conversationOne.title = "A"
        conversationOne.favourite = true
        conversationBox.put(conversationOne)

        val conversationIdTwo = localDataSource.insertConversation()
        val conversationTwo = conversationBox.get(conversationIdTwo)
        conversationTwo.latestMessage = LocalDateTime.now().minusDays(1)
        conversationTwo.title = "B"
        conversationBox.put(conversationTwo)

        val conversationIdThree = localDataSource.insertConversation()
        val conversationThree = conversationBox.get(conversationIdThree)
        conversationThree.latestMessage = LocalDateTime.now().minusDays(8)
        conversationThree.title = "C"
        conversationBox.put(conversationThree)

        return listOf(conversationIdOne, conversationIdTwo, conversationIdThree)
    }

    @Test
    fun testGetConversationsInDefaultOrderAndNoFilter() = runTest {
        val conversations = setupGetConversations()
        val conversationIdOne = conversations[0]
        val conversationIdTwo = conversations[1]
        val conversationIdThree = conversations[2]

        val flow = localDataSource.getConversations(
            ConversationOrder.DATE_DESC,
            ConversationFilter.NONE,
            ""
        )

        val resultList = flow.first()

        assertEquals(3, resultList.size)
        assertEquals(conversationIdTwo, resultList[0].id)
        assertEquals(conversationIdOne, resultList[1].id)
        assertEquals(conversationIdThree, resultList[2].id)
    }

    @Test
    fun testGetConversationsInDateAscendingOrderAndNoFilter() = runTest {
        val conversations = setupGetConversations()
        val conversationIdOne = conversations[0]
        val conversationIdTwo = conversations[1]
        val conversationIdThree = conversations[2]

        val flow = localDataSource.getConversations(
            ConversationOrder.DATE_ASC,
            ConversationFilter.NONE,
            ""
        )

        val resultList = flow.first()

        assertEquals(3, resultList.size)
        assertEquals(conversationIdThree, resultList[0].id)
        assertEquals(conversationIdOne, resultList[1].id)
        assertEquals(conversationIdTwo, resultList[2].id)
    }

    @Test
    fun testGetConversationsInTitleDescendingOrderAndNoFilter() = runTest {
        val conversations = setupGetConversations()
        val conversationIdOne = conversations[0]
        val conversationIdTwo = conversations[1]
        val conversationIdThree = conversations[2]

        val flow = localDataSource.getConversations(
            ConversationOrder.TITLE_DESC,
            ConversationFilter.NONE,
            ""
        )

        val resultList = flow.first()

        assertEquals(3, resultList.size)
        assertEquals(conversationIdThree, resultList[0].id)
        assertEquals(conversationIdTwo, resultList[1].id)
        assertEquals(conversationIdOne, resultList[2].id)
    }

    @Test
    fun testGetConversationsInTitleAscendingOrderAndNoFilter() = runTest {
        val conversations = setupGetConversations()
        val conversationIdOne = conversations[0]
        val conversationIdTwo = conversations[1]
        val conversationIdThree = conversations[2]

        val flow = localDataSource.getConversations(
            ConversationOrder.TITLE_ASC,
            ConversationFilter.NONE,
            ""
        )

        val resultList = flow.first()

        assertEquals(3, resultList.size)
        assertEquals(conversationIdOne, resultList[0].id)
        assertEquals(conversationIdTwo, resultList[1].id)
        assertEquals(conversationIdThree, resultList[2].id)
    }

    @Test
    fun testGetConversationsInDateAscendingOrderAndFavouriteFilter() = runTest {
        val conversations = setupGetConversations()
        val conversationIdOne = conversations[0]

        val flow = localDataSource.getConversations(
            ConversationOrder.DATE_ASC,
            ConversationFilter.FAVOURITES,
            ""
        )

        val resultList = flow.first()

        assertEquals(1, resultList.size)
        assertEquals(conversationIdOne, resultList[0].id)
    }

    @Test
    fun testGetConversationsInDateDescendingOrderAndDeleteSoonFilter() = runTest {
        val conversations = setupGetConversations()
        val conversationIdThree = conversations[2]

        val flow = localDataSource.getConversations(
            ConversationOrder.DATE_DESC,
            ConversationFilter.DELETE_SOON,
            ""
        )

        val resultList = flow.first()

        assertEquals(1, resultList.size)
        assertEquals(conversationIdThree, resultList[0].id)
    }

    @Test
    fun testGetConversationsInDateDescendingOrderAndSearchQuery() = runTest {
        val conversations = setupGetConversations()
        val conversationIdTwo = conversations[1]

        val flow = localDataSource.getConversations(
            ConversationOrder.DATE_DESC,
            ConversationFilter.NONE,
            "B"
        )

        val resultList = flow.first()

        assertEquals(1, resultList.size)
        assertEquals(conversationIdTwo, resultList[0].id)
    }


    /****/

    @Test
    fun testGetConversationTitleFromId() {
        val conversationId = localDataSource.insertConversation()
        val conversation = conversationBox.get(conversationId)

        conversation.title = "Test Title"
        conversationBox.put(conversation)

        val getTitle = localDataSource.getConversationTitleFromId(conversationId)
        assertEquals("Test Title", getTitle)
    }

    @Test
    fun testGetConversationTitleFromIdReturnsNullWithInvalidConversationId() {
        val getTitle = localDataSource.getConversationTitleFromId(999)
        assertNull(getTitle)
    }


    /****/

    @Test
    fun testGetDocumentTitlesFromId() = runTest {
        val conversationId = localDataSource.insertConversation()

        val docOne = Document(name = "A")
        val docTwo = Document(name = "B")
        documentBox.put(docOne, docTwo)

        val conversation = conversationBox.get(conversationId)
        conversation.documents.add(docOne)
        conversation.documents.add(docTwo)
        conversationBox.put(conversation)

        val flow = localDataSource.getDocumentTitlesFromId(conversationId)

        val resultsList = flow.first()

        assertEquals(2, resultsList.size)
        assertEquals(docOne.name, resultsList[0])
        assertEquals(docTwo.name, resultsList[1])
    }

    /****/

    @Test
    fun testGetDocumentCountWithZeroDocuments() {
        val conversationId = localDataSource.insertConversation()
        val documentCount = localDataSource.getDocumentCount(conversationId)
        assertEquals(0, documentCount)
    }

    @Test
    fun testGetDocumentCountWithTwoDocuments() {
        val conversationId = localDataSource.insertConversation()

        val docOne = Document()
        val docTwo = Document()
        documentBox.put(docOne, docTwo)

        val conversation = conversationBox.get(conversationId)
        conversation.documents.add(docOne)
        conversation.documents.add(docTwo)
        conversationBox.put(conversation)

        val documentCount = localDataSource.getDocumentCount(conversationId)
        assertEquals(2, documentCount)
    }

    @Test
    fun testGetDocumentCountReturnsMinus1WithInvalidConversationId() {
        val documentCount = localDataSource.getDocumentCount(999)
        assertEquals(-1, documentCount)
    }


    /****/

    @Test
    fun testUpdateConversationTitle() {
        val conversationId = localDataSource.insertConversation()

        localDataSource.updateConversationTitle(conversationId, "Test Title")
        val updatedConversation = conversationBox.get(conversationId)
        assertEquals("Test Title", updatedConversation.title)
    }

    @Test
    fun testUpdateConversationTitlePerformsNoOperationWithInvalidConversationId() {
        localDataSource.updateConversationTitle(999, "New Title")
        assertEquals(0, conversationBox.count())
    }

    /****/

    @Test
    fun testAddConversationToFavourites() {
        val conversationId = localDataSource.insertConversation()

        localDataSource.addConversationToFavourites(conversationId)
        val updatedConversation = conversationBox.get(conversationId)
        assertTrue(updatedConversation.favourite)
    }

    @Test(expected = IllegalStateException::class)
    fun testAddConversationToFavouritesThrowsExceptionWhenAtMaxFavourites() {
        for (i in 1..10) {
            val conversationId = localDataSource.insertConversation()
            localDataSource.addConversationToFavourites(conversationId)
        }

        val conversationId = localDataSource.insertConversation()
        localDataSource.addConversationToFavourites(conversationId)
    }

    @Test
    fun testAddConversationToFavouritesPerformsNoOperationWithInvalidConversationId() {
        localDataSource.addConversationToFavourites(999L)
        assertEquals(0, conversationBox.count())
    }


    /****/

    @Test
    fun testRemoveConversationFromFavourites() {
        val conversationId = localDataSource.insertConversation()

        localDataSource.addConversationToFavourites(conversationId)
        val updatedConversation = conversationBox.get(conversationId)
        assertTrue(updatedConversation.favourite)

        localDataSource.removeConversationFromFavourites(conversationId)
        val updatedConversationTwo = conversationBox.get(conversationId)
        assertFalse(updatedConversationTwo.favourite)
    }

    @Test
    fun testRemoveConversationFromFavouritesPerformsNoOperationWithInvalidConversationId() {
        localDataSource.removeConversationFromFavourites(999L)
        assertEquals(0, conversationBox.count())
    }


    /****/

    @Test
    fun testNoOldConversationsAreReturnedWhenNoneExist() {
        for (i in 1..5) {
            localDataSource.insertConversation()
        }

        val oldConversations = localDataSource.getOldConversationsId()
        assertEquals(0, oldConversations.size)
    }

    @Test
    fun testNoOldConversationsAreReturnedWhenSomeExistButAreFavouriteConversations() {
        for (i in 1..5) {
            val elevenDaysAgo = LocalDateTime.now().minusDays(11)

            val conversationId = localDataSource.insertConversation()
            val conversation = conversationBox.get(conversationId)
            conversation.latestMessage = elevenDaysAgo
            conversationBox.put(conversation)

            localDataSource.addConversationToFavourites(conversationId)
        }

        val oldConversations = localDataSource.getOldConversationsId()
        assertEquals(0, oldConversations.size)
    }

    @Test
    fun testOldConversationsAreReturnedWhenSomeExist() {
        for (i in 1..5) {
            val elevenDaysAgo = LocalDateTime.now().minusDays(11)

            val conversationId = localDataSource.insertConversation()
            val conversation = conversationBox.get(conversationId)
            conversation.latestMessage = elevenDaysAgo
            conversationBox.put(conversation)
        }

        val oldConversations = localDataSource.getOldConversationsId()
        assertEquals(5, oldConversations.size)
    }


    /****/

    @Test
    fun testDeleteConversationWithNoAssociatedMessagesOrDocuments() {
        val conversationId = localDataSource.insertConversation()

        localDataSource.deleteConversation(conversationId)

        val conversation = conversationBox.get(conversationId)
        assertNull(conversation)
        assertEquals(0, conversationBox.count())
    }

    @Test
    fun testDeleteConversationWithAssociatedMessages() {
        val conversationId = localDataSource.insertConversation()

        val messageOne = Message()
        val messageTwo = Message()
        messageBox.put(messageOne, messageTwo)

        val conversation = conversationBox.get(conversationId)
        conversation.messages.add(messageOne)
        conversation.messages.add(messageTwo)
        conversationBox.put(conversation)

        assertEquals(2, messageBox.count())
        assertEquals(1, conversationBox.count())

        localDataSource.deleteConversation(conversationId)

        assertNull(conversationBox.get(conversationId))
        assertEquals(0, messageBox.count())
        assertEquals(0, conversationBox.count())
    }

    @Test
    fun testDeleteConversationWithLastAssociatedDocument() {
        val conversationId = localDataSource.insertConversation()

        // create document chunks and document
        val documentChunkOne = DocumentChunk()
        val documentChunkTwo = DocumentChunk()
        documentChunkBox.put(documentChunkOne, documentChunkTwo)

        val document = Document()
        document.documentChunks.add(documentChunkOne)
        document.documentChunks.add(documentChunkTwo)
        documentBox.put(document)

        // link document to conversation
        val conversation = conversationBox.get(conversationId)
        conversation.documents.add(document)
        conversationBox.put(conversation)

        // (verify reverse direction)
        val savedDocument = documentBox.get(document.id)
        assertEquals(1, savedDocument.conversations.size)

        assertEquals(2, documentChunkBox.count())
        assertEquals(1, documentBox.count())
        assertEquals(1, conversationBox.count())

        localDataSource.deleteConversation(conversationId)

        assertNull(conversationBox.get(conversationId))
        assertEquals(0, documentChunkBox.count())
        assertEquals(0, documentBox.count())
        assertEquals(0, conversationBox.count())
    }

    @Test
    fun testDeleteConversationWithNotLastAssociatedDocument() {
        val firstConversationId = localDataSource.insertConversation()
        val secondConversationId = localDataSource.insertConversation()

        // create document chunks and document
        val documentChunkOne = DocumentChunk()
        val documentChunkTwo = DocumentChunk()
        documentChunkBox.put(documentChunkOne, documentChunkTwo)

        val document = Document()
        document.documentChunks.add(documentChunkOne)
        document.documentChunks.add(documentChunkTwo)
        documentBox.put(document)

        // link document to conversations
        val conversationOne = conversationBox.get(firstConversationId)
        conversationOne.documents.add(document)
        conversationBox.put(conversationOne)
        val conversationTwo = conversationBox.get(secondConversationId)
        conversationTwo.documents.add(document)
        conversationBox.put(conversationTwo)

        assertEquals(2, documentChunkBox.count())
        assertEquals(1, documentBox.count())
        assertEquals(2, conversationBox.count())

        localDataSource.deleteConversation(firstConversationId)

        assertNull(conversationBox.get(firstConversationId))
        assertEquals(2, documentChunkBox.count())
        assertEquals(1, documentBox.count())
        assertEquals(1, conversationBox.count())
    }

    @Test
    fun testDeleteConversationPerformsNoOperationWithInvalidConversationId() {
        localDataSource.deleteConversation(999)
        assertEquals(0, conversationBox.count())
    }


    /****/

    @Test
    fun testGetDocumentsReturnsEmptyListWhenNoDocumentsAssociatedToConversation() {
        val conversationId = localDataSource.insertConversation()
        val documents = localDataSource.getDocuments(conversationId)
        assertEquals(0, documents.size)
    }

    @Test
    fun testGetDocumentsReturnsAllDocumentsAssociatedToConversation() {
        val conversationId = localDataSource.insertConversation()

        val docOne = Document()
        val docTwo = Document()
        documentBox.put(docOne, docTwo)

        val conversation = conversationBox.get(conversationId)
        conversation.documents.add(docOne)
        conversation.documents.add(docTwo)
        conversationBox.put(conversation)

        val documents = localDataSource.getDocuments(conversationId)
        assertEquals(2, documents.size)
    }

    @Test
    fun testGetDocumentsReturnsEmptyListWithInvalidConversationId() {
        val result = localDataSource.getDocuments(999)
        assertTrue(result.isEmpty())
    }
}