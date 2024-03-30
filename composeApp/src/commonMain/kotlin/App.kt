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
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import ly.com.tahaben.showcase_layout_compose.model.Arrow
import ly.com.tahaben.showcase_layout_compose.model.Head
import ly.com.tahaben.showcase_layout_compose.model.MsgAnimation
import ly.com.tahaben.showcase_layout_compose.model.ShowcaseMsg
import ly.com.tahaben.showcase_layout_compose.model.Side
import ly.com.tahaben.showcase_layout_compose.ui.ShowcaseLayout
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import showcase_layout_compose_kmp.composeapp.generated.resources.Res
import showcase_layout_compose_kmp.composeapp.generated.resources.circle
import showcase_layout_compose_kmp.composeapp.generated.resources.rounded_square
import showcase_layout_compose_kmp.composeapp.generated.resources.square
import showcase_layout_compose_kmp.composeapp.generated.resources.triangle
import kotlin.math.roundToInt

@OptIn(ExperimentalResourceApi::class, ExperimentalMaterial3Api::class)
@Preview
@Composable
fun App(openUrl: (String) -> Boolean, onWebLoadFinish: ()-> Unit = {} ) {
    LaunchedEffect(key1 = Unit, block = { onWebLoadFinish() })
    var messageText by remember { mutableStateOf("A sample showcase message 🙃") }
    var selectedTarget by remember { mutableStateOf("Toolbar title") }
    var selectTargetMenuExpaned by remember { mutableStateOf(false) }
    var selectHeadMenuExpaned by remember { mutableStateOf(false) }
    val clipboardManager = LocalClipboardManager.current
    val snackbarHostState = remember {SnackbarHostState()}
    val targets =
        mapOf("Toolbar title" to 1, "Toolbar menu icon" to 2, "Message text field" to 3, "Showcase Button" to 4)
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
    val scrollState = rememberScrollState()
    MyTheme(useDarkTheme = false) {
        ShowcaseLayout(
            isShowcasing = isShowcasing,
            onFinish = { isShowcasing = false },
            greeting = ShowcaseMsg(messageText, textStyle = TextStyle(color = Color.White)),
            lineThickness = lineThinckness.dp,
            animationDuration = 800
        ) {
            Column(modifier = Modifier.fillMaxSize().verticalScroll(scrollState)) {
                TopAppBar(title = {
                    Text(
                        modifier = Modifier
                            .showcase(
                                1, message = ShowcaseMsg(
                                    text = messageText,
                                    msgBackground = msgBackground,
                                    roundedCorner = msgCornerRadius.dp,
                                    arrow = Arrow(animSize = animateHead, head = headType, headSize = headSize),
                                    enterAnim = if (animateMsg) MsgAnimation.FadeInOut() else MsgAnimation.None
                                )
                            ),
                        text = "Showcase Layout Compose Demo"
                    )
                }, actions = {
                    IconButton(
                        onClick = {}, modifier = Modifier.showcase(
                            2, message = ShowcaseMsg(
                                text = messageText,
                                msgBackground = msgBackground,
                                roundedCorner = msgCornerRadius.dp,
                                arrow = Arrow(animSize = animateHead, head = headType, headSize = headSize, curved = true),
                                enterAnim = if (animateMsg) MsgAnimation.FadeInOut() else MsgAnimation.None
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
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Messge text", modifier = Modifier.width(100.dp))
                        OutlinedTextField(modifier = Modifier.showcase(
                            3, message = ShowcaseMsg(
                                text = messageText,
                                msgBackground = msgBackground,
                                roundedCorner = msgCornerRadius.dp,
                                arrow = Arrow(animSize = animateHead, head = headType, headSize = headSize),
                                enterAnim = if (animateMsg) MsgAnimation.FadeInOut() else MsgAnimation.None
                            )
                        ), value = messageText, onValueChange = { messageText = it })

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
                                modifier = Modifier.menuAnchor().showcase(
                                    5, message = ShowcaseMsg(
                                        text = messageText,
                                        msgBackground = msgBackground,
                                        roundedCorner = msgCornerRadius.dp,
                                        arrow = Arrow(animSize = animateHead, head = headType, headSize = headSize),
                                        enterAnim = if (animateMsg) MsgAnimation.FadeInOut() else MsgAnimation.None
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
                                        text = { Text(text = name, style = MaterialTheme.typography.bodyLarge) },
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
                                    text = { Text(text = "Triangle", style = MaterialTheme.typography.bodyLarge) },
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
                                    text = { Text(text = "Circle", style = MaterialTheme.typography.bodyLarge) },
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
                                    text = { Text(text = "Square", style = MaterialTheme.typography.bodyLarge) },
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
                    Row {
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
                    Row {
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

                    Row {
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
                                    showGreeting(ShowcaseMsg(messageText, textStyle = TextStyle(color = Color.White)))
                                }
                            }) {
                            Text("Show greeting!")
                        }
                        Spacer(Modifier.width(8.dp))
                        Button(
                            modifier = Modifier.showcase(
                                4, message = ShowcaseMsg(
                                    text = messageText,
                                    msgBackground = msgBackground,
                                    roundedCorner = msgCornerRadius.dp,
                                    arrow = Arrow(
                                        animSize = animateHead,
                                        head = headType,
                                        targetFrom = Side.Top,
                                        headSize = headSize
                                    ),
                                    enterAnim = if (animateMsg) MsgAnimation.FadeInOut() else MsgAnimation.None
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
                Box(){
                    SnackbarHost(snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter))
                    Row(Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically) {
                        Text("By Tahaak67")
                            TextButton(onClick = {
                                if (!openUrl("https://github.com/tahaak67")){
                                    clipboardManager.setText(buildAnnotatedString { append("https://github.com/tahaak67") })
                                    coroutineScope.launch { snackbarHostState.showSnackbar("Link copied") }
                                }
                            }) {
                                Text("Github")
                            }
                            TextButton(onClick = {
                                if (!openUrl("https://www.linkedin.com/in/tahabenly/")){
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
                        if (!openUrl("https://github.com/tahaak67/ShowcaseLayoutCompose")){
                            clipboardManager.setText(buildAnnotatedString { append("https://github.com/tahaak67/ShowcaseLayoutCompose") })
                            coroutineScope.launch { snackbarHostState.showSnackbar("Link copied") }
                        }
                    }) {
                        Text("Showcase Layout Compose")
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun AppPreview() {
    MyTheme {
        App(openUrl = {false})
    }
}