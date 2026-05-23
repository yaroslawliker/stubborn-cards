package com.yarek.stubborncards.model;

import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.time.LocalDateTime;

/**
 * This class represents the learning progress of learning a specific flash-card
 * of a specific learner.
 *
 */
@Entity(
        tableName = "learning_progress",
        foreignKeys = @ForeignKey(
                entity = FlashCard.class,
                parentColumns = "id",
                childColumns = "flashCardId",
                onDelete = ForeignKey.CASCADE
        ),
        indices = @Index(value = {"flashCardId"}, unique = true)
)
public class LearningProgress {

    @PrimaryKey(autoGenerate = true)
    private Long id;

    /** A learning score of this word.
     * Usually each score means the word was answered correctly once.
     * Floating score take place when modifiers are applied (i.e. word is learned
     * too early and can't get the full score.
     */
    private float score;
    private ProgressLevel level;
    /** If the user missed a word of level >= KNOWN, it enters the On Review state */
    private boolean isOnReview;
    /** Datetime of the last review session */
    @Nullable
    private LocalDateTime lastReviewed;
    /** Datetime of the the review session before last */
    @Nullable
    private LocalDateTime beforeLastReviewed;
    private Long flashCardId;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getFlashCardId() { return flashCardId; }
    public void setFlashCardId(Long flashCardId) { this.flashCardId = flashCardId; }

    public float getScore() { return score; }
    public void setScore(float score) { this.score = score; }

    public ProgressLevel getLevel() { return level; }
    public void setLevel(ProgressLevel level) { this.level = level; }

    public boolean isOnReview() { return isOnReview; }
    public void setOnReview(boolean onReview) { this.isOnReview = onReview; }

    public LocalDateTime getLastReviewed() { return lastReviewed; }
    public void setLastReviewed(LocalDateTime lastReviewed) { this.lastReviewed = lastReviewed; }

    public LocalDateTime getBeforeLastReviewed() { return beforeLastReviewed; }
    public void setBeforeLastReviewed(LocalDateTime beforeLastReviewed) { this.beforeLastReviewed = beforeLastReviewed; }
}
