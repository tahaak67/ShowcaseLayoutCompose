package ly.com.tahaben.showcase_layout_compose.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateOffsetAsState
import androidx.compose.animation.core.animateSizeAsState
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
import androidx.compose.runtime.State
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
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
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
import androidx.compose.ui.unit.toSize
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import ly.com.tahaben.showcase_layout_compose.domain.Level
import ly.com.tahaben.showcase_layout_compose.domain.ShowcaseEventListener
import ly.com.tahaben.showcase_layout_compose.domain.usecase.CARD_INSET_PX
import ly.com.tahaben.showcase_layout_compose.domain.usecase.MESSAGE_MAX_WIDTH_INSET_PX
import ly.com.tahaben.showcase_layout_compose.domain.usecase.MessageSide
import ly.com.tahaben.showcase_layout_compose.domain.usecase.PlacedMessage
import ly.com.tahaben.showcase_layout_compose.domain.usecase.addRoundedTarget
import ly.com.tahaben.showcase_layout_compose.domain.usecase.computeShowcaseMessagePlacement
import ly.com.tahaben.showcase_layout_compose.domain.usecase.isTapInsideTarget
import ly.com.tahaben.showcase_layout_compose.domain.usecase.validateInitIndex
import ly.com.tahaben.showcase_layout_compose.model.Arrow
import ly.com.tahaben.showcase_layout_compose.model.Gravity
import ly.com.tahaben.showcase_layout_compose.model.Head
import ly.com.tahaben.showcase_layout_compose.model.MsgAnimation
import ly.com.tahaben.showcase_layout_compose.model.ShowcaseData
import ly.com.tahaben.showcase_layout_compose.model.ShowcaseMsg
import ly.com.tahaben.showcase_layout_compose.model.ShowcaseMsgScope
import ly.com.tahaben.showcase_layout_compose.model.Side
import ly.com.tahaben.showcase_layout_compose.model.TargetShape
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.max

/**
 *     Copyright 2023 Taha Ben Ashur (tahaak67)
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
 * Created by Taha Ben Ashur (https://github.com/tahaak67) on 1,August,2022
 */

private const val TAG = "ShowcaseLayout "
private const val INDEX_RESET_DELAY = 250L
private const val TARGET_TAP_TOLERANCE_PX = 16f

/**
 * ShowcaseLayout
 *
 * @param isShowcasing to determine if showcase is starting or not.
 * @param initIndex the initial value of counter, set this to 1 if you don't want a greeting screen before showcasing target.
 * @param animationDuration total animation time taken when switching from current to next target in milliseconds.
 * @param onFinish what happens when all items are showcased.
 * @param greeting greeting message to be shown before showcasing the first composable, leave [initIndex] at 0 if you want to use this.
 * @param lineThickness thickness of the arrow line in dp.
 * @param targetShape the shape of the target highlight (RECTANGLE, CIRCLE, or ROUNDED_RECTANGLE).
 * @param cornerRadius the corner radius for the ROUNDED_RECTANGLE shape in dp.
 * @param advanceOnTargetTapOnly when true, only taps inside the highlighted target shape advance/dismiss the showcase; taps elsewhere are ignored. The greeting/initial screen always advances on any tap. Defaults to false (tap anywhere).
 * @param colors the colors used to draw the overlay, created with [ShowcaseLayoutDefaults.colors]. Pass a light [ShowcaseLayoutDefaults.Colors.overlayColor] for a dark UI.
 **/

@Composable
fun ShowcaseLayout(
    isShowcasing: Boolean,
    initIndex: Int = 0,
    animationDuration: Int = 1000,
    onFinish: () -> Unit,
    greeting: ShowcaseMsg? = null,
    lineThickness: Dp = 5.dp,
    targetShape: TargetShape = TargetShape.RECTANGLE,
    cornerRadius: Dp = 16.dp,
    advanceOnTargetTapOnly: Boolean = false,
    colors: ShowcaseLayoutDefaults.Colors = ShowcaseLayoutDefaults.colors(),
    content: @Composable ShowcaseScope.() -> Unit
) {
    val validatedInitIndex = remember(initIndex, greeting) { validateInitIndex(initIndex, greeting) }
    var currentIndex by remember {
        mutableIntStateOf(validatedInitIndex)
    }
    val currentContent by rememberUpdatedState(content)
    val resetDelay by derivedStateOf { animationDuration.toLong() + INDEX_RESET_DELAY }
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
                currentIndex = validatedInitIndex
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
            val offset by animateOffsetAsState(
                targetValue = scope.getPositionFor(currentIndex),
                animationSpec = tween(animationDuration),
                label = "item offset anim"
            )
            val itemSize by animateSizeAsState(
                targetValue = scope.getSizeFor(currentIndex),
                animationSpec = tween(animationDuration),
                label = "item size anim"
            )
            var message by remember {
                mutableStateOf(scope.getMessageFor(currentIndex))
            }
            val pathPortion = remember {
                Animatable(initialValue = 0f)
            }

            var isArrowDelayOver by remember { mutableStateOf(false) }
            val shouldDrawArrow = (message?.arrow != null && isArrowDelayOver)
            val arrowColor = message?.arrow?.color ?: Color.White
            val density = LocalDensity.current
            var arrowAnimDuration by remember { mutableStateOf(message?.arrow?.animationDuration) }
            val animMsgTextAlpha = remember { Animatable(0f) }
            val animMsgAlpha = remember { Animatable(0f) }
            val animArrow = remember { Animatable(0f) }
            val animArrowHead = remember { Animatable(0f) }
            val canvasAlpha = remember { Animatable(0f) }
            /** bounds of the custom message composable (if any), written from its layout and read
             * by the canvas to start the arrow from its edge */
            val customMsgBounds = remember { mutableStateOf<PlacedMessage?>(null) }

            /** to animate canvas alpha and current arrow line */
            val enterAnimation = remember { EnterAnimation() }
            LaunchedEffect(key1 = currentIndex) {
                if (currentIndex == validatedInitIndex || showCasingItem || isSingleGreeting) {
                    canvasAlpha.animateTo(
                        1f,
                        animationSpec = tween(durationMillis = animationDuration / 2, easing = FastOutSlowInEasing)
                    )
                }
                message = scope.getMessageFor(currentIndex)
                customMsgBounds.value = null
                arrowAnimDuration = message?.arrow?.animationDuration
                isArrowDelayOver = false
                /** cancelled by advance() if the step ends before it is over */
                enterAnimation.job = launch {
                    if (message?.arrow != null) {
                        /** a small delay between moving to new target and drawing the next arrow */
                        delay(animationDuration.toLong())
                        isArrowDelayOver = true
                        launch {
                            message?.arrow?.let { arrow ->
                                pathPortion.animateTo(
                                    targetValue = 1f,
                                    animationSpec = tween(
                                        durationMillis = arrowAnimDuration
                                            ?: arrow.animationDuration
                                    )
                                )
                            }
                        }
                        /* animate the arrow */
                        launch {
                            message?.arrow?.let { arrow ->
                                /** show the arrow if anim is false */
                                if (!arrow.animSize) {
                                    animArrowHead.snapTo(arrow.headSize)
                                }
                                /** move the arrow */
                                animArrow.animateTo(
                                    1f,
                                    tween(
                                        durationMillis = arrowAnimDuration
                                            ?: arrow.animationDuration
                                    )
                                )
                                /** animate the size of the arrow */
                                if (arrow.animSize) {
                                    animArrowHead.animateTo(
                                        arrow.headSize,
                                        tween(
                                            durationMillis = arrowAnimDuration
                                                ?: arrow.animationDuration
                                        )
                                    )
                                }
                            }
                        }
                    }
                    if (currentIndex == validatedInitIndex) {
                        if (isSingleGreeting) {
                            message = singleGreetingMsg
                        }
                        message?.let { msg ->
                            if (msg.msgBackground != null)
                            animMsgAlpha.animateTo(1f, tween(msg.enterAnim.duration))
                            animMsgTextAlpha.animateTo(1f, tween(msg.enterAnim.duration))
                        }
                    } else {
                        scope.showcaseEventListener?.onEvent(
                            Level.VERBOSE,
                            TAG + "index:$currentIndex enterAnim: ${message?.enterAnim}"
                        )
                        message?.let { msg ->
                            when (msg.enterAnim) {
                                is MsgAnimation.FadeInOut -> {
                                    val duration = msg.enterAnim.duration
                                    if (msg.msgBackground != null)
                                    animMsgAlpha.animateTo(1f, tween(duration))
                                    animMsgTextAlpha.animateTo(1f, tween(duration))
                                }

                                is MsgAnimation.None -> {
                                    animMsgAlpha.snapTo(1f)
                                    animMsgTextAlpha.snapTo(1f)
                                }

                            }
                        }
                    }
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
                /** a late enter animation would cancel the exit animation below, see [EnterAnimation] */
                enterAnimation.cancel()
                coroutineScope.launch {

                    /** hide current arrow */
                    arrowAnimDuration?.let { duration ->
                        if (message?.arrow?.animSize == true) {
                            animArrowHead.animateTo(0f, tween(duration / 2))
                        }
                        launch {
                            animArrow.animateTo(0f, tween(duration / 2))
                        }
                        // subtracting Float.MIN_VALUE here to avoid a tiny part of the path left on IOS and Desktop
                        pathPortion.animateTo(
                            0f - Float.MIN_VALUE,
                            tween(duration / 2)
                        )
                    }
                    message?.let { msg ->
                        scope.showcaseEventListener?.onEvent(
                            Level.VERBOSE,
                            TAG + "index:$currentIndex exitAnim: ${msg.exitAnim}"
                        )
                        scope.showcaseEventListener?.onEvent(
                            Level.VERBOSE,
                            TAG + "index:$currentIndex msg: ${message?.text}"
                        )
                        when (msg.exitAnim) {
                            is MsgAnimation.FadeInOut -> {
                                val duration = msg.enterAnim.duration
                                animMsgTextAlpha.animateTo(0f, tween(duration))
                                if (msg.msgBackground != null)
                                animMsgAlpha.animateTo(0f, tween(duration))
                            }

                            is MsgAnimation.None -> {
                                animMsgTextAlpha.snapTo(0f)
                                animMsgAlpha.snapTo(0f)
                            }
                        }
                    }
                    if (showCasingItem) {
                        canvasAlpha.animateTo(
                            0f,
                            animationSpec = tween(durationMillis = animationDuration / 2, easing = FastOutSlowInEasing)
                        )
                        scope.showcaseItemFinished()
                        currentIndex = validatedInitIndex
                        return@launch
                    }
                    if (isSingleGreeting) {
                        canvasAlpha.animateTo(
                            0f,
                            animationSpec = tween(durationMillis = animationDuration / 2, easing = FastOutSlowInEasing)
                        )
                        scope.showGreetingFinished()
                        currentIndex = validatedInitIndex
                        return@launch
                    }
                    if (action == StepAction.PREVIOUS) {
                        scope.showcaseEventListener?.onEvent(
                            Level.INFO,
                            TAG + "moving back to index ${currentIndex - 1}"
                        )
                        /** move to previous item */
                        currentIndex--
                    } else if (action == StepAction.NEXT && currentIndex + 1 < scope.getHashMapSize()) {
                        scope.showcaseEventListener?.onEvent(
                            Level.INFO,
                            TAG + "moving to index ${currentIndex + 1}"
                        )
                        /** move to next item */
                        currentIndex++
                    } else {
                        /** showcase finished */
                        scope.showcaseEventListener?.onEvent(
                            Level.INFO,
                            TAG + "finished"
                        )
                        canvasAlpha.animateTo(
                            0f,
                            animationSpec = tween(durationMillis = animationDuration / 2, easing = FastOutSlowInEasing)
                        )
                        onFinish()
                        delay(resetDelay)
                        currentIndex = validatedInitIndex
                    }
                    isArrowDelayOver = false
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
                    !isTapInsideTarget(position, offset, itemSize, targetShape, TARGET_TAP_TOLERANCE_PX)
                ) {
                    scope.showcaseEventListener?.onEvent(
                        Level.VERBOSE,
                        TAG + "tap outside target ignored at $position"
                    )
                    return
                }
                /** detect taps on the screen */
                advance(StepAction.NEXT)
                scope.showcaseEventListener?.onEvent(
                    Level.VERBOSE,
                    TAG + "tapped here $position"
                )
            }

            val textMeasurer = rememberTextMeasurer()
            scope.showcaseEventListener?.onEvent(
                Level.VERBOSE,
                TAG + "index: $currentIndex offset :$offset"
            )
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .semantics { testTag = "canvas" }
                    /** a locked step must leave the target touchable: no pointer-input node here */
                    .then(
                        if (isStepLocked) Modifier else Modifier.pointerInput(Unit) {
                            detectTapGestures { onOverlayTap(it) }
                        }
                    ),
                onDraw = {

                    /** make transparent background path around the target composable */
                    if (currentIndex == 0 || isSingleGreeting) {
                        // Draw a full canvas without any cutout for greeting or index 0
                        drawRect(
                            color = colors.overlayColor.copy(alpha = colors.overlayColor.alpha * canvasAlpha.value),
                            size = size
                        )
                    } else {
                        when (targetShape) {
                            TargetShape.RECTANGLE -> {
                                // Create a rectangular path around the target
                                val showcasePath = Path().apply {
                                    lineTo(size.width, 0f)
                                    lineTo(size.width, size.height)
                                    lineTo(offset.x + itemSize.width, size.height)
                                    lineTo(offset.x + itemSize.width, 0f)
                                    moveTo(offset.x + itemSize.width, offset.y + itemSize.height)
                                    lineTo(offset.x + itemSize.width, size.height)
                                    lineTo(0f, size.height)
                                    lineTo(0f, offset.y + itemSize.height)
                                    close()
                                    moveTo(0f, 0f)
                                    lineTo(offset.x, 0f)
                                    lineTo(offset.x, offset.y + itemSize.height)
                                    lineTo(0f, offset.y + itemSize.height)
                                    close()
                                    moveTo(offset.x, 0f)
                                    lineTo(offset.x + itemSize.width, 0f)
                                    lineTo(offset.x + itemSize.width, offset.y)
                                    lineTo(offset.x, offset.y)
                                    close()
                                }
                                /** draw the showcasePath */
                                drawPath(
                                    path = showcasePath,
                                    color = colors.overlayColor.copy(alpha = colors.overlayColor.alpha * canvasAlpha.value),
                                )
                            }

                            TargetShape.CIRCLE -> {
                                // Calculate the center and radius of the circle
                                val centerX = offset.x + itemSize.width / 2
                                val centerY = offset.y + itemSize.height / 2
                                val radius = maxOf(itemSize.width, itemSize.height) / 2

                                // Create paths for the outer and inner areas
                                val outerPath = Path().apply {
                                    // Draw a rectangle covering the entire canvas
                                    addRect(Rect(0f, 0f, size.width, size.height))
                                }

                                // Create a path for the target area (circle)
                                val targetPath = Path().apply {
                                    addOval(
                                        Rect(
                                            centerX - radius,
                                            centerY - radius,
                                            centerX + radius,
                                            centerY + radius
                                        )
                                    )
                                }

                                // Create a combined path with a hole
                                val showcasePath = Path().apply {
                                    addPath(outerPath)
                                    op(outerPath, targetPath, androidx.compose.ui.graphics.PathOperation.Difference)
                                }

                                // Draw the path
                                drawPath(
                                    path = showcasePath,
                                    color = colors.overlayColor.copy(alpha = colors.overlayColor.alpha * canvasAlpha.value),
                                )
                            }

                            TargetShape.ROUNDED_RECTANGLE -> {
                                // Create paths for the outer and inner areas
                                val outerPath = Path().apply {
                                    // Draw a rectangle covering the entire canvas
                                    addRect(Rect(0f, 0f, size.width, size.height))
                                }

                                // Create a path for the target area (rounded rectangle)
                                val targetPath = Path().apply {
                                    addRoundedTarget(Rect(offset, itemSize), cornerRadius.toPx())
                                }

                                // Create a combined path with a hole
                                val showcasePath = Path().apply {
                                    addPath(outerPath)
                                    op(outerPath, targetPath, androidx.compose.ui.graphics.PathOperation.Difference)
                                }

                                // Draw the path
                                drawPath(
                                    path = showcasePath,
                                    color = colors.overlayColor.copy(alpha = colors.overlayColor.alpha * canvasAlpha.value),
                                )
                            }
                        }
                    }

                    // Calculate the center and radius of the circle for target
                    val centerX = offset.x + itemSize.width / 2
                    val centerY = offset.y + itemSize.height / 2
                    val radius = maxOf(itemSize.width, itemSize.height) / 2

                    message?.let { msg ->
                        val isGreetingStep = currentIndex == 0 || isSingleGreeting
                        val cardRect: Rect
                        val messageSide: MessageSide
                        val textResult: TextLayoutResult?
                        var textOffset = Offset.Zero
                        if (msg.content != null) {
                            /** custom content is composed as a sibling of this canvas (see below) and
                             * reports its bounds through customMsgBounds so the arrow can start from its edge */
                            val placed = customMsgBounds.value ?: return@let
                            cardRect = placed.bounds
                            messageSide = placed.side
                            textResult = null
                        } else {
                            /**
                            Create a measurer for the message with limited constraints and a 'Visible'
                            overflow to make the text go to a new line if the screen width doesn't fit
                            one line.
                             */
                            val result = textMeasurer.measure(
                                msg.text,
                                style = msg.textStyle,
                                overflow = TextOverflow.Visible,
                                constraints = Constraints(0, max(1, constraints.maxWidth - MESSAGE_MAX_WIDTH_INSET_PX))
                            )
                            /** Determine if message will be shown on top or below target and its horizontal alignment */
                            val placement = computeShowcaseMessagePlacement(
                                messageSize = result.size,
                                targetPosition = scope.getPositionFor(currentIndex),
                                targetSize = scope.getSizeFor(currentIndex),
                                canvasSize = size,
                                gravity = msg.gravity,
                                arrow = msg.arrow,
                                targetShape = targetShape,
                                isGreeting = isGreetingStep,
                                anchorNearEdge = false,
                                log = { scope.showcaseEventListener?.onEvent(Level.INFO, TAG + "index: $currentIndex $it") }
                            )
                            textOffset = placement.offset
                            messageSide = placement.side
                            textResult = result
                            cardRect = Rect(
                                Offset(textOffset.x - CARD_INSET_PX, textOffset.y - CARD_INSET_PX),
                                IntSize(
                                    result.size.width + 36,
                                    result.size.height + 36
                                ).toSize()
                            )

                            // Draw the message card
                            if (msg.roundedCorner == 0.dp) {
                                drawRect(
                                    msg.msgBackground ?: Color.Transparent,
                                    topLeft = cardRect.topLeft,
                                    size = cardRect.size,
                                    alpha = animMsgAlpha.value
                                )
                            } else {
                                drawRoundRect(
                                    msg.msgBackground ?: Color.Transparent,
                                    topLeft = cardRect.topLeft,
                                    size = cardRect.size,
                                    cornerRadius = CornerRadius(msg.roundedCorner.value),
                                    alpha = animMsgAlpha.value
                                )
                            }
                        }
                        val cardOffset = cardRect.topLeft
                        val cardSize = cardRect.size

                        // Draw the arrow if needed
                        val hasArrowHead = msg.arrow?.head != null
                        val arrowHeadMargin = (msg.arrow?.headSize ?: Arrow().headSize) + 25

                        if (currentIndex > 0 && shouldDrawArrow) {
                            // Calculate card center
                            val cardCenterX = cardOffset.x + cardSize.width / 2
                            val cardCenterY = cardOffset.y + cardSize.height / 2
                            /** custom content: the arrow always leaves from the edge facing the target
                             * so it never runs behind the composable, whichever Side it lands on */
                            val nearEdgeStart = if (messageSide == MessageSide.Below) {
                                Offset(cardCenterX, cardOffset.y)
                            } else {
                                Offset(cardCenterX, cardOffset.y + cardSize.height)
                            }
                            /** draw arrow line */
                            val arrowPath = Path().apply {
                                if (msg.arrow?.curved == true) {
                                    // Start from the message card based on targetFrom direction
                                    val start = if (msg.content != null) nearEdgeStart else when (msg.arrow?.targetFrom) {
                                        // bottom center of the message card
                                        Side.Top -> Offset(cardCenterX, cardOffset.y + cardSize.height)
                                        // top center of the message card
                                        Side.Bottom -> Offset(cardCenterX, cardOffset.y)
                                        // right center of the message card
                                        Side.Left -> Offset(cardOffset.x + cardSize.width, cardCenterY)
                                        // left center of the message card
                                        Side.Right -> Offset(cardOffset.x, cardCenterY)
                                        // Default to bottom center if targetFrom is not specified
                                        else -> Offset(cardCenterX, cardOffset.y + cardSize.height)
                                    }
                                    moveTo(start.x, start.y)

                                    val xPoint =
                                        if ((offset.x + itemSize.width + 80) > size.width) {
                                            offset.x - 80
                                        } else {
                                            (offset.x + itemSize.width + 50)
                                        }
                                    quadraticTo(
                                        size.width / 2, offset.y + itemSize.height + 0,
                                        xPoint,
                                        offset.y + (itemSize.height / 2)
                                    )
                                } else {
                                    /** where the straight line starts: the card edge for circles and custom
                                     * content, a fixed distance from the target otherwise */
                                    val lineStart: Offset? = if (msg.content != null) nearEdgeStart else when (msg.arrow?.targetFrom) {
                                        Side.Top -> if (targetShape == TargetShape.CIRCLE) {
                                            Offset(cardCenterX, cardOffset.y + cardSize.height)
                                        } else {
                                            Offset(offset.x + (itemSize.width / 2), offset.y - 180)
                                        }

                                        Side.Bottom -> if (targetShape == TargetShape.CIRCLE) {
                                            Offset(cardCenterX, cardOffset.y)
                                        } else {
                                            Offset(offset.x + (itemSize.width / 2), offset.y + (itemSize.height + 200))
                                        }

                                        Side.Left -> if (targetShape == TargetShape.CIRCLE) {
                                            Offset(cardOffset.x + cardSize.width, cardCenterY)
                                        } else {
                                            Offset(offset.x - 200, offset.y + (itemSize.height / 2))
                                        }

                                        Side.Right -> if (targetShape == TargetShape.CIRCLE) {
                                            Offset(cardOffset.x, cardCenterY)
                                        } else {
                                            Offset(offset.x + (itemSize.width + 200), offset.y + (itemSize.height / 2))
                                        }

                                        null -> null
                                    }
                                    /** custom content follows Gravity.Auto: when the message ended up on the
                                     * other side of the target than targetFrom assumes, point at the facing
                                     * edge instead of drawing the line through the target */
                                    val effectiveTargetFrom = when {
                                        msg.content == null -> msg.arrow?.targetFrom
                                        msg.arrow?.targetFrom == Side.Top && messageSide == MessageSide.Below -> Side.Bottom
                                        msg.arrow?.targetFrom == Side.Bottom && messageSide == MessageSide.Above -> Side.Top
                                        else -> msg.arrow?.targetFrom
                                    }
                                    /** where the straight line ends: just outside the target edge chosen by targetFrom */
                                    val lineEnd: Offset? = when (effectiveTargetFrom) {
                                        Side.Top -> if (targetShape == TargetShape.CIRCLE) {
                                            // For circle, point to the top edge of the circle
                                            Offset(centerX, if (hasArrowHead) centerY - radius - arrowHeadMargin else centerY - radius)
                                        } else {
                                            Offset(offset.x + (itemSize.width / 2), if (hasArrowHead) offset.y - arrowHeadMargin else offset.y)
                                        }

                                        Side.Bottom -> if (targetShape == TargetShape.CIRCLE) {
                                            // For circle, point to the bottom edge of the circle
                                            Offset(centerX, if (hasArrowHead) centerY + radius + arrowHeadMargin else centerY + radius)
                                        } else {
                                            Offset(
                                                offset.x + (itemSize.width / 2),
                                                if (hasArrowHead) offset.y + itemSize.height + arrowHeadMargin else offset.y + itemSize.height
                                            )
                                        }

                                        Side.Left -> if (targetShape == TargetShape.CIRCLE) {
                                            // For circle, point to the left edge of the circle
                                            Offset(if (hasArrowHead) centerX - radius - arrowHeadMargin else centerX - radius, centerY)
                                        } else {
                                            Offset(if (hasArrowHead) offset.x - arrowHeadMargin else offset.x, offset.y + (itemSize.height / 2))
                                        }

                                        Side.Right -> if (targetShape == TargetShape.CIRCLE) {
                                            // For circle, point to the right edge of the circle
                                            Offset(if (hasArrowHead) centerX + radius + arrowHeadMargin else centerX + radius, centerY)
                                        } else {
                                            Offset(
                                                if (hasArrowHead) offset.x + itemSize.width + arrowHeadMargin else offset.x + itemSize.width,
                                                offset.y + (itemSize.height / 2)
                                            )
                                        }

                                        null -> null
                                    }
                                    if (lineStart != null && lineEnd != null) {
                                        moveTo(lineStart.x, lineStart.y)
                                        lineTo(lineEnd.x, lineEnd.y)
                                    }
                                }
                            }

                            val outPath = Path()
                            val pos = FloatArray(2)
                            val tan = FloatArray(2)
                            PathMeasure().apply {
                                setPath(arrowPath, false)
                                getSegment(0f, pathPortion.value * length, outPath, true)
                                getPosition(pathPortion.value * length).apply {
                                    pos[0] = x
                                    pos[1] = y
                                }
                                getTangent(pathPortion.value * length).apply {
                                    tan[0] = x
                                    tan[1] = y
                                }
                                scope.showcaseEventListener?.onEvent(
                                    Level.VERBOSE,
                                    TAG + "pos:${pos} tan:${tan}"
                                )
                            }
                            drawPath(
                                path = outPath,
                                color = arrowColor,
                                style = Stroke(width = lineThickness.toPx(), cap = StrokeCap.Round)
                            )

                            /** draw the arrow head (and rotate if needed) */
                            val arrowSize = animArrowHead.value
                            val x = pos[0]
                            val y = pos[1]
                            val degrees = -atan2(tan[0], tan[1]) * (180f / PI.toFloat()) - 180f
                            scope.showcaseEventListener?.onEvent(
                                Level.VERBOSE,
                                TAG + "max canvas: x:${size.width} y:${size.height}"
                            )
                            when (msg.arrow?.head) {
                                Head.CIRCLE -> {
                                    drawCircle(
                                        center = Offset(x, y),
                                        color = arrowColor,
                                        alpha = animArrow.value,
                                        radius = arrowSize
                                    )

                                }

                                Head.TRIANGLE -> {
                                    rotate(degrees = degrees, pivot = Offset(x, y)) {
                                        drawPath(
                                            path = Path().apply {
                                                moveTo(x, y - arrowSize)
                                                lineTo(x - arrowSize, y + arrowSize)
                                                lineTo(x + arrowSize, y + arrowSize)
                                                close()
                                            },
                                            color = arrowColor,
                                            alpha = animArrow.value
                                        )
                                    }
                                }

                                Head.SQUARE -> {
                                    drawRect(
                                        topLeft = Offset(
                                            x - arrowSize.div(2),
                                            y - arrowSize.div(2)
                                        ),
                                        color = arrowColor,
                                        alpha = animArrow.value,
                                        size = Size(arrowSize, arrowSize)
                                    )
                                }

                                Head.ROUND_SQUARE -> {
                                    val radius = arrowSize.div(4)
                                    drawRoundRect(
                                        topLeft = Offset(
                                            x - arrowSize.div(2),
                                            y - arrowSize.div(2)
                                        ),
                                        color = arrowColor,
                                        alpha = animArrow.value,
                                        size = Size(arrowSize, arrowSize),
                                        cornerRadius = CornerRadius(radius, radius)
                                    )
                                }

                                null -> Unit

                            }
                        }

                        // Draw the message text
                        if (textResult != null) {
                            drawText(
                                textResult,
                                topLeft = textOffset,
                                alpha = animMsgTextAlpha.value
                            )
                        }
                    }
                }
            )
            /** while the step is locked, swallow touches everywhere except over the target so the
             * target itself receives them (see [TouchBlockerWithHole]) */
            if (isStepLocked) {
                TouchBlockerWithHole(
                    hole = { Rect(offset, itemSize) },
                    onTap = { onOverlayTap(it) }
                )
            }
            /** Custom message content is composed on top of the canvas. It is only in the tree
             * while visible so an invisible button can't swallow taps between steps; while it is
             * composed, taps on non-clickable parts fall through to the canvas above. */
            val customContent = message?.content
            val customMsgVisible by remember { derivedStateOf { animMsgTextAlpha.value > 0f } }
            if (customContent != null && customMsgVisible) {
                val isGreetingStep = currentIndex == 0 || isSingleGreeting
                Box(
                    modifier = Modifier
                        .layout { measurable, layoutConstraints ->
                            val placeable = measurable.measure(
                                Constraints(
                                    maxWidth = max(1, layoutConstraints.maxWidth - MESSAGE_MAX_WIDTH_INSET_PX),
                                    maxHeight = layoutConstraints.maxHeight
                                )
                            )
                            // Fill the whole overlay so the placed child stays inside our bounds
                            // (otherwise its buttons would not be hit-testable).
                            layout(layoutConstraints.maxWidth, layoutConstraints.maxHeight) {
                                // Placement is computed here, not in measure, so an animated
                                // target only re-places the content instead of re-measuring it.
                                val placement = computeShowcaseMessagePlacement(
                                    messageSize = IntSize(placeable.width, placeable.height),
                                    targetPosition = scope.getPositionFor(currentIndex),
                                    targetSize = scope.getSizeFor(currentIndex),
                                    canvasSize = Size(
                                        layoutConstraints.maxWidth.toFloat(),
                                        layoutConstraints.maxHeight.toFloat()
                                    ),
                                    gravity = message?.gravity ?: Gravity.Auto,
                                    arrow = message?.arrow,
                                    targetShape = targetShape,
                                    isGreeting = isGreetingStep,
                                    anchorNearEdge = true,
                                    log = {
                                        scope.showcaseEventListener?.onEvent(
                                            Level.INFO,
                                            TAG + "index: $currentIndex $it"
                                        )
                                    }
                                )
                                val placed = PlacedMessage(
                                    Rect(placement.offset, Size(placeable.width.toFloat(), placeable.height.toFloat())),
                                    placement.side
                                )
                                if (customMsgBounds.value != placed) customMsgBounds.value = placed
                                placeable.place(placement.offset.round())
                            }
                        }
                        .graphicsLayer { alpha = animMsgTextAlpha.value }
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
                                    onOverlayTap(up.position + (customMsgBounds.value?.bounds?.topLeft ?: Offset.Zero))
                                }
                            }
                        }
                    ) {
                        msgScope.customContent()
                    }
                }
            }
            scope.showcaseEventListener?.onEvent(
                Level.VERBOSE,
                TAG + "calc: ${offset.y + itemSize.height - (maxHeight.value / 2)}"
            )
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
        "ShowcaseLayout(isShowcasing, initIndex, animationDuration, onFinish, greeting, " +
                "lineThickness, targetShape, cornerRadius, false, " +
                "ShowcaseLayoutDefaults.colors(overlayColor = if (isDarkLayout) Color.White.copy(alpha = 0.9f) else Color.Black.copy(alpha = 0.9f)), content)"
    )
)
@Composable
fun ShowcaseLayout(
    isShowcasing: Boolean,
    isDarkLayout: Boolean,
    initIndex: Int = 0,
    animationDuration: Int = 1000,
    onFinish: () -> Unit,
    greeting: ShowcaseMsg? = null,
    lineThickness: Dp = 5.dp,
    targetShape: TargetShape = TargetShape.RECTANGLE,
    cornerRadius: Dp = 16.dp,
    content: @Composable ShowcaseScope.() -> Unit
) {
    ShowcaseLayout(
        isShowcasing = isShowcasing,
        initIndex = initIndex,
        animationDuration = animationDuration,
        onFinish = onFinish,
        greeting = greeting,
        lineThickness = lineThickness,
        targetShape = targetShape,
        cornerRadius = cornerRadius,
        colors = ShowcaseLayoutDefaults.colors(
            overlayColor = if (isDarkLayout) Color.White.copy(alpha = 0.9f) else Color.Black.copy(alpha = 0.9f)
        ),
        content = content
    )
}

class ShowcaseScopeImpl(greeting: ShowcaseMsg?) : ShowcaseScope {
    private val showcaseDataHashMap = HashMap<Int, ShowcaseData>()

    /** Coordinates of the showcase layout itself, used to convert target positions from
     * root space to the layout's local space (the space the overlay canvas draws in). */
    internal var layoutCoordinatesState: State<LayoutCoordinates?> = mutableStateOf(null)

    /** Per-step `canProceed` / `onProceed`, owned (remembered) by the layout and injected here,
     * since this scope object is recreated on every recomposition. */
    internal var stepRegistry: StepRegistry = StepRegistry()
    override var showcaseEventListener: ShowcaseEventListener? = null
    private val _showcaseActionFlow = MutableStateFlow<Int?>(null)
    val showcaseActionFlow = _showcaseActionFlow.asStateFlow()
    private val _greetingActionFlow = MutableStateFlow<ShowcaseMsg?>(null)
    val greetingActionFlow = _greetingActionFlow.asStateFlow()

    @Composable
    override fun Showcase(
        index: Int,
        message: ShowcaseMsg?,
        canProceed: Boolean,
        onProceed: (() -> Unit)?,
        itemContent: @Composable () -> Unit
    ) {
        require(index >= 1) { "Index must be 1 or greater" }
        Box(modifier = Modifier.stepRegistration(index, canProceed, onProceed, stepRegistry).onGloballyPositioned {
            showcaseDataHashMap[index] = ShowcaseData(it.size, it.positionInRoot(), message, it)

            showcaseEventListener?.onEvent(Level.VERBOSE, TAG + "Index: $index")
            showcaseEventListener?.onEvent(
                Level.VERBOSE,
                TAG + "size: ${it.size} position: ${it.positionInRoot()}"
            )
            showcaseEventListener?.onEvent(
                Level.VERBOSE,
                TAG + "showcase map: $showcaseDataHashMap"
            )

        }) {

            itemContent()
        }
    }

    override fun Modifier.showcase(
        index: Int,
        message: ShowcaseMsg?,
        canProceed: Boolean,
        onProceed: (() -> Unit)?
    ): Modifier {
        require(index >= 1) { "Index must be 1 or greater" }
        return this.stepRegistration(index, canProceed, onProceed, stepRegistry).then(
            onGloballyPositioned {
                if (it.isAttached) {
                    showcaseDataHashMap[index] =
                        ShowcaseData(it.size, it.positionInRoot(), message, it)
                    showcaseEventListener?.onEvent(Level.VERBOSE, TAG + "Index: $index")
                    showcaseEventListener?.onEvent(
                        Level.VERBOSE,
                        TAG + "size: ${it.size} position: ${it.positionInRoot()}"
                    )
                    showcaseEventListener?.onEvent(
                        Level.VERBOSE,
                        TAG + "showcase map: $showcaseDataHashMap"
                    )
                }
            }
        )
    }

    override fun registerEventListener(eventListener: ShowcaseEventListener) {
        this.showcaseEventListener = eventListener
    }

    override suspend fun showcaseItem(index: Int) {
        showcaseEventListener?.onEvent(Level.DEBUG, TAG + "showcase item $index")
        _showcaseActionFlow.emit(index)
    }

    suspend fun showcaseItemFinished() {
        showcaseEventListener?.onEvent(Level.DEBUG, TAG + "showcase item finished")
        _showcaseActionFlow.emit(null)
    }

    override suspend fun showGreeting(message: ShowcaseMsg) {
        _greetingActionFlow.emit(message)
        showcaseEventListener?.onEvent(
            Level.DEBUG,
            TAG + "greeting ${message.text.text.take(10)}"
        )
    }

    suspend fun showGreetingFinished() {
        _greetingActionFlow.emit(null)
        showcaseEventListener?.onEvent(
            Level.DEBUG,
            TAG + "greeting show finished"
        )
    }

    init {
        showcaseDataHashMap[0] = ShowcaseData(IntSize(0, 0), Offset(0f, 0f), greeting)
    }

    /** Index 0 is the greeting; it has no target of its own, so only its message can change. */
    internal fun setGreeting(greeting: ShowcaseMsg?) {
        val current = showcaseDataHashMap[0]
        if (current?.message != greeting) {
            showcaseDataHashMap[0] = ShowcaseData(IntSize(0, 0), Offset(0f, 0f), greeting)
        }
    }

    fun getSizeFor(index: Int): Size {
        val size = showcaseDataHashMap[index]?.size?.toSize() ?: Size(0f, 0f)
        showcaseEventListener?.onEvent(Level.VERBOSE, TAG + "showcase map size: $size")
        return size
    }

    fun getPositionFor(index: Int): Offset {
        val data = showcaseDataHashMap[if (index == 0) 1 else index] ?: return Offset(0f, 0f)
        return positionInLayout(data)
    }

    /** The stored position is relative to the composition root, but the overlay canvas draws
     * in the layout's local space. When the layout doesn't sit at the root origin (status bar
     * padding, app bars, ...) the two spaces differ, so convert; the raw root position is only
     * a fallback for when either set of coordinates is detached. */
    private fun positionInLayout(data: ShowcaseData): Offset {
        val layoutCoordinates = layoutCoordinatesState.value
        val targetCoordinates = data.coordinates
        return if (layoutCoordinates?.isAttached == true && targetCoordinates?.isAttached == true) {
            layoutCoordinates.localPositionOf(targetCoordinates, Offset.Zero)
        } else {
            data.position
        }
    }

    fun getHashMapSize(): Int {
        return showcaseDataHashMap.size
    }

    fun getMessageFor(currentIndex: Int): ShowcaseMsg? {
        return showcaseDataHashMap[currentIndex]?.message
    }

}
