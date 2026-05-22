package com.yarek.stubborncards.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.yarek.stubborncards.model.FlashCard;

import java.util.List;

@Dao
public interface FlashCardDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(FlashCard flashCard);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    List<Long> insertAll(List<FlashCard> cards);

    @Update
    void update(FlashCard flashCard);

    @Delete
    void delete(FlashCard flashCard);

    @Query("SELECT * FROM flash_cards ORDER BY id ASC")
    LiveData<List<FlashCard>> getAll();

    @Query("SELECT * FROM flash_cards WHERE id = :id LIMIT 1")
    LiveData<FlashCard> getById(int id);
}