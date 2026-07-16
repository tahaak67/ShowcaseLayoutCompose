package ly.com.tahaben.showcase_layout_compose.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color


/**
 * Contains the default values used by [ShowcaseLayout] and [TargetShowcaseLayout].
 */
object ShowcaseLayoutDefaults {

    /**
     * Colors used to draw the showcase overlay.
     *
     * Create an instance with [ShowcaseLayoutDefaults.colors].
     *
     * @property overlayColor the color of the dimming overlay drawn around the target. Its own
     * alpha is honored, so pass e.g. `Color.Blue.copy(alpha = 0.5f)` to control transparency.
     * @property pulseColor the color of the pulsing ring drawn around the target. Only used by
     * [TargetShowcaseLayout]; [ShowcaseLayout] has no pulse.
     */
    @Immutable
    data class Colors(
        val overlayColor: Color,
        val pulseColor: Color,
    )

    /**
     * Creates a [Colors] that controls how the showcase overlay is drawn.
     *
     * @param overlayColor the color (including alpha) of the dimming overlay drawn around the
     * target. Defaults to a black overlay at 90% opacity. For a dark UI pass a light color, e.g.
     * `Color.White.copy(alpha = 0.9f)`.
     * @param pulseColor the color of the pulsing ring drawn around the target. Only used by
     * [TargetShowcaseLayout].
     */
    @Composable
    fun colors(
        overlayColor: Color = Color.Black.copy(alpha = 0.9f),
        pulseColor: Color = Color.White,
    ) =
        Colors(
            overlayColor = overlayColor,
            pulseColor = pulseColor,
        )
}
