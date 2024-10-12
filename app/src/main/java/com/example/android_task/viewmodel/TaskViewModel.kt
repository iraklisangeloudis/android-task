package com.example.android_task.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.android_task.database.TaskEntity
import androidx.lifecycle.viewModelScope
import com.example.android_task.TaskRepository
import kotlinx.coroutines.launch

class TaskViewModel(private val repository: TaskRepository) : ViewModel() {

    // LiveData from Room, automatically updated when the database changes
    val tasks: LiveData<List<TaskEntity>> = repository.getAllTasksLive()

    private val _isRefreshing = MutableLiveData<Boolean>()
    val isRefreshing: LiveData<Boolean> = _isRefreshing

    private val _searchResults = MutableLiveData<List<TaskEntity>>()
    val searchResults: LiveData<List<TaskEntity>> = _searchResults

    // Search for tasks based on query
    fun searchTasks(query: String) = viewModelScope.launch {
        val searchResults = repository.searchTasks(query)
        _searchResults.postValue(searchResults)
    }

    // Perform login and fetch tasks (optional)
    fun loginAndFetchTasks(username: String, password: String) = viewModelScope.launch {
        val token = repository.login(username, password)

        token?.let {
            repository.fetchAndStoreTasks(it)
        }
    }

    fun refreshTasks() = viewModelScope.launch {
        val token = repository.getToken()

        token?.let {
            repository.fetchAndStoreTasks(it)
        }
        _isRefreshing.postValue(false)
    }
}

