package com.yarek.stubborncards.engine

import android.content.Context
import android.net.Uri
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import com.yarek.stubborncards.database.repository.ImportExportRepository
import com.yarek.stubborncards.model.ExportConfig
import com.yarek.stubborncards.model.ImportConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

/**
 * Orchestrates the streaming of CSV data to and from the Android file system.
 * Uses pagination and batching to ensure flat memory usage.
 */
class ImportExportEngine(
    private val context: Context,
    private val repository: ImportExportRepository,
    private val csvParser: CsvParser = CsvParser()
) {

    companion object {
        private const val BATCH_SIZE = 500
    }

    suspend fun exportCsv(uri: Uri, config: ExportConfig) = withContext(Dispatchers.IO) {
        val contentResolver = context.contentResolver

        // Open an output stream to the file the user selected via Storage Access Framework
        contentResolver.openOutputStream(uri)?.use { outputStream ->
            csvWriter().open(outputStream) {

                // Write the headers based on user config
                val headers = csvParser.getHeaders(config.includeLearningProgress)
                writeRow(headers)

                // Stream data out of the database in chunks
                var offset = 0
                while (true) {
                    val batch = runBlocking {
                        repository.getWordsForExport(config.includedLevels, BATCH_SIZE, offset)
                    }

                    if (batch.isEmpty()) {
                        break // We've reached the end of the data
                    }

                    // Format Domain models to CSV strings
                    val csvRows = batch.map { cardAndProgress ->
                        csvParser.formatExportRow(cardAndProgress, config.includeLearningProgress)
                    }

                    writeRows(csvRows)

                    offset += BATCH_SIZE

                    if (batch.size < BATCH_SIZE) {
                        break
                    }
                }
            }
        } ?: throw IllegalStateException("Could not open output stream for URI: $uri")
    }

    suspend fun importCsv(uri: Uri, config: ImportConfig) = withContext(Dispatchers.IO) {
        val contentResolver = context.contentResolver

        // Open an input stream from the file the user selected
        contentResolver.openInputStream(uri)?.use { inputStream ->
            csvReader().open(inputStream) {

                readAllWithHeaderAsSequence().chunked(BATCH_SIZE).forEach { csvBatch ->

                    // Map CSV strings to Domain models
                    val parsedCards = csvBatch.mapNotNull { row ->
                        csvParser.parseImportRow(
                            row = row,
                            defaultLevel = config.defaultLevel,
                            overrideAllLevels = config.overrideAllLevels,
                            setMissingDatesToNow = config.defaultLastReviewedAsNow
                        )
                    }

                    // Send the batch to the Repository for insertion/updating
                    if (parsedCards.isNotEmpty()) {
                        runBlocking {
                            repository.processImportBatch(parsedCards, config.updateDuplicates)
                        }
                    }
                }
            }
        } ?: throw IllegalStateException("Could not open input stream for URI: $uri")
    }
}