package scenarios.banking

import ShowcaseLayoutWrapper
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import ly.com.tahaben.showcase_layout_compose.ui.TargetShowcaseLayout
import scenarios.*

// Lumen Bank palette
private val Navy = Color(0xFF1E2A5A)
private val Indigo = Color(0xFF3730A3)
private val Violet = Color(0xFF6D5BD0)
private val Mint = Color(0xFF2DD4BF)
private val Ink = Color(0xFF111827)
private val Slate = Color(0xFF6B7280)
private val Mist = Color(0xFFF3F5FA)
private val PositiveGreen = Color(0xFF15803D)

private val BankColors = lightColorScheme(
    primary = Indigo,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0E7FF),
    onPrimaryContainer = Navy,
    secondary = Mint,
    onSecondary = Navy,
    secondaryContainer = Color(0xFFCCFBF1),
    onSecondaryContainer = Color(0xFF134E4A),
    background = Mist,
    onBackground = Ink,
    surface = Color.White,
    onSurface = Ink,
    surfaceVariant = Color(0xFFE5E7EB),
    onSurfaceVariant = Slate,
    outline = Color(0xFFD1D5DB),
)

private const val TOUR_STEPS = 4

/**
 * A fake banking home screen with an onboarding tour. It runs on [TargetShowcaseLayout] by default
 * and can be switched to `ShowcaseLayout` from the "?" menu to compare the two.
 *
 * What it demonstrates:
 * - a custom composable greeting and custom message cards (`ShowcaseMsg { ... }`)
 * - the highlight morphing between targets of different sizes (`animateToNextTarget`)
 * - a locked, hands-on step (`canProceed`) that only continues once the user hides the balance
 * - a brand overlay and pulse color via [ShowcaseLayoutDefaults.colors]
 */
@Composable
fun BankingScreen(onBack: () -> Unit, autoPlay: Boolean = false, targetLayout: Boolean = true) {
    var isShowcasing by remember { mutableStateOf(false) }
    // The same tour runs on both layouts; switch from the "?" menu to compare them.
    var autoplay by remember { mutableStateOf(autoPlay) }
    val tour = remember { TourController() }
    var useTargetLayout by remember { mutableStateOf(targetLayout) }
    var balanceHidden by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableIntStateOf(0) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Start the tour on its own shortly after the screen appears.
    LaunchedEffect(Unit) {
        delay(900)
        isShowcasing = true
    }

    MaterialTheme(colorScheme = BankColors) {
        ShowcaseLayoutWrapper(
            useTargetShowcaseLayout = useTargetLayout,
            isShowcasing = isShowcasing,
            onFinish = {
                isShowcasing = false
                autoplay = false
                scope.launch { snackbarHostState.showSnackbar("You can replay the tour any time from the ? icon.") }
            },
            greeting = ShowcaseMsg(enterAnim = MsgAnimation.FadeInOut(400)) { WelcomeCard(tour) },
            targetShape = TargetShape.ROUNDED_RECTANGLE,
            cornerRadius = 28.dp,
            animationDuration = 700,
            lineThickness = 3.dp,
            animateToNextTarget = true,
            advanceOnTargetTapOnly = false,
            colors = ShowcaseLayoutDefaults.colors(
                overlayColor = Navy.copy(alpha = 0.9f),
                pulseColor = Violet
            )
        ) {
            registerEventListener(object : ShowcaseEventListener {
                override fun onEvent(level: Level, event: String) {
                    if (level != Level.VERBOSE) println("Lumen tour: $event")
                }
            })
            TourAutoplay(
                active = autoplay && isShowcasing,
                controller = tour,
                performStep = { step -> if (step == 2) { balanceHidden = true; true } else false }
            )
            Scaffold(
                containerColor = MaterialTheme.colorScheme.background,
                snackbarHost = { SnackbarHost(snackbarHostState) },
                bottomBar = {
                    NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                        listOf(
                            "Home" to Icons.Filled.Home,
                            "Cards" to Icons.Outlined.CreditCard,
                            "Insights" to Icons.Outlined.BarChart,
                            "Profile" to Icons.Outlined.Person,
                        ).forEachIndexed { i, (label, icon) ->
                            NavigationBarItem(
                                selected = selectedTab == i,
                                onClick = { selectedTab = i },
                                icon = { Icon(icon, contentDescription = label) },
                                label = { Text(label) }
                            )
                        }
                    }
                }
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
                            .padding(horizontal = 20.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        Header(
                            showBack = !isShowcasing,
                            onBack = onBack,
                            useTargetLayout = useTargetLayout,
                            onReplay = { balanceHidden = false; isShowcasing = true },
                            onLayoutChange = { useTargetLayout = it },
                            onAutoPlay = { balanceHidden = false; autoplay = true; isShowcasing = true }
                        )

                        // Step 1: the whole balance card
                        BalanceCard(
                            modifier = Modifier.showcase(
                                index = 1,
                                message = tourMessage { TourCard(step = 1, scope = this, controller = tour) }
                            ),
                            hidden = balanceHidden,
                            onToggleHidden = { balanceHidden = !balanceHidden },
                            // Step 2: the eye icon, locked until the balance is hidden. The card always
                            // shows the hint so it does not change while it fades out.
                            eyeModifier = Modifier.showcase(
                                index = 2,
                                message = tourMessage { TourCard(step = 2, scope = this, locked = true, controller = tour) },
                                canProceed = balanceHidden
                            )
                        )

                        // Step 3: the Send quick action. Placed below the target so the card never
                        // runs into the status bar on ShowcaseLayout.
                        QuickActions(
                            sendModifier = Modifier.showcase(
                                index = 3,
                                message = tourMessage(gravity = Gravity.Bottom) { TourCard(step = 3, scope = this, controller = tour) }
                            )
                        )

                        // Step 4: the activity list
                        RecentActivity(
                            modifier = Modifier.showcase(
                                index = 4,
                                message = tourMessage { TourCard(step = 4, scope = this, controller = tour) }
                            )
                        )
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

/** Every tour step shares the same arrow and animation; only the card differs. */
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
    onAutoPlay: () -> Unit,
) {
    var menuOpen by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Kept in the layout (alpha only) so the header does not shift when the tour starts.
        IconButton(
            onClick = onBack,
            enabled = showBack,
            modifier = Modifier.graphicsLayer { alpha = if (showBack) 1f else 0f }
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back to scenarios", tint = Ink)
        }
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text("SA", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
        }
        Column(modifier = Modifier.weight(1f)) {
            Text("Good morning,", style = MaterialTheme.typography.bodySmall, color = Slate)
            Text("Sara Ahmed", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        }
        Box {
            IconButton(onClick = { menuOpen = true }) {
                Icon(Icons.Outlined.HelpOutline, contentDescription = "Tour options", tint = Ink)
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
        IconButton(onClick = {}) {
            Icon(Icons.Outlined.Notifications, contentDescription = "Notifications", tint = Ink)
        }
    }
}

@Composable
private fun BalanceCard(
    modifier: Modifier,
    hidden: Boolean,
    onToggleHidden: () -> Unit,
    eyeModifier: Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(Brush.linearGradient(listOf(Navy, Indigo, Violet)))
            .padding(22.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Total balance", color = Color.White.copy(alpha = 0.75f), style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.weight(1f))
                IconButton(onClick = onToggleHidden, modifier = eyeModifier.size(40.dp)) {
                    Icon(
                        if (hidden) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                        contentDescription = if (hidden) "Show balance" else "Hide balance",
                        tint = Color.White
                    )
                }
            }
            Text(
                if (hidden) "•••••••" else "$12,480.50",
                color = Color.White,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("•••• 4821", color = Color.White.copy(alpha = 0.75f), style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.weight(1f))
                Surface(color = Mint.copy(alpha = 0.2f), shape = RoundedCornerShape(50)) {
                    Text(
                        "+2.4% this month",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        color = Mint,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickActions(sendModifier: Modifier) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        QuickAction("Send", Icons.AutoMirrored.Outlined.Send, circleModifier = sendModifier, filled = true)
        QuickAction("Request", Icons.Outlined.CallReceived)
        QuickAction("Scan", Icons.Outlined.QrCodeScanner)
        QuickAction("Top up", Icons.Outlined.Add)
    }
}

@Composable
private fun QuickAction(
    label: String,
    icon: ImageVector,
    circleModifier: Modifier = Modifier,
    filled: Boolean = false
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(
            modifier = circleModifier
                .size(56.dp)
                .background(if (filled) Indigo else Color.White, CircleShape)
                .clickable {},
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = label, tint = if (filled) Color.White else Indigo)
        }
        Text(label, style = MaterialTheme.typography.labelMedium, color = Ink)
    }
}

private data class Transaction(
    val name: String,
    val detail: String,
    val amount: String,
    val icon: ImageVector,
    val tint: Color,
    val positive: Boolean = false
)

private val transactions = listOf(
    Transaction("Morning Brew Café", "Food & drink · 08:12", "-$4.80", Icons.Outlined.LocalCafe, Color(0xFFB45309)),
    Transaction("Fresh Market", "Groceries · Yesterday", "-$62.35", Icons.Outlined.ShoppingBasket, Color(0xFF15803D)),
    Transaction("Northwind Ltd", "Salary · Yesterday", "+$3,200.00", Icons.Outlined.Work, Indigo, positive = true),
    Transaction("City Transit", "Transport · Mon", "-$2.50", Icons.Outlined.DirectionsBus, Color(0xFF0369A1)),
)

@Composable
private fun RecentActivity(modifier: Modifier) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Recent activity", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.weight(1f))
            TextButton(onClick = {}) { Text("See all") }
        }
        Card(
            modifier = modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(modifier = Modifier.padding(vertical = 6.dp)) {
                transactions.forEachIndexed { i, tx ->
                    TransactionRow(tx)
                    if (i < transactions.lastIndex) {
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Mist)
                    }
                }
            }
        }
    }
}

@Composable
private fun TransactionRow(tx: Transaction) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {}
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .background(tx.tint.copy(alpha = 0.12f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(tx.icon, contentDescription = null, tint = tx.tint, modifier = Modifier.size(22.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(tx.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            Text(tx.detail, style = MaterialTheme.typography.bodySmall, color = Slate)
        }
        Text(
            tx.amount,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            color = if (tx.positive) PositiveGreen else Ink
        )
    }
}

// ---------------------------------------------------------------------------------------------
// Tour copy. The cards themselves are shared by every scenario, see scenarios/TourUi.kt
// ---------------------------------------------------------------------------------------------

private val BankTourPalette = TourPalette(
    card = Color.White,
    title = Ink,
    body = Slate,
    accent = Indigo,
    accentSoft = Color(0xFFE0E7FF),
)

@Composable
private fun ShowcaseMsgScope.WelcomeCard(controller: TourController) = WelcomeCard(
    scope = this,
    controller = controller,
    palette = BankTourPalette,
    icon = Icons.Outlined.AccountBalance,
    iconBrush = Brush.linearGradient(listOf(Indigo, Violet)),
    title = "Welcome to Lumen Bank",
    body = "Your new home screen in four quick steps. It takes less than a minute."
)

private data class TourStep(val title: String, val body: String)

private val tourSteps = listOf(
    TourStep(
        "Your money at a glance",
        "Your total balance across all accounts, updated the moment anything changes."
    ),
    TourStep(
        "Hide it in public",
        "Tap the eye icon to mask your balance. Go ahead, try it now."
    ),
    TourStep(
        "Send money in seconds",
        "Pick a contact, enter an amount, done. Transfers between Lumen accounts are instant and free."
    ),
    TourStep(
        "Every transaction, explained",
        "Merchant, category and time for each payment. Tap one to see the full receipt."
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
    palette = BankTourPalette,
    locked = locked,
    lockedHint = "Tap the eye icon to continue"
)
