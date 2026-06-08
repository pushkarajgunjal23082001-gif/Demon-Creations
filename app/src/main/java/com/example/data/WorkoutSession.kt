package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "workout_sessions")
data class WorkoutSession(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val programId: String,
    val programName: String,
    val timestamp: Long = System.currentTimeMillis(),
    val durationSeconds: Int,
    val caloriesBurned: Int
)
