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
package com.vitorpamplona.amethyst.model.nip51Lists.bookmarkSets

import androidx.compose.runtime.Stable
import com.vitorpamplona.quartz.nip01Core.signers.NostrSigner
import com.vitorpamplona.quartz.nip51Lists.bookmarkList.tags.AddressBookmark
import com.vitorpamplona.quartz.nip51Lists.bookmarkList.tags.EventBookmark
import com.vitorpamplona.quartz.nip51Lists.bookmarkList.tags.HashtagBookmark
import com.vitorpamplona.quartz.nip51Lists.bookmarkList.tags.UrlBookmark
import com.vitorpamplona.quartz.nip51Lists.bookmarkSet.BookmarkSetEvent
import kotlinx.coroutines.runBlocking

@Stable
data class BookmarkSet(
    val identifier: String,
    val title: String,
    val description: String?,
    val privateBookmarks: Set<Bookmark> = emptySet(),
    val publicBookmarks: Set<Bookmark> = emptySet(),
) {
    companion object {
        fun mapEventToSet(
            event: BookmarkSetEvent,
            signer: NostrSigner,
        ): BookmarkSet {
            val identifierTag = event.dTag()
            val setTitle = event.nameOrTitle() ?: identifierTag
            val setDescription = event.description()
            val publicBookmarks =
                event
                    .publicBookmarks()
                    .map {
                        when (it) {
                            is EventBookmark -> Bookmark.Post(it.toNEvent())
                            is AddressBookmark -> Bookmark.Article(it.address.toValue())
                            is UrlBookmark -> Bookmark.Link(it.url)
                            is HashtagBookmark -> Bookmark.Hashtag(it.hashtag)
                        }
                    }
            val privateBookmarks =
                runBlocking { event.privateBookmarks(signer) }
                    ?.map {
                        when (it) {
                            is EventBookmark -> Bookmark.Post(it.toNEvent())
                            is AddressBookmark -> Bookmark.Article(it.address.toValue())
                            is UrlBookmark -> Bookmark.Link(it.url)
                            is HashtagBookmark -> Bookmark.Hashtag(it.hashtag)
                        }
                    } ?: emptySet()

            return BookmarkSet(
                identifier = identifierTag,
                title = setTitle,
                description = setDescription,
                privateBookmarks = privateBookmarks.toSet(),
                publicBookmarks = publicBookmarks.toSet(),
            )
        }
    }
}
