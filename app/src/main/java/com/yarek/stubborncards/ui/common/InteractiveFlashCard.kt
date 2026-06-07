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

/** Represents interactive flash-card that can be flipped */
@Composable
fun InteractiveFlashCard(
    word: String,
    translation: String,
    modifier: Modifier = Modifier,
    onCardFlipped: () -> Unit = {}
) {
    var isFlipped by remember(word) { mutableStateOf(false) }

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
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (rotationAnimation <= 90f) {
                Text(
                    text = "Question", 
                    style = Typography.labelMedium, 
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = word,
                    style = Typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
            } else {
                Text(
                    text = "Answer", 
                    style = Typography.labelMedium, 
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.graphicsLayer { rotationY = 180f }
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = translation,
                    style = Typography.headlineMedium.copy(fontWeight = FontWeight.Medium),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .padding(horizontal = 24.dp)
                        .graphicsLayer { rotationY = 180f }
                )
            }
        }
    }
}