package com.yarek.stubborncards.ui.page

import android.util.Log
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yarek.stubborncards.model.ProgressLevel
import com.yarek.stubborncards.ui.layout.PagePadding
import com.yarek.stubborncards.ui.theme.Typography

/**
 * This function represents the page with general info about
 * learner's flash-card library.
 *
 * It lets:
 * - Add a new flash-card
 * - See amount of words in each progress level
 * - See words by a progress level
 */
@Preview(showBackground = true)
@Composable
fun CardsPage() {
    PagePadding {
        // All the content goes here
        Column {
            AddFlashCardButton();
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 20.dp),
                thickness = 2.dp,
            )
            ProgressLevelList();
        }}
}



@Composable
fun AddFlashCardButton() {
    Button(
        onClick = { Log.d("CardsPage", "Add button") },
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
    ) {
        Text(
            "Add a flash-card",
            style = Typography.titleMedium
        );
    }
}

@Composable
fun ProgressLevelList() {
    Column(modifier = Modifier.fillMaxWidth()){
        // Header
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = "Progress levels",
            textAlign = TextAlign.Center,
            style = Typography.titleLarge
        );

        Spacer(modifier = Modifier.height(10.dp))

        // Description
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = "Your words will transit between this progress levels as you learn them.",
            style = Typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Levels
        Column (
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            ProgressLevel.entries.forEach { level ->
                ProgressLevelButton(level.readable, "40");
            }
        }

    }
}

@Composable
fun ProgressLevelButton(levelName: String, wordAmount: String) {
    Button(
        onClick = {Log.d("CardsPage", "Progress level button clicked")},
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
            );
            Text(
                wordAmount,
                color = MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.7f)            );
        }
    }

}