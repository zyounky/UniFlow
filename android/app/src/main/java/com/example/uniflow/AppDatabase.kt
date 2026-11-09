package com.example.uniflow

import androidx.room3.Database
import androidx.room3.RoomDatabase

@Database( // «Этот класс будет базой данных Room, и вот её настройки».
    entities = [TaskEntity::class], // «В этой базе есть таблица, описанная классом TaskEntity».
    version = 1 // текущая версия схемы базы
)

abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao

}