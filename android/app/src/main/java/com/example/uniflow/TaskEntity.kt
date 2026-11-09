package com.example.uniflow

import androidx.room3.Entity
import androidx.room3.PrimaryKey
@Entity
data class TaskEntity(
    @PrimaryKey // id = уникальный идентификатор задачи
    val id: Int,
    val subject: String,
    val title: String,
    val deadline: String,
    val isCompleted: Boolean
)

