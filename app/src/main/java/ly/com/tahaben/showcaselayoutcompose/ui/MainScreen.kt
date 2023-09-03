package ly.com.tahaben.showcaselayoutcompose.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import ly.com.tahaben.showcase_layout_compose.domain.ShowcaseEventListener
import ly.com.tahaben.showcase_layout_compose.model.Arrow
import ly.com.tahaben.showcase_layout_compose.model.Gravity
import ly.com.tahaben.showcase_layout_compose.model.MsgAnimation
import ly.com.tahaben.showcase_layout_compose.model.ShowcaseMsg
import ly.com.tahaben.showcase_layout_compose.model.Side
import ly.com.tahaben.showcase_layout_compose.ui.ShowcaseLayout
import ly.com.tahaben.showcaselayoutcompose.R
import ly.com.tahaben.showcaselayoutcompose.ui.theme.LocalSpacing

@Composable
fun MainScreen(
    tip: String,
    isGrayscaleEnabled: Boolean,
    isInfiniteScrollBlockerEnabled: Boolean,
    isNotificationFilterEnabled: Boolean,
    navigateToAbout: () -> Unit
) {
    val spacing = LocalSpacing.current
    var mDisplayMenu by remember { mutableStateOf(false) }
    var isShowcasing by remember {
        mutableStateOf(false)
    }
    LaunchedEffect(key1 = true) {
        delay(500)
        isShowcasing = true
    }
    val greetingString = buildAnnotatedString {
        append("Welcome to ")
        pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
        append("My App")
        pop()
        append(", let's take you on a quick tour!")
        pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
        append("\n Tap anywhere")
        pop()
        append(" to continue")
    }

    ShowcaseLayout(
        isShowcasing = isShowcasing,
        onFinish = { isShowcasing = false },
        isDarkLayout = isSystemInDarkTheme(),
        greeting = ShowcaseMsg(
            text = greetingString,
            textStyle = TextStyle(color = Color.White, textAlign = TextAlign.Center)
        )
    ) {
        registerEventListener(object: ShowcaseEventListener {
            override fun onEvent(event: String) {
                println(event)
            }
        })
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.primary)
        ) {
            TopAppBar(
                title = {
                    Text(text = "ShowcaseLayout Test", color = MaterialTheme.colors.onPrimary)
                },
                actions = {

                    // Creating Icon button for dropdown menu
                    IconButton(onClick = { mDisplayMenu = !mDisplayMenu }) {
                        Icon(
                            modifier = Modifier.showcase(
                                k = 3,
                                message = ShowcaseMsg(
                                    buildAnnotatedString {
                                        append("From the ")
                                        pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
                                        append("drop down menu")
                                        pop()
                                        append(", you can access the ")
                                        pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
                                        append("About app")
                                        pop()
                                        append(" screen!")
                                    },
                                    textStyle = TextStyle(
                                        color = MaterialTheme.colors.onPrimary,
                                        fontSize = 24.sp
                                    ),
                                    msgBackground = MaterialTheme.colors.primary,
                                    roundedCorner = 25.dp,
                                    arrow = Arrow(
                                        curved = true,
                                        color = MaterialTheme.colors.primary
                                    ),
                                    enterAnim = MsgAnimation.FadeInOut(),
                                    exitAnim = MsgAnimation.FadeInOut()
                                )
                            ),
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = stringResource(id = R.string.drop_down_menu),
                        )
                    }

                    // Creating a dropdown menu
                    DropdownMenu(
                        expanded = mDisplayMenu,
                        onDismissRequest = { mDisplayMenu = false }
                    ) {
                        DropdownMenuItem(onClick = {
                        }) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextButton(onClick = navigateToAbout) {
                                    Text(
                                        text = stringResource(R.string.about_app),
                                        style = MaterialTheme.typography.h6,
                                        fontWeight = FontWeight.Normal,
                                        textAlign = TextAlign.Center,
                                        color = MaterialTheme.colors.onPrimary
                                    )
                                }
                            }
                        }
                    }
                }
            )
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = spacing.spaceLarge)
            ) {
                Spacer(modifier = Modifier.height(spacing.spaceExtraLarge))
                Text(
                    text = stringResource(id = R.string.hello),
                    style = MaterialTheme.typography.h1,
                    color = MaterialTheme.colors.onPrimary
                )
                Spacer(modifier = Modifier.height(spacing.spaceMedium))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_tip),
                        contentDescription = stringResource(R.string.tip_icon_description)
                    )
                    Spacer(modifier = Modifier.width(spacing.spaceExtraSmall))

                    Text(
                        modifier = Modifier.showcase(
                            k = 4, message = ShowcaseMsg(
                                "Useful tip tho :P",
                                textStyle = TextStyle(color = Color.DarkGray),
                                msgBackground = Color(0xFFE0F2F1),
                                arrow = Arrow(
                                    targetFrom = Side.Top,
                                    hasHead = false,
                                    color = MaterialTheme.colors.primary
                                ),

                                )
                        ),
                        text = tip,
                        style = MaterialTheme.typography.h5,
                        color = MaterialTheme.colors.onPrimary
                    )

                }
                Spacer(modifier = Modifier.height(spacing.spaceLarge))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {

                        MainScreenCard(
                            modifier = Modifier.showcase(
                                k = 1, message =
                                ShowcaseMsg(
                                    "Track your phone usage from here",
                                    textStyle = TextStyle(
                                        color = Color(0xFF827717),
                                        fontSize = 18.sp
                                    ),
                                    msgBackground = MaterialTheme.colors.primary,
                                    gravity = Gravity.Bottom,
                                    arrow = Arrow(color = MaterialTheme.colors.primary),
                                    enterAnim = MsgAnimation.FadeInOut(),
                                    exitAnim = MsgAnimation.FadeInOut()
                                )
                            ),
                            text = stringResource(R.string.usage),
                            iconId = R.drawable.ic_usage,
                            status = ""
                        ) { }

                    MainScreenCard(
                        text = stringResource(R.string.notifications_filter),
                        iconId = R.drawable.ic_notification,
                        status = if (isNotificationFilterEnabled) stringResource(id = R.string.enabled) else stringResource(
                            id = R.string.disabled
                        )
                    ) { }
                }
                Spacer(modifier = Modifier.height(spacing.spaceMedium))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    MainScreenCard(
                        text = stringResource(R.string.grayscale),
                        iconId = R.drawable.ic_outline_color_lens_24,
                        status = if (isGrayscaleEnabled) stringResource(id = R.string.enabled) else stringResource(
                            id = R.string.disabled
                        )
                    ) { }
                        MainScreenCard(
                            modifier = Modifier.showcase(k = 2, message = null),
                            text = stringResource(R.string.infinite_scrolling),
                            iconId = R.drawable.ic_swipe_vertical_24,
                            status = if (isInfiniteScrollBlockerEnabled) stringResource(id = R.string.enabled) else stringResource(
                                id = R.string.disabled
                            )
                        ) { }

                }
                Spacer(modifier = Modifier.height(spacing.spaceMedium))
            }
        }
    }
}