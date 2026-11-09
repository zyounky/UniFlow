package com.example.uniflow

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import java.io.IOException

class TasksViewModel(
    application: Application
) : AndroidViewModel(application) {
    var uiState by mutableStateOf(TasksUiState())
        private set

    private val repository = TasksRepository(application.applicationContext)

    fun loadTasks() {
        runTaskOperation("Не удалось загрузить задания") {
            uiState = uiState.copy(isOffline = true)
            uiState = uiState.copy(tasks = repository.getTasks())
//                repository.seedInitialTasks() // добавили в бд задачи
            repository.syncTasks()
        }
    }

    fun updateTaskCompletion(
        changedTask: TaskInfo,
        newValue: Boolean
    ) {
        if (uiState.isOffline) return
        runTaskOperation("Не удалось изменить задание") {
            val updatedTask = changedTask.copy(isCompleted = newValue)
            repository.updateTaskOnline(updatedTask)
        }
    }

    fun deleteTask(task: TaskInfo) {
        if (uiState.isOffline) return
        runTaskOperation("Не удалось удалить задание") {
            repository.deleteTaskOnline(task)
        }
    }

    fun addTask(request: CreateTaskRequest) {
        if (uiState.isOffline) return
        runTaskOperation("Не удалось добавить задание") {
            // Порядок сохранения в Repository:
            // тут добавляем в бэкенд
            // тут добавляем в рум
            repository.createTask(request)
            uiState = uiState.copy(isAddTaskDialogOpen = false)
        }
    }

    fun openAddTaskDialog() {
        uiState = uiState.copy(isAddTaskDialogOpen = true, errorMessage = null)
    }

    fun closeAddTaskDialog() {
        if (!uiState.isLoading) {
            uiState = uiState.copy(isAddTaskDialogOpen = false)
        }
    }

    private fun runTaskOperation(errorMessage: String, operation: suspend () -> Unit) {
        if (uiState.isLoading) return
        uiState = uiState.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            try {
                operation()
                uiState = uiState.copy(tasks = repository.getTasks(), isOffline = false)
// CancellationException — специальное исключение, сигнализирующее об отмене корутины.
// Например, когда ViewModel окончательно очищается, её viewModelScope отменяется.
// Если корутина в этот момент ожидает ответ сервера через Ktor Client,
// приостанавливаемая операция может обнаружить отмену и выбросить CancellationException.
// Так как CancellationException наследуется от Exception,
// обычный catch (e: Exception) тоже может его перехватить.
// Поэтому мы проверяем тип исключения через is.
// Если это CancellationException, повторно выбрасываем его через throw e,
// чтобы не подавлять отмену и не продолжать ненужную работу.
// Если исключение другого типа, выполняем оставшийся код catch,
// например пытаемся получить задачи из локальной Room. про CancellationException
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                uiState = uiState.copy(
                    errorMessage = errorMessage,
                    isOffline = uiState.isOffline || e is IOException
                )
            } finally {
                uiState = uiState.copy(isLoading = false)
            }
        }
    }
}
