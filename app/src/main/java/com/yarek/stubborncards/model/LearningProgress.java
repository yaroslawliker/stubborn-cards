package com.yarek.stubborncards.model;

import java.time.LocalDateTime;

/**
 * This class represents the learning progress of learning a specific flash-card
 * of a specific learner.
 *
 */
public class LearningProgress {

    /** A learning score of this word.
     * Usually each score means the word was answered correctly once.
     * Floating score take place when modifiers are applied (i.e. word is learned
     * too early and can't get the full score.
     */
    float score;
    ProgressLevel level;

    /** If the user missed a word of level >= KNOWN, it enters the On Review state */
    boolean isOnReview;

    /** Datetime of the last review session */
    LocalDateTime lastReviewed;
    /** Datetime of the the review session before last */
    LocalDateTime beforeLastReviewed;
}
