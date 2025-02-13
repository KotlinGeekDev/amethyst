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
package com.vitorpamplona.quartz.nip01Core

import androidx.compose.runtime.Immutable
import com.vitorpamplona.quartz.nip01Core.core.Event
import com.vitorpamplona.quartz.nip01Core.tags.addressables.ATag
import com.vitorpamplona.quartz.nip01Core.tags.dTags.dTag
import com.vitorpamplona.quartz.nip01Core.tags.events.ETag
import com.vitorpamplona.quartz.nip01Core.tags.people.PTag
import com.vitorpamplona.quartz.nip10Notes.tags.MarkedETag
import com.vitorpamplona.quartz.nip19Bech32.entities.NEvent
import com.vitorpamplona.quartz.utils.bytesUsedInMemory
import com.vitorpamplona.quartz.utils.pointerSizeInBytes

@Immutable
data class EventHintBundle<T : Event>(
    val event: T,
) {
    var relay: String? = null
    var authorHomeRelay: String? = null

    constructor(event: T, relayHint: String? = null, authorHomeRelay: String? = null) : this(event) {
        this.relay = relayHint
        this.authorHomeRelay = authorHomeRelay
    }

    fun countMemory(): Long =
        2 * pointerSizeInBytes + // 2 fields, 4 bytes each reference (32bit)
            event.countMemory() +
            (relay?.bytesUsedInMemory() ?: 0)

    fun toNEvent(): String = NEvent.create(event.id, event.pubKey, event.kind, relay)

    fun toETag() = ETag(event.id, relay, event.pubKey)

    fun toATag() = ATag(event.kind, event.pubKey, event.dTag(), relay)

    fun toPTag() = PTag(event.pubKey, authorHomeRelay)

    fun toMarkedETag(marker: MarkedETag.MARKER) = MarkedETag(event.id, relay, marker, event.pubKey)

    fun toETagArray() = ETag.assemble(event.id, relay, event.pubKey)

    fun toQTagArray() = ETag(event.id, relay, event.pubKey).toQTagArray()
}
