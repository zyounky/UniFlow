package com.example.uniflow.bot

import com.example.uniflow.TaskDto
import com.example.uniflow.TelegramTaskRequest
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.plugins.timeout
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess

internal class UniFlowBotApi(private val http: HttpClient, private val settings: BotSettings) {
    suspend fun createTask(task: TelegramTaskRequest): TaskDto {
        val response = http.post("${settings.uniflowApiBaseUrl}/telegram/tasks") {
            contentType(ContentType.Application.Json)
            header("X-Bot-Secret", settings.secret)
            timeout { requestTimeoutMillis = 10_000 }
            setBody(task)
        }
        if (!response.status.isSuccess()) {
            throw BotRequestException("UniFlow", response.status.value)
        }
        return response.body()
    }
}
