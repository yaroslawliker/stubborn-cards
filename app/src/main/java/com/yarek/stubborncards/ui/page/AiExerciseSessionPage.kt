package com.yarek.stubborncards.ui.page

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.yarek.stubborncards.ai.AiCard
import com.yarek.stubborncards.engine.PromotionEngine
import com.yarek.stubborncards.model.CardAndProgress
import com.yarek.stubborncards.ui.layout.PagePadding
import com.yarek.stubborncards.ui.theme.Typography
import com.yarek.stubborncards.ui.viewmodel.AiExerciseViewModel

@Composable
fun AiExerciseSessionPage(
    navController: NavHostController,
    viewModel: AiExerciseViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    PagePadding {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            when (val state = uiState) {
                is AiExerciseViewModel.AiExerciseUiState.Initial -> {
                    AiInitialComponent(
                        onStart = { cleanUp, known, strict ->
                            viewModel.prepareAiCards(cleanUp, known, strict)
                        }
                    )
                }

                is AiExerciseViewModel.AiExerciseUiState.Generating -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("AI is generating practice sentences...", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                is AiExerciseViewModel.AiExerciseUiState.PresentCard -> {
                    AiActiveWorkoutComponent(
                        aiCard = state.aiCard,
                        dbCard = state.dbCard,
                        onAnswerSubmitted = { result, isWeird ->
                            viewModel.submitAnswer(result, isWeird)
                        }
                    )
                }

                is AiExerciseViewModel.AiExerciseUiState.Error -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(state.message, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.prepareAiCards(4, 2, false) }) { Text("Try Again") }
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(onClick = { navController.popBackStack() }) { Text("Go Back") }
                    }
                }

                is AiExerciseViewModel.AiExerciseUiState.Finished -> {
                    SessionFinishedComponent(
                        summaryText = state.summary,
                        onExit = { viewModel.repeatSession() }
                    )
                }
            }
        }
    }
}

@Composable
fun AiInitialComponent(
    onStart: (cleanUpCount: Int, knownCount: Int, strictContext: Boolean) -> Unit
) {
    var isCustomized by remember { mutableStateOf(false) }
    var cleanUpCount by remember { mutableStateOf("4") }
    var knownCount by remember { mutableStateOf("2") }
    var strictContext by remember { mutableStateOf(false) }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "AI Sentence Practice",
            style = Typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Text(
            text = "Generate custom sentences using words you're currently learning.",
            style = Typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp)
        )

        Button(
            onClick = {
                val cuCount = cleanUpCount.toIntOrNull() ?: 4
                val kCount = knownCount.toIntOrNull() ?: 2
                onStart(cuCount, kCount, strictContext)
            },
            modifier = Modifier.fillMaxWidth(0.8f).height(75.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Prepare next AI generated cards", style = Typography.titleMedium)
        }

        // --- Customization Toggle ---
        Row(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .clip(RoundedCornerShape(8.dp))
                .clickable { isCustomized = !isCustomized }
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(checked = isCustomized, onCheckedChange = { isCustomized = it })
            Text("Customize session", style = Typography.bodyLarge, fontWeight = FontWeight.Medium)
        }

        // --- Expanded Settings Panel ---
        AnimatedVisibility(visible = isCustomized) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .padding(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = cleanUpCount,
                    onValueChange = { cleanUpCount = it },
                    label = { Text("Words from Clean Up") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = knownCount,
                    onValueChange = { knownCount = it },
                    label = { Text("Words from Known") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { strictContext = !strictContext }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(checked = strictContext, onCheckedChange = { strictContext = it })
                    Text(
                        text = "Try to use only words I have Learnt or Mastered",
                        style = Typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        if (!isCustomized) {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun AiActiveWorkoutComponent(
    aiCard: AiCard,
    dbCard: CardAndProgress,
    onAnswerSubmitted: (PromotionEngine.ReviewResult, Boolean) -> Unit // Result, isWeird flag
) {
    var isFlipped by remember(aiCard.targetWord) { mutableStateOf(false) }
    var hasBeenFlipped by remember(aiCard.targetWord) { mutableStateOf(false) }

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
            text = "AI Exercise Session",
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
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
            )
        ) {
            Box(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                if (rotationAnimation <= 90f) {
                    Text(
                        text = aiCard.targetLanguageSentence,
                        style = Typography.headlineMedium.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        textAlign = TextAlign.Center
                    )
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.graphicsLayer { rotationY = 180f }
                    ) {
                        // Show the translation of the sentence
                        Text(
                            text = aiCard.translation,
                            style = Typography.titleLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(24.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                        Spacer(modifier = Modifier.height(24.dp))

                        // Explicitly highlight the target word mapping
                        Text(
                            text = "Target Word:",
                            style = Typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "${dbCard.flashCard.word} → ${dbCard.flashCard.translation}",
                            style = Typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }
        }

        // The dedicated, smaller "Skip / Weird" button
        TextButton(
            onClick = { onAnswerSubmitted(PromotionEngine.ReviewResult.ALMOST_CORRECT, true) },
            modifier = Modifier.padding(top = 8.dp, bottom = 12.dp)
        ) {
            Text(
                text = "Skip (if the sentence is weird)",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = Typography.bodyMedium
            )
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
                    text = "How did you do?",
                    style = Typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // The standard 3 grading buttons
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { onAnswerSubmitted(PromotionEngine.ReviewResult.WRONG, false) },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        modifier = Modifier.weight(1f).height(55.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Wrong", color = MaterialTheme.colorScheme.onErrorContainer, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { onAnswerSubmitted(PromotionEngine.ReviewResult.ALMOST_CORRECT, false) },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
                        modifier = Modifier.weight(1f).height(55.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Almost", color = MaterialTheme.colorScheme.onTertiaryContainer, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { onAnswerSubmitted(PromotionEngine.ReviewResult.CORRECT, false) },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                        modifier = Modifier.weight(1f).height(55.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Right", color = MaterialTheme.colorScheme.onSecondaryContainer, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}