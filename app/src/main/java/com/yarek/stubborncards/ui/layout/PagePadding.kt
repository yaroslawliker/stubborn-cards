package com.yarek.stubborncards.ui.layout

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.yarek.stubborncards.ui.theme.AppDimensions

@Composable
fun PagePadding(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .padding(
                AppDimensions.pageHorizontal,
                AppDimensions.pageVertical)
    ) {
        content()
    }
}