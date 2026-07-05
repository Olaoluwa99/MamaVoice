package com.tech.mamavoice.domain.repository

import com.tech.mamavoice.data.remote.dto.VoiceQueryResponse
import com.tech.mamavoice.domain.util.Resource
import java.io.File

/**
 * The AI health-chat backend. Both paths return the same [VoiceQueryResponse] with a
 * native-language answer, an English answer, a risk level, and a TTS audio URL.
 */
interface VoiceRepository {
    /**
     * Uploads a recorded audio file; server transcribes, answers, and speaks it back.
     * Pass [conversationId] to continue an existing chat, or null to start a new one.
     */
    suspend fun voiceQuery(audio: File, conversationId: String? = null): Resource<VoiceQueryResponse>

    /**
     * Submits a typed question; server answers and speaks it back.
     * Pass [conversationId] to continue an existing chat, or null to start a new one.
     */
    suspend fun textQuery(text: String, conversationId: String? = null): Resource<VoiceQueryResponse>
}
