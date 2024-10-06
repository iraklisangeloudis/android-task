package com.example.android_task.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val task: String,
    val title: String,
    val description: String,
    val sort: String,
    val wageType: String,
    val BusinessUnitKey: String,
    val businessUnit: String,
    val parentTaskID: String?,
    val preplanningBoardQuickSelect: String?,
    val colorCode: String,
    val workingTime: String?,
    val isAvailableInTimeTrackingKioskMode: Boolean
)

