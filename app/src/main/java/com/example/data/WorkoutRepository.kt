package com.example.data

import kotlinx.coroutines.flow.Flow

class WorkoutRepository(private val dao: WorkoutSessionDao) {
    val allSessions: Flow<List<WorkoutSession>> = dao.getAllSessions()

    suspend fun insertSession(session: WorkoutSession) {
        dao.insertSession(session)
    }

    suspend fun clearHistory() {
        dao.clearAllSessions()
    }
}
