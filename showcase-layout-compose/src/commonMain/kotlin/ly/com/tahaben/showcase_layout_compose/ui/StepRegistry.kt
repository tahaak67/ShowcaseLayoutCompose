package ly.com.tahaben.showcase_layout_compose.ui

import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Modifier
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.platform.InspectorInfo

/**
 *     Copyright 2026 Taha Ben Ashur (tahaak67)
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */

/**
 * Per-step state that the showcase modifiers publish and the layout reads.
 *
 * Owned (remembered) by the layout and injected into [ShowcaseScopeImpl], since that scope object
 * is recreated on every recomposition. Entries are deliberately kept when a step's node detaches:
 * whether a step that left the screen may proceed is the caller's decision.
 */
internal class StepRegistry {
    /** `canProceed` per step; a `false` entry locks that step. Snapshot state so the overlay reacts. */
    val conditions: SnapshotStateMap<Int, Boolean> = mutableStateMapOf()

    /** `onProceed` per step, read only when the step ends, so a plain map is enough. */
    val onProceed: MutableMap<Int, () -> Unit> = HashMap()
}

/**
 * Publishes a step's `canProceed` flag and `onProceed` callback into [registry].
 *
 * The writes happen from the modifier node's attach/update phase rather than during composition,
 * so a flag that flips inside the target's own recomposition scope is still observed by the
 * overlay without a snapshot write in the middle of composing.
 */
internal fun Modifier.stepRegistration(
    index: Int,
    canProceed: Boolean,
    onProceed: (() -> Unit)?,
    registry: StepRegistry,
): Modifier = this.then(StepRegistrationElement(index, canProceed, onProceed, registry))

private data class StepRegistrationElement(
    val index: Int,
    val canProceed: Boolean,
    val onProceed: (() -> Unit)?,
    val registry: StepRegistry,
) : ModifierNodeElement<StepRegistrationNode>() {

    override fun create() = StepRegistrationNode(index, canProceed, onProceed, registry)

    override fun update(node: StepRegistrationNode) {
        node.index = index
        node.canProceed = canProceed
        node.onProceed = onProceed
        node.registry = registry
        node.publish()
    }

    override fun InspectorInfo.inspectableProperties() {
        name = "stepRegistration"
        properties["index"] = index
        properties["canProceed"] = canProceed
    }
}

private class StepRegistrationNode(
    var index: Int,
    var canProceed: Boolean,
    var onProceed: (() -> Unit)?,
    var registry: StepRegistry,
) : Modifier.Node() {

    override fun onAttach() = publish()

    fun publish() {
        if (registry.conditions[index] != canProceed) registry.conditions[index] = canProceed
        val callback = onProceed
        if (callback == null) registry.onProceed.remove(index) else registry.onProceed[index] = callback
    }
}
