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
package com.vitorpamplona.quartz.nip10Notes

import androidx.compose.runtime.Immutable
import com.vitorpamplona.quartz.nip01Core.HexKey
import com.vitorpamplona.quartz.nip01Core.core.AddressableEvent
import com.vitorpamplona.quartz.nip01Core.core.Event
import com.vitorpamplona.quartz.nip01Core.signers.NostrSigner
import com.vitorpamplona.quartz.nip01Core.tags.addressables.ATag
import com.vitorpamplona.quartz.nip01Core.tags.geohash.geohashMipMap
import com.vitorpamplona.quartz.nip01Core.tags.hashtags.buildHashtagTags
import com.vitorpamplona.quartz.nip10Notes.content.buildUrlRefs
import com.vitorpamplona.quartz.nip10Notes.content.findHashtags
import com.vitorpamplona.quartz.nip10Notes.content.findURLs
import com.vitorpamplona.quartz.nip30CustomEmoji.EmojiUrl
import com.vitorpamplona.quartz.nip31Alts.AltTagSerializer
import com.vitorpamplona.quartz.nip36SensitiveContent.ContentWarningSerializer
import com.vitorpamplona.quartz.nip57Zaps.splits.ZapSplitSetup
import com.vitorpamplona.quartz.nip57Zaps.splits.ZapSplitSetupSerializer
import com.vitorpamplona.quartz.nip57Zaps.zapraiser.ZapRaiserSerializer
import com.vitorpamplona.quartz.nip92IMeta.IMetaTag
import com.vitorpamplona.quartz.nip92IMeta.Nip92MediaAttachments
import com.vitorpamplona.quartz.utils.TimeUtils

@Immutable
class TextNoteEvent(
    id: HexKey,
    pubKey: HexKey,
    createdAt: Long,
    tags: Array<Array<String>>,
    content: String,
    sig: HexKey,
) : BaseTextNoteEvent(id, pubKey, createdAt, KIND, tags, content, sig) {
    fun root() = tags.firstOrNull { it.size > 3 && it[3] == "root" }?.get(1)

    companion object {
        const val KIND = 1
        const val ALT = "A short note: "

        fun shortedMessageForAlt(msg: String): String {
            if (msg.length < 50) return ALT + msg
            return ALT + msg.take(50) + "..."
        }

        fun create(
            msg: String,
            replyTos: List<String>? = null,
            mentions: List<String>? = null,
            addresses: List<ATag>? = null,
            extraTags: List<String>? = null,
            zapReceiver: List<ZapSplitSetup>? = null,
            markAsSensitive: Boolean = false,
            zapRaiserAmount: Long? = null,
            replyingTo: String? = null,
            root: String? = null,
            directMentions: Set<HexKey> = emptySet(),
            geohash: String? = null,
            imetas: List<IMetaTag>? = null,
            emojis: List<EmojiUrl>? = null,
            forkedFrom: Event? = null,
            signer: NostrSigner,
            createdAt: Long = TimeUtils.now(),
            isDraft: Boolean,
            onReady: (TextNoteEvent) -> Unit,
        ) {
            val tags = mutableListOf<Array<String>>()
            tags.add(AltTagSerializer.toTagArray(shortedMessageForAlt(msg)))
            replyTos?.let {
                tags.addAll(
                    it.positionalMarkedTags(
                        tagName = "e",
                        root = root,
                        replyingTo = replyingTo,
                        directMentions = directMentions,
                        forkedFrom = forkedFrom?.id,
                    ),
                )
            }
            mentions?.forEach {
                if (it in directMentions) {
                    tags.add(arrayOf("p", it, "", "mention"))
                } else {
                    tags.add(arrayOf("p", it))
                }
            }
            replyTos?.forEach {
                if (it in directMentions) {
                    tags.add(arrayOf("q", it))
                }
            }
            addresses?.forEach {
                if (it.toTag() in directMentions) {
                    tags.add(arrayOf("q", it.toTag()))
                }
            }
            addresses
                ?.map { it.toTag() }
                ?.let {
                    tags.addAll(
                        it.positionalMarkedTags(
                            tagName = "a",
                            root = root,
                            replyingTo = replyingTo,
                            directMentions = directMentions,
                            forkedFrom = (forkedFrom as? AddressableEvent)?.address()?.toTag(),
                        ),
                    )
                }
            tags.addAll(buildHashtagTags(findHashtags(msg) + (extraTags ?: emptyList())))
            tags.addAll(buildUrlRefs(findURLs(msg)))
            zapReceiver?.forEach { tags.add(ZapSplitSetupSerializer.toTagArray(it)) }
            zapRaiserAmount?.let { tags.add(ZapRaiserSerializer.toTagArray(it)) }

            if (markAsSensitive) {
                tags.add(ContentWarningSerializer.toTagArray())
            }

            geohash?.let { tags.addAll(geohashMipMap(it)) }
            imetas?.forEach { tags.add(Nip92MediaAttachments.createTag(it)) }
            emojis?.forEach { tags.add(it.toTagArray()) }

            if (isDraft) {
                signer.assembleRumor(createdAt, KIND, tags.toTypedArray(), msg, onReady)
            } else {
                signer.sign(createdAt, KIND, tags.toTypedArray(), msg, onReady)
            }
        }
    }
}

/**
 * Returns a list of NIP-10 marked tags that are also ordered at best effort to support the
 * deprecated method of positional tags to maximize backwards compatibility with clients that
 * support replies but have not been updated to understand tag markers.
 *
 * https://github.com/nostr-protocol/nips/blob/master/10.md
 *
 * The tag to the root of the reply chain goes first. The tag to the reply event being responded
 * to goes last. The order for any other tag does not matter, so keep the relative order.
 */
fun List<String>.positionalMarkedTags(
    tagName: String,
    root: String?,
    replyingTo: String?,
    directMentions: Set<HexKey>,
    forkedFrom: String?,
) = sortedWith { o1, o2 ->
    when {
        o1 == o2 -> 0
        o1 == root -> -1 // root goes first
        o2 == root -> 1 // root goes first
        o1 == replyingTo -> 1 // reply event being responded to goes last
        o2 == replyingTo -> -1 // reply event being responded to goes last
        else -> 0 // keep the relative order for any other tag
    }
}.map {
    when (it) {
        root -> arrayOf(tagName, it, "", "root")
        replyingTo -> arrayOf(tagName, it, "", "reply")
        forkedFrom -> arrayOf(tagName, it, "", "fork")
        in directMentions -> arrayOf(tagName, it, "", "mention")
        else -> arrayOf(tagName, it)
    }
}
