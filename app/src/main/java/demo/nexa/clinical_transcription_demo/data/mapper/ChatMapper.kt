package demo.nexa.clinical_transcription_demo.data.mapper

import demo.nexa.clinical_transcription_demo.data.local.entity.ChatConversationEntity
import demo.nexa.clinical_transcription_demo.data.local.entity.ChatMessageEntity
import demo.nexa.clinical_transcription_demo.domain.model.ChatConversation
import demo.nexa.clinical_transcription_demo.domain.model.ChatMessage as DomainChatMessage

/**
 * Mappers between Entity, Domain, and UI representations of chat data
 */

// ── Chat Conversation Mapping ──

fun ChatConversationEntity.toDomain(): ChatConversation {
    return ChatConversation(
        id = id,
        title = title,
        createdAtEpochMs = createdAtEpochMs,
        lastModifiedEpochMs = lastModifiedEpochMs,
        messageCount = messageCount
    )
}

fun ChatConversation.toEntity(): ChatConversationEntity {
    return ChatConversationEntity(
        id = id,
        title = title,
        createdAtEpochMs = createdAtEpochMs,
        lastModifiedEpochMs = lastModifiedEpochMs,
        messageCount = messageCount
    )
}

// ── Chat Message Mapping ──

fun ChatMessageEntity.toDomain(): DomainChatMessage {
    return DomainChatMessage(
        id = id,
        conversationId = conversationId,
        role = role,
        content = content,
        createdAtEpochMs = createdAtEpochMs,
        isError = isError
    )
}

fun DomainChatMessage.toEntity(): ChatMessageEntity {
    return ChatMessageEntity(
        id = id,
        conversationId = conversationId,
        role = role,
        content = content,
        createdAtEpochMs = createdAtEpochMs,
        isError = isError
    )
}

