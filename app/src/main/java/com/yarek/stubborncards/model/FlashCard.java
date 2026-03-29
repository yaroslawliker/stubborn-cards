package com.yarek.stubborncards.model;


/**
 * This entity represents a flesh-card with word, it's translation and
 * some other data about the word.
 * This class is not aware of learner's progress.
 * This class may be used to exchange dictionaries publicly.
 */
public class FlashCard {
    /** The word a learner want's to memorize */
    String word;
    /** The translation of the word (another side of a flash-card) */
    String translation;

    // Few other fields may be added in future:
    // - transcription
    // - context
    // - difficulty
    // - level
    // - topic
    // ect
}
