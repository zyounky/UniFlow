package com.example.uniflow

import io.ktor.server.application.Application

fun main(args: Array<String>) {

    io.ktor.server.netty.EngineMain.main(args)  // Запускает Ktor-сервер через движок Netty.
// EngineMain читает конфигурацию приложения,
// поднимает сервер, например на порту 8080,
// и после этого сервер начинает принимать HTTP-запросы:
// GET, POST, PUT, DELETE и другие.
}

fun Application.module() { // Application Это объект Ktor, внутри которого настраивается сервер.
    val databasePath = environment.config.propertyOrNull("uniflow.databasePath")?.getString()
        ?: System.getenv("UNIFLOW_DB_PATH")
        ?: "uniflow.db"
    val database = DatabaseFactory.init(databasePath)
    val botSecret = (environment.config.propertyOrNull("uniflow.botSecret")?.getString()
        ?: System.getenv("UNIFLOW_BOT_SECRET"))?.trim()

    configureSerialization() // подключаем работу с json
    configureRouting(TaskStore(database), botSecret) // подключаем работу с маршрутами
}
