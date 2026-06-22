package com.yarek.stubborncards.ui.page

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yarek.stubborncards.model.ProgressLevel
import com.yarek.stubborncards.ui.common.SectionHorizontalDivider
import com.yarek.stubborncards.ui.theme.Typography
import com.yarek.stubborncards.ui.viewmodel.ImportViewModel

@Composable
fun ImportPage(viewModel: ImportViewModel = viewModel()) {
    val config by viewModel.config.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    var dropdownExpanded by remember { mutableStateOf(false) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { viewModel.importCsv(it) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Import Settings", style = MaterialTheme.typography.headlineMedium)

        Text(
            text = "You CSV file must contain 'word' and 'translation' columns.\nIt also may contain " +
                    "'level', 'score', 'last_reviewed' and 'is_on_review' columns.",
            style = Typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
        )

        SectionHorizontalDivider()

        Row(verticalAlignment = Alignment.CenterVertically) {
            Switch(
                checked = config.updateDuplicates,
                onCheckedChange = { viewModel.updateConfig(config.copy(updateDuplicates = it)) }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Update existing words (Overwrite translation)")
        }

        Text("Default Progress Level for missing/new data:")
        Box {
            OutlinedButton(onClick = { dropdownExpanded = true }) {
                Text(config.defaultLevel.name)
            }
            DropdownMenu(
                expanded = dropdownExpanded,
                onDismissRequest = { dropdownExpanded = false }
            ) {
                ProgressLevel.values().forEach { level ->
                    DropdownMenuItem(
                        text = { Text(level.name) },
                        onClick = {
                            viewModel.updateConfig(config.copy(defaultLevel = level))
                            dropdownExpanded = false
                        }
                    )
                }
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Switch(
                checked = config.overrideAllLevels,
                onCheckedChange = { viewModel.updateConfig(config.copy(overrideAllLevels = it)) }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Force all imported levels to Default Level")
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Switch(
                checked = config.defaultLastReviewedAsNow,
                onCheckedChange = { viewModel.updateConfig(config.copy(defaultLastReviewedAsNow = it)) }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Set missing review dates to Now")
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (uiState) {
            is ImportViewModel.ImportUiState.Idle -> {
                Button(
                    onClick = { filePickerLauncher.launch(arrayOf("text/comma-separated-values", "text/csv", "*/*")) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Select CSV File to Import")
                }
            }
            is ImportViewModel.ImportUiState.Loading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }
            is ImportViewModel.ImportUiState.Success -> {
                val msg = (uiState as ImportViewModel.ImportUiState.Success).message
                Text(msg, color = MaterialTheme.colorScheme.primary)
                Button(onClick = { viewModel.resetState() }) { Text("Import Another") }
            }
            is ImportViewModel.ImportUiState.Error -> {
                val err = (uiState as ImportViewModel.ImportUiState.Error).message
                Text("Error: $err", color = MaterialTheme.colorScheme.error)
                Button(onClick = { viewModel.resetState() }) { Text("Try Again") }
            }
        }
    }
}