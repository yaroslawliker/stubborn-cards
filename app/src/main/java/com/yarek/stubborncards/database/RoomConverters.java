package com.yarek.stubborncards.database;

import androidx.annotation.Nullable;
import androidx.room.TypeConverter;
import com.yarek.stubborncards.model.ProgressLevel;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class RoomConverters {

    // --- LocalDateTime to Unix Epoch Seconds (Long) ---

    @TypeConverter
    @Nullable
    public static Long fromLocalDateTime(@Nullable LocalDateTime dateTime) {
        return dateTime == null ? null : dateTime.toEpochSecond(ZoneOffset.UTC);
    }

    @TypeConverter
    @Nullable
    public static LocalDateTime toLocalDateTime(@Nullable Long epochSeconds) {
        return epochSeconds == null ? null : LocalDateTime.ofEpochSecond(epochSeconds, 0, ZoneOffset.UTC);
    }

    // --- ProgressLevel Enum Converters ---

    @TypeConverter
    @Nullable
    public static String fromProgressLevel(ProgressLevel level) {
        return level == null ? null : level.name();
    }

    @TypeConverter
    @Nullable
    public static ProgressLevel toProgressLevel(String levelName) {
        return levelName == null ? null : ProgressLevel.valueOf(levelName);
    }
}