package com.example.android_task.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface TaskDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<TaskEntity>)

    @Query("DELETE FROM tasks")
    suspend fun deleteAllTasks()

    @Query("SELECT * FROM tasks WHERE task LIKE :query OR title LIKE :query OR description LIKE :query OR sort LIKE :query OR wageType LIKE :query OR BusinessUnitKey LIKE :query OR businessUnit LIKE :query")
    suspend fun searchTasks(query: String): List<TaskEntity>

    @Query("SELECT * FROM tasks")
    fun getAllTasksLive(): LiveData<List<TaskEntity>>
}

