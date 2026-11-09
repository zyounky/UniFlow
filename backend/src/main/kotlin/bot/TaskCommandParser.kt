package com.example.uniflow.bot

import com.example.uniflow.CreateTaskRequest

internal const val TASK_HELP = "Отправьте /task Предмет | Задание | Срок"

internal fun parseTaskCommand(text: String): CreateTaskRequest? {
    val command = text.trim().split(Regex("\\s+"), limit = 2)
    if (command.size != 2 || command[0] != "/task") return null
    val fields = command[1].split('|').map(String::trim)
    if (fields.size != 3 || fields.any(String::isBlank)) return null
    val (subject, title, deadline) = fields
    if (subject.length > 100 || title.length > 200 || deadline.length > 100) return null
    return CreateTaskRequest(subject, title, deadline)
}
