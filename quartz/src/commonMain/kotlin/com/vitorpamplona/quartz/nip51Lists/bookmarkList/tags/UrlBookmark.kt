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
package com.vitorpamplona.quartz.nip51Lists.bookmarkList.tags

import com.vitorpamplona.quartz.nip01Core.core.Tag
import com.vitorpamplona.quartz.nip01Core.tags.references.ReferenceTag
import com.vitorpamplona.quartz.utils.bytesUsedInMemory
import com.vitorpamplona.quartz.utils.pointerSizeInBytes

class UrlBookmark(
    val url: String,
) : BookmarkIdTag {
    fun countMemory(): Int = pointerSizeInBytes + url.bytesUsedInMemory()

    override fun toTagArray(): Tag = ReferenceTag.assemble(url)

    override fun toTagIdOnly(): Tag = ReferenceTag.assemble(url)

    companion object {
        fun isTagged(
            tag: Array<String>,
            url: String,
        ) = ReferenceTag.isTagged(tag, url)

        fun isIn(
            tag: Array<String>,
            urls: Set<String>,
        ) = ReferenceTag.isIn(tag, urls)

        fun parse(tag: Array<String>): UrlBookmark? = ReferenceTag.parse(tag)?.let { UrlBookmark(it) }

        fun assemble(url: String) = ReferenceTag.assemble(url)

        fun assemble(listOfUrls: List<String>) = ReferenceTag.assemble(listOfUrls)
    }
}
