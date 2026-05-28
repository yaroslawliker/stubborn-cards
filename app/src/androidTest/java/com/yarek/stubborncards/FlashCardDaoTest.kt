package com.yarek.stubborncards

import android.os.Handler
import android.os.Looper
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.yarek.stubborncards.database.AppDatabase
import com.yarek.stubborncards.database.dao.FlashCardDao
import com.yarek.stubborncards.model.FlashCard
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * Instrumented unit tests for FlashCardDao.
 * These tests run on a connected device/emulator.
 */
@RunWith(AndroidJUnit4::class)
class FlashCardDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: FlashCardDao

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        // Create an in-memory database for testing
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.flashCardDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun insertAndRetrieveCard() {
        val card = FlashCard("hello", "привіт")
        dao.insert(card)

        val allCards = dao.getAll().getValueBlocking()
        assert(allCards?.isNotEmpty() == true)
        assert(allCards!![0].word == "hello")
        assert(allCards[0].translation == "привіт")
    }

    @Test
    fun insertMultipleCards() {
        val cards = listOf(
            FlashCard("hello", "привіт"),
            FlashCard("goodbye", "до побачення"),
            FlashCard("thank you", "спасибі")
        )
        dao.insertAll(cards)

        val allCards = dao.getAll().getValueBlocking()
        assert(allCards?.size == 3)
    }

    @Test
    fun updateCard() = runBlocking {
        val card = FlashCard("cat", "кіт")
        val id = dao.insert(card)
        card.id = id

        card.translation = "котик"
        dao.update(card)

        val updated = dao.getById(id).first()

        assert(updated != null)
        assert(updated.translation == "котик")
    }

    @Test
    fun deleteCard() {
        val card = FlashCard("dog", "собака")
        val id = dao.insert(card)
        card.id = id

        dao.delete(card)

        val allCards = dao.getAll().getValueBlocking()
        assert(allCards?.isEmpty() == true)
    }

    @Test
    fun getCardById() = runBlocking {
        val card = FlashCard("water", "вода")
        val id = dao.insert(card)

        val retrieved = dao.getById(id).first()

        assert(retrieved != null)
        assert(retrieved.word == "water")
        assert(retrieved.translation == "вода")
    }
}

/**
 * Helper extension to block on LiveData for testing.
 * Polls for a non-null value or waits for any value with a small delay.
 */
fun <T> androidx.lifecycle.LiveData<T>.getValueBlocking(): T? {
    var result: T? = null
    val latch = CountDownLatch(1)
    var observer: androidx.lifecycle.Observer<T>? = null

    Handler(Looper.getMainLooper()).post {
        observer = androidx.lifecycle.Observer { value ->
            result = value
            latch.countDown()
        }
        observeForever(observer)
    }

    // Wait for first value emission
    latch.await(2, TimeUnit.SECONDS)

    // Give the LiveData a moment to emit any pending changes
    Thread.sleep(200)

    // Remove observer on main thread
    if (observer != null) {
        Handler(Looper.getMainLooper()).post {
            removeObserver(observer)
        }
    }

    return result
}