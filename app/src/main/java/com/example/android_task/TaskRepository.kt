package com.example.android_task

import androidx.lifecycle.LiveData
import com.example.android_task.database.TaskDao
import com.example.android_task.database.TaskEntity

class TaskRepository(private val taskDao: TaskDao) {
    val allTasks: LiveData<List<TaskEntity>> = taskDao.getAllTasks()

    suspend fun insert(task: TaskEntity) {
        taskDao.insertTask(task)
    }

    suspend fun insertAll(tasks: List<TaskEntity>) {
        taskDao.insertTasks(tasks)
    }
}
