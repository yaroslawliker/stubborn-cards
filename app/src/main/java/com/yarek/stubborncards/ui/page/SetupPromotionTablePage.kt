package com.yarek.stubborncards.ui.page

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.yarek.stubborncards.config.AppConfigManager
import com.yarek.stubborncards.engine.ProgressLevelConfig
import com.yarek.stubborncards.model.ProgressLevel
import kotlinx.coroutines.launch

// Helper Enum representing human-friendly interval steps
enum class TimeUnit(val label: String, val secondsMultiplier: Long) {
    SECONDS("Sec", 1L),
    MINUTES("Min", 60L),
    HOURS("Hours", 3600L),
    DAYS("Days", 86400L);

    companion object {
        fun fromSeconds(seconds: Long): Pair<String, TimeUnit> {
            return when {
                seconds <= 0L -> "0" to SECONDS
                seconds % 86400L == 0L -> (seconds / 86400L).toString() to DAYS
                seconds % 3600L == 0L -> (seconds / 3600L).toString() to HOURS
                seconds % 60L == 0L -> (seconds / 60L).toString() to MINUTES
                else -> seconds.toString() to SECONDS
            }
        }
    }
}

/** Page helps manage promotion table constants */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetupPromotionTablePage() {
    val configManager = remember { AppConfigManager.getInstance() }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollState = rememberScrollState()

    val editableConfig = remember {
        mutableStateMapOf<ProgressLevel, ProgressLevelConfig>().apply {
            putAll(configManager.currentPromotionTable)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Interval Settings", fontWeight = FontWeight.Bold) }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // --- Action Buttons ---
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        configManager.resetPromotionTableToDefaults()
                        editableConfig.clear()
                        editableConfig.putAll(configManager.currentPromotionTable)
                        scope.launch { snackbarHostState.showSnackbar("Reset to factory defaults") }
                    },
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Reset Defaults")
                }

                Button(
                    onClick = {
                        configManager.savePromotionTable(editableConfig)
                        scope.launch { snackbarHostState.showSnackbar("Settings saved successfully!") }
                    },
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Save Changes")
                }
            }

            ProgressLevel.entries.forEach { level ->
                val currentConfig = editableConfig[level] ?: ProgressLevelConfig(5, 2, 20)

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(12.dp)
                        ),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = level.readable,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        // --- REQUIRED SCORE SECTION ---
                        var scoreText by remember(currentConfig.requiredScore) {
                            val displayStr = if (currentConfig.requiredScore % 1f == 0f) {
                                currentConfig.requiredScore.toInt().toString()
                            } else {
                                currentConfig.requiredScore.toString()
                            }
                            mutableStateOf(displayStr)
                        }

                        OutlinedTextField(
                            value = scoreText,
                            onValueChange = { newValue ->
                                scoreText = newValue

                                val score = newValue.toIntOrNull() ?: 0
                                editableConfig[level] = currentConfig.copy(requiredScore = score)
                            },
                            label = { Text("Required Target Score") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // --- OPTIMAL INTERVAL SECTION ---
                        UnitAwareInput(
                            label = "Optimal Interval",
                            initialSeconds = currentConfig.optimalIntervalSeconds,
                            onSecondsChanged = { newSec ->
                                editableConfig[level] = currentConfig.copy(optimalIntervalSeconds = newSec)
                            }
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // --- TEST INTERVAL SECTION ---
                        UnitAwareInput(
                            label = "Test Lockout",
                            initialSeconds = currentConfig.testIntervalSeconds,
                            onSecondsChanged = { newSec ->
                                editableConfig[level] = currentConfig.copy(testIntervalSeconds = newSec)
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnitAwareInput(
    label: String,
    initialSeconds: Long,
    onSecondsChanged: (Long) -> Unit
) {
    val (initialValue, initialUnit) = TimeUnit.fromSeconds(initialSeconds)

    var textValue by remember(initialSeconds) { mutableStateOf(initialValue) }
    var selectedUnit by remember(initialSeconds) { mutableStateOf(initialUnit) }
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Numeric Input Field
        OutlinedTextField(
            value = textValue,
            onValueChange = {
                textValue = it
                val numeric = it.toLongOrNull() ?: 0L
                onSecondsChanged(numeric * selectedUnit.secondsMultiplier)
            },
            label = { Text(label) },
            modifier = Modifier.weight(1f),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true
        )

        // Dropdown Unit Selector
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier.width(150.dp)
        ) {
            OutlinedTextField(
                value = selectedUnit.label,
                onValueChange = {},
                readOnly = true,
                label = { Text("Unit") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                modifier = Modifier.menuAnchor()
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                TimeUnit.entries.forEach { unit ->
                    DropdownMenuItem(
                        text = { Text(unit.label) },
                        onClick = {
                            selectedUnit = unit
                            expanded = false
                            val numeric = textValue.toLongOrNull() ?: 0L
                            onSecondsChanged(numeric * unit.secondsMultiplier)
                        }
                    )
                }
            }
        }
    }
}