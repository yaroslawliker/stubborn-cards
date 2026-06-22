package com.yarek.stubborncards.ui.page

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yarek.stubborncards.model.ProgressLevel
import com.yarek.stubborncards.ui.viewmodel.ExportViewModel

@Composable
fun ExportPage(viewModel: ExportViewModel = viewModel()) {
    val config by viewModel.config.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    // This launcher prompts the user to CHOOSE where to save the generated file
    val createDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        uri?.let { viewModel.exportCsv(it) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Export Settings", style = MaterialTheme.typography.headlineMedium)

        Row(verticalAlignment = Alignment.CenterVertically) {
            Switch(
                checked = config.includeLearningProgress,
                onCheckedChange = { viewModel.updateConfig(config.copy(includeLearningProgress = it)) }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Include Learning Progress (Scores, Dates, Levels)")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text("Select Levels to Export:", style = MaterialTheme.typography.titleMedium)
        
        ProgressLevel.values().forEach { level ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = config.includedLevels.contains(level),
                    onCheckedChange = { checked ->
                        val newLevels = if (checked) {
                            config.includedLevels + level
                        } else {
                            config.includedLevels - level
                        }
                        viewModel.updateConfig(config.copy(includedLevels = newLevels))
                    }
                )
                Text(level.name)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (uiState) {
            is ExportViewModel.ExportUiState.Idle -> {
                Button(
                    onClick = { createDocumentLauncher.launch("stubborn_cards_export.csv") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = config.includedLevels.isNotEmpty() // Prevent empty exports
                ) {
                    Text("Export to CSV")
                }
                
                if (config.includedLevels.isEmpty()) {
                    Text("Please select at least one level to export.", color = MaterialTheme.colorScheme.error)
                }
            }
            is ExportViewModel.ExportUiState.Loading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }
            is ExportViewModel.ExportUiState.Success -> {
                val msg = (uiState as ExportViewModel.ExportUiState.Success).message
                Text(msg, color = MaterialTheme.colorScheme.primary)
                Button(onClick = { viewModel.resetState() }) { Text("Export Another") }
            }
            is ExportViewModel.ExportUiState.Error -> {
                val err = (uiState as ExportViewModel.ExportUiState.Error).message
                Text("Error: $err", color = MaterialTheme.colorScheme.error)
                Button(onClick = { viewModel.resetState() }) { Text("Try Again") }
            }
        }
    }
}