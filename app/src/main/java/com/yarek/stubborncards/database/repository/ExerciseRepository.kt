package com.yarek.stubborncards.database.repository

import android.content.Context
import com.yarek.stubborncards.database.AppDatabase
import com.yarek.stubborncards.model.CardAndProgress
import com.yarek.stubborncards.model.LearningProgress
import com.yarek.stubborncards.model.ProgressLevel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ExerciseRepository(context: Context) {

    private val database = AppDatabase.getInstance(context)
    private val progressDao = database.learningProgressDao()

    suspend fun fetchExerciseBatch(
        level: ProgressLevel,
        requiredScore: Float,
        testIntervalSeconds: Long,
        limit: Int
    ): List<CardAndProgress> = withContext(Dispatchers.IO) {
        val nowSeconds = System.currentTimeMillis() / 1000
        progressDao.getExerciseBatchByLevel(
            level,
            requiredScore,
            nowSeconds,
            testIntervalSeconds,
            limit)
    }

    suspend fun peekOldestProgressInLevel(level: ProgressLevel): LearningProgress? = withContext(Dispatchers.IO) {
        progressDao.peekOldestProgressInLevel(level)
    }

    suspend fun updateProgressState(progress: LearningProgress) = withContext(Dispatchers.IO) {
        progressDao.update(progress)
    }
}