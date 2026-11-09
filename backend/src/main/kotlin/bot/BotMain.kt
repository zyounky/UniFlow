package com.example.uniflow.bot

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import java.nio.file.Files
import kotlin.system.exitProcess

fun main() {
    val settings = try {
        BotSettings.fromEnvironment()
    } catch (error: IllegalArgumentException) {
        System.err.println(error.message)
        exitProcess(1)
    }
    try {
        Files.createDirectories(settings.offsetPath.parent)
        HttpClient(CIO) {
            followRedirects = false
            install(HttpTimeout) {
                connectTimeoutMillis = 5_000
                requestTimeoutMillis = 45_000
                socketTimeoutMillis = 45_000
            }
            install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
        }.use { http ->
            println("Бот UniFlow запущен.")
            runBlocking {
                BotWorker(settings, TelegramClient(http, settings), UniFlowBotApi(http, settings)).run()
            }
        }
    } catch (error: BotRequestException) {
        System.err.println(error.message)
        exitProcess(1)
    } catch (_: Exception) {
        System.err.println("Бот остановлен. Проверьте доступ к файлу offset и настройки сервера.")
        exitProcess(1)
    }
}
