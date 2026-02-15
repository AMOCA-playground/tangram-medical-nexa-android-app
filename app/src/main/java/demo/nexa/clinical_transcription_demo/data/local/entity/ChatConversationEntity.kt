package demo.nexa.clinical_transcription_demo.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for persisting chat conversations.
 * Each conversation represents a distinct chat session.
 */
@Entity(tableName = "chat_conversations")
data class ChatConversationEntity(
    @PrimaryKey
    val id: String,
    val title: String = "Untitled Conversation",
    val createdAtEpochMs: Long,
    val lastModifiedEpochMs: Long,
    val messageCount: Int = 0
)

