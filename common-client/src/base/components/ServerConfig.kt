package dev.sunriseydy.acgn.client.base.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.sunriseydy.acgn.base.enums.Language
import dev.sunriseydy.acgn.client.base.utils.getLocalServerConfigOrNull
import dev.sunriseydy.acgn.client.base.utils.setLocalServerConfig
import dev.sunriseydy.acgn.client.res.Res
import dev.sunriseydy.acgn.client.res.choose_language
import dev.sunriseydy.acgn.client.res.server_address
import dev.sunriseydy.acgn.client.res.submit
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

/**
 * @author SunriseYDY
 * @date 2024-07-31 16:06
 */

private val logger = KotlinLogging.logger { }

@Composable
fun ServerConfig(onClick: suspend (Language) -> Pair<Boolean, String>, success: Boolean, errorMessage: String) {
    var selectedLanguage by remember { mutableStateOf(Language.SIMPLIFIED_CHINESE) }
    var serverAddress by remember { mutableStateOf(getLocalServerConfigOrNull() ?: "") }
    var showError by remember { mutableStateOf(!success && errorMessage.isNotBlank()) }
    var errorMessageState by remember { mutableStateOf(errorMessage) }
    val scope = rememberCoroutineScope()

    // Keep local UI state in sync with parent async check result.
    LaunchedEffect(success, errorMessage) {
        showError = !success && errorMessage.isNotBlank()
        errorMessageState = errorMessage
    }

    Row(
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.width(300.dp)
        ) {
            Text(stringResource(Res.string.choose_language))
            Spacer(modifier = Modifier.height(8.dp))
            LanguageRadioButtonGroup(selectedLanguage) { selectedLanguage = it }
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = serverAddress,
                onValueChange = { serverAddress = it },
                label = { Text(stringResource(Res.string.server_address)) },
            )
            Spacer(modifier = Modifier.height(16.dp))
            if (showError) {
                Text(text = errorMessageState, color = MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.height(8.dp))
            }
            Button(
                onClick = {
                    setLocalServerConfig(serverAddress)
                    scope.launch {
                        val (success, message) = onClick(selectedLanguage)
                        showError = !success && message.isNotBlank()
                        errorMessageState = message
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(Res.string.submit))
            }
        }
    }
}

@Composable
fun LanguageRadioButtonGroup(selectedLanguage: Language, onLanguageSelected: (Language) -> Unit) {
    Column {
        Language.entries.forEach { language ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = (language == selectedLanguage),
                    onClick = { onLanguageSelected(language) }
                )
                Text(text = language.originName)
            }
        }
    }
}
