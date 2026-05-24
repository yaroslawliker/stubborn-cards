package com.yarek.stubborncards.ui.page

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.yarek.stubborncards.model.ProgressLevel
import com.yarek.stubborncards.ui.layout.PagePadding
import com.yarek.stubborncards.ui.layout.Page
import com.yarek.stubborncards.ui.theme.Typography

@Composable
fun ExercisesHubPage(navController: NavHostController) {
    // Local memory tracking state for the dynamic "Chosen Level" selector lane
    var selectedLevel by remember { mutableStateOf(ProgressLevel.NEW_BATCH) }

    PagePadding {
        Column(modifier = Modifier.fillMaxSize()) {
            // Screen Header
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                text = "Exercises",
                textAlign = TextAlign.Center,
                style = Typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                item {
                    ExerciseItemCard(
                        description = "Choose a specific single category level you want to focus your drills on exclusively.",
                        buttonText = "${selectedLevel.readable} Focus",
                        onClick = {
                            navController.navigate(
                                Page.ExerciseSession.createRoute(
                                    exerciseId = "single_drill",
                                    categoryName = selectedLevel.name
                                )
                            )
                        },
                        extraContent = {
                            Spacer(modifier = Modifier.height(12.dp))
                            LevelSelectorDropdown(
                                currentLevel = selectedLevel,
                                onLevelSelected = { selectedLevel = it }
                            )
                        }
                    )
                }

                item {
                    ExerciseItemCard(
                        description = "Balanced session: 70% New Batch, 20% Clean Up, 7% Known, and 3% Learned words.",
                        buttonText = "Fresh Mind",
                        onClick = {
                            navController.navigate(Page.ExerciseSession.createRoute(exerciseId = "fresh_mind"))
                        }
                    )
                }

                item {
                    ExerciseItemCard(
                        description = "Focus heavily on short-term memory maintenance and reviewing active Clean-up groups.",
                        buttonText = "Daily Recap",
                        onClick = {
                            navController.navigate(Page.ExerciseSession.createRoute(exerciseId = "recap"))
                        }
                    )
                }

                item {
                    ExerciseItemCard(
                        description = "Dig deep into the vault: Longest neglected cards that are reaching deep storage intervals.",
                        buttonText = "Old Words",
                        onClick = {
                            navController.navigate(Page.ExerciseSession.createRoute(exerciseId = "old_words"))
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ExerciseItemCard(
    description: String,
    buttonText: String,
    onClick: () -> Unit,
    extraContent: @Composable (() -> Unit)? = null
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start
    ) {
        // Descriptive label copy block
        Text(
            text = description,
            style = Typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
        )

        extraContent?.invoke()

        Spacer(modifier = Modifier.height(8.dp))

        // Execution command button designed to match your mockup profile layout
        Button(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp)
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(14.dp)
                ),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        ) {
            Text(
                text = buttonText,
                style = Typography.titleMedium.copy(fontWeight = FontWeight.Medium)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LevelSelectorDropdown(
    currentLevel: ProgressLevel,
    onLevelSelected: (ProgressLevel) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            readOnly = true,
            value = currentLevel.readable,
            onValueChange = {},
            label = { Text("Selected Level Target") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            // Exclude MASTERED by default from standard exercise loops as it represents "learned forever"
            ProgressLevel.entries.filter { it != ProgressLevel.MASTERED }.forEach { level ->
                DropdownMenuItem(
                    text = { Text(level.readable) },
                    onClick = {
                        onLevelSelected(level)
                        expanded = false
                    }
                )
            }
        }
    }
}