package com.yarek.stubborncards.ui.page

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yarek.stubborncards.model.LearningProgress
import com.yarek.stubborncards.model.ProgressLevel
import com.yarek.stubborncards.ui.layout.PagePadding
import com.yarek.stubborncards.ui.theme.Typography
import com.yarek.stubborncards.ui.viewmodel.EditCardViewModel

@Composable
fun EditCardPage(
    onNavigateBack: () -> Unit,
    viewModel: EditCardViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val word by viewModel.wordInput.collectAsState()
    val translation by viewModel.translationInput.collectAsState()

    PagePadding {
        when (val state = uiState) {
            is EditCardViewModel.EditUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is EditCardViewModel.EditUiState.Success -> {
                EditCardContent(
                    word = word,
                    translation = translation,
                    progress = state.progress,
                    maxScore = viewModel.maxScoreLimit,
                    onWordChange = { viewModel.onWordTextChanged(it) },
                    onTranslationChange = { viewModel.onTranslationTextChanged(it) },
                    onSaveClick = { viewModel.saveTextChanges(onNavigateBack) },
                    onLevelSelect = { viewModel.updateLevelInstant(it) },
                    onScoreAdjust = { viewModel.adjustScoreInstant(it) }
                )
            }
            is EditCardViewModel.EditUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = state.message, color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditCardContent(
    word: String,
    translation: String,
    progress: LearningProgress?,
    maxScore: Int,
    onWordChange: (String) -> Unit,
    onTranslationChange: (String) -> Unit,
    onSaveClick: () -> Unit,
    onLevelSelect: (ProgressLevel) -> Unit,
    onScoreAdjust: (Int) -> Unit
) {
    var dropdownExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
            text = "Edit flash-card",
            textAlign = TextAlign.Center,
            style = Typography.titleLarge
        )

        // Flashcard input container (reused custom layout profile wrapper)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(width = 2.dp, color = MaterialTheme.colorScheme.outline, shape = RoundedCornerShape(24.dp))
                .padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CardEmbeddedInputField(label = "Question", placeholder = "Enter the word", value = word, onValueChange = onWordChange)
            DashedDivider(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp))
            CardEmbeddedInputField(label = "Answer", placeholder = "Enter the translation", value = translation, onValueChange = onTranslationChange)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Save Button
        Button(
            onClick = onSaveClick,
            modifier = Modifier.fillMaxWidth().height(55.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Save", style = Typography.titleMedium)
        }

        Spacer(modifier = Modifier.height(20.dp))
        HorizontalDivider(modifier = Modifier.fillMaxWidth(), thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)
        Spacer(modifier = Modifier.height(16.dp))

        // Additional Interactive Properties Section Layout
        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.Start) {
            Text(
                text = "Additional",
                style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // 1. Level Selection Dropdown Row
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Level: ",
                    style = Typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.width(65.dp)
                )

                ExposedDropdownMenuBox(
                    expanded = dropdownExpanded,
                    onExpandedChange = { dropdownExpanded = !dropdownExpanded }
                ) {
                    OutlinedTextField(
                        readOnly = true,
                        value = progress?.level?.readable ?: "Unassigned",
                        onValueChange = {},
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = dropdownExpanded,
                        onDismissRequest = { dropdownExpanded = false }
                    ) {
                        ProgressLevel.entries.forEach { level ->
                            DropdownMenuItem(
                                text = { Text(level.readable) },
                                onClick = {
                                    onLevelSelect(level)
                                    dropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 2. Score Variable Modifier Row Component
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Score: ", style = Typography.bodyLarge.copy(fontWeight = FontWeight.Medium), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(text = "${progress?.score?.toInt() ?: 0}/$maxScore", style = Typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilledTonalIconButton(
                        onClick = { onScoreAdjust(-1) },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Text("-", style = Typography.titleMedium)
                    }
                    FilledTonalIconButton(
                        onClick = { onScoreAdjust(1) },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Text("+", style = Typography.titleMedium)
                    }
                }
            }
        }
    }
}