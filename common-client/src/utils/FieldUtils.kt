package dev.sunriseydy.acgn.client.utils

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

/**
 * @author SunriseYDY
 * @date 2024-08-13 21:02
 */
@Composable
fun RequiredFieldLabel(label: String) {
    Text(text = "$label*", color = MaterialTheme.colorScheme.error)
}

@Composable
fun SupportingText(isError: Boolean, errorMessage: String, supportingText: @Composable () -> Unit = { }) {
    Column {
        supportingText()
        if (isError) {
            Text(errorMessage, color = MaterialTheme.colorScheme.error)
        }
    }
}