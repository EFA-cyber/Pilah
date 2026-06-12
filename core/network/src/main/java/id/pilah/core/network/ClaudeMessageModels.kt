package id.pilah.core.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Kontrak request/response Anthropic Messages API (`POST /v1/messages`). */
@Serializable
data class ClaudeMessageRequest(
    val model: String,
    @SerialName("max_tokens") val maxTokens: Int,
    val messages: List<ClaudeMessage>,
)

@Serializable
data class ClaudeMessage(
    val role: String,
    val content: String,
)

@Serializable
data class ClaudeMessageResponse(
    val content: List<ClaudeContentBlock> = emptyList(),
)

@Serializable
data class ClaudeContentBlock(
    val type: String,
    val text: String? = null,
)
