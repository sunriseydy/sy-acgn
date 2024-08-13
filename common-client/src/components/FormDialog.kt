package dev.sunriseydy.acgn.client.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import dev.sunriseydy.acgn.client.common.enums.CommonString

/**
 * @author SunriseYDY
 * @date 2024-08-13 15:25
 */
@Composable
fun FormDialog(
    formDialogVisible: MutableState<Boolean>,
    onDismissRequest: () -> Unit = { formDialogVisible.value = false },
    onConfirmation: () -> Unit,
    confirmationText: String = CommonString.SUBMIT.localization,
    content: @Composable () -> Unit,
) {
    if (formDialogVisible.value) {
        Dialog(onDismissRequest = onDismissRequest) {
            Card {
                Column(modifier = Modifier.padding(20.dp)) {
                    content()
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(modifier = Modifier.align(Alignment.End)) {
                        TextButton(
                            onClick = {
                                onDismissRequest()
                            },
                        ) { Text(CommonString.CANCEL.localization) }
                        Spacer(modifier = Modifier.width(4.dp))
                        TextButton(
                            onClick = {
                                onConfirmation()
                            },
                        ) { Text(confirmationText) }
                    }
                }
            }
        }
    }
}