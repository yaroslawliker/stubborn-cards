package com.yarek.stubborncards.model;

/** Utility class for storing flashCard with progress */
public class CardAndProgress {
    private FlashCard flashCard;
    private LearningProgress progress;

    public FlashCard getFlashCard() {return flashCard;}
    public void setFlashCard(FlashCard flashCard) {this.flashCard = flashCard;}

    public LearningProgress getProgress() {return progress;}
    public void setProgress(LearningProgress progress) {this.progress = progress;}
}
