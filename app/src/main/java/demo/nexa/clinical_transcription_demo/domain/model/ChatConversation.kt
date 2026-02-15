package demo.nexa.clinical_transcription_demo.domain.model

/**
 * Domain model for a chat conversation.
 */
data class ChatConversation(
    val id: String,
    val title: String,
    val createdAtEpochMs: Long,
    val lastModifiedEpochMs: Long,
    val messageCount: Int = 0
)

