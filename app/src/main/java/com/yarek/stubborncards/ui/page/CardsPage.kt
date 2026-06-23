package com.yarek.stubborncards.ui.page

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.yarek.stubborncards.model.ProgressLevel
import com.yarek.stubborncards.ui.common.SectionHorizontalDivider
import com.yarek.stubborncards.ui.layout.Page
import com.yarek.stubborncards.ui.layout.PagePadding
import com.yarek.stubborncards.ui.theme.Typography
import com.yarek.stubborncards.ui.viewmodel.CardsViewModel

/**
 * This function represents the page with general info about
 * learner's flash-card library.
 *
 * It lets:
 * - Add a new flash-card
 * - See amount of words in each progress level
 * - See words by a progress level
 */
@Composable
fun CardsPage(
    navController: NavHostController,
    viewModel: CardsViewModel = viewModel()
) {
    val cardCounts by viewModel.progressLevelCounts.collectAsState()

    PagePadding {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            AddFlashCardButton(navController)
            SectionHorizontalDivider()
            ProgressLevelList(
                cardCounts = cardCounts,
                onCategoryClick = { level ->
                    navController.navigate(Page.CategoryWords.createRoute(level.name))
                }
            )
            SectionHorizontalDivider()
            AdditionalOptions(navController, cardCounts, viewModel)

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun AddFlashCardButton(navController: NavHostController) {
    Button(
        onClick = { navController.navigate(Page.AddFlashcard.route) },
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
    ) {
        Text(
            "Add a flash-card",
            style = Typography.titleMedium
        )
    }
}

@Composable
fun ProgressLevelList(
    cardCounts: Map<ProgressLevel, Int>,
    onCategoryClick: (ProgressLevel) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = "Progress levels",
            textAlign = TextAlign.Center,
            style = Typography.titleLarge
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            modifier = Modifier.fillMaxWidth(),
            text = "Your words will transit between this progress levels as you learn them.",
            style = Typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(10.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            ProgressLevel.entries.forEach { level ->
                val amount = cardCounts[level] ?: 0

                ProgressLevelButton(
                    levelName = level.readable,
                    wordAmount = amount.toString(),
                    onClick = { onCategoryClick(level) }
                )
            }
        }
    }
}

@Composable
fun ProgressLevelButton(
    levelName: String,
    wordAmount: String,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(45.dp),
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                levelName,
                style = Typography.titleMedium
            )
            Text(
                wordAmount,
                color = MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun AdditionalOptions(
    navController: NavHostController,
    cardCounts: Map<ProgressLevel, Int>,
    viewModel: CardsViewModel
) {
    var showBatchDialog by remember { mutableStateOf(false) }
    var batchAmountInput by remember { mutableStateOf("10") }

    val newCardsCount = cardCounts[ProgressLevel.NEW] ?: 0
    val activeBatchCount = cardCounts[ProgressLevel.NEW_BATCH] ?: 0

    Text(
        modifier = Modifier.fillMaxWidth(),
        text = "Additional options",
        textAlign = TextAlign.Center,
        style = Typography.titleLarge
    )
    Spacer(modifier = Modifier.height(16.dp))

    // Prepare Batch Button
    Button(
        onClick = { showBatchDialog = true },
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            "Prepare a New Batch",
            style = Typography.titleMedium
        )
    }

    Spacer(modifier = Modifier.height(10.dp))

    ImportExportMenuButton(navController)

    // Batch Preparation Dialog
    if (showBatchDialog) {
        AlertDialog(
            onDismissRequest = { showBatchDialog = false },
            title = {
                Text("Prepare Batch")
            },
            text = {
                Column {
                    Text("There are $newCardsCount words in New (waiting) and $activeBatchCount currently active in New Batch.")
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("How many words from New do you want to move into the active batch?")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = batchAmountInput,
                        onValueChange = { newValue ->
                            // Only allow numbers
                            if (newValue.isEmpty() || newValue.all { it.isDigit() }) {
                                batchAmountInput = newValue
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amount = batchAmountInput.toIntOrNull() ?: 0
                        if (amount > 0) {
                            viewModel.prepareNewBatch(amount)
                        }
                        showBatchDialog = false
                        batchAmountInput = "10"
                    },
                    enabled = (batchAmountInput.toIntOrNull() ?: 0) > 0
                ) {
                    Text("Move Words")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showBatchDialog = false
                        batchAmountInput = "10"
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun ImportExportMenuButton(navController: NavHostController) {
    Button(
        onClick = { navController.navigate(Page.ImportExportMenu.route) },
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            "Import/export menu",
            style = Typography.titleMedium
        )
    }
}