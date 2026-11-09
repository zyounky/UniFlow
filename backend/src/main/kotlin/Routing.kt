package com.example.uniflow

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.request.receive
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.security.MessageDigest

// put и post (пут когда что-то заменить, пост когда что то добавить)

fun Application.configureRouting(store: TaskStore, botSecret: String?) { // Application – сам сервер. configureRouting() = настроить его маршруты


    routing {


        get("/") {
            call.respondText("Hello, World!")
        }
        get("/json/kotlinx-serialization") {
            call.respond(mapOf("hello" to "world"))
        }
        get("/tasks") {
            call.respond(store.list())
        }
        post("/tasks") { // нужно будет когда клиент будет отправлять данные на сервер
            val request =
                call.receive<CreateTaskRequest>().normalized() // возьми тело этого HTTP-запроса и попробуй превратить его в объект TaskDto.
            if (!hasValidTaskFields(request.subject, request.title, request.deadline)) {
                call.respondText(INVALID_FIELDS_MESSAGE, status = HttpStatusCode.BadRequest)
                return@post
            }

            call.respond(HttpStatusCode.Created, store.create(request))
        }

        /*
// {id} — это не обязательно число.
// Для Ktor это просто переменная часть URL.
// Например, /tasks/5 -> id = "5",
// а /tasks/abc -> id = "abc".
         */
        put("/tasks/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()?.takeIf { it > 0 } // получаем параметр из url
            if (id == null) {
                call.respondText("Некорректный id", status = HttpStatusCode.BadRequest)
                return@put
            }
            val requestedTask =
                call.receive<TaskDto>() // «Возьми JSON из body текущего call и преврати его в объект TaskDto».

            if (requestedTask.id != id) {
                call.respondText("ID в URL и теле запроса не совпадают", status = HttpStatusCode.BadRequest)
                return@put
            }
            val updatedTask = requestedTask.copy(
                subject = requestedTask.subject.trim(),
                title = requestedTask.title.trim(),
                deadline = requestedTask.deadline.trim(),
            )
            if (!hasValidTaskFields(updatedTask.subject, updatedTask.title, updatedTask.deadline)) {
                call.respondText(INVALID_FIELDS_MESSAGE, status = HttpStatusCode.BadRequest)
                return@put
            }

            if (!store.update(updatedTask)) {
                call.respondText("Задача не найдена", status = HttpStatusCode.NotFound)
                return@put
            }
            call.respond(updatedTask)
        }

        delete("/tasks/{id}") {
            // {id} кладется в parameters уже
            val id = call.parameters["id"]?.toIntOrNull()?.takeIf { it > 0 }
            if (id == null) {
                call.respondText("Некорректный id", status = HttpStatusCode.BadRequest)
                return@delete
            }

            if (!store.delete(id)) {
                call.respondText("Задача не найдена", status = HttpStatusCode.NotFound)
                return@delete
            }

            call.respond("Задача удалена")
        }

        post("/telegram/tasks") {
            val providedSecret = call.request.headers["X-Bot-Secret"]
            if (botSecret.isNullOrBlank() || providedSecret == null ||
                !MessageDigest.isEqual(botSecret.encodeToByteArray(), providedSecret.encodeToByteArray())
            ) {
                call.respondText("Неверный секрет бота", status = HttpStatusCode.Forbidden)
                return@post
            }

            val request = call.receive<TelegramTaskRequest>()
            if (request.updateId < 0) {
                call.respondText("Некорректный updateId", status = HttpStatusCode.BadRequest)
                return@post
            }

            val task = request.task.normalized()
            if (!hasValidTaskFields(task.subject, task.title, task.deadline)) {
                call.respondText(INVALID_FIELDS_MESSAGE, status = HttpStatusCode.BadRequest)
                return@post
            }

            val created = store.createFromTelegram(request.copy(task = task))
            if (created == null) {
                call.respondText("Этот updateId уже использован с другими данными", status = HttpStatusCode.Conflict)
                return@post
            }

            call.respond(HttpStatusCode.Created, created)
        }
    }
}

private const val INVALID_FIELDS_MESSAGE =
    "Предмет и название обязательны. Максимальная длина: предмет 100, название 200, срок 100."

private fun hasValidTaskFields(subject: String, title: String, deadline: String): Boolean =
    subject.isNotBlank() && subject.length <= 100 &&
        title.isNotBlank() && title.length <= 200 && deadline.length <= 100

private fun CreateTaskRequest.normalized(): CreateTaskRequest = copy(
    subject = subject.trim(),
    title = title.trim(),
    deadline = deadline.trim(),
)

/*
(call)
1. Пользователь что-то делает в Android-приложении
Например нажал Checkbox.
2. Приложение решает отправить HTTP-запрос:
PUT /tasks/5
3. Ktor получает этот запрос и для него создаёт объект текущего вызова — call.
4. И уже внутри обработчика ты работаешь с этим call:

*/
