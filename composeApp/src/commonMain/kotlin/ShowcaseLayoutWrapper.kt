import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ly.com.tahaben.showcase_layout_compose.model.ShowcaseMsg
import ly.com.tahaben.showcase_layout_compose.model.TargetShape
import ly.com.tahaben.showcase_layout_compose.ui.ShowcaseLayout
import ly.com.tahaben.showcase_layout_compose.ui.ShowcaseScope
import ly.com.tahaben.showcase_layout_compose.ui.TargetShowcaseLayout

/**
 * A wrapper around ShowcaseLayout and TargetShowcaseLayout that allows switching between them.
 *
 * @param useTargetShowcaseLayout whether to use TargetShowcaseLayout or ShowcaseLayout
 * @param isShowcasing to determine if showcase is starting or not.
 * @param isDarkLayout if true the showcase view will be white instead of black.
 * @param initIndex the initial value of counter, set this to 1 if you don't want a greeting screen before showcasing target.
 * @param animationDuration total animation time taken when switching from current to next target in milliseconds.
 * @param onFinish what happens when all items are showcased.
 * @param greeting greeting message to be shown before showcasing the first composable, leave [initIndex] at 0 if you want to use this.
 * @param lineThickness thickness of the arrow line in dp.
 * @param targetShape the shape of the target highlight (RECTANGLE, CIRCLE, or ROUNDED_RECTANGLE).
 * @param cornerRadius the corner radius for the ROUNDED_RECTANGLE shape in dp.
 * @param animateToNextTarget if true, the target shape will animate smoothly from one target to the next when the index changes (only used in TargetShowcaseLayout).
 */
@Composable
fun ShowcaseLayoutWrapper(
    useTargetShowcaseLayout: Boolean,
    isShowcasing: Boolean,
    isDarkLayout: Boolean = false,
    initIndex: Int = 0,
    animationDuration: Int = 1000,
    onFinish: () -> Unit,
    greeting: ShowcaseMsg? = null,
    lineThickness: Dp = 5.dp,
    targetShape: TargetShape = TargetShape.RECTANGLE,
    cornerRadius: Dp = 16.dp,
    animateToNextTarget: Boolean = true,
    content: @Composable ShowcaseScope.() -> Unit
) {
    if (useTargetShowcaseLayout) {
        TargetShowcaseLayout(
            isShowcasing = isShowcasing,
            isDarkLayout = isDarkLayout,
            initIndex = initIndex,
            animationDuration = animationDuration,
            onFinish = onFinish,
            greeting = greeting,
            lineThickness = lineThickness,
            targetShape = targetShape,
            cornerRadius = cornerRadius,
            animateToNextTarget = animateToNextTarget,
            content = content
        )
    } else {
        ShowcaseLayout(
            isShowcasing = isShowcasing,
            isDarkLayout = isDarkLayout,
            initIndex = initIndex,
            animationDuration = animationDuration,
            onFinish = onFinish,
            greeting = greeting,
            lineThickness = lineThickness,
            targetShape = targetShape,
            cornerRadius = cornerRadius,
            content = content
        )
    }
}
