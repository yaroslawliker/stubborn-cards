package com.yarek.stubborncards.model;

import java.time.Duration;
import java.util.EnumMap;
import java.util.Objects;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Table which defines the rules of promotion form one
 * {@link ProgressLevel progress level} to another.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PromotionTable {

    private EnumMap<ProgressLevel, PromotionTableEntry> table;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PromotionTableEntry {
        private float requiredScore;
        private Duration optimalInterval;
        private Duration testInterval;
    }

    public void setEntry(ProgressLevel level, PromotionTableEntry entry) {
        table.put(level, entry);
    }

    public float getRequiredScore(ProgressLevel level) {
        return Objects.requireNonNull(table.get(level)).requiredScore;
    }

    public Duration getOptimalInterval(ProgressLevel level) {
        return Objects.requireNonNull(table.get(level)).optimalInterval;
    }

    public Duration getTestInterval(ProgressLevel level) {
        return Objects.requireNonNull(table.get(level)).testInterval;
    }

}
