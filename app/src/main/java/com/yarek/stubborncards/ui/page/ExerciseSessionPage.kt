package com.yarek.stubborncards.ui.page

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.yarek.stubborncards.engine.PromotionEngine
import com.yarek.stubborncards.model.FlashCard
import com.yarek.stubborncards.ui.layout.PagePadding
import com.yarek.stubborncards.ui.theme.Typography
import com.yarek.stubborncards.ui.viewmodel.ExerciseViewModel

@Composable
fun ExerciseSessionPage(
    navController: NavHostController,
    viewModel: ExerciseViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    PagePadding {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            when (val state = uiState) {
                is ExerciseViewModel.ExerciseUiState.Loading -> {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }

                is ExerciseViewModel.ExerciseUiState.EffectivenessWarning -> {
                    EffectivenessWarningComponent(
                        message = state.message,
                        onProceed = { state.onProceed() },
                        onBack = { navController.popBackStack() }
                    )
                }

                is ExerciseViewModel.ExerciseUiState.PresentCard -> {
                    ActiveWorkoutComponent(
                        card = state.card,
                        onAnswerSubmitted = { result -> viewModel.submitAnswer(result) }
                    )
                }

                is ExerciseViewModel.ExerciseUiState.Finished -> {
                    SessionFinishedComponent(
                        summaryText = state.summary,
                        onExit = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}

@Composable
fun ActiveWorkoutComponent(
    card: FlashCard,
    onAnswerSubmitted: (PromotionEngine.ReviewResult) -> Unit
) {
    var isFlipped by remember(card.id) { mutableStateOf(false) }
    var hasBeenFlipped by remember(card.id) { mutableStateOf(false) }

    val rotationAnimation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(durationMillis = 400),
        label = "CardFlipAnimation"
    )

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Exercise Session",
            style = Typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(top = 8.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 16.dp)
                .graphicsLayer {
                    rotationY = rotationAnimation
                    cameraDistance = 8 * density
                }
                .clickable {
                    isFlipped = !isFlipped
                    hasBeenFlipped = true
                },
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
            )
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                if (rotationAnimation <= 90f) {
                    Text(
                        text = card.word,
                        style = Typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(24.dp)
                    )
                } else {
                    Text(
                        text = card.translation,
                        style = Typography.headlineMedium.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .padding(24.dp)
                            .graphicsLayer { rotationY = 180f } // Mirror text back to readable direction
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        AnimatedVisibility(
            visible = hasBeenFlipped,
            enter = fadeIn(animationSpec = tween(300)),
            exit = fadeOut()
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "How did you get it?",
                    style = Typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 8.dp, end = 8.dp, bottom = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { onAnswerSubmitted(PromotionEngine.ReviewResult.WRONG) },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        modifier = Modifier.weight(1f).height(50.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Wrong", color = MaterialTheme.colorScheme.onErrorContainer, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { onAnswerSubmitted(PromotionEngine.ReviewResult.ALMOST_CORRECT) },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
                        modifier = Modifier.weight(1f).height(50.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Almost", color = MaterialTheme.colorScheme.onTertiaryContainer, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { onAnswerSubmitted(PromotionEngine.ReviewResult.CORRECT) },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                        modifier = Modifier.weight(1f).height(50.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Right", color = MaterialTheme.colorScheme.onSecondaryContainer, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun EffectivenessWarningComponent(
    message: String,
    onProceed: () -> Unit,
    onBack: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onBack,
        title = { Text("Exercise Efficiency Note") },
        text = { Text(message) },
        confirmButton = {
            Button(onClick = onProceed) { Text("Proceed Anyway") }
        },
        dismissButton = {
            OutlinedButton(onClick = onBack) { Text("Go Back") }
        }
    )
}

@Composable
fun SessionFinishedComponent(
    summaryText: String,
    onExit: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = summaryText,
            style = Typography.headlineSmall,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        Button(
            onClick = onExit,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Repeat the exercise")
        }
    }
}