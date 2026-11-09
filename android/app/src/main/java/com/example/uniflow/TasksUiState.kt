package com.example.uniflow
//этот дата класс хранит все текущее состояние экрана задач UniFlow
data class TasksUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val tasks: List<TaskInfo> = emptyList(),
    val isOffline: Boolean = true,
    val isAddTaskDialogOpen: Boolean = false
)
