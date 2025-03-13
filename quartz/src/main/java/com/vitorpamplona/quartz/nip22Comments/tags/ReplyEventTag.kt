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
package com.vitorpamplona.quartz.nip22Comments.tags

import androidx.compose.runtime.Immutable
import com.vitorpamplona.quartz.nip01Core.core.HexKey
import com.vitorpamplona.quartz.nip01Core.core.Tag
import com.vitorpamplona.quartz.nip01Core.core.has
import com.vitorpamplona.quartz.nip01Core.hints.types.EventIdHint
import com.vitorpamplona.quartz.nip01Core.tags.events.EventReference
import com.vitorpamplona.quartz.utils.Hex
import com.vitorpamplona.quartz.utils.arrayOfNotNull
import com.vitorpamplona.quartz.utils.ensure

@Immutable
class ReplyEventTag(
    val ref: EventReference,
) {
    constructor(eventId: String, relayHint: String?, pubkey: String?) : this(EventReference(eventId, relayHint, pubkey))

    fun toTagArray() = assemble(ref)

    companion object {
        const val TAG_NAME = "e"

        @JvmStatic
        fun match(tag: Tag) = tag.has(1) && tag[0] == TAG_NAME && tag[1].isNotEmpty()

        @JvmStatic
        fun isTagged(
            tag: Array<String>,
            eventId: String,
        ) = tag.has(1) && tag[0] == TAG_NAME && tag[1] == eventId

        @JvmStatic
        fun isIn(
            tag: Array<String>,
            eventIds: Set<String>,
        ) = tag.has(1) && tag[0] == TAG_NAME && tag[1] in eventIds

        @JvmStatic
        fun parse(tag: Array<String>): ReplyEventTag? {
            ensure(tag.has(1)) { return null }
            ensure(tag[0] == TAG_NAME) { return null }
            ensure(tag[1].length == 64) { return null }
            return ReplyEventTag(tag[1], tag.getOrNull(2), tag.getOrNull(3))
        }

        @JvmStatic
        fun parseKey(tag: Array<String>): String? {
            ensure(tag.has(1)) { return null }
            ensure(tag[0] == TAG_NAME) { return null }
            ensure(tag[1].length == 64) { return null }
            return tag[1]
        }

        @JvmStatic
        fun parseValidKey(tag: Array<String>): String? {
            ensure(tag.has(1)) { return null }
            ensure(tag[0] == TAG_NAME) { return null }
            ensure(tag[1].length == 64) { return null }
            ensure(Hex.isHex(tag[1])) { return null }
            return tag[1]
        }

        @JvmStatic
        fun parseAsHint(tag: Array<String>): EventIdHint? {
            ensure(tag.has(2)) { return null }
            ensure(tag[0] == TAG_NAME) { return null }
            ensure(tag[1].length == 64) { return null }
            ensure(tag[2].isNotEmpty()) { return null }
            return EventIdHint(tag[1], tag[2])
        }

        @JvmStatic
        fun assemble(
            eventId: HexKey,
            relay: String?,
            pubkey: String?,
        ) = arrayOfNotNull(TAG_NAME, eventId, relay, pubkey)

        @JvmStatic
        fun assemble(ref: EventReference) = assemble(ref.eventId, ref.relayHint, ref.author)
    }
}
