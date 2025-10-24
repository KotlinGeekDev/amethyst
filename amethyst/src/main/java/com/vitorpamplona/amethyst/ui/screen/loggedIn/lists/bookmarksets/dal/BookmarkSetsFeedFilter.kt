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
package com.vitorpamplona.amethyst.ui.screen.loggedIn.lists.bookmarksets.dal

import android.util.Log
import com.vitorpamplona.amethyst.model.nip51Lists.bookmarkSets.BookmarkSet
import com.vitorpamplona.amethyst.model.nip51Lists.bookmarkSets.BookmarkSetState
import com.vitorpamplona.amethyst.ui.dal.FeedFilter
import kotlinx.coroutines.runBlocking

class BookmarkSetsFeedFilter(
    val bookmarkSetState: BookmarkSetState,
) : FeedFilter<BookmarkSet>() {
    override fun feedKey(): String = bookmarkSetState.user.pubkeyHex + "-bookmarksets"

    override fun feed(): List<BookmarkSet> =
        runBlocking(bookmarkSetState.scope.coroutineContext) {
            try {
                val fetchedBookmarkSets = bookmarkSetState.getBookmarkSetNotes()
                val bookmarkSets = fetchedBookmarkSets.map { bookmarkSetState.mapNoteToBookmarkSet(it) }
                println("Updated bookmark set size for feed filter: ${bookmarkSets.size}")
                bookmarkSets
            } catch (e: Exception) {
                // if (e is CancellationException) throw e
                Log.e(
                    this@BookmarkSetsFeedFilter.javaClass.simpleName,
                    "Failed to load follow lists: ${e.message}",
                )
                throw e
            }
        }
}
