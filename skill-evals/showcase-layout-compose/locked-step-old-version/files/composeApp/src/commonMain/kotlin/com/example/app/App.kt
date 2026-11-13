package com.example.app

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.app.ui.SignUpScreen
import com.example.app.ui.WelcomeScreen

@Composable
fun App() {
    var showSignUp by remember { mutableStateOf(false) }
    MaterialTheme {
        if (showSignUp) SignUpScreen(onSignUp = { _, _, _ -> }) else WelcomeScreen(onGetStarted = { showSignUp = true })
    }
}
