package com.yarek.stubborncards.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.yarek.stubborncards.database.dao.FlashCardDao;
import com.yarek.stubborncards.database.dao.ImportExportDao;
import com.yarek.stubborncards.database.dao.LearningProgressDao;
import com.yarek.stubborncards.model.FlashCard;
import com.yarek.stubborncards.model.LearningProgress;

@Database(
        entities = {
                FlashCard.class,
                LearningProgress.class
        },
        version = 4,
        exportSchema = false)
@TypeConverters({RoomConverters.class})
public abstract class AppDatabase extends RoomDatabase {

    public abstract FlashCardDao flashCardDao();
    public abstract LearningProgressDao learningProgressDao();
    public abstract ImportExportDao importExportDao();

    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getInstance(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "stubborn_cards.db")
                            // TODO: remove destructive migrations on prod
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}