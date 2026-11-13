package ly.com.tahaben.showcase_layout_compose.domain.usecase

/**
 * The fixed pixel distances the layouts used before 1.3.0. The placement tests run against these
 * so their expected numbers keep documenting the original geometry; [ShowcaseSpacingTest] checks
 * that the dp based values land on the same pixels on a typical phone.
 */
internal val LegacyPixelSpacing = ShowcaseSpacing(
    textBaseOffset = 230f,
    cardInset = 18f,
    messageMaxWidthInset = 90,
    circleExtraOffset = 50f,
    arrowStartAbove = 180f,
    arrowStartAside = 200f,
    arrowHeadGap = 25f,
    curvedArrowEndGap = 50f,
    curvedArrowEdgeRoom = 80f,
    targetTapTolerance = 16f,
    targetSafetyPadding = 100f,
    targetMessageWidthInset = 200f,
    greetingPadding = 40f,
)
