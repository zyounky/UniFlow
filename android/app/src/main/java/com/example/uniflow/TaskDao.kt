package com.example.uniflow

import androidx.room3.Dao
import androidx.room3.Delete
import androidx.room3.Insert
import androidx.room3.Query
import androidx.room3.Update

@Dao
interface TaskDao { // DAO - объект доступа к данным (Data Access Object)

    @Query("SELECT * FROM TaskEntity") /*
    SELECT = выбрать данные.
    * = все столбцы.
    FROM TaskEntity = из таблицы TaskEntity.
     */
   suspend fun getAllTasks(): List<TaskEntity> //реализацию этой функции Room сгенерирует сам через KSP.

    @Insert // «Эта функция должна добавлять объект в таблицу базы данных».
   suspend fun insertTask(task: TaskEntity) // какую именно задачу ты хочешь добавить

    @Update // Room посмотрит на @PrimaryKey, то есть на id, найдёт строку с таким id и обновит её данными из task.
   suspend fun updateTask(task: TaskEntity)

    @Delete
   suspend fun deleteTask(task: TaskEntity) // удаляет задачу из таблицы по её id (@PrimaryKey)

}