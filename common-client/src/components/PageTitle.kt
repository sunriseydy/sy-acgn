package dev.sunriseydy.acgn.client.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp

/**
 * @author SunriseYDY
 * @date 2024-08-12 17:31
 */
@Composable
fun PageTitle(title: String, actions: @Composable (() -> Unit)? = null) {
    Card(modifier = Modifier.fillMaxWidth().requiredHeight(50.dp), shape = RectangleShape) {
        Row(modifier = Modifier.fillMaxWidth().fillMaxHeight()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(if (actions == null) 1f else 0.3f)
                    .fillMaxHeight()
                    .padding(start = 8.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = title, style = MaterialTheme.typography.titleLarge)
            }
            actions?.let {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                        .padding(end = 8.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    it()
                }
            }
        }
        HorizontalDivider(thickness = 2.dp)
    }
}