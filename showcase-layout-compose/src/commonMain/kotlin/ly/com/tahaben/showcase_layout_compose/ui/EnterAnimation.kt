package ly.com.tahaben.showcase_layout_compose.ui

import kotlinx.coroutines.Job

/**
 * The enter animation of the current step (target transition, arrow, message fade-in), so that
 * `advance()` can stop it before starting the exit animation.
 *
 * Both animate the same `Animatable`s, and an `Animatable` interrupts whoever was animating it
 * before. If a step ends while its enter animation is still pending (for example a locked step
 * whose condition is met right away, or a quick tap on "Next"), a late `animateTo` from the enter
 * animation would cancel the exit animation and, with it, the coroutine that moves to the next
 * index: the showcase would silently get stuck on the current step.
 */
internal class EnterAnimation {
    var job: Job? = null

    fun cancel() {
        job?.cancel()
        job = null
    }
}
