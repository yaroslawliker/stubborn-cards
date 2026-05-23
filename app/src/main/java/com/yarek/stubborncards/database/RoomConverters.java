package com.yarek.stubborncards.database;

import androidx.annotation.Nullable;
import androidx.room.TypeConverter;
import com.yarek.stubborncards.model.ProgressLevel;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class RoomConverters {
    
    private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    // --- 1. LocalDateTime Converters ---
    
    @TypeConverter
    @Nullable
    public static String fromLocalDateTime(LocalDateTime dateTime) {
        return dateTime == null ? null : dateTime.format(formatter);
    }

    @TypeConverter
    @Nullable
    public static LocalDateTime toLocalDateTime(String dateTimeString) {
        return dateTimeString == null ? null : LocalDateTime.parse(dateTimeString, formatter);
    }

    // --- 2. ProgressLevel Enum Converters ---
    
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