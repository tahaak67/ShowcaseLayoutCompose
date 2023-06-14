package ly.com.tahaben.showcaselayoutcompose.ui

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import ly.com.tahaben.showcaselayoutcompose.BuildConfig
import ly.com.tahaben.showcaselayoutcompose.R
import ly.com.tahaben.showcaselayoutcompose.ui.theme.LocalSpacing

@Composable
fun AboutScreen(
    onNavigateUp: () -> Unit
) {

    val spacing = LocalSpacing.current
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .background(MaterialTheme.colors.primary)
    ) {
        TopAppBar(
            title = {
                Text(
                    text = stringResource(R.string.about_app),
                    color = MaterialTheme.colors.onPrimary
                )
            },
            navigationIcon = {
                IconButton(onClick = onNavigateUp) {
                    Icon(
                        modifier = Modifier,
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = stringResource(id = R.string.back)
                    )
                }
            },
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = spacing.spaceMedium)
        ) {
            Spacer(modifier = Modifier.height(spacing.spaceMedium))
            Text(
                text = stringResource(R.string.about_library),
                style = MaterialTheme.typography.h6,
                color = MaterialTheme.colors.onPrimary
            )
            Spacer(modifier = Modifier.height(spacing.spaceMedium))
            Text(
                text = stringResource(R.string.developer),
                style = MaterialTheme.typography.h5,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colors.onPrimary
            )
            Spacer(modifier = Modifier.height(spacing.spaceMedium))
            Text(
                text = stringResource(R.string.taha_name_dev),
                style = MaterialTheme.typography.h5,
                color = MaterialTheme.colors.onPrimary
            )
            Spacer(modifier = Modifier.height(spacing.spaceMedium))
            Text(
                text = stringResource(R.string.source_code),
                style = MaterialTheme.typography.h5,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colors.onPrimary
            )
            Spacer(modifier = Modifier.height(spacing.spaceMedium))
            Text(
                modifier = Modifier.clickable {
                    val url = Intent(Intent.ACTION_VIEW).apply {
                        data =
                            Uri.parse("https://github.com/tahaak67/ShowcaseLayoutCompose")
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    try {
                        context.startActivity(url)
                    } catch (ex: Exception) {
                        clipboardManager.setText(AnnotatedString("https://github.com/tahaak67/ShowcaseLayoutCompose"))
                        Toast.makeText(
                            context,
                            R.string.cant_open_link_error_link_copied,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                },
                text = "https://github.com/tahaak67/ShowcaseLayoutCompose",
                style = MaterialTheme.typography.h5,
                color = Color.Blue
            )
            Spacer(modifier = Modifier.height(spacing.spaceMedium))
            Text(
                text = stringResource(R.string.version),
                style = MaterialTheme.typography.h5,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colors.onPrimary
            )
            Spacer(modifier = Modifier.height(spacing.spaceMedium))
            Text(
                text = "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
                style = MaterialTheme.typography.h5,
                color = MaterialTheme.colors.onPrimary
            )

        }
    }

}