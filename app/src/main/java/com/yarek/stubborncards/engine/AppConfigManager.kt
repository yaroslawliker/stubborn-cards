package com.yarek.stubborncards.config

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.yarek.stubborncards.engine.ProgressLevelConfig
import com.yarek.stubborncards.model.ProgressLevel
import kotlin.collections.Map
import androidx.core.content.edit

/**
 * Manages application configuration settings.
 * For now the only setting is promotion table.
 */
class AppConfigManager private constructor(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences(
        "settings", Context.MODE_PRIVATE)
    private val gson = Gson()

    /** Name of the promotion table preference */
    private val TABLE_PREF_NAME = "custom_promotion_table"

    private val defaultPromotionTable: Map<ProgressLevel, ProgressLevelConfig> = mapOf(
        ProgressLevel.NEW to ProgressLevelConfig(5, 2, 20),
        ProgressLevel.NEW_BATCH to ProgressLevelConfig(5, 2, 20),
        ProgressLevel.CLEAN_UP to ProgressLevelConfig(5, 600, 64800),
        ProgressLevel.KNOWN to ProgressLevelConfig(8, 172800, 604800),
        ProgressLevel.LEARNED to ProgressLevelConfig(4, 3888000, 15552000),
        ProgressLevel.MASTERED to ProgressLevelConfig(999, 31536000, 31536000)
    )

    var currentPromotionTable: Map<ProgressLevel, ProgressLevelConfig> = emptyMap()
        private set

    init {
        loadPromotionTable()
    }

    companion object {
        @Volatile private var INSTANCE: AppConfigManager? = null

        fun initialize(context: Context): AppConfigManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AppConfigManager(context).also { INSTANCE = it }
            }
        }

        fun getInstance(): AppConfigManager {
            return INSTANCE ?: throw IllegalStateException("AppConfigManager must be initialized in Application class")
        }
    }

    /**
     * Reads custom JSON configuration from disk storage.
     * Falls back to standard hardcoded Engine profiles if null.
     */
    fun loadPromotionTable() {
        val jsonString = prefs.getString(TABLE_PREF_NAME, null)
        currentPromotionTable = if (!jsonString.isNullOrBlank()) {
            parseJson(jsonString)
        } else {
            defaultPromotionTable
        }
    }

    /** Serializes customized data structures down to disk storage */
    fun savePromotionTable(newConfig: Map<ProgressLevel, ProgressLevelConfig>) {
        currentPromotionTable = newConfig
        val jsonString = gson.toJson(newConfig)
        prefs.edit { putString(TABLE_PREF_NAME, jsonString) }
    }

    /** Resets the promotion table to its default values */
    fun resetPromotionTableToDefaults() {
        prefs.edit { remove(TABLE_PREF_NAME) }
        currentPromotionTable = defaultPromotionTable
    }

    private fun parseJson(json: String): Map<ProgressLevel, ProgressLevelConfig> {
        // Magic thing, search online: Super Type Token
        val type = object : TypeToken<Map<ProgressLevel, ProgressLevelConfig>>() {}.type
        return gson.fromJson(json, type)
    }
}