package dev.sunriseydy.acgn.client.base.utils

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import dev.sunriseydy.acgn.client.res.Res
import dev.sunriseydy.acgn.client.res.is_blank
import org.jetbrains.compose.resources.stringResource

/**
 * @author SunriseYDY
 * @date 2024-08-13 21:02
 */
@Composable
fun RequiredFieldLabel(label: String) {
    Text(text = "$label*", color = MaterialTheme.colorScheme.error)
}

@Composable
fun RequiredSupportingText(
    fieldValue: MutableState<String>,
    fieldName: String = "",
    supportingText: @Composable () -> Unit = { }
) {
    Column {
        supportingText()
        if (fieldValue.value.isBlank()) {
            Text(fieldName + stringResource(Res.string.is_blank), color = MaterialTheme.colorScheme.error)
        }
    }
}