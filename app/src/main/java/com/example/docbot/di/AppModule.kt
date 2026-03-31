package com.example.docbot.di

import android.content.Context
import androidx.work.WorkManager
import com.example.docbot.data.ObjectBox
import com.example.docbot.data.models.Conversation
import com.example.docbot.data.models.Document
import com.example.docbot.data.models.DocumentChunk
import com.example.docbot.data.models.Message
import com.example.docbot.data.conversation.ConversationRepository
import com.example.docbot.data.conversation.ConversationRepositoryImpl
import com.example.docbot.data.document.DocumentRepository
import com.example.docbot.data.document.DocumentRepositoryImpl
import com.example.docbot.data.message.MessageRepository
import com.example.docbot.data.message.MessageRepositoryImpl
import com.example.docbot.data.conversation.ConversationLocalDataSource
import com.example.docbot.data.document.DocumentChunkLocalDataSource
import com.example.docbot.data.document.DocumentLocalDataSource
import com.example.docbot.data.document.processing.DocumentProcessingScheduler
import com.example.docbot.data.document.processing.PdfTextExtractor
import com.example.docbot.data.document.processing.TextChunker
import com.example.docbot.data.document.processing.TextExtractor
import com.example.docbot.data.embedding.EmbeddingGenerator
import com.example.docbot.data.embedding.GemmaEmbeddingGenerator
import com.example.docbot.data.message.MessageLocalDataSource
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
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
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
    fun provideConversationRepository(
        conversationLocalDataSource: ConversationLocalDataSource,
        documentLocalDataSource: DocumentLocalDataSource,
        workManager: WorkManager
    ): ConversationRepository {
        return ConversationRepositoryImpl(
            conversationLocalDataSource,
            documentLocalDataSource,
            workManager
        )
    }

    @Provides
    @Singleton
    fun provideMessageRepository(
        messageLocalDataSource: MessageLocalDataSource,
        documentLocalDataSource: DocumentLocalDataSource,
        documentChunkLocalDataSource: DocumentChunkLocalDataSource,
        embeddingGenerator: EmbeddingGenerator,
        engine: Engine
    ): MessageRepository {
        return MessageRepositoryImpl(
            messageLocalDataSource,
            documentLocalDataSource,
            documentChunkLocalDataSource,
            embeddingGenerator,
            engine
        )
    }

    @Provides
    @Singleton
    fun provideDocumentRepository(
        conversationLocalDataSource: ConversationLocalDataSource,
        documentLocalDataSource: DocumentLocalDataSource,
        documentChunkLocalDataSource: DocumentChunkLocalDataSource,
        textChunker: TextChunker,
        textExtractor: TextExtractor,
        embeddingGenerator: EmbeddingGenerator,
        documentProcessingScheduler: DocumentProcessingScheduler
    ): DocumentRepository {
        return DocumentRepositoryImpl(
            conversationLocalDataSource,
            documentLocalDataSource,
            documentChunkLocalDataSource,
            textChunker,
            textExtractor,
            embeddingGenerator,
            documentProcessingScheduler
        )
    }

    @Provides
    @Singleton
    fun provideTextChunker(): TextChunker {
        return TextChunker()
    }

    @Provides
    @Singleton
    fun provideTextExtractor(
        @ApplicationContext applicationContext: Context
    ): TextExtractor {
        return PdfTextExtractor(applicationContext)
    }

    @Provides
    @Singleton
    fun provideEmbeddingGenerator(): EmbeddingGenerator {
        return GemmaEmbeddingGenerator()
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

    @Provides
    @Singleton
    fun provideWorkManager(
        @ApplicationContext applicationContext: Context
    ): WorkManager {
        return WorkManager.getInstance(applicationContext)
    }

    @Provides
    @Singleton
    fun provideDefaultDispatcher(): CoroutineDispatcher = Dispatchers.Default
}