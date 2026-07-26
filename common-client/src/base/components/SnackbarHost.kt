package dev.sunriseydy.acgn.client.base.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.sunriseydy.acgn.client.AppState
import kotlinx.coroutines.launch

/**
 * @author SunriseYDY
 * @date 2024-08-11 15:42
 */
@Composable
fun SnackbarHost(
    appState: AppState,
    modifier: Modifier = Modifier,
) {
    SnackbarHost(appState.snackbarHostState, modifier) { data ->
        // custom snackbar with the custom action button color and border
        val isError = data.visuals is SnackbarVisualsWithError
        val containerColor = if (isError) {
            MaterialTheme.colorScheme.errorContainer
        } else {
            MaterialTheme.colorScheme.primaryContainer
        }
        val contentColor = if (isError) {
            MaterialTheme.colorScheme.onErrorContainer
        } else {
            MaterialTheme.colorScheme.onPrimaryContainer
        }
        val actionLabel = data.visuals.actionLabel
        val actionComposable: (@Composable () -> Unit)? = if (actionLabel != null) {
            @Composable {
                TextButton(
                    colors = ButtonDefaults.textButtonColors(
                        containerColor = containerColor,
                        contentColor = contentColor,
                    ),
                    onClick = { data.performAction() },
                    content = { Text(actionLabel) }
                )
            }
        } else {
            null
        }
        val dismissActionComposable: (@Composable () -> Unit)? =
            if (data.visuals.withDismissAction) {
                @Composable {
                    IconButton(
                        onClick = { data.dismiss() },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = containerColor,
                            contentColor = contentColor,
                        ),
                        content = {
                            Icon(
                                Icons.Filled.Close,
                                contentDescription = null,
                            )
                        }
                    )
                }
            } else {
                null
            }

        Snackbar(
            modifier = Modifier.padding(12.dp),
            action = actionComposable,
            dismissAction = dismissActionComposable,
            containerColor = containerColor,
            contentColor = contentColor,
        ) {
            Text(data.visuals.message)
        }
    }
}

class SnackbarVisualsWithError(override val message: String) :
    SnackbarVisuals {
    override val actionLabel: String?
        get() = null

    override val withDismissAction: Boolean
        get() = true

    override val duration: SnackbarDuration
        get() = SnackbarDuration.Short
}

fun AppState.showMessage(
    message: String,
    actionLabel: String? = null,
    withDismissAction: Boolean = true,
    duration: SnackbarDuration =
        if (actionLabel == null) SnackbarDuration.Short else SnackbarDuration.Indefinite
) {
    scope.launch {
        snackbarHostState.showSnackbar(message, actionLabel, withDismissAction, duration)
    }
}

fun AppState.showMessage(snackbarVisuals: SnackbarVisuals) {
    scope.launch {
        snackbarHostState.showSnackbar(snackbarVisuals)
    }
}

fun AppState.showError(message: String) {
    scope.launch {
        snackbarHostState.showSnackbar(
            SnackbarVisualsWithError(message)
        )
    }
}
