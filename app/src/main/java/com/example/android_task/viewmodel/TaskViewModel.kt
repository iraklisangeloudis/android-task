package com.example.android_task.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.android_task.database.TaskEntity
import androidx.lifecycle.viewModelScope
import com.example.android_task.TaskRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TaskViewModel(private val repository: TaskRepository) : ViewModel() {
    val allTasks: LiveData<List<TaskEntity>> = repository.allTasks

    fun insert(task: TaskEntity) = viewModelScope.launch {
        repository.insert(task)
    }

    fun insertAll(tasks: List<TaskEntity>) = viewModelScope.launch {
        repository.insertAll(tasks)
    }
}

