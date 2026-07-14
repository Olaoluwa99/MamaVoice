package com.tech.mamavoice.presentation.voice

import com.tech.mamavoice.data.remote.dto.ConversationMessageDto

/**
 * Maps a stored [ConversationMessageDto] to the on-screen [ChatMessage].
 *
 * This assumes the backend stores one record per message with a [role] ("user" / "assistant") —
 * the same split the live [VoiceConversationViewModel.handleResult] produces. If the API instead
 * bundles a whole turn (question + answer) into a single record, this is the only place to change:
 * return a `List<ChatMessage>` and emit both a user and an assistant bubble here.
 */
fun ConversationMessageDto.toChatMessage(): ChatMessage {
    val isUser = role.equals("user", ignoreCase = true)
    return if (isUser) {
        ChatMessage(
            id = id,
            isUser = true,
            text = content
        )
    } else {
        ChatMessage(
            id = id,
            isUser = false,
            nativeText = spokenResponse ?: content,
            englishText = spokenResponseEnglish ?: content,
            riskLevel = riskLevel,
            isDangerSign = isDangerSign,
            audioUrl = audioUrlOrNull,
            serverMessageId = id
        )
    }
}
