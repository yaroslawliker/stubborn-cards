package com.yarek.stubborncards.ui.page

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.yarek.stubborncards.model.ProgressLevel
import com.yarek.stubborncards.ui.common.SectionHorizontalDivider
import com.yarek.stubborncards.ui.layout.PagePadding
import com.yarek.stubborncards.ui.layout.Page
import com.yarek.stubborncards.ui.theme.Typography

data class ExerciseOption(
    val description: String,
    val buttonText: String,
    val onClick: () -> Unit,
    val buttonIcon: ImageVector? = null,
    val extraContent: @Composable (() -> Unit)? = null
)

@Composable
fun ExercisesHubPage(navController: NavHostController) {
    var selectedLevel by remember { mutableStateOf(ProgressLevel.NEW_BATCH) }

    val exerciseOptions = listOf(
        ExerciseOption(
            description = "Practice your words with AI generated sentences.",
            buttonText = "Context practice",
            buttonIcon = Icons.Default.AutoAwesome,
            onClick = { navController.navigate(Page.AiExerciseSession.route) }
        ),

        ExerciseOption(
            description = "Choose a specific single category level you want to focus on exclusively.",
            buttonText = "${selectedLevel.readable} Focus",
            onClick = {
                navController.navigate(Page.ExerciseSession.createRoute(exerciseId = "single_drill", categoryName = selectedLevel.name))
            },
            extraContent = {
                Spacer(modifier = Modifier.height(8.dp))
                LevelSelectorDropdown(currentLevel = selectedLevel, onLevelSelected = { selectedLevel = it })
            }
        ),

        ExerciseOption(
            description = "Focus on new words with a bit of review",
            buttonText = "Fresh mind",
            onClick = {
                navController.navigate(Page.ExerciseSession.createRoute(exerciseId = "fresh_mind"))
            }
        ),
        ExerciseOption(
            description = "'Clean up' words with a bit of others",
            buttonText = "Daily Recap",
            onClick = {
                navController.navigate(Page.ExerciseSession.createRoute(exerciseId = "recap"))
            }
        ),
        ExerciseOption(
            description = "Review equal amount of words from 'Clean up', 'Known' and 'Learnt'",
            buttonText = "Balanced",
            onClick = {
                navController.navigate(Page.ExerciseSession.createRoute(exerciseId = "balanced"))
            }
        )
    )

    PagePadding {
        Column(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                itemsIndexed(exerciseOptions) { index, option ->
                    ExerciseItemCard(
                        description = option.description,
                        buttonText = option.buttonText,
                        onClick = option.onClick,
                        buttonIcon = option.buttonIcon,
                        extraContent = option.extraContent
                    )
                    if (setOf(0, 1).contains(index)) {
                        SectionHorizontalDivider()
                    } else {
                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    if (index == 1) {
                        Text(
                            modifier = Modifier.fillMaxWidth(),
                            text = "Predefined exercises",
                            textAlign = TextAlign.Center,
                            style = Typography.titleLarge
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
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
    buttonIcon: ImageVector? = null,
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

        Button (
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = buttonText,
                style = Typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
            )

            if (buttonIcon != null) {
                Icon(
                    imageVector = buttonIcon,
                    contentDescription = "AI Icon",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
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
            label = { Text("Selected Progress Level") },
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