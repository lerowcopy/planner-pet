package com.example.pet.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity (

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String,
    val day: String,

    @ColumnInfo(defaultValue = "0")
    val startMinutes: Int = 0,

    @ColumnInfo(defaultValue = "0")
    val endMinutes: Int = 0,

    @ColumnInfo(defaultValue = "false")
    val isCompleted: Boolean = false,

    @ColumnInfo(defaultValue = "false")
    val isSynced: Boolean = false
)