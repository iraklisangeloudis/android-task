package com.example.android_task

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import com.example.android_task.database.TaskDao
import com.example.android_task.database.TaskEntity
import org.json.JSONArray

class TaskRepository(private val taskDao: TaskDao, private val apiClient: ApiClient, private val context: Context) {

    // Handles login and returns the token
    suspend fun login(username: String, password: String): String? {
        Log.d("Login", "Login successful. Token saved. Thread: ${Thread.currentThread().name}")
        return apiClient.login(username, password) // Interact with API Client for login
    }

    // Fetches tasks from the API and stores them in the Room database
    suspend fun fetchAndStoreTasks(token: String) {
        val tasksResponse = apiClient.getTasks(token)
        tasksResponse?.let {
            val tasks = parseTasks(it)
            taskDao.deleteAllTasks()
            taskDao.insertTasks(tasks)
            Log.d("Tasks", "Tasks fetched and saved to DB. Thread: ${Thread.currentThread().name}")
        }
    }

    // Searches for tasks in the Room database
    suspend fun searchTasks(query: String): List<TaskEntity> {
        return taskDao.searchTasks("%$query%")
    }

    // Automatically provides LiveData of tasks from Room
    fun getAllTasksLive(): LiveData<List<TaskEntity>> {
        return taskDao.getAllTasksLive() // Room's LiveData will notify the ViewModel of changes
    }

    fun getToken(): String? {
        val sharedPref = context.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        return sharedPref.getString("access_token", null)
    }

    // Helper function to parse the API response and map it to TaskEntity
    private fun parseTasks(response: String): List<TaskEntity> {
        val jsonArray = JSONArray(response)
        val tasks = mutableListOf<TaskEntity>()

        for (i in 0 until jsonArray.length()) {
            val jsonObject = jsonArray.getJSONObject(i)
            val task = TaskEntity(
                task = jsonObject.getString("task"),
                title = jsonObject.getString("title"),
                description = jsonObject.getString("description"),
                sort = jsonObject.optString("sort", ""),
                wageType = jsonObject.optString("wageType", ""),
                BusinessUnitKey = jsonObject.optString("BusinessUnitKey", ""),
                businessUnit = jsonObject.optString("businessUnit", ""),
                parentTaskID = jsonObject.optString("parentTaskID", null),
                preplanningBoardQuickSelect = jsonObject.optString("preplanningBoardQuickSelect", null),
                colorCode = jsonObject.getString("colorCode"),
                workingTime = jsonObject.optString("workingTime", null),
                isAvailableInTimeTrackingKioskMode = jsonObject.optBoolean("isAvailableInTimeTrackingKioskMode", false)
            )
            tasks.add(task)
        }
        return tasks
    }
}
