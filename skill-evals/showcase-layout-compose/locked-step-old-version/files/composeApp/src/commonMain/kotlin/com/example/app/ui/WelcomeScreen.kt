package com.example.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import ly.com.tahaben.showcase_layout_compose.model.Arrow
import ly.com.tahaben.showcase_layout_compose.model.Gravity
import ly.com.tahaben.showcase_layout_compose.model.Head
import ly.com.tahaben.showcase_layout_compose.model.ShowcaseMsg
import ly.com.tahaben.showcase_layout_compose.model.Side
import ly.com.tahaben.showcase_layout_compose.ui.ShowcaseLayout

/** The first screen of the app. Already has a one-step showcase on the Get started button. */
@Composable
fun WelcomeScreen(onGetStarted: () -> Unit) {
    var isShowcasing by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(500)
        isShowcasing = true
    }
    Box(Modifier.fillMaxSize()) {
        ShowcaseLayout(
            isShowcasing = isShowcasing,
            onFinish = { isShowcasing = false },
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text("Welcome to Trailmate", style = MaterialTheme.typography.headlineMedium)
                Text("Plan hikes with friends.")
                Button(
                    onClick = onGetStarted,
                    modifier = Modifier.showcase(
                        index = 1,
                        message = ShowcaseMsg(
                            "Tap here to create your account",
                            textStyle = TextStyle(color = Color.White),
                            gravity = Gravity.Top,
                            arrow = Arrow(targetFrom = Side.Top, head = Head.CIRCLE, headSize = 30f),
                        ),
                    ),
                ) { Text("Get started") }
            }
        }
    }
}
