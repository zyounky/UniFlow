package com.example.uniflow.bot

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class TelegramResponse<T>(
    val ok: Boolean,
    val result: T? = null,
    @SerialName("error_code") val errorCode: Int? = null,
    val parameters: TelegramResponseParameters? = null
)

@Serializable
internal data class TelegramResponseParameters(
    @SerialName("retry_after") val retryAfter: Int? = null
)

@Serializable
internal data class TelegramUpdate(
    @SerialName("update_id") val updateId: Long,
    val message: TelegramMessage? = null
)

@Serializable
internal data class TelegramMessage(val chat: TelegramChat, val text: String? = null)

@Serializable
internal data class TelegramChat(val id: Long, val type: String)

@Serializable
internal data class GetUpdatesRequest(
    val offset: Long,
    val timeout: Int,
    @SerialName("allowed_updates") val allowedUpdates: List<String>
)

@Serializable
internal data class SendMessageRequest(
    @SerialName("chat_id") val chatId: Long,
    val text: String
)
