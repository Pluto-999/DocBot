package com.example.docbot.di

import android.content.Context
import com.example.docbot.data.ObjectBox
import com.example.docbot.data.models.Conversation
import com.example.docbot.data.models.Document
import com.example.docbot.data.models.DocumentChunk
import com.example.docbot.data.models.Message
import com.example.docbot.data.repositories.ConversationRepository
import com.example.docbot.data.repositories.ConversationRepositoryImpl
import com.example.docbot.data.repositories.DocumentRepository
import com.example.docbot.data.repositories.DocumentRepositoryImpl
import com.example.docbot.data.repositories.MessageRepository
import com.example.docbot.data.repositories.MessageRepositoryImpl
import com.example.docbot.data.sources.ConversationLocalDataSource
import com.example.docbot.data.sources.DocumentLocalDataSource
import com.example.docbot.data.sources.MessageLocalDataSource
import com.google.ai.edge.litertlm.Backend
import com.google.ai.edge.litertlm.Engine
import com.google.ai.edge.litertlm.EngineConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
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

    @Provides
    @Singleton
    fun provideMessageRepository(
        messageLocalDataSource: MessageLocalDataSource,
        engine: Engine
    ): MessageRepository {
        return MessageRepositoryImpl(messageLocalDataSource, engine)
    }

    @Provides
    @Singleton
    fun provideDocumentRepository(
        documentLocalDataSource: DocumentLocalDataSource,
        @ApplicationContext applicationContext: Context
    ): DocumentRepository {
        return DocumentRepositoryImpl(documentLocalDataSource, applicationContext)
    }

    @Provides
    @Singleton
    fun provideEngine(): Engine {
        return Engine(
            EngineConfig(
                modelPath = "/data/local/tmp/slm/gemma-3n-E2B-it-int4.litertlm",
                backend = Backend.CPU
            )
        )
    }
}