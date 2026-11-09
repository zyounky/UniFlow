package com.example.uniflow

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun TasksScreen(
    modifier: Modifier = Modifier,
    viewModel: TasksViewModel = viewModel()
) {

    val uiState = viewModel.uiState
    val canEdit = !uiState.isOffline && !uiState.isLoading

    LaunchedEffect(Unit) {
        viewModel.loadTasks()
    }

    Column(
        modifier = modifier
    ) {
        TasksHeader(
            onAddTask = viewModel::openAddTaskDialog,
            enabled = canEdit // доступен ли компонент интерфейса
        )

        if (uiState.isLoading) {
            CircularProgressIndicator()
        } else if (uiState.isOffline) {
            Text("Офлайн-режим")
        }

        uiState.errorMessage?.let { message ->
            Text(message, color = MaterialTheme.colorScheme.error)
        }

        TextButton(onClick = viewModel::loadTasks, enabled = !uiState.isLoading) {
            Text(if (uiState.isOffline) "Повторить подключение" else "Обновить список")
        }

        if (uiState.tasks.isEmpty() && !uiState.isLoading) {
            Text(if (uiState.isOffline) "Нет сохранённых заданий" else "Заданий пока нет")
        }

        TasksList(
            uiState.tasks,
            modifier = Modifier.weight(1f),
            onTaskCheckedChange = { changedTask, newValue -> // changedTask какая именно задача изменилась
                //newValue - — какое новое состояние чекбокса, true или false.
                // То что прилетело в onTaskCheckedChange отсюда onTaskCheckedChange(task, newValue),
                // мы просим вот здесь changedTask, newValue ->
                //Вот здесь мы ищем задачу, которая прилетела в onChecked... , по всему списку задач
                viewModel.updateTaskCompletion(changedTask, newValue)
            },
            onTaskDelete = viewModel::deleteTask,
            enabled = canEdit
        )
    }

    if (uiState.isAddTaskDialogOpen) {
        AddTaskDialog(
            uiState = uiState,
            onAddTask = viewModel::addTask,
            onDismiss = viewModel::closeAddTaskDialog,
            onRetry = viewModel::loadTasks
        )
    }
}

@Composable
private fun AddTaskDialog(
    uiState: TasksUiState,
    onAddTask: (CreateTaskRequest) -> Unit,
    onDismiss: () -> Unit,
    onRetry: () -> Unit
) {
    var subject by rememberSaveable { mutableStateOf("") }
    var title by rememberSaveable { mutableStateOf("") }
    var deadline by rememberSaveable { mutableStateOf("") }
    val isEditing = !uiState.isLoading
    val isValid = subject.isNotBlank() && subject.trim().length <= 100 &&
        title.isNotBlank() && title.trim().length <= 200 && deadline.trim().length <= 100

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Новое задание") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = subject,
                    onValueChange = { subject = it },
                    label = { Text("Предмет") },
                    isError = subject.trim().length > 100,
                    enabled = isEditing,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Название") },
                    isError = title.trim().length > 200,
                    enabled = isEditing,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = deadline,
                    onValueChange = { deadline = it },
                    label = { Text("Срок (необязательно)") },
                    isError = deadline.trim().length > 100,
                    enabled = isEditing,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                uiState.errorMessage?.let { message ->
                    Text(message, color = MaterialTheme.colorScheme.error)
                }
                if (uiState.isLoading) {
                    CircularProgressIndicator()
                } else if (uiState.isOffline) {
                    TextButton(onClick = onRetry) { Text("Повторить подключение") }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onAddTask(
                        CreateTaskRequest(
                            subject = subject.trim(),
                            title = title.trim(),
                            deadline = deadline.trim(),
                            isCompleted = false
                        )
                    )
                },
                enabled = isValid && isEditing && !uiState.isOffline
            ) {
                Text("Сохранить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = isEditing) {
                Text("Отмена")
            }
        }
    )
}

@Composable
fun TasksHeader(
    onAddTask: () -> Unit,
    enabled: Boolean
) {
    Text("Ближайшие задания")

    Button(
        onClick = onAddTask,
        enabled = enabled
    ) {
        Text("Добавить новую задачу")
    }
}

@Composable
fun TasksList(
    tasks: List<TaskInfo>,
    modifier: Modifier = Modifier,
    onTaskCheckedChange: (TaskInfo, Boolean) -> Unit,
    onTaskDelete: (TaskInfo) -> Unit,
    enabled: Boolean
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
    ) {
        items(tasks, key = { task -> task.id }) { task ->
            TaskCard(
                task,
                onCheckedChange = { newValue ->
                    onTaskCheckedChange(task, newValue)
                },
                onDelete = {
                    onTaskDelete(task) //вызываем функцию в случае
                    //нажатия на какую то карточку(ib)
                },
                enabled = enabled,
                modifier = Modifier.testTag("task-card")
            )
        }

    }
}

