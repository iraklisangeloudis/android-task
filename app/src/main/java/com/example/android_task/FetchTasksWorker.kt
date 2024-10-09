package com.example.android_task

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.android_task.database.TaskDatabase

class FetchTasksWorker(context: Context, workerParams: WorkerParameters) : CoroutineWorker(context, workerParams) {

    private val repository = TaskRepository(
        TaskDatabase.getDatabase(context.applicationContext).taskDao(),
        ApiClient(context.applicationContext),
        context.applicationContext
    )

    override suspend fun doWork(): Result {
        return try {
            // Login and get the token
            val token = repository.login("365", "1")
            if (token != null) {
                // Fetch and store tasks
                repository.fetchAndStoreTasks(token)
                Result.success()
            } else {
                Result.retry()  // Retry if login failed
            }
        } catch (e: Exception) {
            Result.failure()
        }
    }
}