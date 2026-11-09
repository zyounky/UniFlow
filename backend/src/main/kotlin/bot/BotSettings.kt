package com.example.uniflow.bot

import java.net.URI
import java.nio.file.Path

internal data class BotSettings(
    val token: String,
    val allowedChatId: Long,
    val secret: String,
    val telegramApiBaseUrl: String,
    val uniflowApiBaseUrl: String,
    val offsetPath: Path
) {
    companion object {
        fun fromEnvironment(env: Map<String, String> = System.getenv()): BotSettings {
            fun required(name: String): String =
                requireNotNull(env[name]?.trim()?.takeIf(String::isNotEmpty)) {
                    "Задайте переменную окружения $name."
                }

            fun baseUrl(name: String, default: String): String {
                val value = (env[name] ?: default).trim().trimEnd('/')
                val uri = runCatching { URI(value) }.getOrNull()
                require(uri != null && uri.scheme in setOf("http", "https") && uri.host != null &&
                    uri.userInfo == null && uri.query == null && uri.fragment == null
                ) { "$name должен содержать базовый HTTP(S)-адрес." }
                return value
            }

            val token = required("TELEGRAM_BOT_TOKEN")
            require(token.matches(Regex("[A-Za-z0-9:_-]+"))) { "Некорректный TELEGRAM_BOT_TOKEN." }
            val chatId = requireNotNull(required("ALLOWED_CHAT_ID").toLongOrNull()?.takeIf { it > 0 }) {
                "ALLOWED_CHAT_ID должен быть идентификатором личного чата."
            }
            return BotSettings(
                token = token,
                allowedChatId = chatId,
                secret = required("UNIFLOW_BOT_SECRET"),
                telegramApiBaseUrl = baseUrl("TELEGRAM_API_BASE_URL", "https://api.telegram.org"),
                uniflowApiBaseUrl = baseUrl("UNIFLOW_API_BASE_URL", "http://127.0.0.1:8080"),
                offsetPath = Path.of(env["TELEGRAM_OFFSET_PATH"] ?: ".telegram-offset").toAbsolutePath()
            )
        }
    }
}
