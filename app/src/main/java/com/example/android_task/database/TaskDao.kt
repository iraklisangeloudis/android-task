package com.example.android_task.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface TaskDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<TaskEntity>)

    @Query("SELECT * FROM tasks")
    fun getAllTasks(): LiveData<List<TaskEntity>>

    @Query("SELECT * FROM tasks")
    suspend fun getAllTasksList(): List<TaskEntity> // Use this to return a list

    @Delete
    suspend fun deleteTask(task: TaskEntity)

    @Query("DELETE FROM tasks")
    suspend fun deleteAllTasks()

    @Query("SELECT * FROM tasks WHERE task LIKE :query OR title LIKE :query OR description LIKE :query OR sort LIKE :query OR wageType LIKE :query OR BusinessUnitKey LIKE :query OR businessUnit LIKE :query")
    suspend fun searchTasks(query: String): List<TaskEntity>
}
