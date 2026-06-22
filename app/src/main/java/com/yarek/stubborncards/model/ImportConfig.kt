package com.yarek.stubborncards.model

data class ImportConfig(
    /** The default progress level to set, if the one is missing */
    val defaultLevel: ProgressLevel = ProgressLevel.NEW,
    /** Treats all levels as missing, changing them with default */
    val overrideAllLevels: Boolean,
    /** Sets last reviewed datetime as now if it's null */
    val defaultLastReviewedAsNow: Boolean,
    /** Existing words are updated if the word matches */
    val updateDuplicates: Boolean = false
)