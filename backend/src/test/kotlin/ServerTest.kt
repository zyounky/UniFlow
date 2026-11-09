package com.example.uniflow

import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.json.Json
import java.nio.file.Files
import java.nio.file.Path
import java.sql.DriverManager
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ServerTest {

    @Test
    fun `test root endpoint`() = testWithDatabase {
        // verify server root returns 200
        assertEquals(HttpStatusCode.OK, client.get("/").status)
    }

    @Test
    fun `tasks can be created updated and deleted using server IDs`() = testWithDatabase {
        assertEquals(emptyList(), Json.decodeFromString<List<TaskDto>>(client.get("/tasks").bodyAsText()))

        val createdResponse = client.post("/tasks") {
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(taskRequest.copy(subject = " ${taskRequest.subject} ")))
        }
        assertEquals(HttpStatusCode.Created, createdResponse.status)
        val created = Json.decodeFromString<TaskDto>(createdResponse.bodyAsText())
        assertTrue(created.id > 0)
        assertEquals(task.copy(id = created.id), created)

        val legacyId = 101
        val secondResponse = client.post("/tasks") {
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(task.copy(id = legacyId)))
        }
        assertEquals(HttpStatusCode.Created, secondResponse.status)
        val second = Json.decodeFromString<TaskDto>(secondResponse.bodyAsText())
        assertTrue(second.id > created.id)
        assertTrue(second.id != legacyId)

        val updated = created.copy(title = "Сдать работу", isCompleted = true)
        val updateResponse = client.put("/tasks/${created.id}") {
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(updated))
        }
        assertEquals(HttpStatusCode.OK, updateResponse.status)
        assertEquals(updated, Json.decodeFromString<TaskDto>(updateResponse.bodyAsText()))
        assertEquals(listOf(updated, second), Json.decodeFromString<List<TaskDto>>(client.get("/tasks").bodyAsText()))

        val deleteResponse = client.delete("/tasks/${created.id}")
        assertEquals(HttpStatusCode.OK, deleteResponse.status)
        assertEquals("Задача удалена", deleteResponse.bodyAsText())
        assertEquals(listOf(second), Json.decodeFromString<List<TaskDto>>(client.get("/tasks").bodyAsText()))
    }

    @Test
    fun `invalid input and missing tasks have distinct statuses`() = testWithDatabase {
        val malformed = client.post("/tasks") {
            contentType(ContentType.Application.Json)
            setBody("{")
        }
        val invalidTask = client.post("/tasks") {
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(task.copy(subject = " ")))
        }
        val mismatchedId = client.put("/tasks/1") {
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(task.copy(id = 2)))
        }
        val missingTask = client.put("/tasks/1") {
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(task.copy(id = 1)))
        }

        assertEquals(HttpStatusCode.BadRequest, malformed.status)
        assertEquals(HttpStatusCode.BadRequest, invalidTask.status)
        assertEquals(HttpStatusCode.BadRequest, mismatchedId.status)
        assertEquals(HttpStatusCode.BadRequest, client.delete("/tasks/abc").status)
        assertEquals(HttpStatusCode.NotFound, missingTask.status)
        assertEquals(HttpStatusCode.NotFound, client.delete("/tasks/1").status)
    }

    @Test
    fun `concurrent Telegram replays create one task and preserve the original response`() = testWithDatabase {
        val replays = coroutineScope {
            List(4) { async { deliverTelegram(123, taskRequest) } }.awaitAll()
        }
        val original = Json.decodeFromString<TaskDto>(replays.first().bodyAsText())
        assertTrue(original.id > 0)
        for (response in replays) {
            assertEquals(HttpStatusCode.Created, response.status)
            assertEquals(original, Json.decodeFromString<TaskDto>(response.bodyAsText()))
        }
        assertEquals(listOf(original), Json.decodeFromString<List<TaskDto>>(client.get("/tasks").bodyAsText()))

        val conflict = deliverTelegram(123, taskRequest.copy(title = "Другая задача"))
        assertEquals(HttpStatusCode.Conflict, conflict.status)
        assertEquals(HttpStatusCode.OK, client.delete("/tasks/${original.id}").status)

        val afterDeletion = deliverTelegram(123, taskRequest.copy(subject = "  ${taskRequest.subject} "))
        assertEquals(HttpStatusCode.Created, afterDeletion.status)
        assertEquals(original, Json.decodeFromString<TaskDto>(afterDeletion.bodyAsText()))
        assertEquals(emptyList(), Json.decodeFromString<List<TaskDto>>(client.get("/tasks").bodyAsText()))
    }

    @Test
    fun `Telegram route rejects incorrect secrets and negative update IDs`() = testWithDatabase {
        assertEquals(HttpStatusCode.Forbidden, deliverTelegram(0, taskRequest, "wrong").status)
        assertEquals(HttpStatusCode.BadRequest, deliverTelegram(-1, taskRequest).status)
        assertEquals(HttpStatusCode.Created, deliverTelegram(0, taskRequest).status)
    }

    @Test
    fun `legacy SQLite task IDs are preserved and new IDs are generated`() =
        testWithDatabase(prepareDatabase = ::seedLegacyTask) {
            assertEquals(listOf(task), Json.decodeFromString<List<TaskDto>>(client.get("/tasks").bodyAsText()))

            val response = client.post("/tasks") {
                contentType(ContentType.Application.Json)
                setBody(Json.encodeToString(taskRequest))
            }
            assertEquals(HttpStatusCode.Created, response.status)
            val created = Json.decodeFromString<TaskDto>(response.bodyAsText())
            assertTrue(created.id > task.id)
            assertEquals(listOf(task, created), Json.decodeFromString<List<TaskDto>>(client.get("/tasks").bodyAsText()))
        }

    private fun seedLegacyTask(file: Path) {
        DriverManager.getConnection("jdbc:sqlite:$file").use { connection ->
            connection.createStatement().use { statement ->
                statement.executeUpdate(
                    """
                    CREATE TABLE tasks (
                        id INT NOT NULL PRIMARY KEY,
                        subject VARCHAR(100) NOT NULL,
                        title VARCHAR(200) NOT NULL,
                        deadline VARCHAR(100) NOT NULL,
                        isCompleted BOOLEAN NOT NULL,
                        CONSTRAINT chk_tasks_signed_integer_id CHECK (id BETWEEN -2147483648 AND 2147483647)
                    )
                    """.trimIndent()
                )
            }
            connection.prepareStatement("INSERT INTO tasks VALUES (?, ?, ?, ?, ?)").use { statement ->
                statement.setInt(1, task.id)
                statement.setString(2, task.subject)
                statement.setString(3, task.title)
                statement.setString(4, task.deadline)
                statement.setBoolean(5, task.isCompleted)
                statement.executeUpdate()
            }
        }
    }

    private suspend fun ApplicationTestBuilder.deliverTelegram(
        updateId: Long,
        task: CreateTaskRequest,
        secret: String = testBotSecret,
    ) = client.post("/telegram/tasks") {
        header("X-Bot-Secret", secret)
        contentType(ContentType.Application.Json)
        setBody(Json.encodeToString(TelegramTaskRequest(updateId, task)))
    }

    private fun testWithDatabase(
        prepareDatabase: (Path) -> Unit = {},
        block: suspend ApplicationTestBuilder.() -> Unit,
    ) {
        val file = Files.createTempFile("uniflow-test-", ".db")
        try {
            prepareDatabase(file)
            testApplication {
                // loads default configuration
                configure {
                    put("uniflow.databasePath", file.toString())
                    put("uniflow.botSecret", testBotSecret)
                }
                block()
            }
        } finally {
            Files.deleteIfExists(file)
        }
    }

    private val testBotSecret = "test-fixture"
    private val task = TaskDto(
        id = 101,
        subject = "Физика",
        title = "Лабораторная работа",
        deadline = "",
        isCompleted = false
    )
    private val taskRequest = CreateTaskRequest(task.subject, task.title, task.deadline)
}
