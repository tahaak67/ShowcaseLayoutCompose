package ly.com.tahaben.showcase_layout_compose.ui

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateOffsetAsState
import androidx.compose.animation.core.animateSizeAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import kotlinx.coroutines.delay
import ly.com.tahaben.showcase_layout_compose.model.Gravity
import ly.com.tahaben.showcase_layout_compose.model.ShowcaseData
import ly.com.tahaben.showcase_layout_compose.model.ShowcaseMsg
import ly.com.tahaben.showcase_layout_compose.model.Side
import kotlin.math.PI
import kotlin.math.atan2

/**
 * Created by Taha Ben Ashur (https://github.com/tahaak67) on 1,August,2022
 */

private const val TAG = "ShowcaseLayout"

/**
 * ShowcaseLayout
 *
 * @param isShowcasing to determine if showcase is starting or not.
 * @param isDarkLayout if true the showcase view will be white instead of black.
 * @param initKey the initial value of counter, set this to 1 if you don't want a greeting screen before showcasing target.
 * @param animationDuration total animation time taken when switching from current to next target in milliseconds.
 * @param onTap what happens when all items are showcased.
 * @param greeting greeting message, leave initKey at 0 if you want to use this.
 **/

@Composable
fun ShowcaseLayout(
    isShowcasing: Boolean,
    isDarkLayout: Boolean = false,
    initKey: Int = 0,
    animationDuration: Int = 1000,
    onTap: () -> Unit,
    greeting: ShowcaseMsg? = null,
    content: @Composable ShowcaseScope.() -> Unit
) {
    var currentKey by remember {
        mutableStateOf(initKey)
    }
    val scope = ShowcaseScopeImpl(greeting)
    scope.content()
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        AnimatedVisibility(isShowcasing, enter = fadeIn(), exit = fadeOut()) {
            val offset by animateOffsetAsState(
                targetValue = scope.getPositionFor(currentKey),
                animationSpec = tween(animationDuration)
            )
            val itemSize by animateSizeAsState(
                targetValue = scope.getSizeFor(currentKey),
                animationSpec = tween(animationDuration)
            )
            val message = scope.getMessageFor(currentKey)
            val pathPortion = remember {
                androidx.compose.animation.core.Animatable(initialValue = 0f)
            }

            var isDelayOver by remember { mutableStateOf(false) }
            val shouldDrawArrow = (message?.arrow != null && isDelayOver)
            val arrowColor = message?.arrow?.color ?: Color.White
            val density = LocalDensity.current

            //to animate current arrow line
            LaunchedEffect(key1 = currentKey) {
                pathPortion.snapTo(0f)
                isDelayOver = false
                //a small delay between moving to new target and drawing the next arrow
                delay(1050)
                isDelayOver = true
                if (message?.arrow != null) {
                    pathPortion.animateTo(
                        targetValue = 1f,
                        animationSpec = tween(
                            durationMillis = message.arrow.animationDuration
                        )
                    )
                }
            }

            Log.d(TAG, "offset :$offset")
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .semantics { testTag = "canvas" }
                    .pointerInput(isShowcasing) {
                        detectTapGestures {
                            //detect taps on the screen
                            if (currentKey + 1 < scope.getHashMapSize()) {
                                Log.d(TAG, "current key +")
                                //hide current arrow
                                isDelayOver = false
                                //move to next item
                                currentKey++
                            } else {
                                //showcase finished
                                Log.d(TAG, "on tap")
                                onTap()
                            }
                            Log.d(TAG, "tapped here $it")
                        }
                    },
                onDraw = {

                    //make transparent background path around the target composable
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
                    //draw the showcasePath
                    drawPath(
                        path = showcasePath,
                        color = if (isDarkLayout) Color.White else Color.Black,
                        alpha = 0.80f,
                    )
                    val hasArrowHead = message?.arrow?.hasHead == true

                    if (currentKey > 0 && shouldDrawArrow) {
                        //draw arrow line
                        val arrowPath = Path().apply {
                            if (message?.arrow?.animateFromMsg == true) {
                                moveTo(
                                    (maxWidth / 2).toPx(),
                                    offset.y + itemSize.height + 200
                                )
                                val xPoint = if ((offset.x + itemSize.width + 80) > size.width) {
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
                                            if (hasArrowHead) offset.y - 30 else offset.y
                                        )
                                    }
                                    Side.Bottom -> {
                                        moveTo(
                                            offset.x + (itemSize.width / 2),
                                            offset.y + (itemSize.height + 200)
                                        )
                                        lineTo(
                                            offset.x + (itemSize.width / 2),
                                            if (hasArrowHead) offset.y + itemSize.height + 30 else offset.y + itemSize.height
                                        )
                                    }
                                    Side.Left -> {
                                        moveTo(
                                            offset.x - 200,
                                            offset.y + (itemSize.height / 2)
                                        )
                                        lineTo(
                                            if (hasArrowHead) offset.x - 30 else offset.x,
                                            offset.y + (itemSize.height / 2)
                                        )
                                    }
                                    Side.Right -> {
                                        moveTo(
                                            offset.x + (itemSize.width + 200),
                                            offset.y + (itemSize.height / 2)
                                        )
                                        lineTo(
                                            if (hasArrowHead) offset.x + itemSize.width + 30 else offset.x + itemSize.width,
                                            offset.y + (itemSize.height / 2)
                                        )
                                    }
                                    null -> Unit
                                }
                            }
                        }

                        val outPath = android.graphics.Path()
                        val pos = FloatArray(2)
                        val tan = FloatArray(2)
                        android.graphics.PathMeasure().apply {
                            setPath(arrowPath.asAndroidPath(), false)
                            getSegment(0f, pathPortion.value * length, outPath, true)
                            getPosTan(pathPortion.value * length, pos, tan)
                        }
                        drawPath(
                            path = outPath.asComposePath(),
                            color = arrowColor,
                            style = Stroke(width = 5.dp.toPx(), cap = StrokeCap.Round)
                        )

                        //draw the arrow head (and rotate if needed)
                        if (message?.arrow?.hasHead == true) {
                            val x = pos[0]
                            val y = pos[1]
                            val degrees = -atan2(tan[0], tan[1]) * (180f / PI.toFloat()) - 180f
                            Log.d(TAG, "max canvas: x:${size.width} y:${size.height}")
                            rotate(degrees = degrees, pivot = Offset(x, y)) {
                                drawPath(
                                    path = Path().apply {
                                        moveTo(x, y - 30f)
                                        lineTo(x - 30f, y + 60f)
                                        lineTo(x + 30f, y + 60f)
                                        close()
                                    },
                                    color = arrowColor
                                )
                            }
                        }

                    }
                }
            )


            message?.let { msg ->
                Log.d(TAG, "max: x:${maxWidth} y:${maxHeight}")
                val xDp = if (currentKey == 0) (0.dp) else with(density) {
                    maxWidth / 2
                }
                val yDp = if (currentKey == 0) (maxHeight / 2) else with(density) {
                    //Determine if message will be shown on top or below target
                    when (msg.gravity) {
                        Gravity.Top -> {
                            scope.getPositionFor(currentKey).y.toDp() - scope.getSizeFor(currentKey).height.toDp() - 10.toDp()
                        }
                        Gravity.Bottom -> {
                            scope.getPositionFor(currentKey).y.toDp() + scope.getSizeFor(currentKey).height.toDp() + 150.toDp()
                        }
                        Gravity.Auto -> {
                            val topPosition =
                                scope.getPositionFor(currentKey).y.toDp() - scope.getSizeFor(
                                    currentKey
                                ).height.toDp() - 150.toDp()
                            if (topPosition < 0.dp) {
                                Log.d(TAG, "its true")
                                scope.getPositionFor(currentKey).y.toDp() + scope.getSizeFor(
                                    currentKey
                                ).height.dp + 24.dp
                            } else {
                                Log.d(TAG, "its Not true")
                                topPosition
                            }
                        }
                    }
                }
                val xAnim by animateDpAsState(targetValue = xDp)
                val yAnim by animateDpAsState(targetValue = yDp)
                Log.d(TAG, "msg x: $xDp, y:$yDp")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {

                    Card(
                        Modifier
                            .offset(y = yAnim)
                            .padding(horizontal = 16.dp)
                            .clip(RoundedCornerShape(msg.roundedCorner)),
                        backgroundColor = msg.msgBackground ?: Color.Transparent,
                        elevation = if (msg.msgBackground != null) 18.dp else 0.dp
                    ) {
                        Text(
                            text = msg.text,
                            style = msg.textStyle,
                            modifier = Modifier
                                .padding(12.dp),
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            Log.d(TAG, "calc : ${offset.y + itemSize.height - (maxHeight.value / 2)}")
        }
    }
}


class ShowcaseScopeImpl(greeting: ShowcaseMsg?) : ShowcaseScope {
    private val array = HashMap<Int, ShowcaseData>()

    @Composable
    override fun Showcase(
        k: Int,
        message: ShowcaseMsg?,
        itemContent: @Composable () -> Unit
    ) {
        Box(modifier = Modifier.onGloballyPositioned {
            Log.d(TAG, "putt $k")
            Log.d(TAG, "putt ${it.size} p=${it.positionInRoot()}")
            array[k] = ShowcaseData(it.size, it.positionInRoot(), message)
            Log.d(TAG, "array = $array")
        }) {

            itemContent()
        }
    }

    init {
        array[0] = ShowcaseData(IntSize(0, 0), Offset(0f, 0f), greeting)
    }

    fun getSizeFor(k: Int): Size {
        val s = array[k]?.size?.toSize() ?: Size(0f, 0f)
        Log.d(TAG, "got size: $s")
        return s
    }

    fun getPositionFor(k: Int): Offset {
        val p = array[k]?.position ?: Offset(0f, 0f)
        Log.d(TAG, "get position: $p")
        return p
    }

    fun getHashMapSize(): Int {
        return array.size
    }

    fun getMessageFor(currentKey: Int): ShowcaseMsg? {
        return array[currentKey]?.message
    }
}
