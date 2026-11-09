package com.example.uniflow

import android.content.Context
import androidx.room3.withWriteTransaction

class TasksRepository(
    context: Context
) {


    private val database = DatabaseProvider.getDatabase(context.applicationContext)
    private val taskDao = database.taskDao()
    // это объект доступа к базе данных
//     taskDao() возвращает не "объект интерфейса" напрямую.
// Room генерирует конкретный класс, например TaskDao_Impl,
// который реализует интерфейс TaskDao.
//
// Реальный объект: TaskDao_Impl
// Тип, под которым мы с ним работаем: TaskDao
//
// Аналогия:
// val animal: Animal = Dog()


    private fun TaskEntity.toTaskInfo(): TaskInfo { // extension function
        return TaskInfo(
            id = id,
            subject = subject,
            title = title,
            deadline = deadline,
            isCompleted = isCompleted
        )
    }

    private fun TaskInfo.toTaskEntity(): TaskEntity {
        return TaskEntity(
            id = id,
            subject = subject,
            title = title,
            deadline = deadline,
            isCompleted = isCompleted
        )
    }

    private fun TaskDto.toTaskInfo(): TaskInfo {
        return TaskInfo(
            id = id,
            subject = subject,
            title = title,
            deadline = deadline,
            isCompleted = isCompleted
        )
    }

    private fun TaskInfo.toTaskDto(): TaskDto {
        return TaskDto(
            id = id,
            subject = subject,
            title = title,
            deadline = deadline,
            isCompleted = isCompleted
        )
    }

    suspend fun getTasks(): List<TaskInfo> {
        return taskDao.getAllTasks().map { task ->
            task.toTaskInfo()
        }
    }

    suspend fun updateTask(updatedTask: TaskInfo) {
        taskDao.updateTask(updatedTask.toTaskEntity())
    }

    suspend fun updateTaskOnline(task: TaskInfo) {
        ApiClient.updateTask(task.toTaskDto())
        updateTask(task)
    }
    suspend fun deleteTaskOnline(task: TaskInfo) {
        ApiClient.deleteTask(task.toTaskDto())
        deleteTask(task)
    }

    suspend fun seedInitialTasks() {
        if (taskDao.getAllTasks().isEmpty()) {
            getMockTasks().forEach { task ->
                taskDao.insertTask(task.toTaskEntity())
            }
        }
    }

    //сделать обычное добавление новой задачи через Repository.
    suspend fun insertTask(task: TaskInfo) {
        taskDao.insertTask(task.toTaskEntity())
    }

    suspend fun deleteTask(task: TaskInfo) {
        taskDao.deleteTask(task.toTaskEntity())
    }

    suspend fun getRemoteTasks(): List<TaskInfo> {
        val tasks = ApiClient
            .getTasks() // сюда вернулись List<TaskDto> с бэка
            .map { task ->
                task.toTaskInfo()
            }
        return tasks
    }

    suspend fun createTask(request: CreateTaskRequest) {
        val savedTask = ApiClient.addTask(request).toTaskInfo()
        insertTask(savedTask)
    }

    suspend fun syncTasks() {
        val remoteTasks = getRemoteTasks() // с бэка пришли
        database.withWriteTransaction {
            val localTasks = getTasks() // с рума пришли

            // Прежний поиск через find по списку для каждого элемента:
            // здесь внутри много раз цикл запускается
            // localTasks = List<TaskInfo>, значит T = TaskInfo, поэтому find вернёт TaskInfo? (объект или null)
            val localById = localTasks.associateBy(TaskInfo::id)
            val remoteIds = remoteTasks.mapTo(mutableSetOf(), TaskInfo::id)
            remoteTasks.forEach { remoteTask ->
                val localTask = localById[remoteTask.id]

                if (localTask == null) { // если у пользователя нет интернета задачи должны лежать локально на Room
                    insertTask(remoteTask) // Это обновляет задачу в Room данными сервера.
                } else if (localTask != remoteTask) {
                    updateTask(remoteTask)
                }
            }
            localTasks.forEach { localTask ->
                if (localTask.id !in remoteIds) {
                    deleteTask(localTask)
                }
            }
        }
    }


}
