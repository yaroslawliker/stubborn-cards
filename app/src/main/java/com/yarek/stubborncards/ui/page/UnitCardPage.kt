package com.yarek.stubborncards.ui.page

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
                CardDetailsContent(card = state.card, progress = state.progress)
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
fun CardDetailsContent(card: FlashCard, progress: LearningProgress?) {
    // State to toggle between word and translation text views
    var showTranslation by remember { mutableStateOf(false) }
    // Scale animation logic state holder variables
    var isPressed by remember { mutableStateOf(false) }

    val coroutineScope = androidx.compose.runtime.rememberCoroutineScope()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 1.05f else 1.0f,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = 300f),
        label = "CardScaleAnimation"
    )

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Page Dynamic Header Text
        Text(
            modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
            text = card.word,
            textAlign = TextAlign.Center,
            style = Typography.titleLarge
        )

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
                    indication = null // Disables default grey ripple to keep custom bounce clean
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