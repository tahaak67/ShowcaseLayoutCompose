package scenarios.chat

import ShowcaseLayoutWrapper
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.outlined.AttachFile
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
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
import ly.com.tahaben.showcase_layout_compose.model.ShowcaseMsgScope
import ly.com.tahaben.showcase_layout_compose.model.TargetShape
import ly.com.tahaben.showcase_layout_compose.ui.ShowcaseLayoutDefaults
import scenarios.TourCard
import scenarios.TourOptionsMenu
import scenarios.TourPalette
import scenarios.WelcomeCard

// Messages palette
private val Emerald = Color(0xFF059669)
private val Moss = Color(0xFF065F46)
private val Mint = Color(0xFFD1FAE5)
private val Paper = Color(0xFFF3F6F4)
private val Ink = Color(0xFF111827)
private val Slate = Color(0xFF6B7280)
private val Forest = Color(0xFF0B2A1F)

private val ChatColors = lightColorScheme(
    primary = Emerald,
    onPrimary = Color.White,
    primaryContainer = Mint,
    onPrimaryContainer = Moss,
    background = Paper,
    onBackground = Ink,
    surface = Color.White,
    onSurface = Ink,
    surfaceVariant = Color(0xFFE5E7EB),
    onSurfaceVariant = Slate,
)

private const val TOUR_STEPS = 4

private data class Message(val text: String, val time: String, val mine: Boolean)

private val openingMessages = listOf(
    Message("Hey! Did you get a chance to look at the mockups?", "09:41", mine = false),
    Message("Just did, they look great 🙌", "09:42", mine = true),
    Message("Awesome. Can we go with the darker header?", "09:42", mine = false),
    Message("Also sending the final logo tonight", "09:43", mine = false),
)

/**
 * A chat conversation with an onboarding tour. Runs on `ShowcaseLayout` by default (the other two
 * scenarios default to `TargetShowcaseLayout`), switchable from the "⋮" menu.
 *
 * What it demonstrates: a locked step on a text field. While the step is locked the overlay lets
 * touches through to the composer, so the field takes focus and opens the keyboard; the showcase
 * moves on by itself once a message is sent, and `onProceed` closes the keyboard again.
 */
@Composable
fun ChatScreen(onBack: () -> Unit) {
    var isShowcasing by remember { mutableStateOf(false) }
    var useTargetLayout by remember { mutableStateOf(false) }
    val messages = remember { mutableStateListOf<Message>().apply { addAll(openingMessages) } }
    var draft by remember { mutableStateOf("") }
    var sentDuringTour by remember { mutableIntStateOf(0) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current

    LaunchedEffect(Unit) {
        delay(900)
        isShowcasing = true
    }

    fun send() {
        val text = draft.trim()
        if (text.isEmpty()) return
        messages += Message(text, "Now", mine = true)
        draft = ""
        sentDuringTour++
        // a reply, so the conversation feels alive
        scope.launch {
            delay(1400)
            messages += Message("Got it 👍", "Now", mine = false)
        }
    }

    MaterialTheme(colorScheme = ChatColors) {
        ShowcaseLayoutWrapper(
            useTargetShowcaseLayout = useTargetLayout,
            isShowcasing = isShowcasing,
            onFinish = {
                isShowcasing = false
                scope.launch { snackbarHostState.showSnackbar("You can replay the tour any time from the ⋮ menu.") }
            },
            greeting = ShowcaseMsg(enterAnim = MsgAnimation.FadeInOut(400)) { WelcomeCard() },
            targetShape = TargetShape.ROUNDED_RECTANGLE,
            cornerRadius = 20.dp,
            animationDuration = 700,
            lineThickness = 3.dp,
            animateToNextTarget = true,
            advanceOnTargetTapOnly = false,
            colors = ShowcaseLayoutDefaults.colors(
                overlayColor = Forest.copy(alpha = 0.9f),
                pulseColor = Emerald
            )
        ) {
            registerEventListener(object : ShowcaseEventListener {
                override fun onEvent(level: Level, event: String) {
                    if (level != Level.VERBOSE) println("Messages tour: $event")
                }
            })
            Scaffold(
                containerColor = MaterialTheme.colorScheme.background,
                snackbarHost = { SnackbarHost(snackbarHostState) }
            ) { padding ->
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding).imePadding(),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Column(modifier = Modifier.fillMaxSize().widthIn(max = 520.dp)) {
                        Header(
                            showBack = !isShowcasing,
                            onBack = onBack,
                            useTargetLayout = useTargetLayout,
                            onReplay = { sentDuringTour = 0; isShowcasing = true },
                            onLayoutChange = { useTargetLayout = it },
                            // Step 1: who you are talking to
                            contactModifier = Modifier.showcase(
                                index = 1,
                                message = tourMessage { TourCard(step = 1, scope = this) }
                            ),
                            // Step 4: the video call button
                            videoModifier = Modifier.showcase(
                                index = 4,
                                message = tourMessage { TourCard(step = 4, scope = this) }
                            )
                        )
                        MessageList(
                            messages = messages,
                            modifier = Modifier.weight(1f),
                            // Step 2: a message bubble
                            bubbleModifier = Modifier.showcase(
                                index = 2,
                                message = tourMessage(gravity = Gravity.Bottom) { TourCard(step = 2, scope = this) }
                            )
                        )
                        // Step 3: the composer, locked until a message is sent
                        Composer(
                            draft = draft,
                            onDraftChange = { draft = it },
                            onSend = ::send,
                            modifier = Modifier.showcase(
                                index = 3,
                                message = tourMessage(gravity = Gravity.Top) { TourCard(step = 3, scope = this, locked = true) },
                                canProceed = sentDuringTour > 0,
                                // the step is over: drop focus and close the keyboard it opened
                                onProceed = {
                                    focusManager.clearFocus()
                                    keyboard?.hide()
                                }
                            )
                        )
                    }
                }
            }
        }
    }
}

private fun tourMessage(
    gravity: Gravity = Gravity.Auto,
    content: @Composable ShowcaseMsgScope.() -> Unit
) = ShowcaseMsg(
    gravity = gravity,
    arrow = Arrow(color = Color.White, head = Head.CIRCLE, headSize = 18f, animationDuration = 500),
    enterAnim = MsgAnimation.FadeInOut(350),
    exitAnim = MsgAnimation.FadeInOut(250),
    content = content
)

@Composable
private fun Header(
    showBack: Boolean,
    onBack: () -> Unit,
    useTargetLayout: Boolean,
    onReplay: () -> Unit,
    onLayoutChange: (Boolean) -> Unit,
    contactModifier: Modifier,
    videoModifier: Modifier,
) {
    var menuOpen by remember { mutableStateOf(false) }
    Surface(color = Color.White, shadowElevation = 1.dp) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                enabled = showBack,
                modifier = Modifier.graphicsLayer { alpha = if (showBack) 1f else 0f }
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back to scenarios", tint = Ink)
            }
            Row(
                modifier = contactModifier.weight(1f).padding(horizontal = 6.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier.size(42.dp).background(Brush.linearGradient(listOf(Emerald, Color(0xFF34D399))), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("ML", color = Color.White, fontWeight = FontWeight.Bold)
                }
                Column {
                    Text("Maya Lindqvist", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = Ink)
                    Text("online", style = MaterialTheme.typography.bodySmall, color = Emerald)
                }
            }
            IconButton(onClick = {}, modifier = videoModifier.size(44.dp)) {
                Icon(Icons.Outlined.Videocam, contentDescription = "Video call", tint = Ink)
            }
            IconButton(onClick = {}) {
                Icon(Icons.Outlined.Call, contentDescription = "Call", tint = Ink)
            }
            Box {
                IconButton(onClick = { menuOpen = true }) {
                    Icon(Icons.Outlined.MoreVert, contentDescription = "Tour options", tint = Ink)
                }
                TourOptionsMenu(
                    expanded = menuOpen,
                    onDismiss = { menuOpen = false },
                    useTargetLayout = useTargetLayout,
                    onReplay = onReplay,
                    onLayoutChange = onLayoutChange
                )
            }
        }
    }
}

@Composable
private fun MessageList(messages: List<Message>, modifier: Modifier, bubbleModifier: Modifier) {
    val scrollState = rememberScrollState()
    // keep the newest message in view
    LaunchedEffect(messages.size) { scrollState.animateScrollTo(scrollState.maxValue) }
    Column(
        modifier = modifier.fillMaxWidth().verticalScroll(scrollState).padding(horizontal = 12.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Surface(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            shape = RoundedCornerShape(50),
            color = Color(0xFFE2E8E5)
        ) {
            Text("Today", modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp), style = MaterialTheme.typography.labelMedium, color = Slate)
        }
        Spacer(Modifier.height(4.dp))
        messages.forEachIndexed { i, message ->
            // the last of the opening messages is the tour's example bubble
            val isTourBubble = i == openingMessages.lastIndex
            Bubble(message, modifier = if (isTourBubble) bubbleModifier else Modifier)
        }
    }
}

@Composable
private fun Bubble(message: Message, modifier: Modifier) {
    val shape = if (message.mine) RoundedCornerShape(18.dp, 18.dp, 4.dp, 18.dp) else RoundedCornerShape(18.dp, 18.dp, 18.dp, 4.dp)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.mine) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            modifier = modifier.widthIn(max = 300.dp),
            shape = shape,
            color = if (message.mine) Mint else Color.White,
            shadowElevation = if (message.mine) 0.dp else 1.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(message.text, style = MaterialTheme.typography.bodyLarge, color = Ink, modifier = Modifier.weight(1f, fill = false))
                Text(message.time, style = MaterialTheme.typography.labelSmall, color = Slate)
            }
        }
    }
}

@Composable
private fun Composer(draft: String, onDraftChange: (String) -> Unit, onSend: () -> Unit, modifier: Modifier) {
    val canSend = draft.isNotBlank()
    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        IconButton(onClick = {}) {
            Icon(Icons.Outlined.AttachFile, contentDescription = "Attach", tint = Slate)
        }
        TextField(
            value = draft,
            onValueChange = onDraftChange,
            modifier = Modifier.weight(1f),
            placeholder = { Text("Message") },
            singleLine = true,
            shape = RoundedCornerShape(24.dp),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
            keyboardActions = KeyboardActions(onSend = { onSend() }),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = Emerald
            )
        )
        IconButton(
            onClick = onSend,
            enabled = canSend,
            modifier = Modifier.size(46.dp).background(if (canSend) Emerald else Color(0xFFCBD5D1), CircleShape)
        ) {
            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = Color.White)
        }
    }
}

// ---------------------------------------------------------------------------------------------
// Tour copy. The cards themselves are shared by every scenario, see scenarios/TourUi.kt
// ---------------------------------------------------------------------------------------------

private val ChatTourPalette = TourPalette(
    card = Color.White,
    title = Ink,
    body = Slate,
    accent = Emerald,
    accentSoft = Mint,
)

@Composable
private fun ShowcaseMsgScope.WelcomeCard() = WelcomeCard(
    scope = this,
    palette = ChatTourPalette,
    icon = Icons.Outlined.ChatBubbleOutline,
    iconBrush = Brush.linearGradient(listOf(Emerald, Color(0xFF34D399))),
    title = "Welcome to Messages",
    body = "Four quick steps around a conversation, including sending your first message."
)

private data class TourStep(val title: String, val body: String)

private val tourSteps = listOf(
    TourStep(
        "Who you're talking to",
        "Tap the name to see the profile, shared media and notification settings."
    ),
    TourStep(
        "Messages, with superpowers",
        "Long-press any bubble to react, reply in a thread or forward it."
    ),
    TourStep(
        "Say hello",
        "Type a message and send it to continue. The keyboard works even while the tour is on."
    ),
    TourStep(
        "Face to face",
        "Start a video call from here. Calls are end-to-end encrypted."
    ),
)

@Composable
private fun TourCard(step: Int, scope: ShowcaseMsgScope, locked: Boolean = false) = TourCard(
    step = step,
    total = TOUR_STEPS,
    title = tourSteps[step - 1].title,
    body = tourSteps[step - 1].body,
    scope = scope,
    palette = ChatTourPalette,
    locked = locked,
    lockedHint = "Send a message to continue"
)
