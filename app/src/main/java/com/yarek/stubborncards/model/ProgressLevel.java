package com.yarek.stubborncards.model;

/**
 * Represents the level of how well the user knows the word (flash-card).
 */
public enum ProgressLevel {
    /** Thw word is new to a user, he maybe saw it a few times */
    NEW("New"),

    /** Technical state. Same as new, but included in the current
     * learning list.
     * Made to avoid learning to much words at once.
     * Typically, user may have 100 new words, but only 10-15 of them
     * are going to be reviewed.
     */
    NEW_BATCH("New (active)"),

    /** The word is in learners short memory */
    CLEAN_UP("Clean up"),

    /** The word is known to the learner, but may be forgotten in a week. */
    KNOWN("Known"),

    /** The word is learnt, so it should not be reviewed frequently */
    LEARNED("Learned"),

    /** The word is learned forever.
     * Usually used for simple words that user won't forget at all (like, "a cat", "mother").
     */
    MASTERED("Mastered");

    private final String readable;

    public String getReadable() {
        return readable;
    }

    ProgressLevel(String readable) {
        this.readable = readable;
    }
}
