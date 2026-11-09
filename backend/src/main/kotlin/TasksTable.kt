package com.example.uniflow

import org.jetbrains.exposed.v1.core.Table

object TasksTable : Table("tasks") { // создай описание таблицы с именем "tasks"

    /*
id | subject      | title
---+--------------+-------------------------
1  | Математика   | Решить №2816
2  | Физика       | Сделать лабораторную
3  | Английский   | Выучить слова
 */

    val id = integer("id").autoIncrement() // это Kotlin-переменная, через которую мы будем обращаться к колонке «Создай в таблице колонку с именем id, которая хранит целые числа».

    val subject = varchar("subject", 100) // Дальше добавим следующую колонку — subject. Max 100 символов

    val title = varchar("title", 200 ) // в колонке title можно хранить строку длиной до 200 символов.

    val deadline = varchar("deadline", 100)

    val isCompleted = bool("isCompleted")



    override val primaryKey = PrimaryKey(id)



}