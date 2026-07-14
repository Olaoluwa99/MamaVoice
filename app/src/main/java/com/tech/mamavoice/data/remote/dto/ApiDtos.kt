package com.tech.mamavoice.data.remote.dto

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive

@Serializable
data class ApiResponse<T>(
    val success: Boolean,
    val statusCode: Int,
    val message: String,
    val data: T
)

@Serializable
data class DashboardResponse(
    val firstName: String,
    val statusText: String,
    val currentWeek: Int,
    val daysToNextVaccine: Int,
    val nextVaccineName: String
)

/**
 * Body for `POST api/voice/text-query` — a typed health question. Language is profile-driven
 * server-side. [conversationId] continues an existing chat; null starts a new one.
 */
@Serializable
data class TextQueryRequest(
    val textQuery: String,
    val conversationId: String? = null
)

/**
 * Response for both `POST api/voice/query` (audio) and `POST api/voice/text-query` (text).
 *
 * The STT-only fields ([transcript], [sttConfidence], [detectedLanguage], [profileLanguage]) are
 * present only for the audio endpoint, so everything is nullable and safe for both.
 */
@Serializable
data class VoiceQueryResponse(
    val spokenResponse: String? = null,         // AI answer in the user's native language
    val spokenResponseEnglish: String? = null,  // same answer in English
    val riskLevel: String? = null,              // e.g. LOW / MEDIUM / HIGH
    val aiResponseText: String? = null,         // English answer (fallback text)
    val isDangerSign: Boolean = false,
    val language: String? = null,
    // Native-language TTS clip. The endpoint returns either a URL string, an empty object `{}`,
    // or null, so it is decoded as a raw element and read through [audioUrlOrNull].
    val audioUrl: JsonElement? = null,
    val audioContentType: String? = null,
    val profileLanguage: String? = null,        // audio endpoint only
    val detectedLanguage: String? = null,       // audio endpoint only
    val transcript: String? = null,             // audio endpoint only — what the user said
    val sttConfidence: Double? = null,          // audio endpoint only
    val conversationId: String? = null,         // id of the chat this turn belongs to
    // Id of the stored assistant message. TTS is generated asynchronously, so [audioUrl] is
    // usually null here; poll `GET api/conversations/messages/{id}/audio` with this id.
    val assistantMessageId: String? = null
) {
    /** The audio URL when the server sent a plain string; null for an empty object or absent value. */
    val audioUrlOrNull: String?
        get() = (audioUrl as? JsonPrimitive)?.takeIf { it.isString }?.content
}

/**
 * Response for `GET api/conversations/messages/{messageId}/audio` — the async TTS status for one
 * assistant message. [audioUrl] stays null while [status] is "pending"; a caller polls until it
 * has a URL (ready) or [isFailed] turns true.
 */
@Serializable
data class MessageAudioResponse(
    val messageId: String? = null,
    val status: String? = null,                 // "pending" | "ready" | "failed"
    val audioUrl: JsonElement? = null,
    val audioContentType: String? = null
) {
    /** The clip URL once generated; null while pending, on failure, or if sent as an empty object. */
    val audioUrlOrNull: String?
        get() = (audioUrl as? JsonPrimitive)?.takeIf { it.isString }?.content

    /** Terminal failure — TTS will not arrive for this message, so stop polling. */
    val isFailed: Boolean get() = status.equals("failed", ignoreCase = true)
}

// --- Conversation history ----------------------------------------------------------------------

/** Pagination envelope shared by the conversation list and detail endpoints. */
@Serializable
data class PaginationDto(
    val page: Int = 1,
    val limit: Int = 10,
    val total: Int = 0,
    val totalPages: Int = 0,
    val hasMore: Boolean = false
)

/** One row in the conversation history list. */
@Serializable
data class ConversationSummaryDto(
    val id: String,
    val title: String? = null,
    val lastMessageAt: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

/** Response for `GET api/conversations`. */
@Serializable
data class ConversationListResponse(
    val conversations: List<ConversationSummaryDto> = emptyList(),
    val pagination: PaginationDto = PaginationDto()
)

/**
 * A single stored message. Fields mirror [VoiceQueryResponse] where they overlap.
 *
 * [audioUrl] is kept as a raw [JsonElement] because the endpoint has been observed returning an
 * empty object (`{}`) as well as a URL string; read it through [audioUrlOrNull] to stay safe.
 */
@Serializable
data class ConversationMessageDto(
    val id: String,
    val role: String? = null,                   // "user" | "assistant"
    val content: String? = null,
    val spokenResponse: String? = null,
    val spokenResponseEnglish: String? = null,
    val language: String? = null,
    val riskLevel: String? = null,
    val isDangerSign: Boolean = false,
    val inputType: String? = null,              // "text" | "audio"
    val audioUrl: JsonElement? = null,
    val audioContentType: String? = null,
    val sttConfidence: Double? = null,
    val createdAt: String? = null
) {
    /** The audio URL when the server sent a plain string; null for an empty object or absent value. */
    val audioUrlOrNull: String?
        get() = (audioUrl as? JsonPrimitive)?.takeIf { it.isString }?.content
}

/** Response for `GET api/conversations/{id}` — summary plus a page of messages. */
@Serializable
data class ConversationDetailResponse(
    val id: String,
    val title: String? = null,
    val lastMessageAt: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val messages: List<ConversationMessageDto> = emptyList(),
    val pagination: PaginationDto = PaginationDto()
)

/** Response for `DELETE api/conversations/{id}`. */
@Serializable
data class DeleteConversationResponse(
    val success: Boolean = false
)

@Serializable
data class NutritionalValuesDto(
    val calories: Double? = null,
    val protein: Double? = null,
    val carbs: Double? = null,
    val fat: Double? = null,
    val fiber: Double? = null,
    val sodium: Double? = null,
    val iron: Double? = null,
    val calcium: Double? = null,
    val vitaminC: Double? = null,
    val folate: Double? = null,
    val vitaminA: Double? = null,
    val zinc: Double? = null
)

@Serializable
data class FoodDto(
    val id: String,
    val name: String,
    val category: String,
    val benefits: String,
    val mamaVoiceTip: String? = null,
    val dangerWarning: String? = null,
    val preparationTips: String? = null,
    val affordabilityRating: Int? = null,
    val availabilityRating: Int? = null,
    val imageUrls: List<String> = emptyList(),
    val nutritionalValues: NutritionalValuesDto? = null,
    val suitableFor: List<String> = emptyList(),
    val trimesterRecommendation: List<String> = emptyList(),
    val keyNutrients: List<String> = emptyList(),
    val servingSuggestion: String? = null,
    val pairsWellWith: List<String> = emptyList(),
    val avoidWith: List<String> = emptyList(),
    val isHighIron: Boolean? = null,
    val isHighFolate: Boolean? = null,
    val isHighCalcium: Boolean? = null,
    val isHighProtein: Boolean? = null,
    val isHighVitaminC: Boolean? = null
)

@Serializable
data class FoodsResponseData(
    val foods: List<FoodDto> = emptyList()
)

@Serializable
data class VaccineItem(
    val vaccineId: String,
    val vaccineName: String,
    val dueDateString: String,
    val dueDate: String? = null,
    val isCompleted: Boolean,
    val administeredDate: String? = null,
    val sideEffects: String? = null
)

@Serializable
data class VaccineLogRequest(
    val vaccineId: String,
    val administeredDate: String,
    val vaccineName: String,
    val isCompleted: Boolean,
    val sideEffects: String? = null
)

@Serializable
data class VaccineLogResponse(
    val id: String,
    val vaccineId: String,
    val vaccineName: String,
    val isCompleted: Boolean,
    val administeredDate: String? = null,
    val sideEffects: String? = null,
    val updatedAt: String? = null
)

@Serializable
data class VaccinesData(
    val vaccines: List<VaccineItem> = emptyList()
)

@Serializable
data class TrackerHistoryData(
    val logs: List<HealthLog> = emptyList()
)

@Serializable
data class HealthLog(
    val id: String,
    val logDate: String,
    val weightKg: Double? = null,
    val bloodPressure: String? = null,
    val nutritionNotes: String? = null,
    val symptoms: String? = null
)

@Serializable
data class HealthLogRequest(
    val logDate: String,
    val weightKg: Double? = null,
    val bloodPressure: String? = null,
    val nutritionNotes: String? = null,
    val symptoms: String? = null
)

@Serializable
data class AppEnumsWrapperResponse(
    val success: Boolean,
    val statusCode: Int,
    val message: String,
    val data: AppEnumsResponse
)

@Serializable
data class AppEnumsResponse(
    val profileTypes: List<String> = emptyList(),
    val languages: List<String> = emptyList(),
    val motherStages: List<String> = emptyList(),
    val foodCategories: List<String> = emptyList(),
    val foodStages: List<String> = emptyList(),
    val trimesters: List<String> = emptyList(),
    val devicePlatforms: List<String> = emptyList(),
    val states: List<String> = emptyList(),
    val stateLgas: Map<String, List<String>> = emptyMap()
)
