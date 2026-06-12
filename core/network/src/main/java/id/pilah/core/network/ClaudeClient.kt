package id.pilah.core.network

import javax.inject.Inject

/** Klien tipis untuk Anthropic Messages API, dipakai Analisis Mendalam (Fase 6). */
interface ClaudeClient {

    /** Kirim satu prompt teks ke [model] menggunakan [apiKey], kembalikan teks respons gabungan. */
    suspend fun sendPrompt(apiKey: String, model: ClaudeModel, prompt: String): String
}

class DefaultClaudeClient @Inject constructor(
    private val api: ClaudeApi,
) : ClaudeClient {

    override suspend fun sendPrompt(apiKey: String, model: ClaudeModel, prompt: String): String {
        val response = api.createMessage(
            apiKey = apiKey,
            request = ClaudeMessageRequest(
                model = model.id,
                maxTokens = MAX_TOKENS,
                messages = listOf(ClaudeMessage(role = "user", content = prompt)),
            ),
        )
        return response.content
            .filter { it.type == "text" }
            .mapNotNull { it.text }
            .joinToString(separator = "")
    }

    private companion object {
        const val MAX_TOKENS = 2_048
    }
}
