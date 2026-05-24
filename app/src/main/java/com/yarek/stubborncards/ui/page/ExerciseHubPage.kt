package com.yarek.stubborncards.ui.page

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
            // Screen Header Title
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 24.dp),
                text = "Exercises",
                textAlign = TextAlign.Center,
                style = Typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(24.dp), // Enhanced spacing for readability
                contentPadding = PaddingValues(bottom = 24.dp)
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
                            Spacer(modifier = Modifier.height(8.dp))
                            LevelSelectorDropdown(
                                currentLevel = selectedLevel,
                                onLevelSelected = { selectedLevel = it }
                            )
                        }
                    )
                }

                item {
                    ExerciseItemCard(
                        description = "Clean up fresh words with occurrences from different levels.",
                        buttonText = "Balanced",
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
        // Descriptive context label block
        Text(
            text = description,
            style = Typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
        )

        extraContent?.invoke()

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        ) {
            Text(
                text = buttonText,
                style = Typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
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
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable, true) // Explicit secure layout anchorage point parameter alignment
                .fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            ProgressLevel.entries.filter { it != ProgressLevel.MASTERED }.forEach { level ->
                DropdownMenuItem(
                    text = { Text(level.readable) },
                    onClick = {
                        onLevelSelected(level)
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
            }
        }
    }
}