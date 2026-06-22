package com.yarek.stubborncards.model

data class ExportConfig(
    /** Specifies which levels are included into export */
    val includedLevels: List<ProgressLevel>,
    /** Should we include level, score ect */
    val includeLearningProgress: Boolean
)