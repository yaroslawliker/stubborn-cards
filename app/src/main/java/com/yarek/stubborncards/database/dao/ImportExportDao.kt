package com.yarek.stubborncards.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.yarek.stubborncards.model.CardAndProgress
import com.yarek.stubborncards.model.FlashCard
import com.yarek.stubborncards.model.LearningProgress
import com.yarek.stubborncards.model.ProgressLevel

@Dao
public interface ImportExportDao {

    // ==========================================
    // IMPORT METHODS
    // ==========================================

    @Query("SELECT * FROM flash_card WHERE word IN (:words)")
    suspend fun getFlashCardsByWords(words: List<String>): List<FlashCard>

    @Query("SELECT * FROM learning_progress WHERE flashCardId IN (:cardIds)")
    suspend fun getProgressByCardIds(cardIds: List<Long>): List<LearningProgress>

    /**
     * Returns a list of the newly generated Row IDs.
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertFlashCards(cards: List<FlashCard>): List<Long>

    @Update
    suspend fun updateFlashCards(cards: List<FlashCard>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLearningProgresses(progresses: List<LearningProgress>)

    // ==========================================
    // EXPORT METHODS
    // ==========================================

    /**
     * Uses pagination (Limit/Offset) to stream data safely.
     * Uses INNER JOIN and column aliasing to satisfy the @Embedded(prefix = "progress_")
     * requirement in the CardAndProgress POJO.
     */
    @Query("""
        SELECT fc.*, 
               lp.id AS progress_id, 
               lp.score AS progress_score, 
               lp.level AS progress_level, 
               lp.isOnReview AS progress_isOnReview, 
               lp.lastReviewed AS progress_lastReviewed, 
               lp.flashCardId AS progress_flashCardId
        FROM flash_card fc
        INNER JOIN learning_progress lp ON fc.id = lp.flashCardId
        WHERE lp.level IN (:levels)
        ORDER BY fc.id ASC
        LIMIT :limit OFFSET :offset
    """)
    suspend fun getExportCardsByLevels(levels: List<ProgressLevel>, limit: Int, offset: Int): List<CardAndProgress>

    @Query("DELETE FROM learning_progress")
    suspend fun deleteAllLearningProgress()

    // ==========================================
    // DELETE METHOD
    // ==========================================

    @Query("DELETE FROM flash_card")
    suspend fun deleteAllFlashCards()
}