package com.example.docbot.data.sources

import com.example.docbot.data.models.Conversation
import com.example.docbot.data.models.Document
import com.example.docbot.data.models.ProcessingStatus
import io.objectbox.Box
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class DocumentLocalDataSourceTest: ObjectBoxTest() {

    lateinit var localDataSource: DocumentLocalDataSource

    lateinit var documentBox: Box<Document>
    lateinit var conversationBox: Box<Conversation>

    @Before
    fun setUp() {
        documentBox = store.boxFor(Document::class.java)
        conversationBox = store.boxFor(Conversation::class.java)

        localDataSource = DocumentLocalDataSource(
            documentBox,
            conversationBox
        )
    }


    /****/

    @Test
    fun testGetDocumentIds() {
        val docOne = Document()
        val docTwo = Document()
        documentBox.put(docOne, docTwo)

        val conversation = Conversation()
        conversation.documents.add(docOne)
        conversation.documents.add(docTwo)
        conversationBox.put(conversation)

        val idList = localDataSource.getDocumentIds(conversation.id)
        assertEquals(2, idList.size)
        assertEquals(docOne.id, idList[0])
        assertEquals(docTwo.id, idList[1])
    }


    /****/

    @Test
    fun testFindDocumentHashReturnsTrueWhenDocumentHashExists() {
        val doc = Document(contentHash = "abc")
        documentBox.put(doc)

        val findHash = localDataSource.findDocumentHash("abc")
        assertTrue(findHash)
    }

    @Test
    fun testFindDocumentHashReturnsFalseWhenDocumentHashDoesNotExist() {
        val doc = Document(contentHash = "abc")
        documentBox.put(doc)

        val findHash = localDataSource.findDocumentHash("123")
        assertFalse(findHash)
    }


    /****/

    @Test
    fun testInsertNewDocumentReturnsMinus1WhenConversationIdDoesNotExist() {
        val insertResult = localDataSource.insertNewDocument(
            "A",
            "B",
            ProcessingStatus.PROCESSING,
            999
        )
        assertEquals(-1, insertResult)
    }

    @Test
    fun testInsertNewDocumentReturnsDocumentIdWhenConversationIdExists() {
        val conversation = Conversation()
        conversationBox.put(conversation)

        val insertResult = localDataSource.insertNewDocument(
            "A",
            "B",
            ProcessingStatus.PROCESSING,
            1
        )
        val conversationDocuments = conversationBox.get(conversation.id).documents.toList()
        val insertedDocument = documentBox.get(insertResult)

        assertEquals(1, insertResult)
        assertEquals(insertedDocument.conversations.toList()[0].id, insertResult)
        assertEquals(conversationDocuments[0].id, insertedDocument.id)
    }


    /****/

    @Test
    fun testUpdateProcessingStatusWhenDocumentIdExists() {
        val document = Document(processingStatus = ProcessingStatus.COMPLETED)
        documentBox.put(document)

        localDataSource.updateProcessingStatus(document.id, ProcessingStatus.PROCESSING)

        val updatedDocument = documentBox.get(document.id)

        assertEquals(updatedDocument.processingStatus, ProcessingStatus.PROCESSING)
    }

    @Test
    fun testUpdateProcessingStatusWhenDocumentIdDoesNotExist() {
        localDataSource.updateProcessingStatus(999, ProcessingStatus.PROCESSING)
        assertEquals(0, documentBox.count())
    }


    /****/

    @Test
    fun testGetProcessingFlow() = runTest {
        val docOne = Document(processingStatus = ProcessingStatus.FAILED)
        val docTwo = Document(processingStatus = ProcessingStatus.PROCESSING)
        documentBox.put(docOne, docTwo)

        val conversation = Conversation()
        conversation.documents.add(docOne)
        conversation.documents.add(docTwo)
        conversationBox.put(conversation)

        val flow = localDataSource.getProcessingFlow(conversation.id)
        val resultList = flow.first()

        assertEquals(2, resultList.size)
        assertEquals(docOne.processingStatus, resultList[0])
        assertEquals(docTwo.processingStatus, resultList[1])
    }


    /****/

    @Test
    fun testGetConversationCountFromDocuments() {
        val conversationOne = Conversation()
        val conversationTwo = Conversation()
        conversationBox.put(conversationOne, conversationTwo)

        val document = Document()
        document.conversations.add(conversationOne)
        document.conversations.add(conversationTwo)
        documentBox.put(document)

        assertEquals(2, localDataSource.getConversationCountForDocument(document))
    }


    /****/

    @Test
    fun testLinkDocumentToConversationWhenDocumentHashDoesNotExist() {
        localDataSource.linkDocumentToConversation(999, "ABC")
        assertEquals(0, documentBox.count())
    }

    @Test
    fun testLinkDocumentToConversationWhenConversationDoesNotExist() {
        val document = Document(contentHash = "ABC")
        documentBox.put(document)
        localDataSource.linkDocumentToConversation(999, "ABC")
        assertEquals(0, conversationBox.count())
    }

    @Test
    fun testLinkDocumentToConversation() {
        val conversation = Conversation()
        conversationBox.put(conversation)

        val document = Document(contentHash = "ABC", processingStatus = ProcessingStatus.PROCESSING)
        documentBox.put(document)

        localDataSource.linkDocumentToConversation(conversation.id, "ABC")

        assertEquals(1, document.conversations.size)
        assertEquals(1, document.conversations.toList()[0].id)
    }

    @Test
    fun testLinkDocumentToConversationWhenDocumentNotProcessing() {
        val conversation = Conversation()
        conversationBox.put(conversation)

        val document = Document(contentHash = "ABC")
        documentBox.put(document)

        localDataSource.linkDocumentToConversation(conversation.id, "ABC")

        assertEquals(1, document.conversations.size)
        assertEquals(1, document.conversations.toList()[0].id)
    }
}