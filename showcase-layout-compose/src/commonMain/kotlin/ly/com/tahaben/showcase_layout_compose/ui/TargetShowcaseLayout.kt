package ly.com.tahaben.showcase_layout_compose.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ly.com.tahaben.showcase_layout_compose.domain.Level
import ly.com.tahaben.showcase_layout_compose.model.ShowcaseMsg
import ly.com.tahaben.showcase_layout_compose.model.TargetShape
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt

private const val TAG = "TargetShowcaseLayout "
private const val INDEX_RESET_DELAY = 250L

/**
 * TargetShowcaseLayout
 *
 * @param isShowcasing to determine if showcase is starting or not.
 * @param isDarkLayout if true the showcase view will be white instead of black.
 * @param initIndex the initial value of counter, set this to 1 if you don't want a greeting screen before showcasing target.
 * @param animationDuration total animation time taken when switching from current to next target in milliseconds.
 * @param onFinish what happens when all items are showcased.
 * @param greeting greeting message to be shown before showcasing the first composable, leave [initIndex] at 0 if you want to use this.
 * @param lineThickness thickness of the arrow line in dp.
 * @param targetShape the shape of the target highlight, can be either CIRCLE, RECTANGLE, or ROUNDED_RECTANGLE.
 * @param cornerRadius the radius of the corners when targetShape is ROUNDED_RECTANGLE.
 * @param animateToNextTarget if true, the target shape will animate smoothly from one target to the next when the index changes.
 *                           If false, the shape will shrink at the current location, then expand at the new location.
 **/
@Composable
fun TargetShowcaseLayout(
    isShowcasing: Boolean,
    isDarkLayout: Boolean = false,
    initIndex: Int = 0,
    animationDuration: Int = 1000,
    onFinish: () -> Unit,
    greeting: ShowcaseMsg? = null,
    lineThickness: Dp = 5.dp,
    targetShape: TargetShape = TargetShape.ROUNDED_RECTANGLE,
    cornerRadius: Dp = 8.dp,
    animateToNextTarget: Boolean = true,
    content: @Composable ShowcaseScope.() -> Unit
) {
    var currentIndex by remember {
        mutableIntStateOf(initIndex)
    }
    val currentContent by rememberUpdatedState(content)
    val resetDelay by derivedStateOf { animationDuration.toLong() + INDEX_RESET_DELAY }
    val scope = ShowcaseScopeImpl(greeting)
    scope.currentContent()
    val localDensity = LocalDensity.current
    var singleGreetingMsg by remember { mutableStateOf<ShowcaseMsg?>(null) }
    val showcaseItem = scope.showcaseActionFlow.collectAsState()
    val showCasingItem by remember {
        derivedStateOf {
            if (showcaseItem.value != null) {
                scope.showcaseEventListener?.onEvent(
                    Level.DEBUG,
                    TAG + "showcase single item index: ${showcaseItem.value}"
                )
                currentIndex = showcaseItem.value ?: initIndex
                true
            } else {
                false
            }
        }
    }
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        if (isShowcasing) {
            val itemSize = scope.getSizeFor(currentIndex)
            val offset = scope.getPositionFor(currentIndex)
            val coroutineScope = rememberCoroutineScope()
            val animatedWidth = remember { Animatable(itemSize.width) }
            val animatedHeight = remember { Animatable(itemSize.height) }

            val animatedX = remember { Animatable(offset.x) }
            val animatedY = remember { Animatable(offset.y) }
            val maxDimension =
                max(itemSize.width, itemSize.height)
            val targetRadius = maxDimension / 2f + 40f

            val outerAnimatable = remember { Animatable(0.6f) }
            val outerAlphaAnimatable = remember(currentIndex) { Animatable(0f) }

            // Animation for message text opacity to create smooth transitions
            val messageTextAlpha = remember { Animatable(1f) }

            // Animation for overall canvas alpha to make the circle completely disappear
            val canvasAlpha = remember { Animatable(1f) }

            LaunchedEffect(currentIndex) {
                outerAnimatable.snapTo(0.6f)

                outerAnimatable.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(
                        durationMillis = 500,
                        easing = FastOutSlowInEasing,
                    ),
                )
            }

            LaunchedEffect(currentIndex) {
                outerAlphaAnimatable.animateTo(
                    targetValue = 0.9f,
                    animationSpec = tween(
                        durationMillis = 500,
                        easing = FastOutSlowInEasing,
                    ),
                )
            }

            val d = sqrt(
                animatedHeight.value.toDouble().pow(2.0)
                        + animatedWidth.value.toDouble().pow(2.0)
            ).toFloat().div(1)

            // Animation for the pulse radius
            val pulseRadius = remember { Animatable(0f) }
            // Animation for the pulse transparency
            val pulseAlpha = remember { Animatable(0.6f) }

            LaunchedEffect(currentIndex) {
                val pulseDuration = 1000
                while (true) {
                    pulseAlpha.snapTo(0.6f)
                    pulseRadius.snapTo(0f)
                    launch {
                        pulseAlpha.animateTo(
                            targetValue = 0.0f,
                            animationSpec = tween(
                                durationMillis = pulseDuration,
                                easing = FastOutSlowInEasing,
                            )
                        )
                    }
                    launch {
                        pulseRadius.animateTo(
                            targetValue = maxDimension, // Maximum additional radius of the pulse
                            animationSpec = infiniteRepeatable(
                                animation = tween(
                                    pulseDuration,
                                    easing = FastOutLinearInEasing
                                ), // 1-second animation
                                repeatMode = RepeatMode.Restart
                            )
                        )
                    }
                    delay(pulseDuration+200L)
                }
            }
            // Animation for message text - fade out when changing targets
            /*LaunchedEffect(currentIndex) {
                // If not the first showcase, fade out the message text
                if (currentIndex > 1 && currentIndex != initIndex) {
                    messageTextAlpha.animateTo(
                        0f,
                        animationSpec = tween(
                            durationMillis = animationDuration / 3,
                            easing = FastOutSlowInEasing
                        )
                    )
                    //messageTextAlpha.snapTo(0f)
                }
            }*/

            LaunchedEffect(currentIndex) {
                // Get the previous position and size for smooth transition
                val prevX = animatedX.value
                val prevY = animatedY.value
                val prevWidth = animatedWidth.value
                val prevHeight = animatedHeight.value

                // If this is the first showcase or we're resetting, snap to initial values
                if (currentIndex == 1 || currentIndex == initIndex) {
                    animatedX.snapTo(offset.x)
                    animatedY.snapTo(offset.y)
                    animatedHeight.snapTo(0f)
                    animatedWidth.snapTo(0f)

                    launch {
                        animatedHeight.animateTo(itemSize.height)
                    }
                    launch {
                        animatedWidth.animateTo(itemSize.width)
                    }
                } else if(currentIndex == scope.getHashMapSize()){
                    // last index
                    messageTextAlpha.animateTo(0f, animationSpec = tween(durationMillis = animationDuration / 2, easing = FastOutSlowInEasing))
                    canvasAlpha.animateTo(0f)
                } else {
                    // For transitions between targets
                    messageTextAlpha.snapTo(0f)

                    if (animateToNextTarget) {
                        // Animate directly from one target to another (original behavior)
                        // For smoother transitions, we'll animate the outer circle as well

                        // First, ensure the outer circle is at the right starting point
                        // If it was faded out, we need to reset it
                        if (outerAlphaAnimatable.value < 0.5f) {
                            outerAlphaAnimatable.snapTo(0.5f)
                        }

                        // Reset canvas alpha for the new target
                        canvasAlpha.snapTo(1f)

                        // Animate the position of the inner target highlight
                        launch {
                            animatedX.animateTo(
                                offset.x,
                                animationSpec = tween(
                                    durationMillis = animationDuration,
                                    easing = FastOutSlowInEasing
                                )
                            )
                        }
                        launch {
                            animatedY.animateTo(
                                offset.y,
                                animationSpec = tween(
                                    durationMillis = animationDuration,
                                    easing = FastOutSlowInEasing
                                )
                            )
                        }

                        // Animate the size of the inner target highlight
                        // Use a single coroutine to ensure width and height animations are synchronized
                        launch {
                            // Animate both width and height together with the same duration and easing
                            // This ensures the shape remains circular during the animation
                            val widthAnim = animatedWidth.animateTo(
                                itemSize.width,
                                animationSpec = tween(
                                    durationMillis = animationDuration,
                                    easing = FastOutSlowInEasing
                                )
                            )

                            val heightAnim = animatedHeight.animateTo(
                                itemSize.height,
                                animationSpec = tween(
                                    durationMillis = animationDuration,
                                    easing = FastOutSlowInEasing
                                )
                            )
                        }

                        // Animate the outer circle back to full opacity
                        launch {
                            outerAlphaAnimatable.animateTo(
                                0.9f,
                                animationSpec = tween(
                                    durationMillis = animationDuration,
                                    easing = FastOutSlowInEasing
                                )
                            )
                        }
                    }
                    else {
                        animatedX.animateTo(offset.x)
                        animatedY.animateTo(offset.y)

                        outerAlphaAnimatable.snapTo(0f)
                        delay(animationDuration.toLong() / 3)
                        canvasAlpha.animateTo(1f)


                        // Expand at new location
                        val expandDuration = animationDuration / 3
                        /*
                                                // Use a single coroutine to ensure width and height animations are synchronized
                                                launch {
                                                    // Animate both width and height together with the same duration and easing
                                                    // This ensures the shape remains circular during the expanding animation
                                                    val widthAnim = animatedWidth.animateTo(
                                                        itemSize.width,
                                                        animationSpec = tween(
                                                            durationMillis = expandDuration,
                                                            easing = FastOutSlowInEasing
                                                        )
                                                    )

                                                    val heightAnim = animatedHeight.animateTo(
                                                        itemSize.height,
                                                        animationSpec = tween(
                                                            durationMillis = expandDuration,
                                                            easing = FastOutSlowInEasing
                                                        )
                                                    )
                                                }
                        */

                        // Animate the outer circle back in
                        launch {
                            outerAnimatable.animateTo(
                                1f,
                                animationSpec = tween(
                                    durationMillis = expandDuration,
                                    easing = FastOutSlowInEasing
                                )
                            )
                        }

                        launch {
                            outerAlphaAnimatable.animateTo(
                                0.9f,
                                animationSpec = tween(
                                    durationMillis = expandDuration,
                                    easing = FastOutSlowInEasing
                                )
                            )
                        }
                    }

                    // After the animation has completed, fade the message text back in
                    launch {
                        delay(animationDuration.toLong())
                        messageTextAlpha.animateTo(
                            1f,
                            animationSpec = tween(
                                durationMillis = animationDuration / 2,
                                easing = FastOutSlowInEasing
                            )
                        )
                    }
                }
            }
            LaunchedEffect(isShowcasing){
                if (isShowcasing && currentIndex == 0){
                    currentIndex = 1
                    pulseAlpha.snapTo(0.6f)
                    pulseRadius.snapTo(0f)
                }
            }
            val message = scope.getMessageFor(currentIndex)
            val textMeasurer = rememberTextMeasurer()

            Canvas(
                modifier = Modifier.fillMaxSize()
                    .semantics { testTag = "circleModeCanvas" }
                    .pointerInput(Unit) {
                        detectTapGestures {
                            scope.showcaseEventListener?.onEvent(
                                Level.VERBOSE,
                                TAG + "tapped here $it"
                            )
                            if (currentIndex + 1 < scope.getHashMapSize()) {
                                if (!animateToNextTarget){
                                    // Shrink at current location, then move to new location, then expand
                                    // Step 1: Shrink at current location
                                    val shrinkDuration = animationDuration

                                    // Shrink both width and height simultaneously and shrink the outer circle
                                    coroutineScope.launch {
                                        // Fade out the message text
                                        messageTextAlpha.animateTo(
                                            0f,
                                            animationSpec = tween(
                                                durationMillis = animationDuration / 3,
                                                easing = FastOutSlowInEasing
                                            )
                                        )

                                       /* // Animate all elements simultaneously
                                        // Use a single coroutine to ensure width and height animations are synchronized
                                        val widthHeightAnim = launch {
                                            // Animate both width and height together with the same duration and easing
                                            // This ensures the shape remains circular during the shrinking animation
                                            val widthAnim = animatedWidth.animateTo(
                                                0f,
                                                animationSpec = tween(
                                                    durationMillis = shrinkDuration,
                                                    easing = FastOutSlowInEasing
                                                )
                                            )

                                            val heightAnim = animatedHeight.animateTo(
                                                0f,
                                                animationSpec = tween(
                                                    durationMillis = shrinkDuration,
                                                    easing = FastOutSlowInEasing
                                                )
                                            )
                                        }*/

                                        // Shrink the outer circle
                                        launch {
                                            outerAnimatable.animateTo(
                                                0f,
                                                animationSpec = tween(
                                                    durationMillis = shrinkDuration,
                                                    easing = FastOutSlowInEasing
                                                )
                                            )
                                        }

                                        // Fade out the outer circle
                                        launch {
                                            outerAlphaAnimatable.animateTo(
                                                0f,
                                                animationSpec = tween(
                                                    durationMillis = shrinkDuration,
                                                    easing = FastOutSlowInEasing
                                                )
                                            )
                                        }

                                        // Fade out the entire canvas to make the circle completely disappear
                                        launch {
                                            canvasAlpha.animateTo(
                                                0f,
                                                animationSpec = tween(
                                                    durationMillis = shrinkDuration,
                                                    easing = FastOutSlowInEasing
                                                )
                                            )
                                        }

                                        // Wait for animations to complete
                                        delay(shrinkDuration.toLong())

                                        // Move to the next target
                                        currentIndex++

                                        // Reset canvas alpha for the next target
//                                        canvasAlpha.snapTo(1f)
                                    }

                                } else {
                                    // When animateToNextTarget is true, we still want to fade out the outer circle
                                    // before moving to the next target, but we don't shrink it
                                    coroutineScope.launch {
                                        // Fade out the message text
                                        messageTextAlpha.animateTo(
                                            0f,
                                            animationSpec = tween(
                                                durationMillis = animationDuration / 3,
                                                easing = FastOutSlowInEasing
                                            )
                                        )

                                        // Fade out the outer circle
                                        launch {
                                            outerAlphaAnimatable.animateTo(
                                                0f, // Fade to 30% opacity instead of 0 for smoother transition
                                                animationSpec = tween(
                                                    durationMillis = animationDuration / 3,
                                                    easing = FastOutSlowInEasing
                                                )
                                            )
                                        }

                                        // Fade out the entire canvas to make the circle completely disappear
                                        launch {
                                            canvasAlpha.animateTo(
                                                0f, // Fade to 30% opacity instead of 0 for smoother transition
                                                animationSpec = tween(
                                                    durationMillis = animationDuration / 3,
                                                    easing = FastOutSlowInEasing
                                                )
                                            )
                                        }

                                        // Wait for animations to complete
                                        delay((animationDuration / 3).toLong())

                                        // Reset canvas alpha for the next target
                                        canvasAlpha.snapTo(1f)

                                        // Move to the next target
                                        currentIndex++
                                    }
                                }

                            } else {
                                // This is the last target, finish the showcase

                                    coroutineScope.launch {
                                        val shrinkDuration = animationDuration / 2

                                        // Fade out the message text
                                        messageTextAlpha.animateTo(
                                            0f,
                                            animationSpec = tween(
                                                durationMillis = shrinkDuration / 2,
                                                easing = FastOutSlowInEasing
                                            )
                                        )

                                        // Fade out the entire canvas to make the circle completely disappear
                                        launch {
                                            canvasAlpha.animateTo(
                                                0f,
                                                animationSpec = tween(
                                                    durationMillis = shrinkDuration,
                                                    easing = FastOutSlowInEasing
                                                )
                                            )
                                        }

                                        // Wait for animations to complete
                                        delay(shrinkDuration.toLong())

                                        // Reset index and call onFinish
                                        currentIndex = initIndex
                                        onFinish()
                                    }

                            }
                        }
                    }
            ) {
                // Calculate the radius for the target shape
                // For a circle, this is the actual radius
                // For a rectangle, we'll use the actual width and height
                val circleRadius = max(itemSize.width, itemSize.height).div(2)
                // Half width and height for rectangle mode
                val rectHalfWidth = itemSize.width.div(2)
                val rectHalfHeight = itemSize.height.div(2)
                val itemCenter = Offset(
                    animatedX.value + itemSize.width.div(2),
                    animatedY.value + itemSize.height.div(2)
                )

                // Constants for padding and spacing
                val safetyPadding = 45f
                val textPadding = 100f
                val horizontalSafetyPadding = safetyPadding * 1.5f
                val extraSafetyMargin = safetyPadding * 0.75f
                val bgPadding = textPadding * 1.5f

                // Initial calculation of outer circle dimensions (will be adjusted based on text)
                var outerLeft = animatedX.value - d
                var outerTop = animatedY.value - d
                var outerRight = animatedX.value + maxDimension + d
                var outerBottom = animatedY.value + maxDimension + d

                // Variables to store text measurement and position
                var textX = 0f
                var textY = 0f
                var textWidth = 0
                var textHeight = 0
                var textBackgroundRect: Rect? = null
                var textResult: androidx.compose.ui.text.TextLayoutResult? = null

                // Calculate text dimensions and position first, then adjust outer circle if needed
                message?.let { msg ->
                    // First, calculate an initial estimate of available width for text
                    // This is a conservative estimate that will be refined
                    val initialMaxWidth = min(size.width.toInt() - 200, (outerRight - outerLeft - 4 * safetyPadding).toInt())
                    val maxTextWidth = max(1, initialMaxWidth)

                    // Measure text with appropriate constraints
                    textResult = textMeasurer.measure(
                        msg.text,
                        style = msg.textStyle,
                        overflow = TextOverflow.Visible,
                        constraints = Constraints(0, maxTextWidth)
                    )

                    // Store text dimensions
                    textWidth = textResult.size.width
                    textHeight = textResult.size.height

                    // Determine if text should be above or below the target
                    // Use rectHalfHeight for rectangle mode to position text correctly relative to the rectangle
                    val verticalOffset = if (targetShape == TargetShape.CIRCLE) circleRadius else rectHalfHeight
                    val textAboveY = itemCenter.y - verticalOffset - textHeight - safetyPadding * 1.5f
                    val textBelowY = itemCenter.y + verticalOffset + safetyPadding * 1.5f
                    val isTextAbove = textAboveY >= safetyPadding

                    // Set vertical position of text
                    textY = if (!isTextAbove) {
                        textBelowY
                    } else {
                        textAboveY
                    }

                    // Calculate horizontal position (centered by default)
                    textX = itemCenter.x - textWidth / 2

                    // Ensure text stays within screen bounds horizontally
                    if (textX < horizontalSafetyPadding) {
                        textX = horizontalSafetyPadding
                        scope.showcaseEventListener?.onEvent(
                            Level.INFO,
                            TAG + "Text would extend beyond left edge of screen. Moving text right."
                        )
                    }

                    if (textX + textWidth > size.width - horizontalSafetyPadding) {
                        textX = size.width - textWidth - horizontalSafetyPadding
                        scope.showcaseEventListener?.onEvent(
                            Level.INFO,
                            TAG + "Text would extend beyond right edge of screen. Moving text left."
                        )
                    }

                    // Calculate text background rectangle with padding
                    val bgLeft = textX - bgPadding
                    val bgTop = textY - bgPadding
                    val bgRight = textX + textWidth + bgPadding
                    val bgBottom = textY + textHeight + bgPadding
                    textBackgroundRect = Rect(bgLeft, bgTop, bgRight, bgBottom)

                    // Now adjust outer circle dimensions to accommodate the text background
                    // Expand upward if needed
                    if (isTextAbove && bgTop < outerTop + safetyPadding + extraSafetyMargin) {
                        val extraSpace = (outerTop + safetyPadding + extraSafetyMargin) - bgTop
                        outerTop -= extraSpace
                        scope.showcaseEventListener?.onEvent(
                            Level.INFO,
                            TAG + "Expanding outer circle upward to accommodate text"
                        )
                    }

                    // Expand downward if needed
                    if (!isTextAbove && bgBottom > outerBottom - safetyPadding - extraSafetyMargin) {
                        val extraSpace = bgBottom - (outerBottom - safetyPadding - extraSafetyMargin)
                        outerBottom += extraSpace
                        scope.showcaseEventListener?.onEvent(
                            Level.INFO,
                            TAG + "Expanding outer circle downward to accommodate text"
                        )
                    }

                    // Expand leftward if needed
                    if (bgLeft < outerLeft + safetyPadding + extraSafetyMargin) {
                        val extraSpace = (outerLeft + safetyPadding + extraSafetyMargin) - bgLeft
                        outerLeft -= extraSpace
                        scope.showcaseEventListener?.onEvent(
                            Level.INFO,
                            TAG + "Expanding outer circle leftward to accommodate text"
                        )
                    }

                    // Expand rightward if needed
                    if (bgRight > outerRight - safetyPadding - extraSafetyMargin) {
                        val extraSpace = bgRight - (outerRight - safetyPadding - extraSafetyMargin)
                        outerRight += extraSpace
                        scope.showcaseEventListener?.onEvent(
                            Level.INFO,
                            TAG + "Expanding outer circle rightward to accommodate text"
                        )
                    }
                }

                // Create the donut path after all adjustments to outer circle dimensions
                val donutPath = Path().apply {
                    op(
                        Path().apply {
                            // Outer oval (outer circle) - potentially adjusted for text
                            addOval(
                                Rect(
                                    left = outerLeft,
                                    top = outerTop,
                                    right = outerRight,
                                    bottom = outerBottom
                                )
                            )
                        },
                        Path().apply {
                            // Inner shape - always at the target
                            // Use either a circle, rectangle, or rounded rectangle based on the targetShape parameter
                            when (targetShape) {
                                TargetShape.CIRCLE -> {
                                    // Draw a circle for the inner cutout
                                    addOval(
                                        Rect(
                                            left = itemCenter.x - circleRadius,
                                            top = itemCenter.y - circleRadius,
                                            right = itemCenter.x + circleRadius,
                                            bottom = itemCenter.y + circleRadius
                                        )
                                    )
                                }
                                TargetShape.ROUNDED_RECTANGLE -> {
                                    // Draw a rounded rectangle for the inner cutout
                                    val rect = Rect(
                                        left = itemCenter.x - rectHalfWidth,
                                        top = itemCenter.y - rectHalfHeight,
                                        right = itemCenter.x + rectHalfWidth,
                                        bottom = itemCenter.y + rectHalfHeight
                                    )
                                    RoundRect(
                                        left = itemCenter.x - rectHalfWidth,
                                        top = itemCenter.y - rectHalfHeight,
                                        right = itemCenter.x + rectHalfWidth,
                                        bottom = itemCenter.y + rectHalfHeight,
                                    )

                                    val cornerRadiusPx = with(localDensity){
                                        cornerRadius.toPx()
                                    }

                                    // Top-left arc
                                    moveTo(rect.left, rect.top + cornerRadiusPx)
                                    arcTo(
                                        rect = Rect(
                                            left = rect.left,
                                            top = rect.top,
                                            right = rect.left + cornerRadiusPx * 2,
                                            bottom = rect.top + cornerRadiusPx * 2
                                        ),
                                        startAngleDegrees = 180f,
                                        sweepAngleDegrees = 90f,
                                        forceMoveTo = false
                                    )

                                    // Top-right arc
                                    lineTo(rect.right - cornerRadiusPx, rect.top)
                                    arcTo(
                                        rect = Rect(
                                            left = rect.right - cornerRadiusPx * 2,
                                            top = rect.top,
                                            right = rect.right,
                                            bottom = rect.top + cornerRadiusPx * 2
                                        ),
                                        startAngleDegrees = 270f,
                                        sweepAngleDegrees = 90f,
                                        forceMoveTo = false
                                    )

                                    // Bottom-right arc
                                    lineTo(rect.right, rect.bottom - cornerRadiusPx)
                                    arcTo(
                                        rect = Rect(
                                            left = rect.right - cornerRadiusPx * 2,
                                            top = rect.bottom - cornerRadiusPx * 2,
                                            right = rect.right,
                                            bottom = rect.bottom
                                        ),
                                        startAngleDegrees = 0f,
                                        sweepAngleDegrees = 90f,
                                        forceMoveTo = false
                                    )

                                    // Bottom-left arc
                                    lineTo(rect.left + cornerRadiusPx, rect.bottom)
                                    arcTo(
                                        rect = Rect(
                                            left = rect.left,
                                            top = rect.bottom - cornerRadiusPx * 2,
                                            right = rect.left + cornerRadiusPx * 2,
                                            bottom = rect.bottom
                                        ),
                                        startAngleDegrees = 90f,
                                        sweepAngleDegrees = 90f,
                                        forceMoveTo = false
                                    )

                                    close()
                                }
                                else -> {
                                    // Draw a rectangle for the inner cutout that matches the target's exact dimensions
                                    addRect(
                                        Rect(
                                            left = itemCenter.x - rectHalfWidth,
                                            top = itemCenter.y - rectHalfHeight,
                                            right = itemCenter.x + rectHalfWidth,
                                            bottom = itemCenter.y + rectHalfHeight
                                        )
                                    )
                                }
                            }
                        },
                        operation = PathOperation.Difference
                    )
                }

                // Draw the donut path with the dimensions that have been adjusted for text
                // Apply canvasAlpha to make the circle completely disappear during transitions
                drawPath(
                    path = donutPath,
                    color = Color.Black.copy(alpha = 0.9f * canvasAlpha.value),
                    style = Fill // Fill the donut shape
                )

                // Draw the pulsing ring (outside the hole)
                val pulsePath = Path().apply {
                    op(
                        Path().apply {
                            // Use either a circle, rectangle, or rounded rectangle for the outer pulse based on the targetShape parameter
                            // This ensures that the pulse animation matches the shape of the target
                            when (targetShape) {
                                TargetShape.CIRCLE -> {
                                    // Draw a circle for the outer pulse
                                    addOval(
                                        Rect(
                                            left = itemCenter.x - (circleRadius + pulseRadius.value),
                                            top = itemCenter.y - (circleRadius + pulseRadius.value),
                                            right = itemCenter.x + (circleRadius + pulseRadius.value),
                                            bottom = itemCenter.y + (circleRadius + pulseRadius.value)
                                        )
                                    )
                                }
                                TargetShape.ROUNDED_RECTANGLE -> {
                                    // Draw a rounded rectangle for the outer pulse
                                    val rect = Rect(
                                        left = itemCenter.x - (rectHalfWidth + pulseRadius.value),
                                        top = itemCenter.y - (rectHalfHeight + pulseRadius.value),
                                        right = itemCenter.x + (rectHalfWidth + pulseRadius.value),
                                        bottom = itemCenter.y + (rectHalfHeight + pulseRadius.value)
                                    )
                                    val cornerRadiusPx = cornerRadius.toPx()

                                    // Top-left arc
                                    moveTo(rect.left, rect.top + cornerRadiusPx)
                                    arcTo(
                                        rect = Rect(
                                            left = rect.left,
                                            top = rect.top,
                                            right = rect.left + cornerRadiusPx * 2,
                                            bottom = rect.top + cornerRadiusPx * 2
                                        ),
                                        startAngleDegrees = 180f,
                                        sweepAngleDegrees = 90f,
                                        forceMoveTo = false
                                    )

                                    // Top-right arc
                                    lineTo(rect.right - cornerRadiusPx, rect.top)
                                    arcTo(
                                        rect = Rect(
                                            left = rect.right - cornerRadiusPx * 2,
                                            top = rect.top,
                                            right = rect.right,
                                            bottom = rect.top + cornerRadiusPx * 2
                                        ),
                                        startAngleDegrees = 270f,
                                        sweepAngleDegrees = 90f,
                                        forceMoveTo = false
                                    )

                                    // Bottom-right arc
                                    lineTo(rect.right, rect.bottom - cornerRadiusPx)
                                    arcTo(
                                        rect = Rect(
                                            left = rect.right - cornerRadiusPx * 2,
                                            top = rect.bottom - cornerRadiusPx * 2,
                                            right = rect.right,
                                            bottom = rect.bottom
                                        ),
                                        startAngleDegrees = 0f,
                                        sweepAngleDegrees = 90f,
                                        forceMoveTo = false
                                    )

                                    // Bottom-left arc
                                    lineTo(rect.left + cornerRadiusPx, rect.bottom)
                                    arcTo(
                                        rect = Rect(
                                            left = rect.left,
                                            top = rect.bottom - cornerRadiusPx * 2,
                                            right = rect.left + cornerRadiusPx * 2,
                                            bottom = rect.bottom
                                        ),
                                        startAngleDegrees = 90f,
                                        sweepAngleDegrees = 90f,
                                        forceMoveTo = false
                                    )

                                    close()
                                }
                                else -> {
                                    // Draw a rectangle for the outer pulse that matches the target's exact dimensions
                                    addRect(
                                        Rect(
                                            left = itemCenter.x - (rectHalfWidth + pulseRadius.value),
                                            top = itemCenter.y - (rectHalfHeight + pulseRadius.value),
                                            right = itemCenter.x + (rectHalfWidth + pulseRadius.value),
                                            bottom = itemCenter.y + (rectHalfHeight + pulseRadius.value)
                                        )
                                    )
                                }
                            }
                        },
                        Path().apply {
                            // Use either a circle, rectangle, or rounded rectangle for the inner pulse based on the targetShape parameter
                            // This ensures that the inner cutout of the pulse animation matches the shape of the target
                            when (targetShape) {
                                TargetShape.CIRCLE -> {
                                    // Draw a circle for the inner pulse
                                    addOval(
                                        Rect(
                                            left = itemCenter.x - circleRadius,
                                            top = itemCenter.y - circleRadius,
                                            right = itemCenter.x + circleRadius,
                                            bottom = itemCenter.y + circleRadius
                                        )
                                    )
                                }
                                TargetShape.ROUNDED_RECTANGLE -> {
                                    // Draw a rounded rectangle for the inner pulse
                                    val rect = Rect(
                                        left = itemCenter.x - rectHalfWidth,
                                        top = itemCenter.y - rectHalfHeight,
                                        right = itemCenter.x + rectHalfWidth,
                                        bottom = itemCenter.y + rectHalfHeight
                                    )
                                    val cornerRadiusPx = cornerRadius.toPx()

                                    // Top-left arc
                                    moveTo(rect.left, rect.top + cornerRadiusPx)
                                    arcTo(
                                        rect = Rect(
                                            left = rect.left,
                                            top = rect.top,
                                            right = rect.left + cornerRadiusPx * 2,
                                            bottom = rect.top + cornerRadiusPx * 2
                                        ),
                                        startAngleDegrees = 180f,
                                        sweepAngleDegrees = 90f,
                                        forceMoveTo = false
                                    )

                                    // Top-right arc
                                    lineTo(rect.right - cornerRadiusPx, rect.top)
                                    arcTo(
                                        rect = Rect(
                                            left = rect.right - cornerRadiusPx * 2,
                                            top = rect.top,
                                            right = rect.right,
                                            bottom = rect.top + cornerRadiusPx * 2
                                        ),
                                        startAngleDegrees = 270f,
                                        sweepAngleDegrees = 90f,
                                        forceMoveTo = false
                                    )

                                    // Bottom-right arc
                                    lineTo(rect.right, rect.bottom - cornerRadiusPx)
                                    arcTo(
                                        rect = Rect(
                                            left = rect.right - cornerRadiusPx * 2,
                                            top = rect.bottom - cornerRadiusPx * 2,
                                            right = rect.right,
                                            bottom = rect.bottom
                                        ),
                                        startAngleDegrees = 0f,
                                        sweepAngleDegrees = 90f,
                                        forceMoveTo = false
                                    )

                                    // Bottom-left arc
                                    lineTo(rect.left + cornerRadiusPx, rect.bottom)
                                    arcTo(
                                        rect = Rect(
                                            left = rect.left,
                                            top = rect.bottom - cornerRadiusPx * 2,
                                            right = rect.left + cornerRadiusPx * 2,
                                            bottom = rect.bottom
                                        ),
                                        startAngleDegrees = 90f,
                                        sweepAngleDegrees = 90f,
                                        forceMoveTo = false
                                    )

                                    close()
                                }
                                else -> {
                                    // Draw a rectangle for the inner pulse that matches the target's exact dimensions
                                    addRect(
                                        Rect(
                                            left = itemCenter.x - rectHalfWidth,
                                            top = itemCenter.y - rectHalfHeight,
                                            right = itemCenter.x + rectHalfWidth,
                                            bottom = itemCenter.y + rectHalfHeight
                                        )
                                    )
                                }
                            }
                        },
                        operation = PathOperation.Difference
                    )
                }

                // Apply canvasAlpha to make the pulse animation also disappear during transitions
                drawPath(
                    path = pulsePath,
                    color = Color.White.copy(alpha = pulseAlpha.value * canvasAlpha.value),
                    style = Fill
                )

                // Draw the message text between the inner and outer circle bounds
                message?.let { msg ->
                    // Constants for padding and spacing - use the same values as in the outer circle adjustment
                    // to ensure consistency
                    val safetyPadding = 100f
                    val textPadding = 24f

                    // Calculate the maximum width available for text
                    // Use a narrower width to ensure text wraps appropriately and stays within bounds
                    // Use a more conservative width to ensure text doesn't extend beyond outer circle
                    val calculatedWidth = min(size.width.toInt() - 200, (outerRight - outerLeft - 4 * safetyPadding).toInt())
                    // Ensure maxTextWidth is always positive to avoid Constraints exception
                    val maxTextWidth = max(1, calculatedWidth)

                    // Measure text with appropriate constraints to ensure it wraps if needed
                    val textResult = textMeasurer.measure(
                        msg.text,
                        style = msg.textStyle,
                        overflow = TextOverflow.Visible,
                        constraints = Constraints(0, maxTextWidth)
                    )

                    // Check if displaying the text above the target would make it go outside the screen bounds
                    // Use increased safety padding to ensure more space between text and outer circle
                    // Use rectHalfHeight for rectangle mode to position text correctly relative to the rectangle
                    val verticalOffset = if (targetShape == TargetShape.CIRCLE) circleRadius else rectHalfHeight
                    val textAboveY = itemCenter.y - verticalOffset - textResult.size.height - safetyPadding * 1.5f
                    val textBelowY = itemCenter.y + verticalOffset + safetyPadding * 1.5f
                    val isTextAbove = textAboveY >= safetyPadding

                    // If text would be outside the screen bounds when displayed above, show it below
                    val textY = if (!isTextAbove) {
                        scope.showcaseEventListener?.onEvent(
                            Level.INFO,
                            TAG + "Text would be outside screen bounds if displayed above. Displaying below target instead."
                        )
                        // Make sure text stays within the adjusted outer circle when displayed below
                        min(textBelowY, outerBottom - textResult.size.height - safetyPadding * 1.5f)
                    } else {
                        // Make sure text stays within the adjusted outer circle when displayed above
                        max(textAboveY, outerTop + safetyPadding * 1.5f)
                    }

                    // Log if text is very close to the outer circle bounds
                    if (isTextAbove && textY < outerTop + safetyPadding * 2) {
                        scope.showcaseEventListener?.onEvent(
                            Level.INFO,
                            TAG + "Text is very close to the top edge of the outer circle."
                        )
                    } else if (!isTextAbove && textY + textResult.size.height > outerBottom - safetyPadding * 2) {
                        scope.showcaseEventListener?.onEvent(
                            Level.INFO,
                            TAG + "Text is very close to the bottom edge of the outer circle."
                        )
                    }

                    // Ensure text doesn't extend beyond the left or right edges of the screen and outer circle
                    var textX = itemCenter.x - textResult.size.width / 2

                    // Use increased safety padding for all edge checks
                    val horizontalSafetyPadding = safetyPadding * 1.5f

                    // Adjust if text would go beyond left edge of screen
                    if (textX < horizontalSafetyPadding) {
                        scope.showcaseEventListener?.onEvent(
                            Level.INFO,
                            TAG + "Text would extend beyond left edge of screen. Adjusting position."
                        )
                        textX = horizontalSafetyPadding
                    }

                    // Adjust if text would go beyond right edge of screen
                    if (textX + textResult.size.width > size.width - horizontalSafetyPadding) {
                        scope.showcaseEventListener?.onEvent(
                            Level.INFO,
                            TAG + "Text would extend beyond right edge of screen. Adjusting position."
                        )
                        textX = size.width - textResult.size.width - horizontalSafetyPadding
                    }

                    // Also ensure text stays within the horizontal bounds of the outer circle
                    // with increased safety padding
                    if (textX < outerLeft + horizontalSafetyPadding) {
                        scope.showcaseEventListener?.onEvent(
                            Level.INFO,
                            TAG + "Text would extend beyond left edge of outer circle. Adjusting position."
                        )
                        textX = outerLeft + horizontalSafetyPadding
                    }

                    if (textX + textResult.size.width > outerRight - horizontalSafetyPadding) {
                        scope.showcaseEventListener?.onEvent(
                            Level.INFO,
                            TAG + "Text would extend beyond right edge of outer circle. Adjusting position."
                        )
                        textX = outerRight - textResult.size.width - horizontalSafetyPadding
                    }

                    // If after all adjustments, text still doesn't fit within screen bounds,
                    // prioritize keeping it on screen over keeping it within outer circle
                    if (textX < horizontalSafetyPadding) {
                        textX = horizontalSafetyPadding
                        scope.showcaseEventListener?.onEvent(
                            Level.INFO,
                            TAG + "Text still extends beyond left edge of screen. Forcing on-screen position."
                        )
                    }

                    if (textX + textResult.size.width > size.width - horizontalSafetyPadding) {
                        textX = size.width - textResult.size.width - horizontalSafetyPadding
                        scope.showcaseEventListener?.onEvent(
                            Level.INFO,
                            TAG + "Text still extends beyond right edge of screen. Forcing on-screen position."
                        )
                    }

                    // Draw a background for the text with increased padding
                    // Use a larger padding for the background to ensure text has room to breathe
                    val bgPadding = textPadding * 1.5f
                    val bgRect = Rect(
                        left = textX - bgPadding,
                        top = textY - bgPadding,
                        right = textX + textResult.size.width + bgPadding,
                        bottom = textY + textResult.size.height + bgPadding
                    )

                    // Add an additional safety margin for the check
                    val extraSafetyMargin = safetyPadding * 0.75f

                    // Log if text is very close to the outer circle bounds
                    if (bgRect.left < outerLeft + safetyPadding + extraSafetyMargin ||
                        bgRect.right > outerRight - safetyPadding - extraSafetyMargin ||
                        bgRect.top < outerTop + safetyPadding + extraSafetyMargin ||
                        bgRect.bottom > outerBottom - safetyPadding - extraSafetyMargin) {
                        scope.showcaseEventListener?.onEvent(
                            Level.INFO,
                            TAG + "Text is very close to the edge of the outer circle."
                        )
                    }

                    // Apply both message text alpha and canvas alpha to ensure everything disappears during transitions
                    val backgroundAlpha = messageTextAlpha.value * canvasAlpha.value

                    if (msg.roundedCorner == 0.dp) {
                        drawRect(
                            color = msg.msgBackground ?: Color.Black,
                            topLeft = Offset(bgRect.left, bgRect.top),
                            size = Size(bgRect.width, bgRect.height),
                            alpha = backgroundAlpha
                        )
                    } else {
                        drawRoundRect(
                            color = msg.msgBackground ?: Color.Black,
                            topLeft = Offset(bgRect.left, bgRect.top),
                            size = Size(bgRect.width, bgRect.height),
                            cornerRadius = CornerRadius(msg.roundedCorner.value),
                            alpha = backgroundAlpha
                        )
                    }

                    // Draw the text with the animated alpha
                    drawText(
                        textResult,
                        topLeft = Offset(textX, textY),
                        alpha = messageTextAlpha.value * canvasAlpha.value
                    )
                }
            }
        }
    }
}
