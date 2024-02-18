package ly.com.tahaben.showcase_layout_compose.domain


interface ShowcaseEventListener {
    fun onEvent(level: Level, event: String)
}
