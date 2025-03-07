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

import com.vitorpamplona.quartz.nip01Core.core.Tag
import com.vitorpamplona.quartz.nip01Core.core.match
import com.vitorpamplona.quartz.nip01Core.core.valueIfMatches
import com.vitorpamplona.quartz.nip73ExternalIds.ExternalId

class RootKindTag {
    companion object {
        const val TAG_NAME = "K"
        const val TAG_SIZE = 2

        @JvmStatic
        fun match(tag: Tag) = tag.match(TAG_NAME, TAG_SIZE)

        @JvmStatic
        fun parse(tag: Tag) = tag.valueIfMatches(TAG_NAME, TAG_SIZE)

        @JvmStatic
        fun assemble(kind: String): Tag = arrayOf(TAG_NAME, kind)

        @JvmStatic
        fun assemble(kind: Int): Tag = assemble(kind.toString())

        @JvmStatic
        fun assemble(id: ExternalId) = assemble(id.toKind())

        @JvmStatic
        fun assemble(kinds: List<String>): List<Tag> = kinds.map { assemble(it) }

        @JvmStatic
        fun assemble(kinds: Set<String>): List<Tag> = kinds.map { assemble(it) }
    }
}
