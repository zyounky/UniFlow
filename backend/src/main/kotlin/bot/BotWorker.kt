package com.example.uniflow.bot

import com.example.uniflow.TelegramTaskRequest
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import java.io.IOException
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.Files
import java.nio.file.NoSuchFileException
import java.nio.file.Path
import java.nio.file.StandardCopyOption.ATOMIC_MOVE
import java.nio.file.StandardCopyOption.REPLACE_EXISTING

internal class BotWorker(
    private val settings: BotSettings,
    private val telegram: TelegramClient,
    private val api: UniFlowBotApi
) {
    suspend fun run() {
        var offset = readOffset()
        var backoffMillis = 1_000L
        while (currentCoroutineContext().isActive) {
            val retryAfterMillis = try {
                for (update in telegram.getUpdates(offset).sortedBy { it.updateId }) {
                    if (update.updateId < offset) continue
                    handle(update)
                    offset = update.updateId + 1
                    saveOffset(offset)
                    backoffMillis = 1_000
                }
                backoffMillis = 1_000
                continue
            } catch (error: BotRequestException) {
                if (!error.canRetry) throw error
                System.err.println(error.message)
                (error.retryAfterSeconds ?: 0) * 1_000L
            } catch (_: IOException) {
                System.err.println("Ошибка соединения или записи offset. Повторяю запрос.")
                offset = readOffset()
                0L
            }
            delay(maxOf(backoffMillis, retryAfterMillis))
            backoffMillis = (backoffMillis * 2).coerceAtMost(30_000)
        }
    }

    private suspend fun handle(update: TelegramUpdate) {
        val message = update.message ?: return
        if (message.chat.type != "private" || message.chat.id != settings.allowedChatId) return
        val text = message.text ?: return
        val task = parseTaskCommand(text)
        val reply = if (task == null) {
            TASK_HELP
        } else {
            try {
                val saved = api.createTask(TelegramTaskRequest(update.updateId, task))
                "Задание добавлено. ID: ${saved.id}"
            } catch (error: BotRequestException) {
                when (error.status) {
                    400 -> "Проверьте данные задания. $TASK_HELP"
                    409 -> "Это обновление уже обработано с другими данными."
                    else -> throw error
                }
            }
        }
        telegram.sendMessage(message.chat.id, reply)
    }

    private fun readOffset(): Long {
        val text = try {
            Files.readString(settings.offsetPath)
        } catch (_: NoSuchFileException) {
            return 0
        }
        return requireNotNull(text.trim().toLongOrNull()?.takeIf { it >= 0 }) {
            "Файл offset должен содержать неотрицательное целое число."
        }
    }

    private fun saveOffset(offset: Long) {
        val path = settings.offsetPath
        val temporary = Files.createTempFile(path.parent, ".telegram-offset.", ".tmp")
        try {
            Files.writeString(temporary, offset.toString())
            try {
                Files.move(temporary, path, ATOMIC_MOVE, REPLACE_EXISTING)
            } catch (_: AtomicMoveNotSupportedException) {
                Files.move(temporary, path, REPLACE_EXISTING)
            }
        } finally {
            Files.deleteIfExists(temporary)
        }
    }
}
