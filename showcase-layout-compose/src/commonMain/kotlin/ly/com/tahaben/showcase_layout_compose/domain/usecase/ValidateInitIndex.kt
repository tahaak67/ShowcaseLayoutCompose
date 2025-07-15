package ly.com.tahaben.showcase_layout_compose.domain.usecase

import ly.com.tahaben.showcase_layout_compose.model.ShowcaseMsg


fun validateInitIndex(initIndex: Int, greeting: ShowcaseMsg?): Int {
    return  if (greeting == null) initIndex.coerceAtLeast(1) else initIndex
}
