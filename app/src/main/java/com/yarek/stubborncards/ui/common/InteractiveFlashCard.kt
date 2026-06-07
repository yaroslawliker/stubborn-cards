package com.yarek.stubborncards.ui.common

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.yarek.stubborncards.ui.theme.Typography

@Composable
fun FlippableCardWrapper(
    modifier: Modifier = Modifier,
    onCardFlipped: () -> Unit = {},
    frontContent: @Composable () -> Unit,
    backContent: @Composable () -> Unit
) {
    var isFlipped by remember { mutableStateOf(false) }

    val rotationAnimation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(durationMillis = 400),
        label = "CardFlipAnimation"
    )

    Surface(
        modifier = modifier
            .graphicsLayer {
                rotationY = rotationAnimation
                cameraDistance = 8 * density
            }
            .border(
                width = 2.dp,
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(24.dp)
            )
            .clickable {
                isFlipped = !isFlipped
                onCardFlipped()
            },
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (rotationAnimation <= 90f) {
                frontContent()
            } else {
                Box(modifier = Modifier.graphicsLayer { rotationY = 180f }) {
                    backContent()
                }
            }
        }
    }
}

/** Standard interactive flash-card mapping */
@Composable
fun InteractiveFlashCard(
    word: String,
    translation: String,
    modifier: Modifier = Modifier,
    onCardFlipped: () -> Unit = {}
) {
    FlippableCardWrapper(
        modifier = modifier,
        onCardFlipped = onCardFlipped,
        frontContent = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("Question", style = Typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(8.dp))
                Text(word, style = Typography.headlineMedium.copy(fontWeight = FontWeight.Bold), textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 24.dp))
            }
        },
        backContent = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("Answer", style = Typography.labelMedium, color = MaterialTheme.colorScheme.secondary)
                Spacer(modifier = Modifier.height(8.dp))
                Text(translation, style = Typography.headlineMedium.copy(fontWeight = FontWeight.Medium), textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 24.dp))
            }
        }
    )
}