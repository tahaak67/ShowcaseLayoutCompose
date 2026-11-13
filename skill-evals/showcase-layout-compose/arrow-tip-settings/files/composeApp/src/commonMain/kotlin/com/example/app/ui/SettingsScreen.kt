package com.example.app.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.Badge
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.app.data.SettingsStore

enum class SettingsSection { Account, Notifications, Appearance, Backup, About }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settings: SettingsStore,
    onBack: () -> Unit,
    onOpen: (SettingsSection) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                },
            )
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())) {
            SettingsRow("Account", "Profile, email, password", Icons.Default.AccountCircle) { onOpen(SettingsSection.Account) }
            SettingsRow("Notifications", "Reminders and alerts", Icons.Default.Notifications) { onOpen(SettingsSection.Notifications) }
            SettingsRow("Appearance", "Theme and text size", Icons.Default.Palette) { onOpen(SettingsSection.Appearance) }
            SettingsRow("Backup", "Back up your data to the cloud", Icons.Default.Backup, isNew = true) { onOpen(SettingsSection.Backup) }
            SettingsRow("About", "Version 3.2.0", Icons.Default.Info) { onOpen(SettingsSection.About) }
        }
    }
}

@Composable
private fun SettingsRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    isNew: Boolean = false,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text(subtitle) },
        leadingContent = { Icon(icon, contentDescription = null) },
        trailingContent = if (isNew) ({ Badge { Text("NEW") } }) else null,
        modifier = modifier.clickable(onClick = onClick),
    )
    HorizontalDivider()
}
