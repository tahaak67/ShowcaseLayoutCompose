import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ly.com.tahaben.showcase_layout_compose.model.*
import ly.com.tahaben.showcase_layout_compose.ui.ShowcaseLayout
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import showcase_layout_compose_kmp.composeapp.generated.resources.*
import kotlin.math.roundToInt

@OptIn(ExperimentalResourceApi::class, ExperimentalMaterial3Api::class)
@Preview
@Composable
fun App(openUrl: (String) -> Boolean, onWebLoadFinish: () -> Unit = {}) {
    LaunchedEffect(key1 = Unit, block = { onWebLoadFinish() })

    var selectedTarget by remember { mutableStateOf("Toolbar title") }
    var selectTargetMenuExpaned by remember { mutableStateOf(false) }
    var selectHeadMenuExpaned by remember { mutableStateOf(false) }
    val clipboardManager = LocalClipboardManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    var finishedSubsequentShowcase by remember { mutableStateOf(false) }
    val targets =
        mapOf(
            "Toolbar title" to 1,
            "Toolbar menu icon" to 2,
            "Showcase Button" to 3,
            "Target Dropdown menu" to 4
        )
    val arrowHeadMap =
        mapOf(
            "Triangle" to Res.drawable.triangle,
            "Circle" to Res.drawable.circle,
            "Square" to Res.drawable.square,
            "Rounded Square" to Res.drawable.rounded_square
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
    val msgBackground by remember { mutableStateOf(Color.White) }
    var msgCornerRadius by remember { mutableStateOf(0) }
    var headSize by remember { mutableStateOf(25f) }
    var lineThinckness by remember { mutableStateOf(5) }
    var durationSliderValue by remember { mutableStateOf(500) }
    var animationDuration by remember { mutableStateOf(500) }
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
        ShowcaseLayout(
            isShowcasing = isShowcasing,
            onFinish = { isShowcasing = false; finishedSubsequentShowcase = true },
            greeting = ShowcaseMsg(greetingMsg, textStyle = TextStyle(color = Color.White)),
            lineThickness = lineThinckness.dp,
            animationDuration = animationDuration
        ) {
            Column(modifier = Modifier.fillMaxSize().verticalScroll(scrollState)) {
                TopAppBar(title = {
                    Text(
                        modifier = Modifier
                            .showcase(
                                1, message = ShowcaseMsg(
                                    text = "This is the title of the toolbar 🤫",
                                    msgBackground = msgBackground,
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
                                msgBackground = msgBackground,
                                roundedCorner = msgCornerRadius.dp,
                                arrow = Arrow(
                                    animSize = animateHead,
                                    head = headType,
                                    headSize = headSize,
                                    curved = true,
                                    animationDuration = animationDuration
                                ),
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
                        modifier = Modifier,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("Target", modifier = Modifier.width(100.dp))
                        ExposedDropdownMenuBox(
                            expanded = selectTargetMenuExpaned,
                            onExpandedChange = { selectTargetMenuExpaned = it }) {
                            OutlinedTextField(
                                modifier = Modifier.menuAnchor().showcase(
                                    4, message = ShowcaseMsg(
                                        text = buildAnnotatedString {
                                            append("You can choose what target to showcase form here then click the ")
                                            pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
                                            append("Showcase!")
                                            pop()
                                            append(" button")
                                        },
                                        msgBackground = msgBackground,
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
                                modifier = Modifier.menuAnchor(),
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
                                //modifier = Modifier.exposedDropdownSize(),
                                //properties = PopupProperties(focusable = false),
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
                    Row(
                        Modifier.selectable(
                            selected = animateHead,
                            onClick = { animateHead = !animateHead },
                            role = Role.RadioButton
                        ), verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Animate arrow head")
                        Checkbox(checked = animateHead, onCheckedChange = { animateHead = it })
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
                                    text = "Click here to showcase the target selected above.",
                                    msgBackground = Color.Transparent,
                                    textStyle = TextStyle(color = Color.White),
                                    roundedCorner = msgCornerRadius.dp,
                                    arrow = Arrow(
                                        animSize = animateHead,
                                        head = headType,
                                        targetFrom = Side.Top,
                                        headSize = headSize,
                                        animationDuration = animationDuration
                                    ),
                                    enterAnim = if (animateMsg) MsgAnimation.FadeInOut(animationDuration) else MsgAnimation.None
                                )
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

                    Button(onClick = {
                        isShowcasing = true
                    }) {
                        Text("Subsequent Showcase!")
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
        }
    }
}

@Preview
@Composable
fun AppPreview() {
    MyTheme {
        App(openUrl = { false })
    }
}