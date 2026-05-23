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

    suspend fun getCardDetailsWithProgress(cardId: Long): Pair<FlashCard?, LearningProgress?> {
        return withContext(Dispatchers.IO) {
            // Fetch the target flashcard model row
            val card = flashCardDao.getById(cardId)
            // Fetch the corresponding learning progress model row
            val progress = progressDao.getProgressByCardId(cardId)

            Pair(card, progress)
        }
    }

    suspend fun getCardIdsByLevel(level: ProgressLevel): List<Long> {
        return withContext(Dispatchers.IO) {
            progressDao.getCardIdsByLevel(level)
        }
    }
}