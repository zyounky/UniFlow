package com.example.uniflow

import android.content.Context
import androidx.room3.Room

object DatabaseProvider {

    private var database: AppDatabase? = null

    fun getDatabase(context: Context): AppDatabase {

        if (database != null) {
            return database!!
        }
        database = Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "uniflow_database"
        ).build()
        return database!!
    }
}