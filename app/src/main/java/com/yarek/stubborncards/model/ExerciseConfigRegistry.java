package com.yarek.stubborncards.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class ExerciseConfigRegistry {

    // Suppress default constructor to enforce static utility/registry usage
    private ExerciseConfigRegistry() {}

    public static final ExerciseConfig FRESH_MIND;
    public static final ExerciseConfig RECAP;
    public static final ExerciseConfig BALANCED;

    static {
        FRESH_MIND = new ExerciseConfig(
                "fresh_mind",
                "Fresh Mind",
                Map.of(
                        ProgressLevel.NEW_BATCH, 70,
                        ProgressLevel.CLEAN_UP, 20,
                        ProgressLevel.KNOWN, 7,
                        ProgressLevel.LEARNED, 3),
                false
        );

        RECAP = new ExerciseConfig(
                "recap",
                "Daily Recap",
                Map.of(
                        ProgressLevel.CLEAN_UP, 75,
                        ProgressLevel.KNOWN, 20,
                        ProgressLevel.LEARNED, 5),
                true
        );

        BALANCED = new ExerciseConfig(
                "balanced",
                "Balanced",
                Map.of(
                    ProgressLevel.CLEAN_UP, 32,
                    ProgressLevel.KNOWN, 32,
                    ProgressLevel.LEARNED, 32,
                    ProgressLevel.MASTERED, 4),
                true
        );
    }

    public static ExerciseConfig buildSingleCategoryConfig(ProgressLevel level) {
        Map<ProgressLevel, Integer> singleWeight = new HashMap<>();
        singleWeight.put(level, 100);

        return new ExerciseConfig(
                "single_" + level.name().toLowerCase(),
                level.getReadable() + " Focused Drill",
                Collections.unmodifiableMap(singleWeight),
                false
        );
    }
}