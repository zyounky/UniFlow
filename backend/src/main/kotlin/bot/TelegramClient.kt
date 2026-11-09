package com.example.uniflow.bot

import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject

internal class BotRequestException(
    val service: String,
    val status: Int,
    val retryAfterSeconds: Int? = null
) : RuntimeException("$service вернул HTTP $status.") {
    val canRetry: Boolean get() = status == 408 || status == 429 || status in 500..599
}

internal class TelegramClient(
    private val http: HttpClient,
    settings: BotSettings
) {
    private val baseUrl = "${settings.telegramApiBaseUrl}/bot${settings.token}"
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun getUpdates(offset: Long): List<TelegramUpdate> =
        call("getUpdates", GetUpdatesRequest(offset, 30, listOf("message")))

    suspend fun sendMessage(chatId: Long, text: String) {
        call<SendMessageRequest, JsonObject>("sendMessage", SendMessageRequest(chatId, text))
    }

    private suspend inline fun <reified Request : Any, reified Result : Any> call(
        method: String,
        request: Request
    ): Result {
        val response = http.post("$baseUrl/$method") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        val body = response.bodyAsText()
        val envelope = try {
            json.decodeFromString<TelegramResponse<Result>>(body)
        } catch (_: SerializationException) {
            null
        }
        if (!response.status.isSuccess() || envelope?.ok != true) {
            val status = envelope?.errorCode ?: response.status.value.takeUnless { it in 200..299 } ?: 502
            throw BotRequestException("Telegram", status, envelope?.parameters?.retryAfter)
        }
        return envelope.result ?: throw BotRequestException("Telegram", 502)
    }
}
