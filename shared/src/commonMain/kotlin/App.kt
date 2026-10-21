import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Label
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ly.com.tahaben.showcase_layout_compose.domain.Level
import ly.com.tahaben.showcase_layout_compose.domain.ShowcaseEventListener
import ly.com.tahaben.showcase_layout_compose.model.Arrow
import ly.com.tahaben.showcase_layout_compose.model.Gravity
import ly.com.tahaben.showcase_layout_compose.model.Head
import ly.com.tahaben.showcase_layout_compose.model.MsgAnimation
import ly.com.tahaben.showcase_layout_compose.model.ShowcaseMsg
import ly.com.tahaben.showcase_layout_compose.model.Side
import ly.com.tahaben.showcase_layout_compose.model.TargetShape
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import showcase_layout_compose_kmp.shared.generated.resources.Res
import showcase_layout_compose_kmp.shared.generated.resources.circle
import showcase_layout_compose_kmp.shared.generated.resources.rounded_square
import showcase_layout_compose_kmp.shared.generated.resources.square
import showcase_layout_compose_kmp.shared.generated.resources.triangle
import kotlin.math.roundToInt

@OptIn(ExperimentalResourceApi::class, ExperimentalMaterial3Api::class)
@Composable
fun App(openUrl: (String) -> Boolean, onWebLoadFinish: () -> Unit = {}) {
    LaunchedEffect(key1 = Unit, block = { onWebLoadFinish() })

    var selectedTarget by remember { mutableStateOf("Toolbar title") }
    var selectTargetMenuExpaned by remember { mutableStateOf(false) }
    var selectHeadMenuExpaned by remember { mutableStateOf(false) }
    var selectTargetShapeMenuExpaned by remember { mutableStateOf(false) }
    val clipboardManager = LocalClipboardManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    var finishedSubsequentShowcase by remember { mutableStateOf(false) }
    // State to track which layout is currently selected (ShowcaseLayout or TargetShowcaseLayout)
    var useTargetShowcaseLayout by remember { mutableStateOf(false) }
    val targets =
        mapOf(
            "Toolbar title" to 1,
            "Toolbar menu icon" to 2,
            "Showcase Button" to 3,
            "Target Dropdown menu" to 4,
            "Locked text field" to 6
        )
    val arrowHeadMap =
        mapOf(
            "Triangle" to Res.drawable.triangle,
            "Circle" to Res.drawable.circle,
            "Square" to Res.drawable.square,
            "Rounded Square" to Res.drawable.rounded_square
        )
    val targetShapeMap =
        mapOf(
            "Triangle" to Res.drawable.triangle,
            "Circle" to Res.drawable.circle,
            "Rectangle" to Res.drawable.square,
            "Rounded Rectangle" to Res.drawable.rounded_square
        )
    var selectedHead by remember { mutableStateOf("Triangle") }
    val headType by derivedStateOf {
        when (selectedHead) {
            "Triangle" -> Head.TRIANGLE
            "Circle" -> Head.CIRCLE
            "Square" -> Head.SQUARE
            "Rounded Square" -> Head.ROUND_SQUARE
            else -> null
        }
    }
    val coroutineScope = rememberCoroutineScope()
    var isShowcasing by remember { mutableStateOf(false) }
    var animateHead by remember { mutableStateOf(false) }
    var animateMsg by remember { mutableStateOf(false) }
    val msgBackground by remember { mutableStateOf(Color.Yellow) }
    var msgCornerRadius by remember { mutableStateOf(0) }
    var headSize by remember { mutableStateOf(25f) }
    var lineThinckness by remember { mutableStateOf(5) }
    var durationSliderValue by remember { mutableStateOf(500) }
    var animationDuration by remember { mutableStateOf(500) }
    var animateToNextTarget by remember { mutableStateOf(true) }
    var advanceOnTargetTapOnly by remember { mutableStateOf(true) }
    // what the user typed in the locked step's text field; step 6 can only proceed once it says "showcase"
    var typedText by remember { mutableStateOf("") }
    var targetShape by remember { mutableStateOf(TargetShape.CIRCLE) }
    val targetShapeName by derivedStateOf {
        when(targetShape){
            TargetShape.CIRCLE -> "Circle"
            TargetShape.RECTANGLE -> "Rectangle"
            TargetShape.ROUNDED_RECTANGLE -> "Rounded Rectangle"
        }
    }
    val scrollState = rememberScrollState()
    val greetingMsg = buildAnnotatedString {
        withStyle(ParagraphStyle(textAlign = TextAlign.Center)){
            append("Welcome to ")
            pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
            append("Showcase Layout Compose Demo ")
            pop()
            append("lets take you on a quick tour!")
        }
    }

    MyTheme(useDarkTheme = false) {
        ShowcaseLayoutWrapper(
            useTargetShowcaseLayout = useTargetShowcaseLayout,
            isShowcasing = isShowcasing,
            onFinish = { isShowcasing = false; finishedSubsequentShowcase = true },
            greeting = ShowcaseMsg(greetingMsg, textStyle = TextStyle(color = Color.White)),
            lineThickness = lineThinckness.dp,
            animationDuration = animationDuration,
            targetShape = targetShape,
            animateToNextTarget = animateToNextTarget,
            advanceOnTargetTapOnly = advanceOnTargetTapOnly
        ) {
            Column(modifier = Modifier.fillMaxSize().verticalScroll(scrollState)) {
                TopAppBar(title = {
                    Text(
                        modifier = Modifier
                            .showcase(
                                1, message = ShowcaseMsg(
                                    text = "This is the title of the toolbar 🤫",
                                    msgBackground = Color(0xffb7ffb3),
                                    roundedCorner = msgCornerRadius.dp,
                                    arrow = Arrow(
                                        animSize = animateHead,
                                        head = headType,
                                        headSize = headSize,
                                        animationDuration = animationDuration
                                    ),
                                    enterAnim = if (animateMsg) MsgAnimation.FadeInOut(animationDuration) else MsgAnimation.None
                                )
                            ),
                        text = "Showcase Layout Compose Demo"
                    )
                }, actions = {
                    IconButton(
                        onClick = {}, modifier = Modifier.showcase(
                            2, message = ShowcaseMsg(
                                text = "This is the menu button click here to see the menu.",
                                msgBackground = Color(0xff0042fd),
                                roundedCorner = msgCornerRadius.dp,
                                arrow = Arrow(
                                    animSize = animateHead,
                                    head = headType,
                                    headSize = headSize,
                                    curved = true,
                                    animationDuration = animationDuration
                                ),
                                textStyle = TextStyle(color = Color.White, fontWeight = FontWeight.Bold),
                                enterAnim = if (animateMsg) MsgAnimation.FadeInOut(animationDuration) else MsgAnimation.None
                            )
                        )
                    ) {
                        Icon(Icons.Default.MoreVert, contentDescription = null)
                    }
                })
                Column(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.showcase(
                            5, message = ShowcaseMsg(
                                text = "Switch between full screen showcase and target showcase from here, Try it now ;)",
                                msgBackground = Color(0xff000000),
                                textStyle = TextStyle(color = Color.White, fontWeight = FontWeight.Bold),
                            )
                        ),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("Layout Type", modifier = Modifier.width(100.dp))
                        Switch(
                            checked = useTargetShowcaseLayout,
                            onCheckedChange = { useTargetShowcaseLayout = it }
                        )
                        Text(if (useTargetShowcaseLayout) "Target Showcase Layout" else "Showcase Layout")
                    }

                    // A locked step (canProceed = false): the overlay ignores taps, touches on the field
                    // reach it (focus + soft keyboard) and the showcase moves on by itself once the
                    // condition is met.
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("Locked step", modifier = Modifier.width(100.dp))
                        OutlinedTextField(
                            modifier = Modifier.showcase(
                                6, message = ShowcaseMsg(
                                    text = "This step is locked \uD83D\uDD12 tap the field and type \"showcase\" to continue.",
                                    msgBackground = Color(0xff9c27b0),
                                    roundedCorner = msgCornerRadius.dp,
                                    textStyle = TextStyle(color = Color.White, fontWeight = FontWeight.Bold),
                                    arrow = Arrow(
                                        animSize = animateHead,
                                        head = headType,
                                        headSize = headSize,
                                        animationDuration = animationDuration
                                    ),
                                    enterAnim = if (animateMsg) MsgAnimation.FadeInOut(animationDuration) else MsgAnimation.None
                                ),
                                canProceed = typedText.trim().equals("showcase", ignoreCase = true),
                                // the step is over: drop focus and hide the keyboard the step opened
                                onProceed = {
                                    focusManager.clearFocus()
                                    keyboardController?.hide()
                                    typedText = ""
                                }
                            ),
                            value = typedText,
                            onValueChange = { typedText = it },
                            singleLine = true,
                            label = { Text("Type \"showcase\"") },
                        )
                    }

                    Row(
                        modifier = Modifier,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("Target", modifier = Modifier.width(100.dp))
                        ExposedDropdownMenuBox(
                            expanded = selectTargetMenuExpaned,
                            onExpandedChange = { selectTargetMenuExpaned = it }) {
                            OutlinedTextField(
                                modifier = Modifier.menuAnchor(type = ExposedDropdownMenuAnchorType.PrimaryNotEditable).showcase(
                                    4, message = ShowcaseMsg(
                                        text = buildAnnotatedString {
                                            append("You can choose what target to showcase form here then click the ")
                                            pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
                                            append("Showcase!")
                                            pop()
                                            append(" button")
                                        },
                                        msgBackground = Color(0xff450099),
                                        roundedCorner = msgCornerRadius.dp,
                                        arrow = Arrow(
                                            animSize = animateHead,
                                            head = headType,
                                            headSize = headSize,
                                            animationDuration = animationDuration
                                        ),
                                        textStyle = TextStyle(color = Color.White, fontWeight = FontWeight.Bold),
                                        enterAnim = if (animateMsg) MsgAnimation.FadeInOut(animationDuration) else MsgAnimation.None
                                    )
                                ),
                                value = selectedTarget,
                                onValueChange = {},
                                readOnly = true,
                                singleLine = true,
                                label = {},
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = selectTargetMenuExpaned) },
                                colors = ExposedDropdownMenuDefaults.textFieldColors(),
                            )
                            ExposedDropdownMenu(
                                expanded = selectTargetMenuExpaned,
                                onDismissRequest = { selectTargetMenuExpaned = false }
                            ) {
                                targets.forEach { (name, index) ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = name,
                                                style = MaterialTheme.typography.bodyLarge
                                            )
                                        },
                                        onClick = {
                                            selectedTarget = name
                                            selectTargetMenuExpaned = false
                                        },
                                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                                    )
                                }
                            }
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Head style", modifier = Modifier.width(100.dp))
                        ExposedDropdownMenuBox(
                            expanded = selectHeadMenuExpaned,
                            onExpandedChange = { selectHeadMenuExpaned = it }) {
                            OutlinedTextField(
                                modifier = Modifier.menuAnchor(type = ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                                value = selectedHead,
                                onValueChange = {},
                                readOnly = true,
                                singleLine = true,
                                label = { },
                                trailingIcon = {
                                    Image(
                                        modifier = Modifier.size(18.dp),
                                        painter = painterResource(arrowHeadMap[selectedHead]!!),
                                        contentDescription = null
                                    )
                                },
                                colors = ExposedDropdownMenuDefaults.textFieldColors(),
                            )
                            ExposedDropdownMenu(
                                expanded = selectHeadMenuExpaned,
                                onDismissRequest = { selectHeadMenuExpaned = false }
                            ) {
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = "Triangle",
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                    },
                                    onClick = {
                                        selectedHead = "Triangle"
                                        selectHeadMenuExpaned = false
                                    },
                                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                                    trailingIcon = {
                                        Image(
                                            modifier = Modifier.size(18.dp),
                                            painter = painterResource(Res.drawable.triangle),
                                            contentDescription = null
                                        )
                                    }
                                )
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = "Circle",
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                    },
                                    onClick = {
                                        selectedHead = "Circle"
                                        selectHeadMenuExpaned = false
                                    },
                                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                                    trailingIcon = {
                                        Image(
                                            modifier = Modifier.size(18.dp),
                                            painter = painterResource(Res.drawable.circle),
                                            contentDescription = null
                                        )
                                    }
                                )
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = "Square",
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                    },
                                    onClick = {
                                        selectedHead = "Square"
                                        selectHeadMenuExpaned = false
                                    },
                                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                                    trailingIcon = {
                                        Image(
                                            modifier = Modifier.size(18.dp),
                                            painter = painterResource(Res.drawable.square),
                                            contentDescription = null
                                        )
                                    }
                                )
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = "Rounded Square",
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                    },
                                    onClick = {
                                        selectedHead = "Rounded Square"
                                        selectHeadMenuExpaned = false
                                    },
                                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                                    trailingIcon = {
                                        Image(
                                            modifier = Modifier.size(18.dp),
                                            painter = painterResource(Res.drawable.rounded_square),
                                            contentDescription = null
                                        )
                                    }
                                )
                            }
                        }
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Target shape", modifier = Modifier.width(100.dp))
                        ExposedDropdownMenuBox(
                            expanded = selectTargetShapeMenuExpaned,
                            onExpandedChange = { selectTargetShapeMenuExpaned = it }) {
                            OutlinedTextField(
                                modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true),
                                value = targetShapeName,
                                onValueChange = {},
                                readOnly = true,
                                singleLine = true,
                                label = { },
                                trailingIcon = {
                                    Image(
                                        modifier = Modifier.size(18.dp),
                                        painter = painterResource(targetShapeMap[targetShapeName]!!),
                                        contentDescription = null
                                    )
                                },
                                colors = ExposedDropdownMenuDefaults.textFieldColors(),
                            )
                            ExposedDropdownMenu(
                                expanded = selectTargetShapeMenuExpaned,
                                onDismissRequest = { selectTargetShapeMenuExpaned = false }
                            ) {
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = "Circle",
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                    },
                                    onClick = {
                                        targetShape = TargetShape.CIRCLE
                                        selectTargetShapeMenuExpaned = false
                                    },
                                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                                    trailingIcon = {
                                        Image(
                                            modifier = Modifier.size(18.dp),
                                            painter = painterResource(Res.drawable.circle),
                                            contentDescription = null
                                        )
                                    }
                                )
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = "Rectangle",
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                    },
                                    onClick = {
                                        targetShape = TargetShape.RECTANGLE
                                        selectTargetShapeMenuExpaned = false
                                    },
                                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                                    trailingIcon = {
                                        Image(
                                            modifier = Modifier.size(18.dp),
                                            painter = painterResource(Res.drawable.square),
                                            contentDescription = null
                                        )
                                    }
                                )
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = "Rounded Rectangle",
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                    },
                                    onClick = {
                                        targetShape = TargetShape.ROUNDED_RECTANGLE
                                        selectTargetShapeMenuExpaned = false
                                    },
                                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                                    trailingIcon = {
                                        Image(
                                            modifier = Modifier.size(18.dp),
                                            painter = painterResource(Res.drawable.rounded_square),
                                            contentDescription = null
                                        )
                                    }
                                )
                            }
                        }
                    }
                    AnimatedVisibility(!isShowcasing, enter = fadeIn(), exit = fadeOut()) {
                        Column{
                            AnimatedVisibility(visible = !useTargetShowcaseLayout, enter = fadeIn(), exit = fadeOut()) {
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("Line thickness: ")
                                        Slider(
                                            value = lineThinckness.toFloat(),
                                            onValueChange = { lineThinckness = it.toInt() },
                                            valueRange = 1f..10f,
                                            thumb = {
                                                Label(
                                                    label = {
                                                        PlainTooltip(
                                                            modifier = Modifier
                                                                .requiredSize(45.dp, 25.dp)
                                                                .wrapContentWidth()
                                                        ) {
                                                            Text("${lineThinckness}dp")
                                                        }
                                                    }
                                                ) {
                                                    Text(modifier = Modifier.drawBehind {
                                                        drawCircle(
                                                            color = Color.LightGray,
                                                            radius = 20.dp.toPx()
                                                        )
                                                    }, text = "${lineThinckness}dp")
                                                }
                                            }
                                        )
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("Message corner radius: ")
                                        Slider(
                                            value = msgCornerRadius.toFloat(),
                                            onValueChange = { msgCornerRadius = it.toInt() },
                                            valueRange = 0f..45f,
                                            thumb = {
                                                Label(
                                                    label = {
                                                        PlainTooltip(
                                                            modifier = Modifier
                                                                .requiredSize(45.dp, 25.dp)
                                                                .wrapContentWidth()
                                                        ) {
                                                            Text("${msgCornerRadius}dp")
                                                        }
                                                    }
                                                ) {
                                                    Text(modifier = Modifier.drawBehind {
                                                        drawCircle(
                                                            color = Color.LightGray,
                                                            radius = 20.dp.toPx()
                                                        )
                                                    }, text = "${msgCornerRadius}dp")
                                                }
                                            }
                                        )
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("Head size: ")
                                        Slider(
                                            value = headSize,
                                            onValueChange = { headSize = it },
                                            valueRange = 10f..45f,
                                            thumb = {
                                                Label(
                                                    label = {
                                                        PlainTooltip(
                                                            modifier = Modifier
                                                                .requiredSize(45.dp, 25.dp)
                                                                .wrapContentWidth()
                                                        ) {
                                                            Text("${headSize}")
                                                        }
                                                    }
                                                ) {
                                                    Text(modifier = Modifier.drawBehind {
                                                        drawCircle(
                                                            color = Color.LightGray,
                                                            radius = 20.dp.toPx()
                                                        )
                                                    }, text = headSize.roundToInt().toString())
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Animation time: ")
                                Slider(
                                    value = durationSliderValue.toFloat(),
                                    onValueChange = { durationSliderValue = it.roundToInt() },
                                    onValueChangeFinished = {animationDuration = durationSliderValue.div(3)},
                                    steps = 24,
                                    valueRange = 500f..3000f,
                                    thumb = {
                                        Label(
                                            label = {
                                                PlainTooltip(
                                                    modifier = Modifier
                                                        .requiredSize(45.dp, 25.dp)
                                                        .wrapContentWidth()
                                                ) {
                                                    Text("${durationSliderValue}")
                                                }
                                            }
                                        ) {
                                            Text(modifier = Modifier.drawBehind {
                                                drawRoundRect(
                                                    topLeft = Offset(-10f,0f),
                                                    color = Color.LightGray,
                                                    size = Size(70.dp.toPx(),25.dp.toPx()),
                                                    cornerRadius = CornerRadius(10f,10f)
                                                )
                                            }, text = "$durationSliderValue ms")
                                        }
                                    }
                                )
                            }
                            Row(
                                Modifier.selectable(
                                    selected = animateMsg,
                                    onClick = { animateMsg = !animateMsg },
                                    role = Role.RadioButton
                                ), verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Animate msg")
                                Checkbox(checked = animateMsg, onCheckedChange = { animateMsg = it })
                            }
                            Crossfade (useTargetShowcaseLayout) {
                                if (it){
                                    Row(
                                        modifier = Modifier.selectable(
                                            selected = animateToNextTarget,
                                            onClick = { animateToNextTarget = !animateToNextTarget },
                                            role = Role.Checkbox
                                        ),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Text("Animate to next target")
                                        Checkbox(
                                            checked = animateToNextTarget,
                                            onCheckedChange = { animateToNextTarget = it }
                                        )
                                    }
                                }else{
                                    Row(
                                        Modifier.selectable(
                                            selected = animateHead,
                                            onClick = { if (!useTargetShowcaseLayout) animateHead = !animateHead },
                                            role = Role.Checkbox,
                                            enabled = !useTargetShowcaseLayout
                                        ), verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            "Animate arrow head",
                                            color = if (useTargetShowcaseLayout) Color.Gray else Color.Unspecified
                                        )
                                        Checkbox(
                                            checked = animateHead,
                                            onCheckedChange = { if (!useTargetShowcaseLayout) animateHead = it },
                                            enabled = !useTargetShowcaseLayout
                                        )
                                    }
                                }
                            }
                            Row(
                                modifier = Modifier.selectable(
                                    selected = advanceOnTargetTapOnly,
                                    onClick = { advanceOnTargetTapOnly = !advanceOnTargetTapOnly },
                                    role = Role.Checkbox
                                ),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text("Advance on target tap only")
                                Checkbox(
                                    checked = advanceOnTargetTapOnly,
                                    onCheckedChange = { advanceOnTargetTapOnly = it }
                                )
                            }
                        }
                    }
                    Row {
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    showGreeting(
                                        ShowcaseMsg(
                                            "👋Hello this is a greeting \n\nGreeting is what usually is displayed before showcasing, it doesn't target any view but is used to display a message to the user 🧐\n\njust like this one.",
                                            textStyle = TextStyle(color = Color.White, textAlign = TextAlign.Center)
                                        )
                                    )
                                }
                            }) {
                            Text("Show greeting!")
                        }
                        Spacer(Modifier.width(8.dp))
                        Button(
                            modifier = Modifier.showcase(
                                3, message = ShowcaseMsg(
                                    // a custom composable message: the library only places it and draws the arrow
                                    gravity = Gravity.Auto,
                                    arrow = Arrow(
                                        animSize = animateHead,
                                        head = headType,
                                        targetFrom = Side.Top,
                                        headSize = headSize,
                                        animationDuration = animationDuration
                                    ),
                                    enterAnim = if (animateMsg) MsgAnimation.FadeInOut(animationDuration) else MsgAnimation.None,
                                    // only used by TargetShowcaseLayout, where it is the overlay color of this step
                                    msgBackground = Color(0xff0042fd)
                                ) {
                                    Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
                                        Column(
                                            modifier = Modifier.padding(16.dp),
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xff0042fd))
                                                Text("Custom composable message", fontWeight = FontWeight.Bold)
                                            }
                                            Text("This message is a Card you provide. Click the button below it to showcase the target selected above.")
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                TextButton(onClick = { previous() }, enabled = hasPrevious) { Text("Back") }
                                                Spacer(Modifier.weight(1f))
                                                TextButton(onClick = { skip() }) { Text("Skip") }
                                                Button(onClick = { next() }) { Text("Next") }
                                            }
                                        }
                                    }
                                }
                            ),
                            onClick = {
                                val indexToShowcase = targets[selectedTarget]!!
                                coroutineScope.launch {
                                    showcaseItem(indexToShowcase)
                                }
                            }) {
                            Text("Showcase!")
                        }
                    }

                    Row {
                        Button(onClick = {
                            isShowcasing = true
                        }) {
                            Text("Subsequent Showcase!")
                        }
                        Spacer(Modifier.width(8.dp))
                        Button(onClick = {
                            coroutineScope.launch {
                                // a greeting can be a custom composable too
                                showGreeting(ShowcaseMsg {
                                    Card {
                                        Column(
                                            modifier = Modifier.padding(24.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Info,
                                                contentDescription = null,
                                                modifier = Modifier.size(48.dp),
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                            Text("A custom greeting", fontWeight = FontWeight.Bold)
                                            Text(
                                                "Greetings can be composables too. Tap anywhere, or press the button.",
                                                textAlign = TextAlign.Center
                                            )
                                            Button(onClick = { next() }) { Text("Got it") }
                                        }
                                    }
                                })
                            }
                        }) {
                            Text("Custom greeting")
                        }
                    }
                }
                Box() {
                    SnackbarHost(
                        snackbarHostState,
                        modifier = Modifier.align(Alignment.BottomCenter)
                    )
                    Row(
                        Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("By Tahaak67")
                        TextButton(onClick = {
                            if (!openUrl("https://github.com/tahaak67")) {
                                clipboardManager.setText(buildAnnotatedString { append("https://github.com/tahaak67") })
                                coroutineScope.launch { snackbarHostState.showSnackbar("Link copied") }
                            }
                        }) {
                            Text("Github")
                        }
                        TextButton(onClick = {
                            if (!openUrl("https://www.linkedin.com/in/tahabenly/")) {
                                clipboardManager.setText(buildAnnotatedString { append("https://www.linkedin.com/in/tahabenly/") })
                                coroutineScope.launch { snackbarHostState.showSnackbar("Link copied") }
                            }
                        }) {
                            Text("Linkedin")
                        }
                    }
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    TextButton(onClick = {
                        if (!openUrl("https://github.com/tahaak67/ShowcaseLayoutCompose")) {
                            clipboardManager.setText(buildAnnotatedString { append("https://github.com/tahaak67/ShowcaseLayoutCompose") })
                            coroutineScope.launch { snackbarHostState.showSnackbar("Link copied") }
                        }
                    }) {
                        Text("Showcase Layout Compose")
                    }
                }
            }
            if (finishedSubsequentShowcase){
                coroutineScope.launch {
                    delay(500)
                    showGreeting(ShowcaseMsg(buildAnnotatedString {
                        withStyle(ParagraphStyle(textAlign = TextAlign.Center)){
                            append("That's a lot of useful information!,\n ")
                            pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
                            append("i know ")
                            pop()
                            append(" i hope you where taking notes 📝 🫠")
                        }
                    }, TextStyle(color = Color.White)))
                }
                finishedSubsequentShowcase = false
            }
            registerEventListener(object: ShowcaseEventListener {
                override fun onEvent(level: Level, event: String) {
                    println("$level: $event")
                }
            })
        }
    }
}

@Composable
fun AppPreview() {
    MyTheme {
        App(openUrl = { false })
    }
}
