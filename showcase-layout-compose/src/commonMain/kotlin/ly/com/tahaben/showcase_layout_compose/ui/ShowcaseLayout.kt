package ly.com.tahaben.showcase_layout_compose.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ly.com.tahaben.showcase_layout_compose.domain.Level
import ly.com.tahaben.showcase_layout_compose.domain.ShowcaseEventListener
import ly.com.tahaben.showcase_layout_compose.model.*
import kotlin.math.*

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

/**
 * ShowcaseLayout
 *
 * @param isShowcasing to determine if showcase is starting or not.
 * @param isDarkLayout if true the showcase view will be white instead of black.
 * @param initIndex the initial value of counter, set this to 1 if you don't want a greeting screen before showcasing target.
 * @param animationDuration total animation time taken when switching from current to next target in milliseconds.
 * @param onFinish what happens when all items are showcased.
 * @param greeting greeting message to be shown before showcasing the first composable, leave [initIndex] at 0 if you want to use this.
 * @param lineThickness thickness of the arrow line in dp.
 **/

@Composable
fun ShowcaseLayout(
    isShowcasing: Boolean,
    isDarkLayout: Boolean = false,
    initIndex: Int = 0,
    animationDuration: Int = 1000,
    onFinish: () -> Unit,
    greeting: ShowcaseMsg? = null,
    lineThickness: Dp = 5.dp,
    circleMode: Boolean = true,
    content: @Composable ShowcaseScope.() -> Unit
) {
    var currentIndex by remember {
        mutableIntStateOf(initIndex)
    }
    val currentContent by rememberUpdatedState(content)
    val resetDelay by derivedStateOf { animationDuration.toLong() + INDEX_RESET_DELAY }
    val scope = ShowcaseScopeImpl(greeting)
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
                currentIndex = showcaseItem.value ?: initIndex
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
                currentIndex = 0
                true
            } else {
                false
            }
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        if (!circleMode) {
            AnimatedVisibility(
                isShowcasing || showCasingItem || isSingleGreeting,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
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
                val coroutineScope = rememberCoroutineScope()
                var arrowAnimDuration by remember { mutableStateOf(message?.arrow?.animationDuration) }
                val animMsgTextAlpha = remember { Animatable(0f) }
                val animMsgAlpha = remember { Animatable(0f) }
                val animArrow = remember { Animatable(0f) }
                val animArrowHead = remember { Animatable(0f) }


                /** to animate current arrow line */
                LaunchedEffect(key1 = currentIndex) {
                    message = scope.getMessageFor(currentIndex)
                    arrowAnimDuration = message?.arrow?.animationDuration
                    isArrowDelayOver = false
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
                    if (currentIndex == 0) {
                        if (isSingleGreeting) {
                            message = singleGreetingMsg
                        }
                        message?.let { msg ->
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

                val textMeasurer = rememberTextMeasurer()
                scope.showcaseEventListener?.onEvent(
                    Level.VERBOSE,
                    TAG + "index: $currentIndex offset :$offset"
                )
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .semantics { testTag = "canvas" }
                        .pointerInput(isShowcasing) {
                            detectTapGestures {
                                /** detect taps on the screen */
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
                                                animMsgAlpha.animateTo(0f, tween(duration))
                                            }

                                            is MsgAnimation.None -> {
                                                animMsgTextAlpha.snapTo(0f)
                                                animMsgAlpha.snapTo(0f)
                                            }
                                        }
                                    }
                                    if (showCasingItem) {
                                        scope.showcaseItemFinished()
                                        delay(resetDelay)
                                        currentIndex = initIndex
                                        return@launch
                                    }
                                    if (isSingleGreeting) {
                                        scope.showGreetingFinished()
                                        delay(resetDelay)
                                        currentIndex = initIndex
                                        return@launch
                                    }
                                    if (currentIndex + 1 < scope.getHashMapSize()) {
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
                                        onFinish()
                                        delay(resetDelay)
                                        currentIndex = initIndex
                                    }
                                    isArrowDelayOver = false
                                }
                                scope.showcaseEventListener?.onEvent(
                                    Level.VERBOSE,
                                    TAG + "tapped here $it"
                                )
                            }
                        },
                    onDraw = {

                        /** make transparent background path around the target composable */
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
                            color = if (isDarkLayout) Color.White else Color.Black,
                            alpha = 0.80f,
                        )
                        val hasArrowHead = message?.arrow?.head != null
                        val arrowHeadMargin = (message?.arrow?.headSize ?: Arrow().headSize) + 25

                        if (currentIndex > 0 && shouldDrawArrow) {
                            /** draw arrow line */
                            val arrowPath = Path().apply {
                                if (message?.arrow?.curved == true) {
                                    moveTo(
                                        (maxWidth / 2).toPx(),
                                        offset.y + itemSize.height + 200
                                    )
                                    val xPoint =
                                        if ((offset.x + itemSize.width + 80) > size.width) {
                                            offset.x - 80
                                        } else {
                                            (offset.x + itemSize.width + 50)
                                        }
                                    quadraticBezierTo(
                                        (size.width / 2),
                                        offset.y + itemSize.height + 0,
                                        xPoint,
                                        offset.y + (itemSize.height / 2)
                                    )
                                } else {
                                    when (message?.arrow?.targetFrom) {
                                        Side.Top -> {
                                            moveTo(
                                                offset.x + (itemSize.width / 2),
                                                offset.y - 200
                                            )
                                            lineTo(
                                                offset.x + (itemSize.width / 2),
                                                if (hasArrowHead) offset.y - arrowHeadMargin else offset.y
                                            )
                                        }

                                        Side.Bottom -> {
                                            moveTo(
                                                offset.x + (itemSize.width / 2),
                                                offset.y + (itemSize.height + 250)
                                            )
                                            lineTo(
                                                offset.x + (itemSize.width / 2),
                                                if (hasArrowHead) offset.y + itemSize.height + arrowHeadMargin else offset.y + itemSize.height
                                            )
                                        }

                                        Side.Left -> {
                                            moveTo(
                                                offset.x - 200,
                                                offset.y + (itemSize.height / 2)
                                            )
                                            lineTo(
                                                if (hasArrowHead) offset.x - arrowHeadMargin else offset.x,
                                                offset.y + (itemSize.height / 2)
                                            )
                                        }

                                        Side.Right -> {
                                            moveTo(
                                                offset.x + (itemSize.width + 200),
                                                offset.y + (itemSize.height / 2)
                                            )
                                            lineTo(
                                                if (hasArrowHead) offset.x + itemSize.width + arrowHeadMargin else offset.x + itemSize.width,
                                                offset.y + (itemSize.height / 2)
                                            )
                                        }

                                        null -> Unit
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
                            when (message?.arrow?.head) {
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
                        message?.let { msg ->
                            /**
                            Create a measurer for the message with limited constraints and a 'Visible'
                            overflow to make the text go to a new line if the screen width doesn't fit
                            one line.
                             */
                            val textResult = textMeasurer.measure(
                                msg.text,
                                style = msg.textStyle,
                                overflow = TextOverflow.Visible,
                                constraints = Constraints(0, max(1, constraints.maxWidth - 90))
                            )

                            /** Determine if message will be shown on top or below target */
                            val yOffset =
                                if (currentIndex == 0) (size.height / 2) else with(density) {
                                    val currentItemYPosition = scope.getPositionFor(currentIndex).y
                                    val currentItemHeight = scope.getSizeFor(currentIndex).height
                                    when (msg.gravity) {
                                        Gravity.Top -> {
                                            currentItemYPosition - 230
                                        }

                                        Gravity.Bottom -> {
                                            currentItemYPosition + currentItemHeight + 230
                                        }

                                        Gravity.Auto -> {
                                            val topPosition =
                                                currentItemYPosition - 230
                                            if (topPosition < 0) {
                                                scope.showcaseEventListener?.onEvent(
                                                    Level.INFO,
                                                    TAG + "index: $currentIndex Not enough space on top show msg on bottom"
                                                )
                                                currentItemYPosition + currentItemHeight + 230
                                            } else {
                                                scope.showcaseEventListener?.onEvent(
                                                    Level.INFO,
                                                    TAG + "index: $currentIndex message can be shown on top"
                                                )
                                                topPosition
                                            }
                                        }
                                    }
                                }
                            val halfWidth = (size.width / 2)
                            val messageWidthHalf = textResult.size.width / 2

                            /**
                            Determine the horizontal alignment of the message, we try to center it
                            to the target but if the target is on the edge of the screen the message
                            will get cut off, if that's the case we align the message Start or End to
                            the target Start or End as appropriate
                             */
                            val xOffset = if (currentIndex == 0 || msg.arrow?.curved == true) {
                                halfWidth - messageWidthHalf
                            } else {
                                val currentItemXPosition = scope.getPositionFor(currentIndex).x
                                val currentItemWidth = scope.getSizeFor(currentIndex).width
                                val currentItemXMiddlePoint =
                                    currentItemXPosition + (currentItemWidth / 2)
                                when {
                                    (currentItemXMiddlePoint < halfWidth) -> {
                                        scope.showcaseEventListener?.onEvent(
                                            Level.INFO,
                                            TAG + "index: $currentIndex layout on start half"
                                        )
                                        if ((currentItemXMiddlePoint - messageWidthHalf) < 0) {
                                            currentItemXPosition
                                        } else {
                                            currentItemXMiddlePoint - messageWidthHalf
                                        }
                                    }

                                    (currentItemXMiddlePoint == halfWidth) -> {
                                        scope.showcaseEventListener?.onEvent(
                                            Level.INFO,
                                            TAG + "index: $currentIndex layout in middle"
                                        )
                                        currentItemXMiddlePoint - messageWidthHalf
                                    }

                                    else -> {
                                        scope.showcaseEventListener?.onEvent(
                                            Level.INFO,
                                            TAG + "index: $currentIndex layout on end half"
                                        )
                                        if (currentItemXMiddlePoint + messageWidthHalf > size.width) {
                                            currentItemXPosition + currentItemWidth - textResult.size.width
                                        } else {
                                            currentItemXMiddlePoint - messageWidthHalf
                                        }
                                    }
                                }
                            }

                            val textOffset = Offset(xOffset, yOffset)
                            val cardOffset = Offset(textOffset.x - 18, textOffset.y - 18)
                            val cardSize = IntSize(
                                textResult.size.width + 36,
                                textResult.size.height + 36
                            ).toSize()
                            if (msg.roundedCorner == 0.dp) {
                                drawRect(
                                    msg.msgBackground ?: Color.Transparent,
                                    topLeft = cardOffset,
                                    size = cardSize,
                                    alpha = animMsgAlpha.value
                                )
                            } else {
                                drawRoundRect(
                                    msg.msgBackground ?: Color.Transparent,
                                    topLeft = cardOffset,
                                    size = cardSize,
                                    cornerRadius = CornerRadius(msg.roundedCorner.value),
                                    alpha = animMsgAlpha.value
                                )
                            }
                            drawText(
                                textResult,
                                topLeft = textOffset,
                                alpha = animMsgTextAlpha.value
                            )
                        }
                    }
                )
                scope.showcaseEventListener?.onEvent(
                    Level.VERBOSE,
                    TAG + "calc: ${offset.y + itemSize.height - (maxHeight.value / 2)}"
                )
            }
        }

        if (circleMode && isShowcasing) {
            val itemSize = scope.getSizeFor(currentIndex)
            val offset = scope.getPositionFor(currentIndex)
            val animatedWidth = remember { Animatable(itemSize.width) }
            val animatedHeight = remember { Animatable(itemSize.height) }

            val animatedX = remember { Animatable(offset.x) }
            val animatedY = remember { Animatable(offset.y) }
            val maxDimension =
                max(itemSize.width, itemSize.height)
            val targetRadius = maxDimension / 2f + 40f

            val outerAnimatable = remember { Animatable(0.6f) }
            val outerAlphaAnimatable = remember(currentIndex) { Animatable(0f) }

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
            LaunchedEffect(currentIndex) {
                animatedX.snapTo(offset.x)
                animatedY.snapTo(offset.y)
                animatedHeight.snapTo(0f)
                animatedWidth.snapTo(0f)
                /*launch {
                    animatedX.animateTo(offset.x)
                }
                launch {
                    animatedY.animateTo(offset.y)
                }*/
                launch {
                    animatedHeight.animateTo(itemSize.height)
                }
                launch {
                    animatedWidth.animateTo(itemSize.width)
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
                                currentIndex++
                            } else {
                                currentIndex = initIndex
                                onFinish()
                            }
                        }
                    }
            ) {
                val holeRadius = max(itemSize.width, itemSize.height).div(2)
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
                        style = msg.textStyle ?: TextStyle(color = Color.White),
                        overflow = TextOverflow.Visible,
                        constraints = Constraints(0, maxTextWidth)
                    )

                    // Store text dimensions
                    textWidth = textResult.size.width
                    textHeight = textResult.size.height

                    // Determine if text should be above or below the target
                    val textAboveY = itemCenter.y - holeRadius - textHeight - safetyPadding * 1.5f
                    val textBelowY = itemCenter.y + holeRadius + safetyPadding * 1.5f
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
                            // Inner oval (inner circle) - always at the target
                            addOval(
                                Rect(
                                    left = itemCenter.x - holeRadius,
                                    top = itemCenter.y - holeRadius,
                                    right = itemCenter.x + holeRadius,
                                    bottom = itemCenter.y + holeRadius
                                )
                            )
                        },
                        operation = PathOperation.Difference
                    )
                }

                // Draw the donut path with the dimensions that have been adjusted for text
                drawPath(
                    path = donutPath,
                    color = Color.Black.copy(alpha = 0.9f),
                    style = Fill // Fill the donut shape
                )

                // Draw the pulsing ring (outside the hole)
                val pulsePath = Path().apply {
                    op(
                        Path().apply {
                            addOval(
                                Rect(
                                    left = itemCenter.x - (holeRadius + pulseRadius.value),
                                    top = itemCenter.y - (holeRadius + pulseRadius.value),
                                    right = itemCenter.x + (holeRadius + pulseRadius.value),
                                    bottom = itemCenter.y + (holeRadius + pulseRadius.value)
                                )
                            )
                        },
                        Path().apply {
                            addOval(
                                Rect(
                                    left = itemCenter.x - holeRadius,
                                    top = itemCenter.y - holeRadius,
                                    right = itemCenter.x + holeRadius,
                                    bottom = itemCenter.y + holeRadius
                                )
                            )
                        },
                        operation = PathOperation.Difference
                    )
                }

                drawPath(
                    path = pulsePath,
                    color = Color.White.copy(alpha = pulseAlpha.value),
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
                    val textAboveY = itemCenter.y - holeRadius - textResult.size.height - safetyPadding * 1.5f
                    val textBelowY = itemCenter.y + holeRadius + safetyPadding * 1.5f
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

                    if (msg.roundedCorner == 0.dp) {
                        drawRect(
                            color = msg.msgBackground ?: Color.Black.copy(alpha = 0.7f),
                            topLeft = Offset(bgRect.left, bgRect.top),
                            size = Size(bgRect.width, bgRect.height)
                        )
                    } else {
                        drawRoundRect(
                            color = msg.msgBackground ?: Color.Black.copy(alpha = 0.7f),
                            topLeft = Offset(bgRect.left, bgRect.top),
                            size = Size(bgRect.width, bgRect.height),
                            cornerRadius = CornerRadius(msg.roundedCorner.value)
                        )
                    }

                    // Draw the text
                    drawText(
                        textResult,
                        topLeft = Offset(textX, textY)
                    )
                }
            }
        }
    }
}

class ShowcaseScopeImpl(greeting: ShowcaseMsg?) : ShowcaseScope {
    private val showcaseDataHashMap = HashMap<Int, ShowcaseData>()
    override var showcaseEventListener: ShowcaseEventListener? = null
    private val _showcaseActionFlow = MutableStateFlow<Int?>(null)
    val showcaseActionFlow = _showcaseActionFlow.asStateFlow()
    private val _greetingActionFlow = MutableStateFlow<ShowcaseMsg?>(null)
    val greetingActionFlow = _greetingActionFlow.asStateFlow()

    @Composable
    override fun Showcase(
        index: Int,
        message: ShowcaseMsg?,
        itemContent: @Composable () -> Unit
    ) {
        require(index >= 1) { "Index must be 1 or greater" }
        Box(modifier = Modifier.onGloballyPositioned {
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

    override fun Modifier.showcase(index: Int, message: ShowcaseMsg?): Modifier {
        require(index >= 1) { "Index must be 1 or greater" }
        return this.then(
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
            TAG + "greeting ${message.text.substring(0..10)}"
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

    fun getSizeFor(index: Int): Size {
        val size = showcaseDataHashMap[index]?.size?.toSize() ?: Size(0f, 0f)
        showcaseEventListener?.onEvent(Level.VERBOSE, TAG + "showcase map size: $size")
        return size
    }

    fun getCoordinatesFor(index: Int): LayoutCoordinates? {
        val coordinates = showcaseDataHashMap[index]?.coordinates
        showcaseEventListener?.onEvent(
            Level.VERBOSE,
            TAG + "showcase map coordinates: $coordinates"
        )
        return coordinates
    }

    fun getPositionFor(index: Int): Offset {
        if (index == 0) {
            return showcaseDataHashMap[1]?.position ?: Offset(0f, 0f)
        }
        val p = showcaseDataHashMap[index]?.position ?: Offset(0f, 0f)
        return p
    }

    fun getHashMapSize(): Int {
        return showcaseDataHashMap.size
    }

    fun getMessageFor(currentIndex: Int): ShowcaseMsg? {
        return showcaseDataHashMap[currentIndex]?.message
    }

}

private fun getOuterRect(contentRect: Rect, targetRect: Rect): Rect {

    val topLeftX = min(contentRect.topLeft.x, targetRect.topLeft.x)
    val topLeftY = min(contentRect.topLeft.y, targetRect.topLeft.y)
    val bottomRightX = max(contentRect.bottomRight.x, targetRect.bottomRight.x)
    val bottomRightY = max(contentRect.bottomRight.y, targetRect.bottomRight.y)

    return Rect(topLeftX, topLeftY, bottomRightX, bottomRightY)
}


private fun getOuterRadius(outerRect: Rect): Float {
    val d = sqrt(
        outerRect.height.toDouble().pow(2.0)
                + outerRect.width.toDouble().pow(2.0)
    ).toFloat()

    return (d / 2f)
}
