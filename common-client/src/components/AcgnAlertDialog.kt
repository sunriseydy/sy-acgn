package dev.sunriseydy.acgn.client.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.graphics.vector.ImageVector
import dev.sunriseydy.acgn.client.common.enums.CommonString

/**
 * @author SunriseYDY
 * @date 2024-08-13 16:34
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AcgnAlertDialog(
    alertDialogVisible: MutableState<Boolean>,
    onDismissRequest: () -> Unit = { alertDialogVisible.value = false },
    onConfirmation: () -> Unit,
    dialogTitle: String,
    dialogText: String? = null,
    icon: ImageVector? = null,
) {
    if (alertDialogVisible.value) {
        AlertDialog(
            icon = {
                icon?.let {
                    Icon(
                        imageVector = it,
                        contentDescription = null
                    )
                }
            },
            title = {
                Text(dialogTitle)
            },
            text = {
                dialogText?.let {
                    Text(it)
                }
            },
            onDismissRequest = {
                onDismissRequest()
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onConfirmation()
                    }
                ) {
                    Text(CommonString.CONFIRM.localization)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        onDismissRequest()
                    }
                ) {
                    Text(CommonString.CANCEL.localization)
                }
            }
        )
    }
}