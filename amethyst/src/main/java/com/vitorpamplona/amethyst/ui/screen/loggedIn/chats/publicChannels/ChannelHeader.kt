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
package com.vitorpamplona.amethyst.ui.screen.loggedIn.chats.publicChannels

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.vitorpamplona.amethyst.model.LiveActivitiesChannel
import com.vitorpamplona.amethyst.model.Note
import com.vitorpamplona.amethyst.model.PublicChatChannel
import com.vitorpamplona.amethyst.ui.navigation.INav
import com.vitorpamplona.amethyst.ui.note.LoadChannel
import com.vitorpamplona.amethyst.ui.screen.loggedIn.AccountViewModel
import com.vitorpamplona.amethyst.ui.screen.loggedIn.chats.publicChannels.nip28PublicChat.header.PublicChatChannelHeader
import com.vitorpamplona.amethyst.ui.screen.loggedIn.chats.publicChannels.nip53LiveActivities.LiveActivitiesChannelHeader
import com.vitorpamplona.amethyst.ui.theme.Size10dp
import com.vitorpamplona.amethyst.ui.theme.StdPadding
import com.vitorpamplona.amethyst.ui.theme.innerPostModifier

@Composable
fun RenderChannelHeader(
    channelNote: Note,
    showVideo: Boolean,
    sendToChannel: Boolean,
    accountViewModel: AccountViewModel,
    nav: INav,
) {
    channelNote.channelHex()?.let {
        ChannelHeader(
            channelHex = it,
            showVideo = showVideo,
            sendToChannel = sendToChannel,
            modifier = MaterialTheme.colorScheme.innerPostModifier.padding(Size10dp),
            accountViewModel = accountViewModel,
            nav = nav,
        )
    }
}

@Composable
fun ChannelHeader(
    channelHex: String,
    showVideo: Boolean,
    showFlag: Boolean = true,
    sendToChannel: Boolean = false,
    modifier: Modifier = StdPadding,
    accountViewModel: AccountViewModel,
    nav: INav,
) {
    LoadChannel(channelHex, accountViewModel) {
        when (it) {
            is LiveActivitiesChannel ->
                LiveActivitiesChannelHeader(
                    it,
                    showVideo,
                    showFlag,
                    sendToChannel,
                    modifier,
                    accountViewModel,
                    nav,
                )
            is PublicChatChannel ->
                PublicChatChannelHeader(
                    it,
                    sendToChannel,
                    modifier,
                    accountViewModel,
                    nav,
                )
        }
    }
}
