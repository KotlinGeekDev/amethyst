/**
 * Copyright (c) 2024 Vitor Pamplona
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the
 * Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN
 * AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.vitorpamplona.amethyst.service.playback.composable

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.ui.platform.LocalView
import androidx.media3.common.Player

@Composable
fun ControlWhenPlayerIsActive(
    mediaControllerState: MediaControllerState,
    automaticallyStartPlayback: State<Boolean>,
    isClosestToTheCenterOfTheScreen: MutableState<Boolean>,
) {
    val controller = mediaControllerState.controller.value ?: return

    LaunchedEffect(key1 = isClosestToTheCenterOfTheScreen.value, key2 = mediaControllerState) {
        // active means being fully visible
        if (isClosestToTheCenterOfTheScreen.value) {
            // should auto start video from settings?
            if (!automaticallyStartPlayback.value) {
                if (controller.isPlaying) {
                    // if it is visible, it's playing but it wasn't supposed to start automatically.
                    controller.pause()
                }
            } else if (!controller.isPlaying) {
                // if it is visible, was supposed to start automatically, but it's not

                // If something else is playing, play on mute.
                if (BackgroundMedia.hasBackgroundButNot(mediaControllerState)) {
                    controller.volume = 0f
                }
                controller.play()
            }
        } else {
            // Pauses the video when it becomes invisible.
            // Destroys the video later when it Disposes the element
            // meanwhile if the user comes back, the position in the track is saved.
            if (!mediaControllerState.keepPlaying.value) {
                controller.pause()
            }
        }
    }

    val view = LocalView.current

    // Keeps the screen on while playing and viewing videos.
    DisposableEffect(key1 = controller, key2 = view) {
        val listener =
            object : Player.Listener {
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    // doesn't consider the mutex because the screen can turn off if the video
                    // being played in the mutex is not visible.
                    if (view.keepScreenOn != isPlaying) {
                        view.keepScreenOn = isPlaying
                    }
                }
            }

        controller.addListener(listener)
        onDispose {
            if (view.keepScreenOn) {
                view.keepScreenOn = false
            }
            controller.removeListener(listener)
        }
    }
}
