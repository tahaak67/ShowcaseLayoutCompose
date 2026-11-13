package ly.com.tahaben.showcase_layout_compose.domain.usecase

import ly.com.tahaben.showcase_layout_compose.model.Side

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
 * The side of the target an arrow comes from, given where its message ended up.
 *
 * A `Side.Top` or `Side.Bottom` arrow always comes from the message's side, so it never runs in from
 * the far side of the target. That happens with the defaults (`Side.Bottom` with `Gravity.Auto`,
 * which puts the message above when there is room) and with a gravity that contradicts
 * [targetFrom]. `Side.Left` and `Side.Right` are kept: they are a deliberate choice for targets near
 * a screen edge.
 */
internal fun arrowSideFacing(targetFrom: Side?, messageSide: MessageSide): Side? = when {
    targetFrom == Side.Top && messageSide == MessageSide.Below -> Side.Bottom
    targetFrom == Side.Bottom && messageSide == MessageSide.Above -> Side.Top
    else -> targetFrom
}
