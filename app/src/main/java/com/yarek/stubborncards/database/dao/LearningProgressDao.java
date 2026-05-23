package com.yarek.stubborncards.database.dao;

import androidx.annotation.Nullable;
import androidx.paging.PagingSource;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.MapColumn;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

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
    LearningProgress getProgressByCardId(Long cardId);

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
}
