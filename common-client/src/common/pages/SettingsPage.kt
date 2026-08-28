package dev.sunriseydy.acgn.client.common.pages

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import dev.sunriseydy.acgn.base.enums.Language
import dev.sunriseydy.acgn.client.AppState
import dev.sunriseydy.acgn.client.base.api.onSuccessData
import dev.sunriseydy.acgn.client.base.components.showMessage
import dev.sunriseydy.acgn.common.dto.AppConfig
import dev.sunriseydy.acgn.tools.AppConfigTool
import dev.sunriseydy.acgn.tools.i
import kotlinx.coroutines.launch

private data class ConfigItem(
    val key: String,
    val displayName: String,
    val currentValue: MutableState<String>,
    val originalValue: String,
    val dbId: ULong,
    val isReadOnly: Boolean,
    val isSensitive: Boolean,
    val isLanguage: Boolean,
) {
    val isChanged: Boolean get() = currentValue.value != originalValue
}

private data class ConfigGroup(
    val displayName: String,
    val items: List<ConfigItem>,
)

private const val DB_CONFIG_PREFIX = "config.db."
private const val STANDARD_CONFIG_PREFIX = "config."

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsPage(appState: AppState) {
    var configGroups by remember { mutableStateOf<List<ConfigGroup>>(emptyList()) }

    val loadConfigs: () -> Unit = {
        appState.scope.launch {
            val allConfigs = AppConfigTool.getAppConfigs()
            val dbConfigsResult = appState.api.common.getAllAppConfigFromDB()
            val dbIdMap = mutableMapOf<String, ULong>()
            dbConfigsResult.onSuccessData(
                onSuccess = { list -> list.forEach { dbIdMap[it.configKey] = it.id } }
            )
            configGroups = buildConfigGroups(allConfigs, dbIdMap)
        }
    }

    LaunchedEffect(Unit) { loadConfigs() }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(i("settings.title", "设置")) })
        },
        floatingActionButton = {
            val hasChanges = configGroups.any { group -> group.items.any { it.isChanged } }
            if (hasChanges) {
                ExtendedFloatingActionButton(
                    onClick = {
                        appState.scope.launch {
                            val changedItems = configGroups
                                .flatMap { it.items }
                                .filter { it.isChanged && !it.isReadOnly }
                            saveConfigs(appState, changedItems)
                            loadConfigs()
                        }
                    },
                    icon = { Icon(Icons.Default.Save, contentDescription = null) },
                    text = { Text(i("settings.save", "保存")) },
                )
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            items(configGroups) { group ->
                ConfigGroupCard(group)
            }
        }
    }
}

@Composable
private fun ConfigGroupCard(group: ConfigGroup) {
    Card(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = group.displayName,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.height(12.dp))
            group.items.forEachIndexed { index, item ->
                if (index > 0) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                }
                ConfigItemRow(item)
            }
        }
    }
}

@Composable
private fun ConfigItemRow(item: ConfigItem) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(
            text = item.displayName,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(4.dp))
        if (item.isLanguage) {
            LanguageDropdown(item)
        } else if (item.isSensitive) {
            SensitiveTextField(item)
        } else {
            OutlinedTextField(
                value = item.currentValue.value,
                onValueChange = { item.currentValue.value = it },
                modifier = Modifier.fillMaxWidth(),
                enabled = !item.isReadOnly,
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LanguageDropdown(item: ConfigItem) {
    var expanded by remember { mutableStateOf(false) }
    val currentLanguage = try {
        Language.valueOf(item.currentValue.value)
    } catch (_: Exception) {
        Language.SIMPLIFIED_CHINESE
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
    ) {
        OutlinedTextField(
            value = currentLanguage.meaning,
            onValueChange = {},
            modifier = Modifier.fillMaxWidth().menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyMedium,
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            Language.entries.forEach { language ->
                DropdownMenuItem(
                    text = { Text(language.meaning) },
                    onClick = {
                        item.currentValue.value = language.name
                        expanded = false
                    },
                )
            }
        }
    }
}

@Composable
private fun SensitiveTextField(item: ConfigItem) {
    var showPassword by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = item.currentValue.value,
        onValueChange = { item.currentValue.value = it },
        modifier = Modifier.fillMaxWidth(),
        enabled = !item.isReadOnly,
        singleLine = true,
        textStyle = MaterialTheme.typography.bodyMedium,
        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            IconButton(onClick = { showPassword = !showPassword }) {
                Icon(
                    imageVector = if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                    contentDescription = if (showPassword) i("settings.hide_password", "隐藏") else i("settings.show_password", "显示"),
                )
            }
        },
    )
}

private fun buildConfigGroups(
    allConfigs: Map<String, Pair<AppConfig?, String?>>,
    dbIdMap: Map<String, ULong>,
): List<ConfigGroup> {
    val groups = mutableMapOf<String, MutableList<ConfigItem>>()

    for ((key, pair) in allConfigs) {
        val (dbConfig, fileValue) = pair
        val effectiveValue = dbConfig?.configValue ?: fileValue ?: ""
        val groupKey = extractGroupKey(key) ?: continue
        val displayName = getDisplayName(key)
        val isReadOnly = key.startsWith(DB_CONFIG_PREFIX)
        val isSensitive = key.contains("password", ignoreCase = true) || key.contains("secret", ignoreCase = true)
        val isLanguage = key.contains("language", ignoreCase = true)

        val item = ConfigItem(
            key = key,
            displayName = displayName,
            currentValue = mutableStateOf(effectiveValue),
            originalValue = effectiveValue,
            dbId = dbConfig?.id ?: dbIdMap[key] ?: 0u,
            isReadOnly = isReadOnly,
            isSensitive = isSensitive,
            isLanguage = isLanguage,
        )
        groups.getOrPut(groupKey) { mutableListOf() }.add(item)
    }

    val serverAddress = dev.sunriseydy.acgn.client.base.utils.getLocalServerConfigOrNull() ?: ""
    groups["client_server"] = mutableListOf(
        ConfigItem(
            key = "__client_server_address__",
            displayName = i("settings.server_address", "服务器地址"),
            currentValue = mutableStateOf(serverAddress),
            originalValue = serverAddress,
            dbId = 0u,
            isReadOnly = false,
            isSensitive = false,
            isLanguage = false,
        )
    )

    val groupOrder = listOf("COMMON", "client_server", "ANIME", "COMMON_S3", "db_postgresql")
    return groups.entries.sortedBy { (key, _) ->
        val idx = groupOrder.indexOf(key)
        if (idx >= 0) idx else groupOrder.size
    }.map { (key, items) ->
        ConfigGroup(
            displayName = i("config.group.$key", key),
            items = items,
        )
    }
}

private fun extractGroupKey(configKey: String): String? {
    return when {
        configKey.startsWith(DB_CONFIG_PREFIX) -> {
            val rest = configKey.removePrefix(DB_CONFIG_PREFIX)
            val subsystem = rest.substringBefore(".", "")
            if (subsystem.isNotEmpty()) "db_$subsystem" else null
        }
        configKey.startsWith(STANDARD_CONFIG_PREFIX) -> {
            val rest = configKey.removePrefix(STANDARD_CONFIG_PREFIX)
            val module = rest.substringBefore(".", "")
            val name = rest.substringAfter(".", "")
            when {
                module.isEmpty() || name.isEmpty() -> null
                module == "COMMON" && name.startsWith("S3") -> "COMMON_S3"
                else -> module
            }
        }
        else -> null
    }
}

private fun getDisplayName(configKey: String): String {
    val meaningKey = "$configKey.meaning"
    val localized = i(meaningKey)
    if (localized != meaningKey) return localized
    return configKey.substringAfterLast(".").replace(Regex("([a-z])([A-Z])"), "$1 $2")
}

private suspend fun saveConfigs(appState: AppState, changedItems: List<ConfigItem>) {
    val serverConfigs = mutableListOf<AppConfig>()
    var serverAddressChanged = false

    for (item in changedItems) {
        if (item.key == "__client_server_address__") {
            dev.sunriseydy.acgn.client.base.utils.setLocalServerConfig(item.currentValue.value)
            serverAddressChanged = true
        } else {
            serverConfigs.add(
                AppConfig(
                    id = item.dbId,
                    configKey = item.key,
                    configValue = item.currentValue.value,
                )
            )
        }
    }

    if (serverConfigs.isNotEmpty()) {
        appState.api.common.saveAppConfigs(serverConfigs).onSuccessData(
            appState = appState,
            onSuccess = { saved ->
                AppConfigTool.fromAppConfigList(saved)
                appState.showMessage(i("settings.save_success", "设置已保存"))
            },
            onError = { message ->
                appState.showMessage(message)
            }
        )
    }

    if (serverAddressChanged && serverConfigs.isEmpty()) {
        appState.showMessage(i("settings.save_success", "设置已保存"))
    }
}
