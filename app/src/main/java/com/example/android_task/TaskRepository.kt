package com.example.android_task

import com.example.android_task.database.TaskDao
import com.example.android_task.database.TaskEntity

class TaskRepository(private val taskDao: TaskDao) {
    suspend fun searchTasks(query: String): List<TaskEntity> {
        return taskDao.searchTasks("%$query%")
    }

    // Get all tasks for initializing or resetting the search
    suspend fun getAllTasks(): List<TaskEntity> {
        return taskDao.getAllTasksList() // Add a method that returns List<TaskEntity>
    }

    suspend fun insertAll(tasks: List<TaskEntity>) {
        taskDao.deleteAllTasks()
        taskDao.insertTasks(tasks)
    }
}
