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
package com.vitorpamplona.quartz.experimental.profileGallery

import androidx.compose.runtime.Immutable
import com.vitorpamplona.quartz.nip01Core.core.Event
import com.vitorpamplona.quartz.nip01Core.core.HexKey
import com.vitorpamplona.quartz.nip01Core.core.TagArrayBuilder
import com.vitorpamplona.quartz.nip01Core.core.any
import com.vitorpamplona.quartz.nip01Core.signers.eventTemplate
import com.vitorpamplona.quartz.nip01Core.tags.events.ETag
import com.vitorpamplona.quartz.nip31Alts.alt
import com.vitorpamplona.quartz.nip94FileMetadata.tags.BlurhashTag
import com.vitorpamplona.quartz.nip94FileMetadata.tags.DimensionTag
import com.vitorpamplona.quartz.nip94FileMetadata.tags.FallbackTag
import com.vitorpamplona.quartz.nip94FileMetadata.tags.HashSha256Tag
import com.vitorpamplona.quartz.nip94FileMetadata.tags.ImageTag
import com.vitorpamplona.quartz.nip94FileMetadata.tags.MagnetTag
import com.vitorpamplona.quartz.nip94FileMetadata.tags.MimeTypeTag
import com.vitorpamplona.quartz.nip94FileMetadata.tags.ServiceTag
import com.vitorpamplona.quartz.nip94FileMetadata.tags.SizeTag
import com.vitorpamplona.quartz.nip94FileMetadata.tags.SummaryTag
import com.vitorpamplona.quartz.nip94FileMetadata.tags.ThumbTag
import com.vitorpamplona.quartz.nip94FileMetadata.tags.TorrentInfoHash
import com.vitorpamplona.quartz.nip94FileMetadata.tags.UrlTag
import com.vitorpamplona.quartz.utils.TimeUtils

@Immutable
class ProfileGalleryEntryEvent(
    id: HexKey,
    pubKey: HexKey,
    createdAt: Long,
    tags: Array<Array<String>>,
    content: String,
    sig: HexKey,
) : Event(id, pubKey, createdAt, KIND, tags, content, sig) {
    fun url() = tags.firstNotNullOfOrNull(UrlTag::parse)

    fun urls() = tags.mapNotNull(UrlTag::parse)

    fun mimeType() = tags.firstNotNullOfOrNull(MimeTypeTag::parse)

    fun hash() = tags.firstNotNullOfOrNull(HashSha256Tag::parse)

    fun size() = tags.firstNotNullOfOrNull(SizeTag::parse)

    fun dimensions() = tags.firstNotNullOfOrNull(DimensionTag::parse)

    fun magnetURI() = tags.firstNotNullOfOrNull(MagnetTag::parse)

    fun torrentInfoHash() = tags.firstNotNullOfOrNull(TorrentInfoHash::parse)

    fun blurhash() = tags.firstNotNullOfOrNull(BlurhashTag::parse)

    fun image() = tags.firstNotNullOfOrNull(ImageTag::parse)

    fun thumb() = tags.firstNotNullOfOrNull(ThumbTag::parse)

    fun service() = tags.firstNotNullOfOrNull(ServiceTag::parse)

    fun summary() = tags.firstNotNullOfOrNull(SummaryTag::parse)

    fun fallback() = tags.firstNotNullOfOrNull(FallbackTag::parse)

    fun hasUrl() = tags.any(UrlTag::isTag)

    fun isOneOf(mimeTypes: Set<String>) = tags.any(MimeTypeTag::isIn, mimeTypes)

    fun fromEvent() = tags.firstNotNullOfOrNull(ETag::parseId)

    fun hasFromEvent() = tags.any(ETag::isTagged)

    companion object {
        const val KIND = 1163
        const val ALT_DESCRIPTION = "Profile Gallery Entry"

        fun build(
            url: String,
            createdAt: Long = TimeUtils.now(),
            initializer: TagArrayBuilder<ProfileGalleryEntryEvent>.() -> Unit = {},
        ) = eventTemplate(KIND, "", createdAt) {
            url(url)
            alt(ALT_DESCRIPTION)
            initializer()
        }
    }
}
