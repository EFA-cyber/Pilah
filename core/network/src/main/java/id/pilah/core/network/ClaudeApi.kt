package id.pilah.core.network

import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Header

interface ClaudeApi {

    @Headers("anthropic-version: 2023-06-01")
    @POST("v1/messages")
    suspend fun createMessage(
        @Header("x-api-key") apiKey: String,
        @Body request: ClaudeMessageRequest,
    ): ClaudeMessageResponse
}
