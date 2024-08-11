package dev.sunriseydy.acgn.client.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.sunriseydy.acgn.client.utils.getLocalServerConfigOrNull
import dev.sunriseydy.acgn.client.utils.setLocalServerConfig
import dev.sunriseydy.acgn.enums.Language
import io.github.oshai.kotlinlogging.KotlinLogging

/**
 * @author SunriseYDY
 * @date 2024-07-31 16:06
 */

private val logger = KotlinLogging.logger { }

@Composable
fun ServerConfig(onClick: (Language) -> Pair<Boolean, String>, success: Boolean, errorMessage: String) {
    var selectedLanguage by remember { mutableStateOf(Language.SIMPLIFIED_CHINESE) }
    var serverAddress by remember { mutableStateOf(getLocalServerConfigOrNull() ?: "") }
    var showError by remember { mutableStateOf(!success) }
    var errorMessage by remember { mutableStateOf(errorMessage) }

    Row(
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.width(300.dp)
        ) {
            Text("选择语言：")
            Spacer(modifier = Modifier.height(8.dp))
            LanguageRadioButtonGroup(selectedLanguage) { selectedLanguage = it }
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = serverAddress,
                onValueChange = { serverAddress = it },
                label = { Text("服务器地址") },
            )
            Spacer(modifier = Modifier.height(16.dp))
            if (showError) {
                Text(text = errorMessage, color = MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.height(8.dp))
            }
            Button(
                onClick = {
                    setLocalServerConfig(serverAddress)
                    val (success, message) = onClick(selectedLanguage)
                    if (!success) {
                        showError = true
                        errorMessage = message
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("提交")
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