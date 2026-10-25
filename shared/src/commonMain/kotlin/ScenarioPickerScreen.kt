import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

/** A demo scenario: a realistic mini app with a showcase tour built on top of it. */
enum class Scenario(
    val title: String,
    val tagline: String,
    val icon: ImageVector,
    val accent: Color,
    val available: Boolean = true,
) {
    BANKING(
        title = "Lumen Bank",
        tagline = "Onboarding tour of a banking home screen: balance card, quick actions and a hands-on \"try it\" step.",
        icon = Icons.Outlined.AccountBalance,
        accent = Color(0xFF3730A3),
    ),
    MUSIC(
        title = "Now Playing",
        tagline = "A dark music player: circular targets, light overlay and a pulsing play button.",
        icon = Icons.Outlined.MusicNote,
        accent = Color(0xFFB91C1C),
        available = false,
    ),
    CHAT(
        title = "Messages",
        tagline = "Learn-by-doing tour of a chat app, driven by step locks.",
        icon = Icons.Outlined.ChatBubbleOutline,
        accent = Color(0xFF047857),
        available = false,
    ),
    PLAYGROUND(
        title = "Playground",
        tagline = "Every knob of the library: arrows, heads, shapes, animations, custom messages and step locks.",
        icon = Icons.Outlined.Tune,
        accent = Color(0xFF006971),
    ),
}

private const val REPO_URL = "https://github.com/tahaak67/ShowcaseLayoutCompose"

@Composable
fun ScenarioPickerScreen(openUrl: (String) -> Boolean, onOpen: (Scenario) -> Unit) {
    val snackbarHostState = remember { SnackbarHostState() }
    val clipboard = LocalClipboardManager.current
    val scope = rememberCoroutineScope()

    MyTheme(useDarkTheme = false) {
        Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState()),
                contentAlignment = Alignment.TopCenter
            ) {
                Column(
                    modifier = Modifier
                        .widthIn(max = 520.dp)
                        .padding(horizontal = 20.dp, vertical = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "Showcase Layout Compose",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Pick a scenario to see the library inside a real app. Each tour is a few lines of code on top of an ordinary screen.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(8.dp))
                    Scenario.entries.forEach { scenario ->
                        ScenarioCard(scenario = scenario, onClick = { onOpen(scenario) })
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("By Tahaak67", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        TextButton(onClick = {
                            if (!openUrl(REPO_URL)) {
                                clipboard.setText(AnnotatedString(REPO_URL))
                                scope.launch { snackbarHostState.showSnackbar("Link copied") }
                            }
                        }) {
                            Text("GitHub")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ScenarioCard(scenario: Scenario, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = scenario.available, onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (scenario.available) MaterialTheme.colorScheme.surface
            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (scenario.available) 2.dp else 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(scenario.accent.copy(alpha = if (scenario.available) 0.12f else 0.06f), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    scenario.icon,
                    contentDescription = null,
                    tint = if (scenario.available) scenario.accent else scenario.accent.copy(alpha = 0.4f),
                    modifier = Modifier.size(28.dp)
                )
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        scenario.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = if (scenario.available) MaterialTheme.colorScheme.onSurface
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (!scenario.available) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Text(
                                "Coming soon",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }
                Text(
                    scenario.tagline,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (scenario.available) {
                Icon(
                    Icons.Outlined.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
