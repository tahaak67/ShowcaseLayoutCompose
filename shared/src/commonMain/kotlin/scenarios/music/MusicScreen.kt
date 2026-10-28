package scenarios.music

import ShowcaseLayoutWrapper
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.QueueMusic
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ly.com.tahaben.showcase_layout_compose.domain.Level
import ly.com.tahaben.showcase_layout_compose.domain.ShowcaseEventListener
import ly.com.tahaben.showcase_layout_compose.model.*
import ly.com.tahaben.showcase_layout_compose.ui.ShowcaseLayoutDefaults
import scenarios.*
import kotlin.math.roundToInt

// Now Playing palette
private val Night = Color(0xFF0F0F14)
private val Panel = Color(0xFF1C1C24)
private val Coral = Color(0xFFF0544F)
private val Amber = Color(0xFFFFB86B)
private val Plum = Color(0xFF3B1D5A)
private val Snow = Color(0xFFF4F4F8)
private val Fog = Color(0xFFA1A1AA)
private val Ink = Color(0xFF16161D)

private val PlayerColors = darkColorScheme(
    primary = Coral,
    onPrimary = Color.White,
    secondary = Amber,
    background = Night,
    onBackground = Snow,
    surface = Panel,
    onSurface = Snow,
    surfaceVariant = Color(0xFF2A2A34),
    onSurfaceVariant = Fog,
    outline = Color(0xFF3A3A45),
)

private const val TOUR_STEPS = 4
private const val TRACK_LENGTH_SEC = 228

/**
 * A dark "now playing" screen with an onboarding tour.
 *
 * What it demonstrates, on top of what the banking scenario shows:
 * - a light overlay on a dark UI (`ShowcaseLayoutDefaults.colors(overlayColor = ...)`)
 * - `TargetShape.CIRCLE`: the artwork, the play button and the icon buttons all get round highlights
 * - the pulse ring around a round target
 * - a locked step on a button that changes state (play / pause) rather than on a text field
 */
@Composable
fun MusicScreen(onBack: () -> Unit, autoPlay: Boolean = false, targetLayout: Boolean = true) {
    var isShowcasing by remember { mutableStateOf(false) }
    var autoplay by remember { mutableStateOf(autoPlay) }
    val tour = remember { TourController() }
    var useTargetLayout by remember { mutableStateOf(targetLayout) }
    var isPlaying by remember { mutableStateOf(false) }
    var isFavorite by remember { mutableStateOf(false) }
    var positionSec by remember { mutableFloatStateOf(84f) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        delay(900)
        isShowcasing = true
    }

    // the track "plays": one second per second until the end, then wraps around
    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            delay(1000)
            positionSec = (positionSec + 1f) % TRACK_LENGTH_SEC
        }
    }

    MaterialTheme(colorScheme = PlayerColors) {
        ShowcaseLayoutWrapper(
            useTargetShowcaseLayout = useTargetLayout,
            isShowcasing = isShowcasing,
            onFinish = {
                isShowcasing = false
                autoplay = false
                scope.launch { snackbarHostState.showSnackbar("You can replay the tour any time from the ⋮ menu.") }
            },
            greeting = ShowcaseMsg(enterAnim = MsgAnimation.FadeInOut(400)) { WelcomeCard(tour) },
            targetShape = TargetShape.CIRCLE,
            animationDuration = 700,
            lineThickness = 3.dp,
            animateToNextTarget = true,
            advanceOnTargetTapOnly = false,
            // a light overlay for a dark UI; the pulse ring takes the accent color
            colors = ShowcaseLayoutDefaults.colors(
                overlayColor = Snow.copy(alpha = 0.93f),
                pulseColor = Coral
            )
        ) {
            registerEventListener(object : ShowcaseEventListener {
                override fun onEvent(level: Level, event: String) {
                    if (level != Level.VERBOSE) println("Now Playing tour: $event")
                }
            })
            TourAutoplay(
                active = autoplay && isShowcasing,
                controller = tour,
                performStep = { step -> if (step == 2) { isPlaying = true; true } else false }
            )
            Scaffold(
                containerColor = MaterialTheme.colorScheme.background,
                snackbarHost = { SnackbarHost(snackbarHostState) }
            ) { padding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState()),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Column(
                        modifier = Modifier
                            .widthIn(max = 480.dp)
                            .padding(horizontal = 24.dp, vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Header(
                            showBack = !isShowcasing,
                            onBack = onBack,
                            useTargetLayout = useTargetLayout,
                            onReplay = { isPlaying = false; isShowcasing = true },
                            onLayoutChange = { useTargetLayout = it },
                            onAutoPlay = { isPlaying = false; autoplay = true; isShowcasing = true }
                        )
                        Spacer(Modifier.height(28.dp))

                        // Step 1: the artwork
                        Artwork(
                            modifier = Modifier.showcase(
                                index = 1,
                                message = tourMessage(gravity = Gravity.Bottom) { TourCard(step = 1, scope = this, controller = tour) }
                            ),
                            spinning = isPlaying
                        )
                        Spacer(Modifier.height(32.dp))

                        // Step 3: the favourite button
                        TrackTitle(
                            isFavorite = isFavorite,
                            onToggleFavorite = { isFavorite = !isFavorite },
                            favoriteModifier = Modifier.showcase(
                                index = 3,
                                message = tourMessage { TourCard(step = 3, scope = this, controller = tour) }
                            )
                        )
                        Spacer(Modifier.height(20.dp))

                        SeekBar(positionSec = positionSec, onSeek = { positionSec = it })
                        Spacer(Modifier.height(12.dp))

                        // Step 2: play, locked until the user presses it. The card always shows the
                        // "tap play" hint: the library moves on by itself, so the card never needs a
                        // Next button, and it must not change while it fades out.
                        Controls(
                            isPlaying = isPlaying,
                            onTogglePlay = { isPlaying = !isPlaying },
                            playModifier = Modifier.showcase(
                                index = 2,
                                message = tourMessage { TourCard(step = 2, scope = this, locked = true, controller = tour) },
                                canProceed = isPlaying
                            )
                        )
                        Spacer(Modifier.height(28.dp))

                        // Step 4: the queue
                        BottomActions(
                            queueModifier = Modifier.showcase(
                                index = 4,
                                message = tourMessage(gravity = Gravity.Top) { TourCard(step = 4, scope = this, controller = tour) }
                            )
                        )
                        Spacer(Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

/** Every step shares the arrow and animation; the arrow is dark because the overlay is light. */
private fun tourMessage(
    gravity: Gravity = Gravity.Auto,
    content: @Composable ShowcaseMsgScope.() -> Unit
) = ShowcaseMsg(
    gravity = gravity,
    arrow = Arrow(color = Ink, head = Head.CIRCLE, headSize = 18f, animationDuration = 500),
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
    onAutoPlay: () -> Unit,
) {
    var menuOpen by remember { mutableStateOf(false) }
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        // Collapsing the player is how you leave a now-playing screen, so it doubles as "back".
        // Kept in the layout (alpha only) so nothing shifts when the tour starts.
        IconButton(
            onClick = onBack,
            enabled = showBack,
            modifier = Modifier.graphicsLayer { alpha = if (showBack) 1f else 0f }
        ) {
            Icon(Icons.Outlined.KeyboardArrowDown, contentDescription = "Back to scenarios", tint = Snow)
        }
        Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("PLAYING FROM PLAYLIST", style = MaterialTheme.typography.labelSmall, color = Fog)
            Text("Late Night Drive", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = Snow)
        }
        Box {
            IconButton(onClick = { menuOpen = true }) {
                Icon(Icons.Outlined.MoreVert, contentDescription = "Tour options", tint = Snow)
            }
            TourOptionsMenu(
                expanded = menuOpen,
                onDismiss = { menuOpen = false },
                useTargetLayout = useTargetLayout,
                onReplay = onReplay,
                onLayoutChange = onLayoutChange,
                onAutoPlay = onAutoPlay
            )
        }
    }
}

/** A round "record" that spins while the track plays. */
@Composable
private fun Artwork(modifier: Modifier, spinning: Boolean) {
    val rotation = remember { Animatable(0f) }
    LaunchedEffect(spinning) {
        while (spinning) {
            rotation.animateTo(rotation.value + 360f, tween(durationMillis = 14000, easing = LinearEasing))
        }
    }
    Box(
        modifier = modifier
            .size(260.dp)
            .graphicsLayer { rotationZ = rotation.value % 360f }
            .background(Brush.sweepGradient(listOf(Plum, Coral, Amber, Plum)), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(200.dp)
                .background(Brush.radialGradient(listOf(Color(0xFF2A1240), Night)), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Outlined.MusicNote, contentDescription = null, tint = Snow.copy(alpha = 0.9f), modifier = Modifier.size(56.dp))
        }
    }
}

@Composable
private fun TrackTitle(isFavorite: Boolean, onToggleFavorite: () -> Unit, favoriteModifier: Modifier) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
            Text("Midnight Signals", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = Snow)
            Text("Nova Collective", style = MaterialTheme.typography.bodyLarge, color = Fog)
        }
        IconButton(onClick = onToggleFavorite, modifier = favoriteModifier.size(48.dp)) {
            Icon(
                if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                contentDescription = if (isFavorite) "Remove from library" else "Save to library",
                tint = if (isFavorite) Coral else Snow,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
private fun SeekBar(positionSec: Float, onSeek: (Float) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Slider(
            value = positionSec,
            onValueChange = onSeek,
            valueRange = 0f..TRACK_LENGTH_SEC.toFloat(),
            colors = SliderDefaults.colors(
                thumbColor = Snow,
                activeTrackColor = Coral,
                inactiveTrackColor = Color(0xFF2A2A34)
            )
        )
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(formatTime(positionSec), style = MaterialTheme.typography.labelMedium, color = Fog)
            Spacer(Modifier.weight(1f))
            Text(formatTime(TRACK_LENGTH_SEC.toFloat()), style = MaterialTheme.typography.labelMedium, color = Fog)
        }
    }
}

private fun formatTime(seconds: Float): String {
    val total = seconds.roundToInt()
    return "${total / 60}:${(total % 60).toString().padStart(2, '0')}"
}

@Composable
private fun Controls(isPlaying: Boolean, onTogglePlay: () -> Unit, playModifier: Modifier) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        SmallControl(Icons.Outlined.Shuffle, "Shuffle")
        LargeControl(Icons.Filled.SkipPrevious, "Previous")
        Box(
            modifier = playModifier
                .size(76.dp)
                .background(Coral, CircleShape)
                .clickable(onClick = onTogglePlay),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                contentDescription = if (isPlaying) "Pause" else "Play",
                tint = Color.White,
                modifier = Modifier.size(40.dp)
            )
        }
        LargeControl(Icons.Filled.SkipNext, "Next")
        SmallControl(Icons.Outlined.Repeat, "Repeat")
    }
}

@Composable
private fun SmallControl(icon: ImageVector, label: String) {
    IconButton(onClick = {}) { Icon(icon, contentDescription = label, tint = Fog) }
}

@Composable
private fun LargeControl(icon: ImageVector, label: String) {
    IconButton(onClick = {}, modifier = Modifier.size(56.dp)) {
        Icon(icon, contentDescription = label, tint = Snow, modifier = Modifier.size(40.dp))
    }
}

@Composable
private fun BottomActions(queueModifier: Modifier) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = {}) { Icon(Icons.Outlined.Subtitles, contentDescription = "Lyrics", tint = Fog) }
        IconButton(onClick = {}) { Icon(Icons.Outlined.Speaker, contentDescription = "Devices", tint = Fog) }
        IconButton(onClick = {}, modifier = queueModifier.size(48.dp)) {
            Icon(Icons.AutoMirrored.Outlined.QueueMusic, contentDescription = "Queue", tint = Snow)
        }
    }
}

// ---------------------------------------------------------------------------------------------
// Tour copy. The cards themselves are shared by every scenario, see scenarios/TourUi.kt
// ---------------------------------------------------------------------------------------------

private val PlayerTourPalette = TourPalette(
    card = Panel,
    title = Snow,
    body = Fog,
    accent = Coral,
    accentSoft = Color(0xFF3A2A33),
)

@Composable
private fun ShowcaseMsgScope.WelcomeCard(controller: TourController) = WelcomeCard(
    scope = this,
    controller = controller,
    palette = PlayerTourPalette,
    icon = Icons.Outlined.MusicNote,
    iconBrush = Brush.linearGradient(listOf(Coral, Amber)),
    title = "Welcome to Now Playing",
    body = "Four quick stops around the player. It takes less than a minute."
)

private data class TourStep(val title: String, val body: String)

private val tourSteps = listOf(
    TourStep(
        "Artwork that plays along",
        "The record spins while the track plays. Tap it any time to flip to the lyrics."
    ),
    TourStep(
        "Press play",
        "Playback picks up right where you left off, on any device. Give it a try."
    ),
    TourStep(
        "Keep what you love",
        "Save the track to your library and it shows up on every device you sign in to."
    ),
    TourStep(
        "Up next",
        "See and reorder what plays after this one, or send the music to a speaker."
    ),
)

@Composable
private fun TourCard(step: Int, scope: ShowcaseMsgScope, locked: Boolean = false, controller: TourController) = TourCard(
    step = step,
    controller = controller,
    total = TOUR_STEPS,
    title = tourSteps[step - 1].title,
    body = tourSteps[step - 1].body,
    scope = scope,
    palette = PlayerTourPalette,
    locked = locked,
    lockedHint = "Tap play to continue"
)
