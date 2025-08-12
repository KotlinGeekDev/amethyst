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
package com.vitorpamplona.amethyst.model

import androidx.compose.runtime.Stable
import com.vitorpamplona.amethyst.ui.dal.DefaultFeedOrder
import com.vitorpamplona.amethyst.ui.dal.ListChange
import com.vitorpamplona.quartz.nip01Core.core.HexKey
import com.vitorpamplona.quartz.nip01Core.relay.normalizer.NormalizedRelayUrl
import com.vitorpamplona.quartz.utils.LargeCache
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import java.lang.ref.WeakReference

@Stable
abstract class Channel : NotesGatherer {
    val notes = LargeCache<HexKey, Note>()
    var lastNote: Note? = null

    private var relays = mapOf<NormalizedRelayUrl, Counter>()

    private var changesFlow: WeakReference<MutableSharedFlow<ListChange<Note>>> = WeakReference(null)

    fun changesFlow(): MutableSharedFlow<ListChange<Note>> {
        val current = changesFlow.get()
        if (current != null) return current
        val new = MutableSharedFlow<ListChange<Note>>(0, 100, BufferOverflow.DROP_OLDEST)
        changesFlow = WeakReference(new)
        return new
    }

    abstract fun toBestDisplayName(): String

    open fun relays(): Set<NormalizedRelayUrl> =
        relays.keys
            .toSortedSet { o1, o2 ->
                val o1Count = relays[o1]?.number ?: 0
                val o2Count = relays[o2]?.number ?: 0
                o2Count.compareTo(o1Count) // descending
            }

    fun updateChannelInfo() {
        flowSet?.metadata?.invalidateData()
    }

    @Synchronized
    fun addRelaySync(briefInfo: NormalizedRelayUrl) {
        if (briefInfo !in relays) {
            relays = relays + Pair(briefInfo, Counter(1))
        }
    }

    fun addRelay(relay: NormalizedRelayUrl) {
        val counter = relays[relay]
        if (counter != null) {
            counter.number++
        } else {
            addRelaySync(relay)
        }
    }

    fun addNote(
        note: Note,
        relay: NormalizedRelayUrl? = null,
    ) {
        if (!notes.containsKey(note.idHex)) {
            notes.put(note.idHex, note)
            note.addGatherer(this)

            if ((note.createdAt() ?: 0) > (lastNote?.createdAt() ?: 0)) {
                lastNote = note
            }

            if (relay != null) {
                addRelay(relay)
            }

            changesFlow.get()?.tryEmit(ListChange.Addition(note))

            flowSet?.notes?.invalidateData()
        }
    }

    override fun removeNote(note: Note) {
        if (notes.containsKey(note.idHex)) {
            notes.remove(note.idHex)
            note.removeGatherer(this)

            if (note == lastNote) {
                lastNote = notes.values().sortedWith(DefaultFeedOrder).firstOrNull()
            }

            changesFlow.get()?.tryEmit(ListChange.Deletion(note))

            flowSet?.notes?.invalidateData()
        }
    }

    fun pruneOldMessages(): Set<Note> {
        val important =
            notes
                .values()
                .sortedWith(DefaultFeedOrder)
                .take(500)
                .toSet()

        val toBeRemoved = notes.filter { key, it -> it !in important }

        toBeRemoved.forEach { notes.remove(it.idHex) }

        changesFlow.get()?.tryEmit(ListChange.SetDeletion(toBeRemoved.toSet()))

        flowSet?.notes?.invalidateData()

        return toBeRemoved.toSet()
    }

    fun pruneHiddenMessages(account: Account): Set<Note> {
        val hidden =
            notes
                .filter { key, it ->
                    it.author?.let { author -> account.isHidden(author) } == true
                }.toSet()

        hidden.forEach { notes.remove(it.idHex) }

        changesFlow.get()?.tryEmit(ListChange.SetDeletion(hidden))

        flowSet?.notes?.invalidateData()

        return hidden.toSet()
    }

    var flowSet: ChannelFlowSet? = null

    @Synchronized
    fun createOrDestroyFlowSync(create: Boolean) {
        if (create) {
            if (flowSet == null) {
                flowSet = ChannelFlowSet(this)
            }
        } else {
            if (flowSet != null && flowSet?.isInUse() == false) {
                flowSet = null
            }
        }
    }

    fun flow(): ChannelFlowSet {
        if (flowSet == null) {
            createOrDestroyFlowSync(true)
        }
        return flowSet!!
    }

    fun clearFlow() {
        if (flowSet != null && flowSet?.isInUse() == false) {
            createOrDestroyFlowSync(false)
        }
    }
}

data class Counter(
    var number: Int = 0,
)

@Stable
class ChannelFlowSet(
    u: Channel,
) {
    // Observers line up here.
    val metadata = ChannelFlow(u)
    val notes = ChannelFlow(u)

    fun isInUse(): Boolean =
        metadata.hasObservers() ||
            notes.hasObservers()
}

class ChannelFlow(
    val channel: Channel,
) {
    val stateFlow = MutableStateFlow(ChannelState(channel))

    fun invalidateData() {
        stateFlow.tryEmit(ChannelState(channel))
    }

    fun hasObservers() = stateFlow.subscriptionCount.value > 0
}

class ChannelState(
    val channel: Channel,
)
