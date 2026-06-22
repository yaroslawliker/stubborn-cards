package com.yarek.stubborncards.engine

import com.yarek.stubborncards.model.CardAndProgress
import com.yarek.stubborncards.model.FlashCard
import com.yarek.stubborncards.model.LearningProgress
import com.yarek.stubborncards.model.ProgressLevel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Duration
import java.time.LocalDateTime

class CsvParserTest {

    private lateinit var parser: CsvParser

    @Before
    fun setUp() {
        parser = CsvParser()
    }

    @Test
    fun testParse_SimpleWordAndTranslation_ParsedCorrectly() {
        val row = mapOf(
            CsvParser.HEADER_WORD to "apple",
            CsvParser.HEADER_TRANSLATION to "яблуко"
        )

        val result = parser.parseImportRow(
            row = row,
            defaultLevel = ProgressLevel.NEW,
            overrideAllLevels = false,
            setMissingDatesToNow = false
        )

        assertNotNull(result)
        assertEquals("apple", result!!.flashCard.word)
        assertEquals("яблуко", result.flashCard.translation)
        assertNotNull(result.progress)
    }

    @Test
    fun testParse_MissingWordOrTranslation_ReturnsNull() {
        val missingWordRow = mapOf(CsvParser.HEADER_TRANSLATION to "яблуко")
        val missingTranslationRow = mapOf(CsvParser.HEADER_WORD to "apple")
        val emptyRow = emptyMap<String, String>()

        assertNull(parser.parseImportRow(missingWordRow, ProgressLevel.NEW, false, false))
        assertNull(parser.parseImportRow(missingTranslationRow, ProgressLevel.NEW, false, false))
        assertNull(parser.parseImportRow(emptyRow, ProgressLevel.NEW, false, false))
    }

    @Test
    fun testParse_ValidLevel_ParsedCorrectly() {
        val row = mapOf(
            CsvParser.HEADER_WORD to "apple",
            CsvParser.HEADER_TRANSLATION to "яблуко",
            CsvParser.HEADER_LEVEL to "MASTERED"
        )

        val result = parser.parseImportRow(row, ProgressLevel.NEW, false, false)

        assertNotNull(result)
        assertEquals(ProgressLevel.MASTERED, result!!.progress!!.level)
    }

    @Test
    fun testParse_NullLevel_ReturnsDefaultLevel() {
        val row = mapOf(
            CsvParser.HEADER_WORD to "apple",
            CsvParser.HEADER_TRANSLATION to "яблуко"
            // Level is omitted
        )

        val result = parser.parseImportRow(row, ProgressLevel.KNOWN, false, false)

        assertNotNull(result)
        // Should fall back to the configured default level
        assertEquals(ProgressLevel.KNOWN, result!!.progress!!.level)
    }

    @Test
    fun testParse_InvalidLevel_ReturnsDefaultLevel() {
        val row = mapOf(
            CsvParser.HEADER_WORD to "apple",
            CsvParser.HEADER_TRANSLATION to "яблуко",
            CsvParser.HEADER_LEVEL to "super-mastered" // Invalid enum
        )

        val result = parser.parseImportRow(row, ProgressLevel.LEARNED, false, false)

        assertNotNull(result)
        // Should catch the exception and fall back to default
        assertEquals(ProgressLevel.LEARNED, result!!.progress!!.level)
    }

    @Test
    fun testParse_IsOnReviewTrue_ParsedCorrectly() {
        val row = mapOf(
            CsvParser.HEADER_WORD to "apple",
            CsvParser.HEADER_TRANSLATION to "яблуко",
            CsvParser.HEADER_IS_ON_REVIEW to "true"
        )

        val result = parser.parseImportRow(row, ProgressLevel.NEW, false, false)

        assertNotNull(result)
        assertTrue(result!!.progress!!.isOnReview)
    }

    @Test
    fun testParse_IsOnReviewMissing_DefaultsToFalse() {
        val row = mapOf(
            CsvParser.HEADER_WORD to "apple",
            CsvParser.HEADER_TRANSLATION to "яблуко"
            // is_on_review is omitted
        )

        val result = parser.parseImportRow(row, ProgressLevel.NEW, false, false)

        assertNotNull(result)
        assertFalse(result!!.progress!!.isOnReview)
    }

    @Test
    fun testParse_ValidScore_ParsedCorrectly() {
        val row = mapOf(
            CsvParser.HEADER_WORD to "apple",
            CsvParser.HEADER_TRANSLATION to "яблуко",
            CsvParser.HEADER_SCORE to "4.5"
        )

        val result = parser.parseImportRow(row, ProgressLevel.NEW, false, false)

        assertNotNull(result)
        assertEquals(4.5f, result!!.progress!!.score, 0.001f) // delta required for float comparison
    }

    @Test
    fun testParse_MissingScore_DefaultsToZero() {
        val row = mapOf(
            CsvParser.HEADER_WORD to "apple",
            CsvParser.HEADER_TRANSLATION to "яблуко"
            // Score is omitted
        )

        val result = parser.parseImportRow(row, ProgressLevel.NEW, false, false)

        assertNotNull(result)
        assertEquals(0.0f, result!!.progress!!.score, 0.001f)
    }

    @Test
    fun testParse_ValidDate_ParsedCorrectly() {
        val expectedDateString = "2024-05-12T10:15:30"
        val expectedDate = LocalDateTime.parse(expectedDateString)

        val row = mapOf(
            CsvParser.HEADER_WORD to "apple",
            CsvParser.HEADER_TRANSLATION to "яблуко",
            CsvParser.HEADER_LAST_REVIEWED to expectedDateString
        )

        val result = parser.parseImportRow(row, ProgressLevel.NEW, false, false)

        assertNotNull(result)
        assertEquals(expectedDate, result!!.progress!!.lastReviewed)
    }

    @Test
    fun testParse_MissingDateWithSetToNowConfigured_SetsToCurrentTime() {
        val row = mapOf(
            CsvParser.HEADER_WORD to "apple",
            CsvParser.HEADER_TRANSLATION to "яблуко"
            // Date is omitted
        )

        val timeBeforeExecution = LocalDateTime.now()

        val result = parser.parseImportRow(
            row = row,
            defaultLevel = ProgressLevel.NEW,
            overrideAllLevels = false,
            setMissingDatesToNow = true // Trigger the fallback logic
        )

        assertNotNull(result)
        val parsedDate = result!!.progress!!.lastReviewed
        assertNotNull("Date should not be null when setToNowIfMissing is true", parsedDate)

        // Check that the parsed date is extremely close to 'now' (within 5 seconds)
        // This avoids test flakiness while ensuring the logic executed correctly
        val diffInSeconds = Duration.between(timeBeforeExecution, parsedDate).abs().seconds
        assertTrue("Parsed date should be close to current time", diffInSeconds < 5)
    }

    @Test
    fun testParse_MissingDateWithoutSetToNowConfigured_RemainsNull() {
        val row = mapOf(
            CsvParser.HEADER_WORD to "apple",
            CsvParser.HEADER_TRANSLATION to "яблуко"
            // Date is omitted
        )

        val result = parser.parseImportRow(
            row = row,
            defaultLevel = ProgressLevel.NEW,
            overrideAllLevels = false,
            setMissingDatesToNow = false // Do not use fallback
        )

        assertNotNull(result)
        assertNull(result!!.progress!!.lastReviewed)
    }

    @Test
    fun testExport_GetHeaders_WithProgress_ReturnsAllHeaders() {
        val headers = parser.getHeaders(includeProgress = true)
        assertEquals(6, headers.size)
        assertEquals(
            listOf(
                CsvParser.HEADER_WORD,
                CsvParser.HEADER_TRANSLATION,
                CsvParser.HEADER_LEVEL,
                CsvParser.HEADER_SCORE,
                CsvParser.HEADER_IS_ON_REVIEW,
                CsvParser.HEADER_LAST_REVIEWED
            ),
            headers
        )
    }

    @Test
    fun testExport_GetHeaders_WithoutProgress_ReturnsBasicHeaders() {
        val headers = parser.getHeaders(includeProgress = false)
        assertEquals(2, headers.size)
        assertEquals(listOf(CsvParser.HEADER_WORD, CsvParser.HEADER_TRANSLATION), headers)
    }

    @Test
    fun testExport_FormatRow_WithProgress_FormatsCorrectly() {
        val dateString = "2024-05-12T10:15:30"
        val date = LocalDateTime.parse(dateString)
        val flashCard = FlashCard("apple", "яблуко")
        val progress = LearningProgress(4.5f, ProgressLevel.KNOWN, true, date)

        // Mocking CardAndProgress structure
        val cardAndProgress = CardAndProgress()
        cardAndProgress.flashCard = flashCard
        cardAndProgress.progress = progress

        val row = parser.formatExportRow(cardAndProgress, includeProgress = true)

        assertEquals(6, row.size)
        assertEquals("apple", row[0])
        assertEquals("яблуко", row[1])
        assertEquals("KNOWN", row[2])
        assertEquals("4.5", row[3])
        assertEquals("true", row[4])
        assertEquals(dateString, row[5])
    }

    @Test
    fun testExport_FormatRow_WithoutProgress_FormatsBasicCorrectly() {
        val flashCard = FlashCard("apple", "яблуко")
        val progress = LearningProgress(4.5f, ProgressLevel.KNOWN, true, LocalDateTime.now())

        val cardAndProgress = CardAndProgress()
        cardAndProgress.flashCard = flashCard
        cardAndProgress.progress = progress

        val row = parser.formatExportRow(cardAndProgress, includeProgress = false)

        assertEquals(2, row.size)
        assertEquals("apple", row[0])
        assertEquals("яблуко", row[1])
    }

    @Test
    fun testExport_FormatRow_WithNullProgress_HandlesGracefully() {
        val flashCard = FlashCard("apple", "яблуко")

        val cardAndProgress = CardAndProgress()
        cardAndProgress.flashCard = flashCard
        cardAndProgress.progress = null // Simulating missing progress

        val row = parser.formatExportRow(cardAndProgress, includeProgress = true)

        assertEquals(6, row.size)
        assertEquals("apple", row[0])
        assertEquals("яблуко", row[1])
        assertEquals("NEW", row[2]) // Defaults to NEW
        assertEquals("0.0", row[3]) // Defaults to 0.0
        assertEquals("false", row[4]) // Defaults to false
        assertEquals("", row[5]) // Defaults to empty string
    }
}