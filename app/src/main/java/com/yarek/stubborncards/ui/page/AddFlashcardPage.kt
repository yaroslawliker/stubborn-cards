package com.yarek.stubborncards.ui.page

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yarek.stubborncards.ui.layout.PagePadding
import com.yarek.stubborncards.ui.theme.Typography
import com.yarek.stubborncards.ui.viewmodel.AddCardViewModel

@Preview(showBackground = true)
@Composable
fun AddFlashcardPage(
    viewModel: AddCardViewModel = viewModel()
) {
    val word by viewModel.word.collectAsState()
    val translation by viewModel.translation.collectAsState()

    PagePadding {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Page Header
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                text = "Add a new flash-card",
                textAlign = TextAlign.Center,
                style = Typography.titleLarge
            )

            // Single Flash-card Box Container
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.outline,
                        shape = RoundedCornerShape(24.dp) // Large corner radius like the mockup
                    )
                    .padding(vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Field: Question / Word
                CardEmbeddedInputField(
                    label = "Question",
                    placeholder = "Enter the word",
                    value = word,
                    onValueChange = { viewModel.onWordChange(it) }
                )

                // Dashed Divider Line
                DashedDivider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )

                // Bottom Field: Answer / Translation
                CardEmbeddedInputField(
                    label = "Answer",
                    placeholder = "Enter the translation",
                    value = translation,
                    onValueChange = { viewModel.onTranslationChange(it) }
                )
            }

            Spacer(modifier = Modifier.height(30.dp))

            // Add Button
            Button(
                onClick = {
                    viewModel.saveCard()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
            ) {
                Text(
                    "Add",
                    style = Typography.titleMedium
                )
            }
        }
    }
}

@Composable
fun CardEmbeddedInputField(
    label: String,
    placeholder: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Label centered
        Text(
            text = label,
            style = Typography.titleMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp)
        )

        // Text Field without borders, centered text
        TextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = {
                Text(
                    text = placeholder,
                    style = Typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )
            },
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = Color.Transparent,
                focusedContainerColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent
            ),
            textStyle = Typography.bodyLarge.copy(textAlign = TextAlign.Center),
            singleLine = true
        )
    }
}

@Composable
fun DashedDivider(modifier: Modifier = Modifier) {
    val strokeColor = MaterialTheme.colorScheme.outlineVariant
    val strokeWidthPx = 2.dp

    androidx.compose.foundation.Canvas(modifier = modifier.height(2.dp)) {
        drawStroke(
            color = strokeColor,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 15f), 0f),
            strokeWidth = strokeWidthPx.toPx()
        )
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawStroke(
    color: Color,
    pathEffect: PathEffect,
    strokeWidth: Float
) {
    drawLine(
        color = color,
        start = androidx.compose.ui.geometry.Offset(0f, size.height / 2),
        end = androidx.compose.ui.geometry.Offset(size.width, size.height / 2),
        strokeWidth = strokeWidth,
        pathEffect = pathEffect
    )
}