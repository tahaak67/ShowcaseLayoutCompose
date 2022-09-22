package ly.com.tahaben.showcase_layout_compose.model

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntSize

/**
 * Created by Taha Ben Ashur (https://github.com/tahaak67) on 1,August,2022
 */

data class ShowcaseData(
    val size: IntSize,
    val position: Offset,
    val message: ShowcaseMsg? = null
)
