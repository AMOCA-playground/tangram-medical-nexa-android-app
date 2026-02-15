package demo.nexa.clinical_transcription_demo.domain.model

/**
 * Domain model for a chat message.
 */
data class ChatMessage(
    val id: String,
    val conversationId: String,
    val role: String,
    val content: String,
    val createdAtEpochMs: Long,
    val isError: Boolean = false
)

