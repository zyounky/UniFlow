package com.example.uniflow

import kotlinx.serialization.Serializable


//объект для передачи данных о задаче между клиентом и сервером
//Data transfer object (форма задачи для передачи по сети)

@Serializable
data class TaskDto(
    val id: Int,
    val subject: String,
    val title: String,
    val deadline: String,
    val isCompleted: Boolean
)