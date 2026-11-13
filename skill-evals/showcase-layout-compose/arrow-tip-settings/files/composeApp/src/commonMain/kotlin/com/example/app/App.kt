package com.example.app

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.example.app.data.InMemorySettingsStore
import com.example.app.data.SettingsStore
import com.example.app.ui.SettingsScreen

@Composable
fun App(settings: SettingsStore = remember { InMemorySettingsStore() }) {
    MaterialTheme {
        SettingsScreen(settings = settings, onBack = {}, onOpen = {})
    }
}
