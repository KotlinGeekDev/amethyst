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
package com.vitorpamplona.quartz.nip23LongContent

import androidx.compose.runtime.Immutable
import com.vitorpamplona.quartz.nip01Core.HexKey
import com.vitorpamplona.quartz.nip01Core.core.AddressableEvent
import com.vitorpamplona.quartz.nip01Core.core.TagArrayBuilder
import com.vitorpamplona.quartz.nip01Core.signers.eventTemplate
import com.vitorpamplona.quartz.nip01Core.tags.addressables.ATag
import com.vitorpamplona.quartz.nip01Core.tags.addressables.Address
import com.vitorpamplona.quartz.nip01Core.tags.dTags.dTag
import com.vitorpamplona.quartz.nip01Core.tags.hashtags.hashtags
import com.vitorpamplona.quartz.nip10Notes.BaseThreadedEvent
import com.vitorpamplona.quartz.nip23LongContent.tags.ImageTag
import com.vitorpamplona.quartz.nip23LongContent.tags.PublishedAtTag
import com.vitorpamplona.quartz.nip23LongContent.tags.SummaryTag
import com.vitorpamplona.quartz.nip23LongContent.tags.TitleTag
import com.vitorpamplona.quartz.nip31Alts.alt
import com.vitorpamplona.quartz.utils.TimeUtils
import java.util.UUID

@Immutable
class LongTextNoteEvent(
    id: HexKey,
    pubKey: HexKey,
    createdAt: Long,
    tags: Array<Array<String>>,
    content: String,
    sig: HexKey,
) : BaseThreadedEvent(id, pubKey, createdAt, KIND, tags, content, sig),
    AddressableEvent {
    override fun dTag() = tags.dTag()

    override fun aTag(relayHint: String?) = ATag(kind, pubKey, dTag(), relayHint)

    override fun address() = Address(kind, pubKey, dTag())

    override fun addressTag() = ATag.assembleATagId(kind, pubKey, dTag())

    fun topics() = hashtags()

    fun title() = tags.firstNotNullOfOrNull(TitleTag::parse)

    fun image() = tags.firstNotNullOfOrNull(ImageTag::parse)

    fun summary() = tags.firstNotNullOfOrNull(SummaryTag::parse)

    fun publishedAt() = tags.firstNotNullOfOrNull(PublishedAtTag::parse)

    companion object {
        const val KIND = 30023

        fun build(
            description: String,
            title: String,
            summary: String? = null,
            image: String? = null,
            publishedAt: Long? = null,
            dTag: String = UUID.randomUUID().toString(),
            createdAt: Long = TimeUtils.now(),
            initializer: TagArrayBuilder<LongTextNoteEvent>.() -> Unit = {},
        ) = eventTemplate(KIND, description, createdAt) {
            dTag(dTag)
            alt("Blog post: $title")

            title(title)
            summary?.let { summary(it) }
            image?.let { image(it) }
            publishedAt?.let { publishedAt(it) }

            initializer()
        }
    }
}
