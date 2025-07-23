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
package com.vitorpamplona.quartz.nip51Lists.relayLists.tags

import com.vitorpamplona.quartz.nip01Core.core.TagArrayBuilder
import com.vitorpamplona.quartz.nip01Core.relay.normalizer.NormalizedRelayUrl
import com.vitorpamplona.quartz.nip37Drafts.privateOutbox.PrivateOutboxRelayListEvent
import com.vitorpamplona.quartz.nip50Search.SearchRelayListEvent
import com.vitorpamplona.quartz.nip51Lists.PrivateTagArrayEvent
import com.vitorpamplona.quartz.nip51Lists.relayLists.BlockedRelayListEvent
import com.vitorpamplona.quartz.nip51Lists.relayLists.BroadcastRelayListEvent
import com.vitorpamplona.quartz.nip51Lists.relayLists.IndexerRelayListEvent
import com.vitorpamplona.quartz.nip51Lists.relayLists.TrustedRelayListEvent
import com.vitorpamplona.quartz.nip51Lists.relaySets.RelaySetEvent
import com.vitorpamplona.quartz.nip51Lists.tags.NameTag
import kotlin.collections.map

fun <T : PrivateTagArrayEvent> TagArrayBuilder<T>.name(name: String) = addUnique(NameTag.assemble(name))

fun TagArrayBuilder<SearchRelayListEvent>.searchRelays(relays: List<NormalizedRelayUrl>) = addAll(relays.map { RelayTag.assemble(it) })

fun TagArrayBuilder<BlockedRelayListEvent>.blockedRelays(relays: List<NormalizedRelayUrl>) = addAll(relays.map { RelayTag.assemble(it) })

fun TagArrayBuilder<TrustedRelayListEvent>.trustedRelays(relays: List<NormalizedRelayUrl>) = addAll(relays.map { RelayTag.assemble(it) })

fun TagArrayBuilder<BroadcastRelayListEvent>.broadcastRelays(relays: List<NormalizedRelayUrl>) = addAll(relays.map { RelayTag.assemble(it) })

fun TagArrayBuilder<IndexerRelayListEvent>.indexerRelays(relays: List<NormalizedRelayUrl>) = addAll(relays.map { RelayTag.assemble(it) })

fun TagArrayBuilder<PrivateOutboxRelayListEvent>.privateRelays(relays: List<NormalizedRelayUrl>) = addAll(relays.map { RelayTag.assemble(it) })

fun TagArrayBuilder<RelaySetEvent>.relaySet(relays: List<NormalizedRelayUrl>) = addAll(relays.map { RelayTag.assemble(it) })
