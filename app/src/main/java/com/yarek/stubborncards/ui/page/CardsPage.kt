package com.yarek.stubborncards.ui.page

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
        Column {
            AddFlashCardButton(navController)
            SectionHorizontalDivider()
            ProgressLevelList(
                cardCounts = cardCounts,
                onCategoryClick = { level ->
                    navController.navigate(Page.CategoryWords.createRoute(level.name))
                }
            )
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