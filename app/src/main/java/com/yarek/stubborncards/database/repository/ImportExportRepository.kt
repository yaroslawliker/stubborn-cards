package com.yarek.stubborncards.database.repository

import android.app.Application
import androidx.room.withTransaction
import com.yarek.stubborncards.database.AppDatabase
import com.yarek.stubborncards.database.dao.ImportExportDao
import com.yarek.stubborncards.model.CardAndProgress
import com.yarek.stubborncards.model.FlashCard
import com.yarek.stubborncards.model.LearningProgress
import com.yarek.stubborncards.model.ProgressLevel

class ImportExportRepository(application: Application) {

    private val db = AppDatabase.getInstance(application)
    private val dao: ImportExportDao = db.importExportDao()

    suspend fun getWordsForExport(
        levels: List<ProgressLevel>, limit: Int, offset: Int): List<CardAndProgress> {
        return dao.getExportCardsByLevels(levels, limit, offset)
    }

    /**
     * Handles the complex logic of separating new words from existing words,
     * and assigning the correct Foreign Keys before saving to the DB.
     * Wrapped in a transaction so if a batch fails, it rolls back safely.
     */
    suspend fun processImportBatch(
        parsedCards: List<CardAndProgress>,
        updateDuplicates: Boolean
    ) {
        db.withTransaction {
            val incomingWords = parsedCards.map { it.flashCard.word }
            
            // Find which words already exist in the DB
            val existingCards = dao.getFlashCardsByWords(incomingWords)
            val existingWordsMap = existingCards.associateBy { it.word }

            val cardsToInsert = mutableListOf<FlashCard>()
            val progressToInsertForNewCards = mutableListOf<LearningProgress>() // Wait for generated IDs
            
            val cardsToUpdate = mutableListOf<FlashCard>()

            // Sort into Insert vs Update buckets
            for (parsed in parsedCards) {
                val existingCard = existingWordsMap[parsed.flashCard.word]

                if (existingCard != null && updateDuplicates) {
                    existingCard.translation = parsed.flashCard.translation
                    cardsToUpdate.add(existingCard)
                } else {
                    cardsToInsert.add(parsed.flashCard)
                    progressToInsertForNewCards.add(parsed.progress!!)
                }
            }

            // Execute Updates (For Overwrite)
            if (cardsToUpdate.isNotEmpty()) {
                dao.updateFlashCards(cardsToUpdate)
            }

            // Execute Inserts (For New Words)
            if (cardsToInsert.isNotEmpty()) {
                // insertFlashCards returns a list of the newly generated Row IDs!
                val generatedIds = dao.insertFlashCards(cardsToInsert)

                // Now we map those new IDs to their corresponding LearningProgress objects
                val finalProgressList = progressToInsertForNewCards.mapIndexed { index, progress ->
                    progress.apply { flashCardId = generatedIds[index] }
                }

                dao.insertLearningProgresses(finalProgressList)
            }
        }
    }

    suspend fun deleteAllCards() {
        db.withTransaction {
            // Delete progress first to avoid foreign key constraint violations
            dao.deleteAllLearningProgress()
            dao.deleteAllFlashCards()
        }
    }
}