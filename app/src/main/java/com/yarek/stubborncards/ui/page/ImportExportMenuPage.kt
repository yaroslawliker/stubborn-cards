package com.yarek.stubborncards.ui.page

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.yarek.stubborncards.ui.common.SectionHorizontalDivider
import com.yarek.stubborncards.ui.layout.Page
import com.yarek.stubborncards.ui.layout.PagePadding
import com.yarek.stubborncards.ui.theme.Typography
import com.yarek.stubborncards.ui.viewmodel.DeleteAllViewModel

/**
 * This function represents the page export/import options.
 *
 * It lets:
 * - Import cards from CSV
 * - Export cards to CSV
 * - Delete all cards
 */
@Composable
fun ImportExportMenuPage(
    navController: NavHostController,
    viewModel: DeleteAllViewModel = viewModel()
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var confirmationText by remember { mutableStateOf("") }

    PagePadding {
        Column {

            ImportExportMenuButton("Import from CSV") {
                navController.navigate(Page.ImportPage.route)
            }

            Spacer(modifier = Modifier.height(16.dp))

            ImportExportMenuButton("Export to CSV") {
                navController.navigate(Page.ExportPage.route)
            }

            SectionHorizontalDivider()

            ImportExportMenuButton(
                message = "Delete all cards",
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                )
            ) {
                showDeleteDialog = true
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = {
                showDeleteDialog = false
                confirmationText = ""
            },
            title = {
                Text("Delete All Flashcards")
            },
            text = {
                Column {
                    Text("Are you sure you want to delete all your flash-cards? This action cannot be undone.")
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Type \"delete all\" below to confirm:")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = confirmationText,
                        onValueChange = { confirmationText = it },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteAllCards()
                        showDeleteDialog = false
                        confirmationText = ""
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    ),
                    // Button is disabled until they type the exact phrase
                    enabled = confirmationText.trim().lowercase() in listOf("delete all", "\"delete all\"")

                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        confirmationText = ""
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun ImportExportMenuButton(
    message: String,
    colors: ButtonColors = ButtonDefaults.buttonColors(),
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(12.dp),
        colors = colors
    ) {
        Text(
            text = message, // Fixed: Uses the provided string instead of hardcoded text
            style = Typography.titleMedium
        )
    }
}