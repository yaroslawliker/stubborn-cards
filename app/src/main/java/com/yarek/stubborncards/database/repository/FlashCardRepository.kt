package com.yarek.stubborncards.database.repository

import android.content.Context
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.yarek.stubborncards.database.AppDatabase
import com.yarek.stubborncards.model.FlashCard
import com.yarek.stubborncards.model.LearningProgress
import com.yarek.stubborncards.model.ProgressLevel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.withContext

class FlashCardRepository(context: Context) {

    private val database = AppDatabase.getInstance(context)
    private val flashCardDao = database.flashCardDao()
    private val progressDao = database.learningProgressDao()

    /**
     * Get an updating real-time stream of word counts grouped per ProgressLevel.
     * Consuming this stream requires no loop calculations on the UI thread.
     */
    val progressLevelCounts: Flow<Map<ProgressLevel, Int>> = progressDao.progressLevelCounts

    /**
     * Create a new word, add it to DB, and automatically initialize
     * its related initial LearningProgress instance.
     */
    suspend fun createCardWithInitialProgress(word: String, translation: String) {
        withContext(Dispatchers.IO) {
            // Ensures both inserts complete together, or both fail together
            database.runInTransaction {

                // Create and insert the flashcard
                val newCard = FlashCard(word, translation)
                val generatedCardId = flashCardDao.insert(newCard)

                // Setup the initial progress state mapping
                val initialProgress = LearningProgress().apply {
                    flashCardId = generatedCardId

                    level = ProgressLevel.NEW
                    score = 0.0f
                    isOnReview = false
                    lastReviewed = null
                    beforeLastReviewed = null
                }
                // Save the progress properties row
                progressDao.insert(initialProgress)
            }
        }
    }

    fun getCardsByLevelPaged(level: ProgressLevel, query: String): Flow<PagingData<FlashCard>> {
        val formattedQuery = if (query.isBlank()) null else "%$query%"

        return Pager(
            config = PagingConfig(
                pageSize = 30,
                prefetchDistance = 10,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { progressDao.getCardsByLevelPaged(level, formattedQuery) }
        ).flow
    }

    fun getCardDetailsFlow(cardId: Long): Flow<Pair<FlashCard?, LearningProgress?>> {
        val cardFlow: Flow<FlashCard?> = flashCardDao.getById(cardId)

        val progressFlow: Flow<LearningProgress?> = progressDao.getProgressByCardId(cardId)

        return cardFlow.combine(progressFlow) { card, progress ->
            Pair(card, progress)
        }
    }

    suspend fun getCardIdsByLevel(level: ProgressLevel): List<Long> {
        return withContext(Dispatchers.IO) {
            progressDao.getCardIdsByLevel(level)
        }
    }

    suspend fun updateCardText(card: FlashCard) {
        withContext(Dispatchers.IO) {
            flashCardDao.update(card)
        }
    }

    suspend fun updateProgressLevelDirectly(cardId: Long, newLevel: ProgressLevel) {
        withContext(Dispatchers.IO) {
            progressDao.updateLevelByCardId(cardId, newLevel)
        }
    }

    suspend fun updateProgressScoreDirectly(cardId: Long, newScore: Float) {
        withContext(Dispatchers.IO) {
            progressDao.updateScoreByCardId(cardId, newScore)
        }
    }
}