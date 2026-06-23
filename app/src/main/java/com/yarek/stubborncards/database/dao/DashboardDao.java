package com.yarek.stubborncards.database.dao;

import androidx.room.Dao;
import androidx.room.Query;

import com.yarek.stubborncards.model.ProgressLevel;

@Dao
public interface DashboardDao {

    @Query("SELECT count(*) FROM learning_progress")
    Long countAll();

    @Query("SELECT COUNT(*) FROM learning_progress " +
            "WHERE level = :level " +
            "AND score < :requiredScore " +
            "AND (lastReviewed IS NULL OR lastReviewed + :optimalIntervalSeconds <= :nowSeconds)")
    Integer countReadyToReview(
            ProgressLevel level,
            Long nowSeconds,
            Long optimalIntervalSeconds,
            Integer requiredScore);

    @Query("SELECT COUNT(*) FROM learning_progress " +
            "WHERE level = :level " +
            "AND score >= :requiredScore " +
            "AND (lastReviewed IS NULL OR lastReviewed + :testIntervalSeconds <= :nowSeconds)")
    Integer countReadyToTest(
            ProgressLevel level,
            Long nowSeconds,
            Long testIntervalSeconds,
            Integer requiredScore);

    @Query("SELECT COUNT(*) FROM learning_progress " +
            "WHERE level = :level " +
            "AND score >= :requiredScore " +
            "AND (lastReviewed IS NULL " +
            "OR lastReviewed + :testIntervalSeconds > :nowSeconds)")
    Integer countHiddenBeforeTest(
            ProgressLevel level,
            Long nowSeconds,
            Long testIntervalSeconds,
            Integer requiredScore);
}