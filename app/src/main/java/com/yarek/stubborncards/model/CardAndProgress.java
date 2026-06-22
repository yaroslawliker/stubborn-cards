package com.yarek.stubborncards.model;

import androidx.room.Embedded;

/** Utility class for storing flashCard with progress */
public class CardAndProgress {
    @Embedded
    private FlashCard flashCard;
    @Embedded(prefix = "progress_")
    private LearningProgress progress;

    public CardAndProgress() {}

    public CardAndProgress(
            final FlashCard flashCard,
            final LearningProgress progress) {
        this.flashCard = flashCard;
        this.progress = progress;
    }

    public FlashCard getFlashCard() {return flashCard;}
    public void setFlashCard(FlashCard flashCard) {this.flashCard = flashCard;}

    public LearningProgress getProgress() {return progress;}
    public void setProgress(LearningProgress progress) {this.progress = progress;}
}
