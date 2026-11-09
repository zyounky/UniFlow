package com.example.uniflow

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonIgnoreUnknownKeys

@Serializable // объект этого класса можно превращать в JSON и обратно.
data class TaskDto(
    val id: Int,
    val subject: String,
    val title: String,
    val deadline: String,
    val isCompleted: Boolean
)

@Serializable
@OptIn(ExperimentalSerializationApi::class)
@JsonIgnoreUnknownKeys
data class CreateTaskRequest(
    val subject: String,
    val title: String,
    val deadline: String,
    val isCompleted: Boolean = false
)

@Serializable
data class TelegramTaskRequest(
    val updateId: Long,
    val task: CreateTaskRequest
)
