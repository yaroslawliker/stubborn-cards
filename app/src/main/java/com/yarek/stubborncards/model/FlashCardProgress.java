package com.yarek.stubborncards.model;

/** Utility class for storing flashCard with progress */
public class FlashCardProgress extends FlashCard {
    private LearningProgress progress;

    public LearningProgress getProgress() {return progress;}
    public void setProgress(LearningProgress progress) {this.progress = progress;}
}
