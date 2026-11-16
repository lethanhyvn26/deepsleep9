package com.sleepsounds.storytelling.presentation.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import com.sleepsounds.storytelling.R

/**
 * Dialog prompting the user to name and save their active sound mix to the local database.
 */
@Composable
fun SaveMixDialog(
    defaultName: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
) {
    var draftMixName by remember(defaultName) { mutableStateOf(defaultName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = stringResource(R.string.mixer_save_mix))
        },
        text = {
            OutlinedTextField(
                value = draftMixName,
                onValueChange = { updated -> draftMixName = updated },
                label = { Text(text = stringResource(R.string.mixer_save_name_label)) },
                singleLine = true,
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val cleanName = draftMixName.trim()
                    if (cleanName.isNotEmpty()) {
                        onSave(cleanName)
                    }
                },
            ) {
                Text(text = stringResource(R.string.mixer_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.action_cancel))
            }
        },
    )
}
