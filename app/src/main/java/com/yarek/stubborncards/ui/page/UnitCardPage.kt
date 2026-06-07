package com.yarek.stubborncards.ui.page

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.yarek.stubborncards.model.FlashCard
import com.yarek.stubborncards.model.LearningProgress
import com.yarek.stubborncards.ui.common.InteractiveFlashCard
import com.yarek.stubborncards.ui.layout.Page
import com.yarek.stubborncards.ui.layout.PagePadding
import com.yarek.stubborncards.ui.theme.Typography
import com.yarek.stubborncards.ui.viewmodel.UnitCardViewModel

@Composable
fun UnitCardPage(
    navController: NavHostController,
    viewModel: UnitCardViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    PagePadding {
        when (val state = uiState) {
            is UnitCardViewModel.UiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is UnitCardViewModel.UiState.Success -> {
                CardDetailsContent(
                    navController,
                    card = state.card,
                    progress = state.progress,
                    hasPrevious = state.hasPrevious,
                    hasNext = state.hasNext,
                    requiredScore = state.requiredScore,
                    onPreviousClick = { viewModel.navigateToPreviousCard() },
                    onNextClick = { viewModel.navigateToNextCard() }
                )
            }
            is UnitCardViewModel.UiState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = state.message, color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
fun CardDetailsContent(
    navController: NavHostController,
    card: FlashCard,
    progress: LearningProgress?,
    hasPrevious: Boolean,
    hasNext: Boolean,
    requiredScore: Int,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilledTonalIconButton(
                onClick = onPreviousClick,
                enabled = hasPrevious,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.size(45.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Previous Card")
            }

            Text(
                text = "Flash-card",
                style = Typography.titleLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )

            FilledTonalIconButton(
                onClick = onNextClick,
                enabled = hasNext,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.size(45.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Next Card")
            }
        }

        InteractiveFlashCard(
            word = card.word,
            translation = card.translation,
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { navController.navigate(Page.EditCard.createRoute(card.id)) },
            modifier = Modifier.fillMaxWidth().height(55.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(text = "Edit", style = Typography.titleMedium)
        }

        Spacer(modifier = Modifier.height(20.dp))
        HorizontalDivider(modifier = Modifier.fillMaxWidth(), thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)
        Spacer(modifier = Modifier.height(16.dp))

        // Additional Information Info Block View Canvas
        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.Start) {
            Text(
                text = "Additional",
                style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(bottom = 12.dp)
            )

            InfoRow(label = "Level:", value = progress?.level?.readable ?: "Unassigned")
            InfoRow(
                label = "Score:",
                value = "${String.format("%.1f", progress?.score ?: 0f)}/${requiredScore}"
            )
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
    ) {
        Text(text = "$label ", style = Typography.bodyLarge.copy(fontWeight = FontWeight.Medium), color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = Typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
    }
}