package ly.com.tahaben.showcase_layout_compose.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color


object ShowcaseLayoutDefaults {

    @Immutable
    data class Colors(
        val overlayColor: Color,
    )

    @Composable
    fun colors(
        overlayColor: Color = Color.Black.copy(alpha = 0.9f),
    ) =
        Colors(
            overlayColor = overlayColor,
        )
}