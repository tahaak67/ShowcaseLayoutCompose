package ly.com.tahaben.showcase_layout_compose.model

/**
 *     Copyright 2023 Taha Ben Ashur (tahaak67)
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
 *
 * Created by Taha Ben Ashur (https://github.com/tahaak67) on 27,Feb,2023
 */

/**
 * MsgAnimation
 * animate the message text and background
 **/

sealed class MsgAnimation(open val duration: Int = DEFAULT_DURATION) {
    companion object {
        const val DEFAULT_DURATION = 500
    }

    /**
     *  animates the text and msg background by changing alpha values from 0f to 1f for enterAnim and 1f to 0f for exitAnim, this is the default animation for message text.
     *  @param duration the time the animation takes from start to finish in milliseconds
     **/
    data class FadeInOut(override val duration: Int = DEFAULT_DURATION) : MsgAnimation(duration)

    /**
     *  completely removes animations from text and msg
     **/
    object None : MsgAnimation(0)

}