package com.yarek.stubborncards.model;


import androidx.room.Entity;
import androidx.room.PrimaryKey;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/**
 * This entity represents a flesh-card with word, it's translation and
 * some other data about the word.
 * This class is not aware of learner's progress.
 * This class may be used to exchange dictionaries publicly.
 */
@Entity(tableName = "flash_cards")
public class FlashCard {

    @PrimaryKey(autoGenerate = true)
    private int id;

    /** The word a learner want's to memorize */
    private String word;
    /** The translation of the word (another side of a flash-card) */
    private String translation;

    // Few other fields may be added in future:
    // - transcription
    // - context
    // - difficulty
    // - level
    // - topic
    // ect

    // Constructors
    public FlashCard() {
    }

    public FlashCard(String word, String translation) {
        this.word = word;
        this.translation = translation;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public String getTranslation() {
        return translation;
    }

    public void setTranslation(String translation) {
        this.translation = translation;
    }
}
