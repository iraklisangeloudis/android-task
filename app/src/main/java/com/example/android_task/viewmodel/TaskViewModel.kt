package com.example.android_task.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.android_task.database.TaskEntity
import androidx.lifecycle.viewModelScope
import com.example.android_task.TaskRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TaskViewModel(private val repository: TaskRepository) : ViewModel() {
    // LiveData to hold all tasks or filtered tasks
    private val _tasks = MutableLiveData<List<TaskEntity>>()
    val tasks: LiveData<List<TaskEntity>> = _tasks

    init {
        // Initialize with all tasks
        viewModelScope.launch {
            _tasks.postValue(repository.getAllTasks())
        }
    }

    fun insert(task: TaskEntity) = viewModelScope.launch {
        repository.insert(task)
    }

    fun insertAll(tasks: List<TaskEntity>) = viewModelScope.launch {
        repository.insertAll(tasks)
    }

    fun searchTasks(query: String) = viewModelScope.launch {
        val searchResults = repository.searchTasks(query)
        // Update the LiveData to reflect filtered tasks
        _tasks.postValue(searchResults)
    }

    fun deleteAllAndInsert(tasks: List<TaskEntity>) = viewModelScope.launch {
        repository.deleteAllAndInsert(tasks)
        _tasks.postValue(repository.getAllTasks())
    }
}

