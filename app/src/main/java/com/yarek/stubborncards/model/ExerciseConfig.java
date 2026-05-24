package com.yarek.stubborncards.model;

import java.util.Map;

public class ExerciseConfig {
    private final String id;
    private final String name;
    private final Map<ProgressLevel, Integer> levelWeights;
    private final boolean skipIneffective;

    public ExerciseConfig(String id, String name,
                          Map<ProgressLevel, Integer> levelWeights,
                          boolean skipIneffective) {
        this.id = id;
        this.name = name;
        this.levelWeights = levelWeights;
        this.skipIneffective = skipIneffective;
    }

    public ExerciseConfig(String id, String name, Map<ProgressLevel, Integer> levelWeights) {
        this(id, name, levelWeights, true);
    }

    // --- Getters ---

    public String getId() { return id; }
    public String getName() { return name; }
    public Map<ProgressLevel, Integer> getLevelWeights() { return levelWeights; }
    public boolean getSkipIneffective() { return skipIneffective; }
}