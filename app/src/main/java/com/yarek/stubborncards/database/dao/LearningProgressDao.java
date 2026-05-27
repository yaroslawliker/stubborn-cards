package com.yarek.stubborncards.database.dao;

import androidx.annotation.Nullable;
import androidx.paging.PagingSource;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.MapColumn;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.yarek.stubborncards.model.CardAndProgress;
import com.yarek.stubborncards.model.FlashCard;
import com.yarek.stubborncards.model.LearningProgress;
import com.yarek.stubborncards.model.ProgressLevel;

import java.util.List;
import java.util.Map;

import kotlinx.coroutines.flow.Flow;

@Dao
public interface LearningProgressDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Long insert(LearningProgress progress);

    @Query("SELECT * FROM learning_progress WHERE flashCardId = :cardId")
    Flow<LearningProgress> getProgressByCardId(Long cardId);

    @Update
    void update(LearningProgress learningProgress);

    @Query("UPDATE learning_progress SET level = :newLevel WHERE flashCardId = :cardId")
    void updateLevelByCardId(long cardId, ProgressLevel newLevel);

    @Query("UPDATE learning_progress SET score = :newScore WHERE flashCardId = :cardId")
    void updateScoreByCardId(long cardId, float newScore);

    @Query("SELECT * FROM learning_progress")
    Flow<List<LearningProgress>> getAllProgress();

    @Query("SELECT level, COUNT(id) as count FROM learning_progress GROUP BY level")
    Flow<Map<
            @MapColumn(columnName = "level") ProgressLevel,
            @MapColumn(columnName = "count") Integer
            >> getProgressLevelCounts();

    @Query("SELECT fc.* FROM flash_card fc " +
            "INNER JOIN learning_progress lp ON fc.id = lp.flashCardId " +
            "WHERE lp.level = :level AND (:searchQuery IS NULL OR fc.word LIKE :searchQuery) " +
            "ORDER BY lp.lastReviewed ASC")
    PagingSource<Integer, FlashCard> getCardsByLevelPaged(
            ProgressLevel level, @Nullable String searchQuery);

    @Query("SELECT fc.id FROM flash_card fc " +
            "INNER JOIN learning_progress lp ON fc.id = lp.flashCardId " +
            "WHERE lp.level = :level " +
            "ORDER BY lp.lastReviewed ASC")
    List<Long> getCardIdsByLevel(ProgressLevel level);


    /**
     * Fetches the oldest reviewed card rows within a category that are legally ready for review.
     * Excludes cards whose scores match or exceed the target threshold unless their Test Interval lockout has passed.
     * The standard ISO text mapping format maps String dates flawlessly to Room LocalDateTime converters.
     */
    @Query("SELECT fc.*, " +
            "lp.id AS progress_id, " +
            "lp.score AS progress_score, " +
            "lp.level AS progress_level, " +
            "lp.isOnReview AS progress_isOnReview, " +
            "lp.lastReviewed AS progress_lastReviewed, " +
            "lp.flashCardId AS progress_flashCardId " +
            "FROM flash_card fc " +
            "INNER JOIN learning_progress lp ON fc.id = lp.flashCardId " +
            "WHERE lp.level = :level " +
            "AND (lp.score < :reqScore " +
            "OR (lp.score >= :reqScore AND (:nowSeconds - lp.lastReviewed) >= :testIntervalSeconds)) " +
            "ORDER BY lp.lastReviewed ASC LIMIT :limitAmount")
    List<CardAndProgress> getExerciseBatchByLevel(
            ProgressLevel level,
            int reqScore,
            long nowSeconds,
            long testIntervalSeconds,
            int limitAmount
    );

    /**
     * Quick peek tracking query to check the single oldest card in a category,
     * regardless of whether it's locked, to compute exhaustion states.
     */
    @Query("SELECT * FROM learning_progress WHERE level = :level ORDER BY lastReviewed ASC LIMIT 1")
    @Nullable
    LearningProgress peekOldestProgressInLevel(ProgressLevel level);

    @Query("SELECT * FROM learning_progress")
    List<LearningProgress> getAll();

}
