package dev.sunriseydy.acgn.client.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.sunriseydy.acgn.client.common.enums.CommonString

/**
 * @author SunriseYDY
 * @date 2025-02-15 20:56
 */
@Composable
fun FormCard(
    errorMessage: MutableState<String?> = mutableStateOf(null),
    onDismissRequest: (() -> Unit)?,
    onConfirmation: () -> Unit,
    confirmationText: String = CommonString.SUBMIT.localization,
    content: @Composable () -> Unit,
) {
    Card {
        Column(modifier = Modifier.padding(20.dp)) {
            content()
            Spacer(modifier = Modifier.height(4.dp))
            errorMessage.value?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.height(4.dp))
            }
            Row(modifier = Modifier.align(Alignment.End)) {
                onDismissRequest?.let {
                    TextButton(
                        onClick = {
                            try {
                                it()
                            } catch (e: Exception) {
                                errorMessage.value = e.message
                            }
                        },
                    ) { Text(CommonString.CANCEL.localization) }
                    Spacer(modifier = Modifier.width(4.dp))
                }
                TextButton(
                    onClick = {
                        try {
                            onConfirmation()
                        } catch (e: Exception) {
                            errorMessage.value = e.message
                        }
                    },
                ) { Text(confirmationText) }
            }
        }
    }
}