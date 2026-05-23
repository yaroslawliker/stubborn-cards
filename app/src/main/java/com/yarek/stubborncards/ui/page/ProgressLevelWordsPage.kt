package com.yarek.stubborncards.ui.page

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.yarek.stubborncards.model.FlashCard
import com.yarek.stubborncards.ui.layout.PagePadding
import com.yarek.stubborncards.ui.layout.Page
import com.yarek.stubborncards.ui.theme.Typography
import com.yarek.stubborncards.ui.viewmodel.ProgressLevelWordsViewModel

@Composable
fun ProgressLevelWordsPage(
    navController: NavHostController,
    viewModel: ProgressLevelWordsViewModel = viewModel()
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val showTranslations by viewModel.showTranslations.collectAsState()

    val lazyCards: LazyPagingItems<FlashCard> = viewModel.pagedCards.collectAsLazyPagingItems()

    PagePadding {
        Column(modifier = Modifier.fillMaxSize()) {

            Text(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                text = "${viewModel.selectedLevel.readable} List",
                textAlign = TextAlign.Center,
                style = Typography.titleLarge
            )

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.onSearchQueryChanged(it) },
                placeholder = { Text("Search words...", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search Icon", tint = MaterialTheme.colorScheme.primary) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { viewModel.onToggleTranslations(!showTranslations) }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = showTranslations,
                    onCheckedChange = { viewModel.onToggleTranslations(it) },
                    colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Show translations",
                    style = Typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    count = lazyCards.itemCount,
                    key = { index -> lazyCards[index]?.id ?: index }
                ) { index ->
                    val card = lazyCards[index]
                    if (card != null) {
                        WordRowItem(
                            flashCard = card,
                            showTranslation = showTranslations,
                            onClick = { cardId ->
                                navController.navigate(
                                    Page.CardDetails.createRoute(
                                        categoryName = viewModel.selectedLevel.name,
                                        cardId = cardId
                                    )
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WordRowItem(
    flashCard: FlashCard,
    showTranslation: Boolean,
    onClick: (Long) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick(flashCard.id) }
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                shape = RoundedCornerShape(12.dp)
            ),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = flashCard.word,
                style = Typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (showTranslation) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = flashCard.translation,
                    style = Typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}