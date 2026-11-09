package com.example.uniflow

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json


object ApiClient {

    private val tasksUrl = "${BuildConfig.API_BASE_URL}/tasks"

    val client = HttpClient {   // через него Android будет делать GET / POST запросы
        expectSuccess = true
        install(HttpTimeout) {
            connectTimeoutMillis = 5_000
            requestTimeoutMillis = 10_000
        }

        install(ContentNegotiation) { // Он нужен, чтобы Ktor Client понимал, как преобразовывать данные между форматом HTTP и Kotlin-объектами.
            //Сам HttpClient умеет отправить запрос и получить HTTP-ответ. Но вот как превратить тело ответа из JSON в Kotlin-объект, он сам по себе не знает.
            json() // для обмена данными используй json (Идея в том что бэк дает в json а мы тут преобразовываем в Котлин объект)
        }


    }


    suspend fun getTasks(): List<TaskDto> {
        return client
            .get(tasksUrl) // возвращает http ответ
            .body() //возьми тело ответа и преобразуй JSON в тот тип, который функция должна вернуть — List<TaskDto>.
    }

    suspend fun addTask(task: CreateTaskRequest): TaskDto { // функция для отправки задачи на backend через POST.
        return client.post(tasksUrl) {
            contentType(ContentType.Application.Json) //скажи серверу, что тело запроса отправляется в формате JSON.
            setBody(task) // «Вот данные, которые я хочу положить в тело HTTP-запроса».
        }.body()
    }

    suspend fun updateTask(task: TaskDto) {
        client.put("$tasksUrl/${task.id}") { //  КАКУЮ именно задачу хотим обновить
            contentType(ContentType.Application.Json)
            setBody(task) //  КАКИМИ новыми данными её хотим заменить
        }
    }
    suspend fun deleteTask(task: TaskDto) {
        client.delete("$tasksUrl/${task.id}")
    }
}

