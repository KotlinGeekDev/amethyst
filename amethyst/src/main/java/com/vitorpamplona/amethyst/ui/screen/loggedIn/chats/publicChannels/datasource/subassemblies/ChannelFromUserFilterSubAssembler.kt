/**
 * Copyright (c) 2025 Vitor Pamplona
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
package com.vitorpamplona.amethyst.ui.screen.loggedIn.chats.publicChannels.datasource.subassemblies

import com.vitorpamplona.amethyst.model.Channel
import com.vitorpamplona.amethyst.model.EphemeralChatChannel
import com.vitorpamplona.amethyst.model.LiveActivitiesChannel
import com.vitorpamplona.amethyst.model.PublicChatChannel
import com.vitorpamplona.amethyst.service.relayClient.eoseManagers.PerUserAndFollowListEoseManager
import com.vitorpamplona.amethyst.service.relays.SincePerRelayMap
import com.vitorpamplona.amethyst.ui.screen.loggedIn.chats.publicChannels.datasource.ChannelQueryState
import com.vitorpamplona.quartz.nip01Core.relay.client.NostrClient
import com.vitorpamplona.quartz.nip01Core.relay.client.pool.RelayBasedFilter

class ChannelFromUserFilterSubAssembler(
    client: NostrClient,
    allKeys: () -> Set<ChannelQueryState>,
) : PerUserAndFollowListEoseManager<ChannelQueryState, Channel>(client, allKeys) {
    override fun updateFilter(
        key: ChannelQueryState,
        since: SincePerRelayMap?,
    ): List<RelayBasedFilter>? =
        when (val channel = key.channel) {
            is EphemeralChatChannel -> filterMyMessagesToEphemeralChat(channel, userHex(key), since)
            is PublicChatChannel -> filterMyMessagesToPublicChat(channel, user(key).pubkeyHex, since)
            is LiveActivitiesChannel -> filterMyMessagesToLiveActivities(channel, userHex(key), since)
            else -> null
        }

    fun userHex(key: ChannelQueryState) = key.account.userProfile().pubkeyHex

    override fun user(key: ChannelQueryState) = key.account.userProfile()

    override fun list(key: ChannelQueryState) = key.channel
}
