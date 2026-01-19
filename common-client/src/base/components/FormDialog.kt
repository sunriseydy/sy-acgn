package dev.sunriseydy.acgn.client.base.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.window.Dialog
import dev.sunriseydy.acgn.client.common.enums.CommonString

/**
 * @author SunriseYDY
 * @date 2024-08-13 15:25
 */
@Composable
fun FormDialog(
    formDialogVisible: MutableState<Boolean>,
    errorMessage: MutableState<String?> = mutableStateOf(null),
    onDismissRequest: () -> Unit = { formDialogVisible.value = false },
    onConfirmation: () -> Unit,
    confirmationText: String = CommonString.SUBMIT.meaning,
    content: @Composable () -> Unit,
) {
    if (formDialogVisible.value) {
        Dialog(onDismissRequest = onDismissRequest) {
            FormCard(
                errorMessage,
                onDismissRequest,
                onConfirmation,
                confirmationText,
            ) {
                content()
            }
        }
    }
}