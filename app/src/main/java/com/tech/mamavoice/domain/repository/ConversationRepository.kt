package com.tech.mamavoice.domain.repository

import com.tech.mamavoice.data.remote.dto.ConversationDetailResponse
import com.tech.mamavoice.data.remote.dto.ConversationListResponse
import com.tech.mamavoice.domain.util.Resource

/**
 * Access to the AI chat history: the list of past conversations and the paginated message
 * history of any single one. Live querying (voice/text) stays in [VoiceRepository].
 */
interface ConversationRepository {
    /** Lists the user's conversations, most-recent first, paginated. */
    suspend fun getConversations(page: Int = 1, limit: Int = 10): Resource<ConversationListResponse>

    /** Fetches one conversation with a page of its message history. */
    suspend fun getConversation(id: String, page: Int = 1, limit: Int = 10): Resource<ConversationDetailResponse>

    /** Deletes a conversation and its messages; returns true on success. */
    suspend fun deleteConversation(id: String): Resource<Boolean>
}
