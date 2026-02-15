package demo.nexa.clinical_transcription_demo.data.repository

import android.content.Context
import demo.nexa.clinical_transcription_demo.data.local.AppDatabase
import demo.nexa.clinical_transcription_demo.data.mapper.toEntity
import demo.nexa.clinical_transcription_demo.data.mapper.toDomain
import demo.nexa.clinical_transcription_demo.domain.model.ChatConversation
import demo.nexa.clinical_transcription_demo.domain.model.ChatMessage as DomainChatMessage
import demo.nexa.clinical_transcription_demo.presentation.ChatMessageUi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * Repository for managing chat conversations and messages.
 * Handles persistence of chat history to Room database.
 */
class ChatRepository(
    private val database: AppDatabase,
    private val context: Context
) {

    private val conversationDao = database.chatConversationDao()
    private val messageDao = database.chatMessageDao()

    // Background scope for persistence operations
    private val backgroundScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /**
     * Get or create a new chat conversation.
     */
    suspend fun getOrCreateConversation(): ChatConversation {
        val conversationId = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()

        val conversation = ChatConversation(
            id = conversationId,
            title = "Conversation ${generateShortId()}",
            createdAtEpochMs = now,
            lastModifiedEpochMs = now,
            messageCount = 0
        )

        conversationDao.insert(conversation.toEntity())
        return conversation
    }

    /**
     * Load a conversation with its messages.
     */
    suspend fun loadConversation(conversationId: String): Pair<ChatConversation?, List<DomainChatMessage>> {
        val conversation = conversationDao.getById(conversationId)?.toDomain()
        val messages = messageDao.getByConversation(conversationId).map { it.toDomain() }
        return conversation to messages
    }

    /**
     * Observe all conversations.
     */
    fun observeConversations(): Flow<List<ChatConversation>> {
        return conversationDao.observeAll().map { conversations ->
            conversations.map { it.toDomain() }
        }
    }

    /**
     * Observe messages in a conversation.
     */
    fun observeConversationMessages(conversationId: String): Flow<List<DomainChatMessage>> {
        return messageDao.observeByConversation(conversationId).map { messages ->
            messages.map { it.toDomain() }
        }
    }

    /**
     * Save a new message to a conversation.
     */
    fun saveMessage(
        conversationId: String,
        role: String,
        content: String,
        isError: Boolean = false
    ) {
        backgroundScope.launch {
            val messageId = UUID.randomUUID().toString()
            val now = System.currentTimeMillis()

            val message = DomainChatMessage(
                id = messageId,
                conversationId = conversationId,
                role = role,
                content = content,
                createdAtEpochMs = now,
                isError = isError
            )

            messageDao.insert(message.toEntity())

            // Update conversation's lastModified time and messageCount
            val conversation = conversationDao.getById(conversationId)
            if (conversation != null) {
                val messageCount = messageDao.countByConversation(conversationId)
                conversationDao.insert(
                    conversation.copy(
                        lastModifiedEpochMs = now,
                        messageCount = messageCount
                    )
                )
            }
        }
    }

    /**
     * Save multiple messages at once.
     */
    fun saveMessages(conversationId: String, messages: List<DomainChatMessage>) {
        backgroundScope.launch {
            val now = System.currentTimeMillis()
            val entitiesToInsert = messages.map { it.toEntity() }
            messageDao.insertAll(entitiesToInsert)

            // Update conversation
            val conversation = conversationDao.getById(conversationId)
            if (conversation != null) {
                val messageCount = messageDao.countByConversation(conversationId)
                conversationDao.insert(
                    conversation.copy(
                        lastModifiedEpochMs = now,
                        messageCount = messageCount
                    )
                )
            }
        }
    }

    /**
     * Clear all messages in a conversation (for starting fresh).
     */
    suspend fun clearConversation(conversationId: String) {
        messageDao.deleteByConversation(conversationId)
        val conversation = conversationDao.getById(conversationId)
        if (conversation != null) {
            conversationDao.insert(
                conversation.copy(
                    lastModifiedEpochMs = System.currentTimeMillis(),
                    messageCount = 0
                )
            )
        }
    }

    /**
     * Delete a conversation and all its messages.
     */
    suspend fun deleteConversation(conversationId: String) {
        conversationDao.deleteById(conversationId)
    }

    /**
     * Get recent keywords/topics from notes for suggested prompts.
     * Extracts unique words from note summaries and transcripts.
     */
    suspend fun getRecentTopics(limit: Int = 10): List<String> {
        val notesRepository = NotesRepository.getInstance(context)

        return try {
            // This would require access to notes repository
            // For now, return empty - will be populated when linked to NotesRepository
            emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Get suggested prompts based on recent interactions.
     */
    suspend fun getSuggestedPrompts(): List<String> {
        return listOf(
            "What is Atrial Fibrillation?",
            "Side effects of Metformin",
            "What is the ICD-10 code for hypertension?",
            "Explain diabetes management",
            "Common blood pressure medications"
        )
    }

    private fun generateShortId(): String {
        return UUID.randomUUID().toString().take(8).uppercase()
    }

    companion object {
        @Volatile
        private var INSTANCE: ChatRepository? = null

        fun getInstance(context: Context): ChatRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ChatRepository(
                    AppDatabase.getInstance(context),
                    context
                ).also { INSTANCE = it }
            }
        }
    }
}

