package com.example.pet.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity (

    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val title: String,
    val description: String,
    val day: String,

    @ColumnInfo(defaultValue = "false")
    val isCompleted: Boolean = false
)