package com.example.uniflow

import kotlinx.serialization.Serializable

@Serializable
data class CreateTaskRequest(
    val subject: String,
    val title: String,
    val deadline: String,
    val isCompleted: Boolean
)
