package dev.sunriseydy.acgn.client.base.components

import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

/**
 * @author SunriseYDY
 * @date 2024-08-12 17:31
 */
@Composable
fun PageTitle(
    title: String,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable (() -> Unit)? = null,
    actions: @Composable (RowScope.() -> Unit)? = null
) {
    Card(modifier = modifier.fillMaxWidth().height(64.dp), shape = RectangleShape) {
        Row(
            modifier = Modifier.fillMaxWidth().fillMaxHeight().padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            navigationIcon?.let {
                it()
                Spacer(modifier = Modifier.width(4.dp))
            }
            Box(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.basicMarquee()
                )
            }
            actions?.let {
                Row(
                    modifier = Modifier.wrapContentWidth().fillMaxHeight(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                    content = it
                )
            }
        }
        HorizontalDivider(thickness = 2.dp)
    }
}