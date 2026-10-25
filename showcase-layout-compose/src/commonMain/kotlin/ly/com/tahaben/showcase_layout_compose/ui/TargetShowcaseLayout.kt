package ly.com.tahaben.showcase_layout_compose.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.round
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import ly.com.tahaben.showcase_layout_compose.domain.Level
import ly.com.tahaben.showcase_layout_compose.domain.usecase.addRoundedTarget
import ly.com.tahaben.showcase_layout_compose.domain.usecase.computeTargetLayoutMessagePlacement
import ly.com.tahaben.showcase_layout_compose.domain.usecase.coveringRadius
import ly.com.tahaben.showcase_layout_compose.domain.usecase.isTapInsideTarget
import ly.com.tahaben.showcase_layout_compose.domain.usecase.targetLayoutMessageMaxWidth
import ly.com.tahaben.showcase_layout_compose.domain.usecase.validateInitIndex
import ly.com.tahaben.showcase_layout_compose.model.MsgAnimation
import ly.com.tahaben.showcase_layout_compose.model.ShowcaseMsg
import ly.com.tahaben.showcase_layout_compose.model.ShowcaseMsgScope
import ly.com.tahaben.showcase_layout_compose.model.TargetShape
import kotlin.math.max
import kotlin.math.min


/**
 *     Copyright 2025 Taha Ben Ashur (tahaak67)
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 * Created by Taha Ben Ashur (https://github.com/tahaak67) on 20,June,2025
 */

private const val TAG = "TargetShowcaseLayout "
private const val INDEX_RESET_DELAY = 250L
private const val TARGET_TAP_TOLERANCE_PX = 16f

/**
 * TargetShowcaseLayout
 *
 * @param isShowcasing to determine if showcase is starting or not.
 * @param initIndex the initial value of counter, set this to 1 if you don't want a greeting screen before showcasing target.
 * @param animationDuration total animation time taken when switching from current to next target in milliseconds.
 * @param onFinish what happens when all items are showcased.
 * @param greeting greeting message to be shown before showcasing the first composable, leave [initIndex] at 0 if you want to use this.
 * @param lineThickness thickness of the arrow line in dp.
 * @param targetShape the shape of the target highlight, can be either CIRCLE, RECTANGLE, or ROUNDED_RECTANGLE.
 * @param cornerRadius the radius of the corners when targetShape is ROUNDED_RECTANGLE.
 * @param animateToNextTarget if true, the target shape will animate smoothly from one target to the next when the index changes.
 *                           If false, the shape will shrink at the current location, then expand at the new location.
 * @param advanceOnTargetTapOnly when true, only taps inside the highlighted target shape advance/dismiss the showcase; taps elsewhere are ignored. The greeting/initial screen always advances on any tap. Defaults to false (tap anywhere).
 * @param colors the colors used to draw the overlay and pulse, created with [ShowcaseLayoutDefaults.colors]. Pass a light [ShowcaseLayoutDefaults.Colors.overlayColor] for a dark UI. A per-step [ShowcaseMsg.msgBackground] still takes precedence over [ShowcaseLayoutDefaults.Colors.overlayColor].
 **/
@Composable
fun TargetShowcaseLayout(
    isShowcasing: Boolean,
    initIndex: Int = 0,
    animationDuration: Int = 1000,
    onFinish: () -> Unit,
    greeting: ShowcaseMsg? = null,
    lineThickness: Dp = 5.dp,
    targetShape: TargetShape = TargetShape.ROUNDED_RECTANGLE,
    cornerRadius: Dp = 8.dp,
    animateToNextTarget: Boolean = true,
    advanceOnTargetTapOnly: Boolean = false,
    colors: ShowcaseLayoutDefaults.Colors = ShowcaseLayoutDefaults.colors(),
    content: @Composable ShowcaseScope.() -> Unit
) {
    val validatedInitIndex = remember(initIndex, greeting) { validateInitIndex(initIndex, greeting) }
    var currentIndex by remember {
        mutableIntStateOf(validatedInitIndex)
    }
    val currentContent by rememberUpdatedState(content)
    val layoutCoordinatesState = remember { mutableStateOf<LayoutCoordinates?>(null) }
    /** Remembered: the scope owns the map of registered targets, which the showcase modifiers only
     * refill on the next layout pass. A new scope per recomposition left that map empty for a frame,
     * long enough for a custom message to be removed and re-added at full alpha mid fade-out. */
    val scope = remember { ShowcaseScopeImpl(greeting) }
    scope.setGreeting(greeting)
    scope.layoutCoordinatesState = layoutCoordinatesState
    /** per-step canProceed flags and onProceed callbacks, published by the showcase modifiers */
    val stepRegistry = remember { StepRegistry() }
    scope.stepRegistry = stepRegistry
    val stepConditions = stepRegistry.conditions
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
                currentIndex = showcaseItem.value ?: validatedInitIndex
                true
            } else {
                false
            }
        }
    }
    val singleGreeting = scope.greetingActionFlow.collectAsState()
    val isSingleGreeting by remember {
        derivedStateOf {
            if (singleGreeting.value != null) {
                scope.showcaseEventListener?.onEvent(
                    Level.DEBUG,
                    TAG + "showcase single greeting: ${singleGreeting.value?.text}"
                )
                singleGreetingMsg = singleGreeting.value
                true
            } else {
                false
            }
        }
    }

    BoxWithConstraints(modifier = Modifier
        .fillMaxSize()
        .onGloballyPositioned { layoutCoordinatesState.value = it }
    ) {
        val coroutineScope = rememberCoroutineScope()
        if (isShowcasing || showCasingItem || isSingleGreeting) {
            var itemSize = scope.getSizeFor(currentIndex)
            var offset = scope.getPositionFor(currentIndex)
            val animatedWidth = remember { Animatable(itemSize.width) }
            val animatedHeight = remember { Animatable(itemSize.height) }

            val animatedX = remember { Animatable(offset.x) }
            val animatedY = remember { Animatable(offset.y) }
            // The tap handler lives in a pointerInput(Unit) that never restarts, so it would
            // otherwise capture the first composition's (greeting) bounds. Track the current
            // target's resting bounds in State so the hit-test always reads the live values.
            val currentTargetTopLeft by rememberUpdatedState(offset)
            val currentTargetSize by rememberUpdatedState(itemSize)
            val maxDimension =
                max(maxHeight.value, maxWidth.value)

            val outerAnimatable = remember { Animatable(0.0f) }

            var message =  if (isSingleGreeting) singleGreetingMsg else scope.getMessageFor(currentIndex)
            val textMeasurer = rememberTextMeasurer()
            val messageTextAlpha = remember { Animatable(0f) }
            var canvasColor =  message?.msgBackground ?: colors.overlayColor
            val canvasColorAnimated by animateColorAsState(
                canvasColor,
                animationSpec = tween(durationMillis = animationDuration, easing = FastOutSlowInEasing)
            )

            // Animation for overall canvas alpha to make the circle completely disappear
            val canvasAlpha = remember { Animatable(0f) }

            // Animation for the pulse radius
            val pulseRadius = remember { Animatable(0f) }
            // Animation for the pulse transparency
            val pulseAlpha = remember { Animatable(0.6f) }

            LaunchedEffect(currentIndex) {
                val pulseDuration = 1200
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
                    delay(pulseDuration.toLong())
                }
            }

            val enterAnimation = remember { EnterAnimation() }
            LaunchedEffect(currentIndex) {
                /** cancelled by advance() if the step ends before the transition is over */
                enterAnimation.job = coroutineContext[Job]
                if (currentIndex == 0 || isSingleGreeting) {
                    //canvasAlpha.snapTo(0f)
                    canvasAlpha.animateTo(
                        1f,
                        animationSpec = tween(durationMillis = animationDuration / 2, easing = FastOutSlowInEasing)
                    )
                }
                // If this is the first showcase or we're resetting, snap to initial values
                if (currentIndex == 0 || currentIndex == validatedInitIndex) {

                    canvasAlpha.snapTo(1f)
                    itemSize = scope.getSizeFor(currentIndex)
                    offset = scope.getPositionFor(currentIndex)
                    animatedX.snapTo(offset.x)
                    animatedY.snapTo(offset.y)
                    message = scope.getMessageFor(currentIndex)
                    canvasColor =  message?.msgBackground ?: colors.overlayColor
                    delay(animationDuration.toLong())
                    handleMessageEnterAnimation(message, messageTextAlpha, animationDuration)
                    outerAnimatable.animateTo(
                        1f,
                        animationSpec = tween(
                            durationMillis = animationDuration,
                            easing = FastOutSlowInEasing
                        ))
                } else {
                    if (animateToNextTarget && !showCasingItem) {
                        outerAnimatable.snapTo(1f)
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
                            animatedWidth.animateTo(
                                itemSize.width,
                                animationSpec = tween(
                                    durationMillis = animationDuration,
                                    easing = FastOutSlowInEasing
                                )
                            )

                            animatedHeight.animateTo(
                                itemSize.height,
                                animationSpec = tween(
                                    durationMillis = animationDuration,
                                    easing = FastOutSlowInEasing
                                )
                            )
                        }

                    } else {
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

                        delay(animationDuration.toLong() / 3)
                        canvasAlpha.animateTo(1f)

                        // Expand at new location
                        val expandDuration = animationDuration / 3

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

                    }

                    // After the animation has completed, fade the message text back in
                    launch {
                        delay(animationDuration.toLong())
                        handleMessageEnterAnimation(message, messageTextAlpha, animationDuration)
                    }
                }
            }
            LaunchedEffect(isShowcasing) {
                if (isShowcasing && currentIndex != 0 && !isSingleGreeting) {
                    pulseAlpha.snapTo(0.6f)
                    pulseRadius.snapTo(0f)
                }
            }

            /** true when there is a target before the current one; never during a single item / greeting */
            fun canGoBack() = currentIndex > validatedInitIndex && !showCasingItem && !isSingleGreeting

            /** Moves the showcase forward, back or to the end. Shared by the tap handler and by
             * [ShowcaseMsgScope] so custom message content can drive the showcase. */
            fun advance(action: StepAction) {
                if (action == StepAction.PREVIOUS && !canGoBack()) {
                    scope.showcaseEventListener?.onEvent(Level.VERBOSE, TAG + "no previous target to go back to")
                    return
                }
                /** the step is over, whatever ends it: let the caller react (hide the keyboard, ...) */
                if (!isSingleGreeting && currentIndex != 0) {
                    stepRegistry.onProceed[currentIndex]?.let { onProceed ->
                        scope.showcaseEventListener?.onEvent(Level.VERBOSE, TAG + "index:$currentIndex onProceed")
                        onProceed()
                    }
                }
                /** a late enter animation would cancel the exit animations below, see [EnterAnimation] */
                enterAnimation.cancel()
                if (showCasingItem) {
                    val shrinkDuration = animationDuration

                    // Shrink both width and height simultaneously and shrink the outer circle
                    coroutineScope.launch {
                        handleMessageExitAnimation(message, messageTextAlpha, animationDuration)
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
                        delay((animationDuration / 3).toLong())

                        // Finish showcasing the single item
                        scope.showcaseItemFinished()
                        currentIndex = validatedInitIndex
                    }
                    return
                } else if (isSingleGreeting) {
                    coroutineScope.launch {
                        handleMessageExitAnimation(singleGreetingMsg, messageTextAlpha, animationDuration)

                        // Fade out the entire canvas to make the circle completely disappear
                        launch {
                            canvasAlpha.animateTo(
                                0f,
                                animationSpec = tween(
                                    durationMillis = animationDuration / 3,
                                    easing = FastOutSlowInEasing
                                )
                            )
                        }

                        // Wait for animations to complete
                        delay((animationDuration / 3).toLong())

                        // Finish showcasing the greeting
                        scope.showGreetingFinished()

                        currentIndex = validatedInitIndex
                    }
                    return
                } else if (action != StepAction.SKIP && (action == StepAction.PREVIOUS || currentIndex + 1 < scope.getHashMapSize())) {
                    val step = if (action == StepAction.PREVIOUS) -1 else 1
                    if (!animateToNextTarget) {
                        // Shrink at current location, then move to new location, then expand
                        // Step 1: Shrink at current location
                        val shrinkDuration = animationDuration

                        // Shrink both width and height simultaneously and shrink the outer circle
                        coroutineScope.launch {
                            handleMessageExitAnimation(message, messageTextAlpha, animationDuration)

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

                            // Move to the next / previous target
                            currentIndex += step

                        }

                    } else {
                        // When animateToNextTarget is true, we don't want to fade out the outer circle
                        // we just handle message animation if any then move to the next target
                        coroutineScope.launch {
                            handleMessageExitAnimation(message, messageTextAlpha, animationDuration)

                            // Move to the next / previous target
                            currentIndex += step
                        }
                    }

                } else {
                    // This is the last target, finish the showcase

                    coroutineScope.launch {
                        val shrinkDuration = animationDuration / 2
                        handleMessageExitAnimation(message, messageTextAlpha, animationDuration)

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
                        currentIndex = validatedInitIndex
                        onFinish()
                    }

                }
            }

            val currentAdvance by rememberUpdatedState(::advance)
            val currentHasPrevious by rememberUpdatedState(canGoBack())
            val msgScope = remember {
                object : ShowcaseMsgScope {
                    override fun next() = currentAdvance(StepAction.NEXT)
                    override fun previous() = currentAdvance(StepAction.PREVIOUS)
                    override fun skip() = currentAdvance(StepAction.SKIP)
                    override val hasPrevious: Boolean get() = currentHasPrevious
                }
            }

            /** locked: the current step declared canProceed = false (never a greeting). While locked the
             * overlay ignores taps and touches inside the target fall through to the target itself;
             * the step ends on its own once the condition is met. [ShowcaseMsgScope] calls still work. */
            val isStepLocked = !isSingleGreeting && currentIndex != 0 && stepConditions[currentIndex] == false
            val currentIsStepLocked by rememberUpdatedState(isStepLocked)
            LaunchedEffect(currentIndex, showCasingItem, isSingleGreeting) {
                if (isSingleGreeting || currentIndex == 0) return@LaunchedEffect
                val gate = snapshotFlow { stepConditions[currentIndex] ?: true }
                /** only a step that starts locked advances by itself; one that may already proceed
                 * behaves like any other step */
                if (!gate.first()) {
                    scope.showcaseEventListener?.onEvent(
                        Level.INFO,
                        TAG + "step $currentIndex locked until its condition is met"
                    )
                    gate.first { it }
                    scope.showcaseEventListener?.onEvent(
                        Level.INFO,
                        TAG + "step $currentIndex condition met, advancing"
                    )
                    currentAdvance(StepAction.NEXT)
                }
            }

            /** A tap on the overlay: the canvas, or custom message content that didn't handle it. */
            fun onOverlayTap(position: Offset) {
                scope.showcaseEventListener?.onEvent(
                    Level.VERBOSE,
                    TAG + "tapped here $position"
                )
                if (currentIsStepLocked) {
                    scope.showcaseEventListener?.onEvent(
                        Level.VERBOSE,
                        TAG + "step $currentIndex locked, tap ignored at $position"
                    )
                    return
                }
                /** when enabled, only taps inside the target shape advance the showcase
                 * (the greeting/initial screen always advances on any tap) */
                if (advanceOnTargetTapOnly && currentIndex != 0 && !isSingleGreeting &&
                    !isTapInsideTarget(
                        position,
                        currentTargetTopLeft,
                        currentTargetSize,
                        targetShape,
                        TARGET_TAP_TOLERANCE_PX
                    )
                ) {
                    scope.showcaseEventListener?.onEvent(
                        Level.VERBOSE,
                        TAG + "tap outside target ignored at $position"
                    )
                    return
                }
                advance(StepAction.NEXT)
            }

            /** top-left of the custom message content, used to map its taps to overlay coordinates */
            val customMsgTopLeft = remember { mutableStateOf(Offset.Zero) }

            Canvas(
                modifier = Modifier.fillMaxSize()
                    .semantics { testTag = "circleModeCanvas" }
                    /** a locked step must leave the target touchable: no pointer-input node here */
                    .then(
                        if (isStepLocked) Modifier else Modifier.pointerInput(Unit) {
                            detectTapGestures { onOverlayTap(it) }
                        }
                    )
            ) {
                // The per-step msgBackground keeps its historical fixed 0.9 alpha; a layout-wide
                // overlayColor honors its own alpha so developers can control transparency.
                val overlayBaseAlpha = if (message?.msgBackground != null) 0.9f else colors.overlayColor.alpha
                if (isSingleGreeting || currentIndex == 0) {
                    // For greeting, fill the entire screen with a solid color
                    drawRect(
                        color = canvasColor.copy(alpha = overlayBaseAlpha),
                        size = size,
                        alpha = canvasAlpha.value
                    )

                    // Display the greeting message in the middle of the screen
                    message?.let { msg ->
                        // custom content is composed as a sibling of this canvas, nothing to draw here
                        if (msg.content != null) return@let
                        // Measure text with appropriate constraints to ensure it wraps if needed
                        val maxTextWidth = max(1, (size.width * 0.8f).toInt()) // Use 80% of screen width
                        val textResult = textMeasurer.measure(
                            msg.text,
                            style = msg.textStyle,
                            overflow = TextOverflow.Visible,
                            constraints = Constraints(0, maxTextWidth)
                        )

                        // Center the text on the screen
                        val textX = (size.width - textResult.size.width) / 2
                        val textY = (size.height - textResult.size.height) / 2

                        // Draw a background for the text with padding
                        val bgPadding = 40f
                        val bgRect = Rect(
                            left = textX - bgPadding,
                            top = textY - bgPadding,
                            right = textX + textResult.size.width + bgPadding,
                            bottom = textY + textResult.size.height + bgPadding
                        )

                        // Draw the text background
                        if (msg.roundedCorner == 0.dp) {
                            drawRect(
                                color = msg.msgBackground ?: Color.Transparent,
                                topLeft = Offset(bgRect.left, bgRect.top),
                                size = Size(bgRect.width, bgRect.height),
                                alpha = messageTextAlpha.value
                            )
                        } else {
                            drawRoundRect(
                                color = msg.msgBackground ?: Color.Transparent,
                                topLeft = Offset(bgRect.left, bgRect.top),
                                size = Size(bgRect.width, bgRect.height),
                                cornerRadius = CornerRadius(msg.roundedCorner.value),
                                alpha = messageTextAlpha.value
                            )
                        }

                        // Draw the text
                        drawText(
                            textResult,
                            topLeft = Offset(textX, textY),
                            alpha = messageTextAlpha.value
                        )
                    }

                    // Return early to avoid drawing the target shape
                    return@Canvas
                }
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
                val horizontalSafetyPadding = safetyPadding * 1.5f


                // Initial calculation of outer circle dimensions (will be adjusted based on text)
                // Set the outer circle radius to half of the canvas width
                val outerRadius = coveringRadius(itemCenter, size)
                val outerLeft = itemCenter.x - outerRadius
                val outerTop = itemCenter.y - outerRadius
                val outerRight = itemCenter.x + outerRadius
                val outerBottom = itemCenter.y + outerRadius

                // Variables to store text measurement and position
                var textX = 0f
                var textWidth = 0
                var textHeight = 0
                var textResult: TextLayoutResult? = null

                // Calculate text dimensions and position first, then adjust outer circle if needed
                message?.takeIf { it.content == null }?.let { msg ->
                    // First, calculate an initial estimate of available width for text
                    // This is a conservative estimate that will be refined
                    val initialMaxWidth =
                        min(size.width.toInt() - 200, (outerRight - outerLeft - 4 * safetyPadding).toInt())
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
                        scope.showcaseEventListener?.onEvent(
                            Level.INFO,
                            TAG + "Text would extend beyond right edge of screen. Moving text left."
                        )
                    }

                }

                // Create the donut path after all adjustments to outer circle dimensions
                // Calculate the center of the outer circle
                val outerCenterX = itemCenter.x//(outerLeft + outerRight) / 2
                val outerCenterY = itemCenter.y//(outerTop + outerBottom) / 2

                // Calculate the width and height of the outer circle
                val outerWidth = outerRight - outerLeft
                val outerHeight = outerBottom - outerTop

                // Apply the scale animation to the outer circle
                val scaledOuterWidth = outerWidth * outerAnimatable.value
                val scaledOuterHeight = outerHeight * outerAnimatable.value

                // Calculate the new bounds of the scaled outer circle
                val scaledOuterLeft = outerCenterX - scaledOuterWidth / 2
                val scaledOuterTop = outerCenterY - scaledOuterHeight / 2
                val scaledOuterRight = outerCenterX + scaledOuterWidth / 2
                val scaledOuterBottom = outerCenterY + scaledOuterHeight / 2

                val donutPath = Path().apply {
                    op(
                        Path().apply {
                            // Outer oval (outer circle) - potentially adjusted for text and scaled
                            addOval(
                                Rect(
                                    left = scaledOuterLeft,
                                    top = scaledOuterTop,
                                    right = scaledOuterRight,
                                    bottom = scaledOuterBottom
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
                                    addRoundedTarget(rect, with(localDensity) { cornerRadius.toPx() })
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
                if (currentIndex == validatedInitIndex){
                    drawPath(
                        path = donutPath,
                        color = canvasColor.copy(alpha = overlayBaseAlpha * canvasAlpha.value),
                        style = Fill // Fill the donut shape
                    )
                }else{
                    drawPath(
                        path = donutPath,
                        color = canvasColorAnimated.copy(alpha = overlayBaseAlpha * canvasAlpha.value),
                        style = Fill // Fill the donut shape
                    )
                }

                // Draw the pulsing ring (outside the punch)
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
                                    addRoundedTarget(rect, cornerRadius.toPx())
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
                                    addRoundedTarget(rect, cornerRadius.toPx())
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
                    color = colors.pulseColor.copy(alpha = colors.pulseColor.alpha * pulseAlpha.value * canvasAlpha.value),
                    style = Fill
                )

                // Draw the message text between the inner and outer circle bounds
                message?.let { msg ->
                    // custom content is composed as a sibling of this canvas, nothing to draw here
                    if (msg.content != null) return@let

                    // Measure text with appropriate constraints to ensure it wraps if needed
                    val textResult = textMeasurer.measure(
                        msg.text,
                        style = msg.textStyle,
                        overflow = TextOverflow.Visible,
                        constraints = Constraints(0, targetLayoutMessageMaxWidth(size))
                    )

                    // Use rectHalfHeight for rectangle mode to position text correctly relative to the rectangle
                    val verticalOffset = if (targetShape == TargetShape.CIRCLE) circleRadius else rectHalfHeight
                    val placement = computeTargetLayoutMessagePlacement(
                        messageSize = textResult.size,
                        itemCenter = itemCenter,
                        verticalOffset = verticalOffset,
                        outer = Rect(outerLeft, outerTop, outerRight, outerBottom),
                        canvasSize = size,
                        log = { scope.showcaseEventListener?.onEvent(Level.INFO, TAG + it) }
                    )

                    // Draw the text with the animated alpha
                    drawText(
                        textResult,
                        topLeft = placement.offset,
                        alpha = messageTextAlpha.value * canvasAlpha.value
                    )
                }
            }
            /** while the step is locked, swallow touches everywhere except over the target so the
             * target itself receives them (see [TouchBlockerWithHole]) */
            if (isStepLocked) {
                TouchBlockerWithHole(
                    hole = { Rect(Offset(animatedX.value, animatedY.value), itemSize) },
                    onTap = { onOverlayTap(it) }
                )
            }
            /** Custom message content is composed on top of the canvas. It is only in the tree
             * while visible so an invisible button can't swallow taps between steps; while it is
             * composed, taps on non-clickable parts fall through to the canvas above. */
            val customContent = message?.content
            val customMsgVisible by remember { derivedStateOf { messageTextAlpha.value > 0f } }
            if (customContent != null && customMsgVisible) {
                val isGreetingStep = currentIndex == 0 || isSingleGreeting
                Box(
                    modifier = Modifier
                        .layout { measurable, layoutConstraints ->
                            val canvasSize = Size(
                                layoutConstraints.maxWidth.toFloat(),
                                layoutConstraints.maxHeight.toFloat()
                            )
                            // Same width limits the text path uses
                            val maxContentWidth = if (isGreetingStep) {
                                max(1, (canvasSize.width * 0.8f).toInt())
                            } else {
                                targetLayoutMessageMaxWidth(canvasSize)
                            }
                            val placeable = measurable.measure(
                                Constraints(maxWidth = maxContentWidth, maxHeight = layoutConstraints.maxHeight)
                            )
                            // Fill the whole overlay so the placed child stays inside our bounds
                            // (otherwise its buttons would not be hit-testable).
                            layout(layoutConstraints.maxWidth, layoutConstraints.maxHeight) {
                                val position = if (isGreetingStep) {
                                    // Greeting: centered on the screen
                                    Offset(
                                        (canvasSize.width - placeable.width) / 2,
                                        (canvasSize.height - placeable.height) / 2
                                    )
                                } else {
                                    // Same geometry the canvas uses for its donut and text
                                    val circleRadius = max(itemSize.width, itemSize.height) / 2
                                    val itemCenter = Offset(
                                        animatedX.value + itemSize.width / 2,
                                        animatedY.value + itemSize.height / 2
                                    )
                                    val outerRadius = coveringRadius(itemCenter, canvasSize)
                                    computeTargetLayoutMessagePlacement(
                                        messageSize = IntSize(placeable.width, placeable.height),
                                        itemCenter = itemCenter,
                                        verticalOffset = if (targetShape == TargetShape.CIRCLE) circleRadius else itemSize.height / 2,
                                        outer = Rect(center = itemCenter, radius = outerRadius),
                                        canvasSize = canvasSize,
                                        log = { scope.showcaseEventListener?.onEvent(Level.INFO, TAG + it) }
                                    ).offset
                                }
                                if (customMsgTopLeft.value != position) customMsgTopLeft.value = position
                                placeable.place(position.round())
                            }
                        }
                        .graphicsLayer { alpha = messageTextAlpha.value * canvasAlpha.value }
                ) {
                    Box(
                        modifier = Modifier.pointerInput(Unit) {
                            /** A tap that no clickable inside the content consumed behaves like a tap on
                             * the overlay. Observed in the Final pass so buttons inside keep their clicks,
                             * and so content such as a Card (whose Surface blocks touches behind it) still
                             * lets the user tap through to advance. */
                            awaitEachGesture {
                                awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Final)
                                val up = waitForUpOrCancellation(pass = PointerEventPass.Final)
                                if (up != null && !up.isConsumed) {
                                    onOverlayTap(up.position + customMsgTopLeft.value)
                                }
                            }
                        }
                    ) {
                        msgScope.customContent()
                    }
                }
            }
        }
    }
}

/**
 * Backwards-compatible overload that keeps the [isDarkLayout] flag working.
 *
 * @deprecated [isDarkLayout] has been replaced by [colors]. Pass a light overlay color via
 * [ShowcaseLayoutDefaults.colors] instead, e.g. `colors = ShowcaseLayoutDefaults.colors(overlayColor = Color.White.copy(alpha = 0.9f))`.
 */
@Deprecated(
    message = "isDarkLayout has been replaced by the colors parameter; pass a light overlay color instead.",
    replaceWith = ReplaceWith(
        "TargetShowcaseLayout(isShowcasing, initIndex, animationDuration, onFinish, greeting, " +
                "lineThickness, targetShape, cornerRadius, animateToNextTarget, false, " +
                "ShowcaseLayoutDefaults.colors(overlayColor = if (isDarkLayout) Color.White.copy(alpha = 0.9f) else Color.Black.copy(alpha = 0.9f)), content)"
    )
)
@Composable
fun TargetShowcaseLayout(
    isShowcasing: Boolean,
    isDarkLayout: Boolean,
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
    TargetShowcaseLayout(
        isShowcasing = isShowcasing,
        initIndex = initIndex,
        animationDuration = animationDuration,
        onFinish = onFinish,
        greeting = greeting,
        lineThickness = lineThickness,
        targetShape = targetShape,
        cornerRadius = cornerRadius,
        animateToNextTarget = animateToNextTarget,
        colors = ShowcaseLayoutDefaults.colors(
            overlayColor = if (isDarkLayout) Color.White.copy(alpha = 0.9f) else Color.Black.copy(alpha = 0.9f)
        ),
        content = content
    )
}

suspend fun handleMessageExitAnimation(
    message: ShowcaseMsg?,
    messageTextAlpha: Animatable<Float, AnimationVector1D>,
    animationDuration: Int
) {
    when (message?.exitAnim) {
        is MsgAnimation.FadeInOut -> {
            // Fade out the message text
            messageTextAlpha.animateTo(
                0f,
                animationSpec = tween(
                    durationMillis = animationDuration / 3,
                    easing = FastOutSlowInEasing
                )
            )
        }

        MsgAnimation.None -> {
            // Make the message text disappear
            messageTextAlpha.snapTo(0f)
        }

        null -> Unit
    }
}

suspend fun handleMessageEnterAnimation(
    message: ShowcaseMsg?,
    messageTextAlpha: Animatable<Float, AnimationVector1D>,
    animationDuration: Int
) {
    when (message?.enterAnim) {
        is MsgAnimation.FadeInOut -> {
            // Fade out the message text
            messageTextAlpha.animateTo(
                1f,
                animationSpec = tween(
                    durationMillis = animationDuration / 3,
                    easing = FastOutSlowInEasing
                )
            )
        }

        MsgAnimation.None -> {
            // Make the message text disappear
            messageTextAlpha.snapTo(1f)
        }

        null -> Unit
    }
}
