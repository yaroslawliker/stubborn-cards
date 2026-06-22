package com.yarek.stubborncards.database.repository

import android.content.Context
import com.yarek.stubborncards.database.AppDatabase
import com.yarek.stubborncards.engine.PromotionEngine
import com.yarek.stubborncards.model.ProgressLevel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.time.ZoneOffset

data class DashboardTotals(
    val totalCards: Int,
    val totalReady: Int,
    val totalHidden: Int
)

class DashboardRepository(context: Context) {

    private val database = AppDatabase.getInstance(context)
    private val dashboardDao = database.dashboardDao()
    private val progressDao = database.learningProgressDao()

    /**
     * Calculates the aggregated review/test states for ALL levels at once,
     * utilizing the DashboardDao sum queries.
     */
    suspend fun getDashboardTotals(now: LocalDateTime): DashboardTotals = withContext(Dispatchers.IO) {
        val nowSeconds = now.toEpochSecond(ZoneOffset.UTC)

        var totalReady = 0
        var totalHidden = 0

        for (level in ProgressLevel.entries) {
            val config = PromotionEngine.getLevelConfig(level)

            // Mapped to the integer types expected by the DAO
            val requiredScore = config.requiredScore
            val optimalIntervalSeconds = config.optimalIntervalSeconds
            val testIntervalSeconds = config.testIntervalSeconds

            // Combine both standard review and test review into one "actionable" pool
            totalReady += dashboardDao.countReadyToReview(
                level, nowSeconds, optimalIntervalSeconds, requiredScore
            ) ?: 0

            totalReady += dashboardDao.countReadyToTest(
                level, nowSeconds, testIntervalSeconds, requiredScore
            ) ?: 0

            totalHidden += dashboardDao.countHiddenBeforeTest(
                level, nowSeconds, testIntervalSeconds, requiredScore
            ) ?: 0
        }

        val totalCards = (dashboardDao.countAll() ?: 0L).toInt()

        return@withContext DashboardTotals(
            totalCards = totalCards,
            totalReady = totalReady,
            totalHidden = totalHidden
        )
    }
}