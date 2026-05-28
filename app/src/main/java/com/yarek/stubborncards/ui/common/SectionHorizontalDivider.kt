package com.yarek.stubborncards.ui.common

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SectionHorizontalDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(vertical = 20.dp),
        thickness = 2.dp,
    )
}