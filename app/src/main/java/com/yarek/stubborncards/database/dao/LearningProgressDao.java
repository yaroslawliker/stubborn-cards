package com.yarek.stubborncards.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.MapColumn;
import androidx.room.MapInfo;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

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
    Flow<LearningProgress> getProgressForCard(int cardId);

    @Query("SELECT * FROM learning_progress")
    Flow<List<LearningProgress>> getAllProgress();

    @Query("SELECT level, COUNT(id) as count FROM learning_progress GROUP BY level")
    Flow<Map<
            @MapColumn(columnName = "level") ProgressLevel,
            @MapColumn(columnName = "count") Integer
            >> getProgressLevelCounts();
}
