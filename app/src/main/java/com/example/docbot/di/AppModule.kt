package com.example.docbot.di

import com.example.docbot.data.ObjectBox
import com.example.docbot.data.models.Conversation
import com.example.docbot.data.models.Document
import com.example.docbot.data.models.DocumentChunk
import com.example.docbot.data.models.Message
import com.example.docbot.data.repositories.ConversationRepository
import com.example.docbot.data.repositories.ConversationRepositoryImpl
import com.example.docbot.data.sources.ConversationLocalDataSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.objectbox.Box
import io.objectbox.kotlin.boxFor
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideConversationBox(): Box<Conversation> {
        return ObjectBox.store.boxFor(Conversation::class)
    }

    @Provides
    @Singleton
    fun provideMessageBox(): Box<Message> {
        return ObjectBox.store.boxFor(Message::class)
    }

    @Provides
    @Singleton
    fun provideDocumentBox(): Box<Document> {
        return ObjectBox.store.boxFor(Document::class)
    }

    @Provides
    @Singleton
    fun provideDocumentChunkBox(): Box<DocumentChunk> {
        return ObjectBox.store.boxFor(DocumentChunk::class)
    }

    @Provides
    @Singleton
    fun provideConversationLocalDatSource(
        conversationBox: Box<Conversation>,
        messageBox: Box<Message>,
        documentBox: Box<Document>,
        documentChunkBox: Box<DocumentChunk>
    ): ConversationLocalDataSource {
        return ConversationLocalDataSource(
            conversationBox,
            messageBox,
            documentBox,
            documentChunkBox
        )
    }

    @Provides
    @Singleton
    fun provideConversationRepository(
        conversationLocalDataSource: ConversationLocalDataSource
    ): ConversationRepository {
        return ConversationRepositoryImpl(conversationLocalDataSource)
    }
}