package ly.com.tahaben.showcaselayoutcompose.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ly.com.tahaben.showcase_layout_compose.model.Arrow
import ly.com.tahaben.showcase_layout_compose.model.Gravity
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
        mutableStateOf(true)
    }

    ShowcaseLayout(
        isShowcasing = isShowcasing,
        onFinish = { isShowcasing = false },
        isDarkLayout = isSystemInDarkTheme()
    ) {

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
                        Showcase(
                            k = 3,
                            message = ShowcaseMsg(
                                "From drop down menu, you can access the About app screen!",
                                textStyle = TextStyle(color = MaterialTheme.colors.onPrimary),
                                msgBackground = MaterialTheme.colors.primary,
                                roundedCorner = 15.dp,
                                arrow = Arrow(
                                    animateFromMsg = true,
                                    color = MaterialTheme.colors.primary
                                )
                            )
                        ) {
                            Icon(Icons.Default.MoreVert, stringResource(R.string.drop_down_menu))
                        }
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
                    Showcase(
                        k = 4, message = ShowcaseMsg(
                            "Useful tip tho :P",
                            textStyle = TextStyle(color = Color.DarkGray),
                            msgBackground = Color(0xFFE0F2F1),
                            arrow = Arrow(
                                targetFrom = Side.Top,
                                hasHead = false,
                                color = MaterialTheme.colors.primary
                            )
                        )
                    ) {
                        Text(
                            text = tip,
                            style = MaterialTheme.typography.h5,
                            color = MaterialTheme.colors.onPrimary
                        )
                    }
                }
                Spacer(modifier = Modifier.height(spacing.spaceLarge))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Showcase(
                        k = 1, message =
                        ShowcaseMsg(
                            "Track your phone usage from here",
                            textStyle = TextStyle(color = Color(0xFF827717)),
                            msgBackground = MaterialTheme.colors.primary,
                            gravity = Gravity.Bottom,
                            arrow = Arrow(color = MaterialTheme.colors.primary)
                        )
                    ) {
                        MainScreenCard(
                            text = stringResource(R.string.usage),
                            iconId = R.drawable.ic_usage,
                            status = ""
                        ) { }
                    }
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
                    Showcase(k = 2, message = null) {
                        MainScreenCard(
                            text = stringResource(R.string.infinite_scrolling),
                            iconId = R.drawable.ic_swipe_vertical_24,
                            status = if (isInfiniteScrollBlockerEnabled) stringResource(id = R.string.enabled) else stringResource(
                                id = R.string.disabled
                            )
                        ) { }
                    }
                }
                Spacer(modifier = Modifier.height(spacing.spaceMedium))
            }
        }
    }
}