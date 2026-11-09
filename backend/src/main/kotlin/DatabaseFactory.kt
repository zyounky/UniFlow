package com.example.uniflow

import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.JdbcTransaction
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import java.io.File


object DatabaseFactory {

    // init() здесь означает примерно: «инициализировать базу данных / подготовить подключение к базе».

    fun init(path: String): Database {
        val databaseFile = File(path).absoluteFile

        val database = Database.connect( // подключи наше Kotlin-приложение к конкретной базе данных.
            url = "jdbc:sqlite:${databaseFile.path}",   /*
             // jdbc     — используем JDBC для подключения к базе данных
            // sqlite   — тип базы данных: SQLite
            // uniflow.db — файл, в котором SQLite будет хранить данные
            */
            driver = "org.sqlite.JDBC"   // org.sqlite.JDBC — JDBC-драйвер SQLite.
        // Он нужен, чтобы JVM-приложение могло общаться с SQLite.
        )

            /*
            // transaction выполняет операции с БД как одну общую работу.
            // Если всё прошло успешно -> COMMIT: изменения сохраняются.
            // Если внутри произошла ошибка -> ROLLBACK: изменения откатываются.
             */
        transaction(database) {
            ensureTasksTable()
            SchemaUtils.create(TelegramUpdatesTable)
        }

        return database
    }

    private fun JdbcTransaction.ensureTasksTable() {
        val hasLegacyIds = exec(
            """
            SELECT EXISTS (
                SELECT 1 FROM pragma_table_info('tasks')
                WHERE name = 'id' AND upper(type) = 'INT' AND pk = 1
            )
            """.trimIndent()
        ) { rows -> rows.next() && rows.getBoolean(1) } ?: false
        // The legacy INT primary key is independent of SQLite's rowid and cannot generate an ID.
        if (hasLegacyIds) exec("ALTER TABLE tasks RENAME TO uniflow_tasks_legacy")
        SchemaUtils.create(TasksTable) // создаем таблицу
        if (!hasLegacyIds) return

        exec(
            """
            INSERT INTO tasks (id, subject, title, deadline, isCompleted)
            SELECT id, subject, title, deadline, isCompleted FROM uniflow_tasks_legacy
            """.trimIndent()
        )
        exec("DROP TABLE uniflow_tasks_legacy")
    }

}
/*

DatabaseFactory.init()

1. Подключил Exposed к SQLite
2. Создал таблицу tasks, если нужно
3. Подготовил backend к работе с данными

*/
