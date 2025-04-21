package dev.sunriseydy.acgn.client.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import dev.sunriseydy.acgn.client.common.enums.CommonString

/**
 * @author SunriseYDY
 * @date 2024-08-13 16:34
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AcgnAlertDialog(
    alertDialogVisible: MutableState<Boolean>,
    errorMessage: MutableState<String?> = mutableStateOf(null),
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
                errorMessage.value?.let {
                    Text(it, color = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.height(4.dp))
                }
            },
            onDismissRequest = {
                try {
                    onDismissRequest()
                } catch (e: Exception) {
                    errorMessage.value = e.message
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        try {
                            onConfirmation()
                        } catch (e: Exception) {
                            errorMessage.value = e.message
                        }
                    }
                ) {
                    Text(CommonString.CONFIRM.meaning)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        try {
                            onDismissRequest()
                        } catch (e: Exception) {
                            errorMessage.value = e.message
                        }
                    }
                ) {
                    Text(CommonString.CANCEL.meaning)
                }
            }
        )
    }
}