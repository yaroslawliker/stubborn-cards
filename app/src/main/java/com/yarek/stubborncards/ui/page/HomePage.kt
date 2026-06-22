package com.yarek.stubborncards.ui.page

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.yarek.stubborncards.ui.common.SectionHorizontalDivider
import com.yarek.stubborncards.ui.layout.PagePadding
import com.yarek.stubborncards.ui.theme.Typography
import com.yarek.stubborncards.ui.viewmodel.HomeViewModel

@Composable
fun HomePage(
    navController: NavHostController,
    viewModel: HomeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    PagePadding {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Button(
                onClick = {
                    // TODO: Navigate to About page
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
            ) {
                Text("About", style = Typography.titleMedium)
            }

            SectionHorizontalDivider()

            Text(
                modifier = Modifier.fillMaxWidth(),
                text = "Dashboard",
                textAlign = TextAlign.Center,
                style = Typography.titleLarge
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (!uiState.isLoaded && !uiState.isLoading) {
                Button(
                    onClick = { viewModel.loadDashboardStats() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Load up a dashboard",
                        style = Typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                }
            } else if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                DashboardCard(
                    title = "Ready to review",
                    count = uiState.readyToReview,
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )

                Spacer(modifier = Modifier.height(12.dp))

                DashboardCard(
                    title = "Not ready for review",
                    count = uiState.notReady,
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )

                Spacer(modifier = Modifier.height(12.dp))

                DashboardCard(
                    title = "Hidden until Test",
                    count = uiState.hiddenUntilTest,
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
fun DashboardCard(
    title: String,
    count: Int,
    containerColor: Color,
    contentColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = Typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = count.toString(),
                style = Typography.headlineMedium,
                fontWeight = FontWeight.Black
            )
        }
    }
}