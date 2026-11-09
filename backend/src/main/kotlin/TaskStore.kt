package com.example.uniflow

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.JdbcTransaction
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.insertIgnore
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update

class TaskStore(private val database: Database) {
    suspend fun list(): List<TaskDto> = query {
        TasksTable.selectAll().orderBy(TasksTable.id).map { row ->
            TaskDto(
                id = row[TasksTable.id],
                subject = row[TasksTable.subject],
                title = row[TasksTable.title],
                deadline = row[TasksTable.deadline],
                isCompleted = row[TasksTable.isCompleted],
            )
        } // получил // строки из базы данных
    }

    suspend fun create(task: CreateTaskRequest): TaskDto = query {
        insertTask(task)
    }

    suspend fun update(task: TaskDto): Boolean =
        query { //Причём update() возвращает количество обновлённых строк, которое мы сохраняем в updatedRows.
            val updatedRows = TasksTable.update({ TasksTable.id eq task.id }) { // eq (equal) — инфиксная функция Exposed, которая создаёт SQL-условие равенства.
// TasksTable.id — колонка id в таблице базы данных.
// id — конкретный идентификатор задачи, полученный из HTTP-запроса.
// Например, если id = 5, то TasksTable.id eq id означает WHERE id = 5.
// eq не сравнивает значения сразу и не возвращает обычный Boolean,
// а создаёт SQL-условие, которое проверит база при выполнении запроса.
// update изменит только строки, подходящие под это условие.
// Если задача с таким id не найдена, update вернёт 0.
                it[TasksTable.subject] = task.subject
                it[TasksTable.title] = task.title
                it[TasksTable.deadline] = task.deadline
                it[TasksTable.isCompleted] = task.isCompleted
            }
            updatedRows > 0
        }

    suspend fun delete(id: Int): Boolean = query {
        TasksTable.deleteWhere { TasksTable.id eq id } > 0
    }

    suspend fun createFromTelegram(request: TelegramTaskRequest): TaskDto? = query {
        val requestJson = Json.encodeToString(request.task)
        // The first write claims the update ID before reading, so concurrent deliveries serialize in SQLite.
        val claimed = TelegramUpdatesTable.insertIgnore {
            it[updateId] = request.updateId
            it[TelegramUpdatesTable.requestJson] = requestJson
        }.insertedCount > 0

        if (!claimed) {
            val saved = TelegramUpdatesTable.selectAll().where {
                TelegramUpdatesTable.updateId eq request.updateId
            }.single()
            if (saved[TelegramUpdatesTable.requestJson] != requestJson) return@query null
            return@query Json.decodeFromString<TaskDto>(checkNotNull(saved[TelegramUpdatesTable.responseJson]))
        }

        val created = insertTask(request.task)
        TelegramUpdatesTable.update({ TelegramUpdatesTable.updateId eq request.updateId }) {
            it[responseJson] = Json.encodeToString(created)
        }
        created
    }

    private fun insertTask(task: CreateTaskRequest): TaskDto {
        val insertResult = TasksTable.insert {
            //  it[TasksTable.id] =
            //     task.id // возьми id задачи, полученной от Android, и запиши его в колонку id новой строки таблицы SQLite.
            it[TasksTable.subject] = task.subject
            it[TasksTable.title] = task.title
            it[TasksTable.deadline] = task.deadline
            it[TasksTable.isCompleted] = task.isCompleted
        }
        return TaskDto(
            id = insertResult[TasksTable.id],
            subject = task.subject,
            title = task.title,
            deadline = task.deadline,
            isCompleted = task.isCompleted,
        )
    }

    private suspend fun <T> query(block: JdbcTransaction.() -> T): T =
        withContext(Dispatchers.IO) {
            transaction(database, statement = block)
        }
}
