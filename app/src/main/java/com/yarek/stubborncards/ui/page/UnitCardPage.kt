package com.yarek.stubborncards.ui.page

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yarek.stubborncards.model.FlashCard
import com.yarek.stubborncards.model.LearningProgress
import com.yarek.stubborncards.ui.layout.PagePadding
import com.yarek.stubborncards.ui.theme.Typography
import com.yarek.stubborncards.ui.viewmodel.UnitCardViewModel
import kotlinx.coroutines.launch

@Composable
fun UnitCardPage(
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
                    card = state.card,
                    progress = state.progress,
                    hasPrevious = state.hasPrevious,
                    hasNext = state.hasNext,
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
    card: FlashCard,
    progress: LearningProgress?,
    hasPrevious: Boolean,
    hasNext: Boolean,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit
) {
    // We key remember to card.id so that flipping resets when navigating between different words
    var showTranslation by remember(card.id) { mutableStateOf(false) }
    var isPressed by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 1.05f else 1.0f,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = 300f),
        label = "CardScaleAnimation"
    )

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // Dynamic Title row with Mockup Navigation Switches
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilledTonalIconButton(
                onClick = onPreviousClick,
                enabled = hasPrevious,
                shape = RoundedCornerShape(12.dp), // Distinct square-ish look from mockup
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

        // Flash-card Interactive Presentation Canvas Container
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
                .border(
                    width = 2.dp,
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(24.dp)
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    coroutineScope.launch {
                        isPressed = true
                        showTranslation = !showTranslation
                        kotlinx.coroutines.delay(80)
                        isPressed = false
                    }
                },
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (!showTranslation) {
                    Text(text = "Question", style = Typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = card.word, style = Typography.headlineMedium, textAlign = TextAlign.Center)
                } else {
                    Text(text = "Answer", style = Typography.labelMedium, color = MaterialTheme.colorScheme.secondary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = card.translation, style = Typography.headlineMedium, textAlign = TextAlign.Center)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Dumb Edit Button
        Button(
            onClick = { /* Will implement edit later */ },
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
            InfoRow(label = "Score:", value = "${progress?.score?.toInt() ?: 0}/5")
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