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
package com.vitorpamplona.amethyst.ui.screen.loggedIn.chats.rooms.datasource

import com.vitorpamplona.amethyst.model.Constants
import com.vitorpamplona.amethyst.service.relays.SincePerRelayMap
import com.vitorpamplona.quartz.experimental.ephemChat.chat.EphemeralChatEvent
import com.vitorpamplona.quartz.experimental.ephemChat.chat.RoomId
import com.vitorpamplona.quartz.nip01Core.relay.client.pool.RelayBasedFilter
import com.vitorpamplona.quartz.nip01Core.relay.filters.Filter
import com.vitorpamplona.quartz.utils.mapOfSet

fun filterFollowingEphemeralChats(
    followingChannels: Set<RoomId>,
    since: SincePerRelayMap?,
): List<RelayBasedFilter>? {
    if (followingChannels.isEmpty()) return null

    val relayRoomDTags =
        mapOfSet {
            followingChannels.forEach { room ->
                if (!room.relayUrl.url.isBlank()) {
                    add(room.relayUrl, room.id.ifBlank { "_" })
                } else {
                    Constants.eventFinderRelays.forEach {
                        add(it, room.id.ifBlank { "_" })
                    }
                }
            }
        }

    return relayRoomDTags.map {
        RelayBasedFilter(
            // Metadata comes from any relay
            relay = it.key,
            filter =
                Filter(
                    kinds = listOf(EphemeralChatEvent.KIND),
                    tags = mapOf("d" to it.value.sorted()),
                    limit = 100,
                    since = since?.get(it.key)?.time,
                ),
        )
    }
}
