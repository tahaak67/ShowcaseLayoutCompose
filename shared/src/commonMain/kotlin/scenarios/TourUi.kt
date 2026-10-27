package scenarios

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Replay
import androidx.compose.material.icons.outlined.TouchApp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ly.com.tahaben.showcase_layout_compose.model.ShowcaseMsgScope

/**
 * Colors of the tour cards a scenario shows inside the showcase overlay. Each scenario picks a
 * palette that matches its app, so the same card reads well on a dark overlay and on a light one.
 */
data class TourPalette(
    val card: Color,
    val title: Color,
    val body: Color,
    val accent: Color,
    val onAccent: Color = Color.White,
    /** inactive step dots and the background of the "tap to continue" hint */
    val accentSoft: Color,
)

/**
 * The greeting card shown before the first step: an icon, a title, a line of copy and the two
 * buttons every onboarding has. Buttons drive the showcase through [scope].
 */
@Composable
fun WelcomeCard(
    scope: ShowcaseMsgScope,
    palette: TourPalette,
    icon: ImageVector,
    iconBrush: Brush,
    title: String,
    body: String,
    startLabel: String = "Start the tour",
    laterLabel: String = "Maybe later",
) {
    Card(
        modifier = Modifier.width(300.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = palette.card)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier.size(64.dp).background(iconBrush, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = palette.onAccent, modifier = Modifier.size(32.dp))
            }
            Text(
                title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = palette.title,
                textAlign = TextAlign.Center
            )
            Text(body, style = MaterialTheme.typography.bodyMedium, color = palette.body, textAlign = TextAlign.Center)
            Spacer(Modifier.height(4.dp))
            Button(
                onClick = { scope.next() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = palette.accent, contentColor = palette.onAccent),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(startLabel)
            }
            TextButton(onClick = { scope.skip() }) {
                Text(laterLabel, color = palette.body)
            }
        }
    }
}

/**
 * The message card of one tour step: progress dots, a title, a line of copy and Back / Skip / Next.
 * A [locked] step hides Next and shows [lockedHint] instead; the showcase moves on by itself once
 * the user performs the action (`canProceed` on the step's showcase modifier).
 */
@Composable
fun TourCard(
    step: Int,
    total: Int,
    title: String,
    body: String,
    scope: ShowcaseMsgScope,
    palette: TourPalette,
    locked: Boolean = false,
    lockedHint: String = "",
) {
    Card(
        modifier = Modifier.width(300.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = palette.card)
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                StepDots(current = step, total = total, palette = palette)
                Spacer(Modifier.weight(1f))
                Text("$step of $total", style = MaterialTheme.typography.labelMedium, color = palette.body)
            }
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = palette.title)
            Text(body, style = MaterialTheme.typography.bodyMedium, color = palette.body)
            Spacer(Modifier.height(2.dp))
            if (locked) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(palette.accentSoft, RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Outlined.TouchApp, contentDescription = null, tint = palette.accent, modifier = Modifier.size(20.dp))
                    Text(
                        lockedHint,
                        style = MaterialTheme.typography.labelLarge,
                        color = palette.accent,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = { scope.skip() }, contentPadding = ButtonDefaults.TextButtonContentPadding) {
                        Text("Skip", color = palette.body)
                    }
                }
            } else {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    // step 1's "previous" is the greeting, which Back should not reopen
                    if (step > 1 && scope.hasPrevious) {
                        TextButton(onClick = { scope.previous() }) { Text("Back", color = palette.body) }
                    }
                    Spacer(Modifier.weight(1f))
                    TextButton(onClick = { scope.skip() }) { Text("Skip", color = palette.body) }
                    Button(
                        onClick = { scope.next() },
                        colors = ButtonDefaults.buttonColors(containerColor = palette.accent, contentColor = palette.onAccent),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(if (step == total) "Finish" else "Next")
                    }
                }
            }
        }
    }
}

@Composable
private fun StepDots(current: Int, total: Int, palette: TourPalette) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
        repeat(total) { i ->
            val active = i + 1 == current
            Box(
                modifier = Modifier
                    .height(6.dp)
                    .width(if (active) 18.dp else 6.dp)
                    .background(if (active) palette.accent else palette.accentSoft, RoundedCornerShape(50))
            )
        }
    }
}

/**
 * The dropdown behind a scenario's "?" or "⋮" icon: replay the tour and switch between the two
 * showcase layouts so the same tour can be compared on both.
 */
@Composable
fun TourOptionsMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    useTargetLayout: Boolean,
    onReplay: () -> Unit,
    onLayoutChange: (Boolean) -> Unit,
) {
    DropdownMenu(expanded = expanded, onDismissRequest = onDismiss) {
        DropdownMenuItem(
            text = { Text("Replay tour") },
            leadingIcon = { Icon(Icons.Outlined.Replay, contentDescription = null) },
            onClick = { onDismiss(); onReplay() }
        )
        HorizontalDivider()
        LayoutMenuItem("TargetShowcaseLayout", "Spotlight, morphs between targets", useTargetLayout) {
            onDismiss(); onLayoutChange(true)
        }
        LayoutMenuItem("ShowcaseLayout", "Full-screen overlay with cutout", !useTargetLayout) {
            onDismiss(); onLayoutChange(false)
        }
    }
}

@Composable
private fun LayoutMenuItem(title: String, subtitle: String, selected: Boolean, onClick: () -> Unit) {
    DropdownMenuItem(
        text = {
            Column {
                Text(title)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        leadingIcon = {
            if (selected) Icon(Icons.Outlined.Check, contentDescription = "Selected", tint = MaterialTheme.colorScheme.primary)
            else Spacer(Modifier.size(24.dp))
        },
        onClick = onClick
    )
}
