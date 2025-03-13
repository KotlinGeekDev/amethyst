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
package com.vitorpamplona.quartz.nip01Core.tags.kinds

import com.vitorpamplona.quartz.nip01Core.core.has
import com.vitorpamplona.quartz.utils.ensure

class KindTag {
    companion object {
        const val TAG_NAME = "k"

        fun match(tag: Array<String>) = tag.has(1) && tag[0] == TAG_NAME

        fun isTagged(
            tag: Array<String>,
            kind: String,
        ) = tag.has(1) && tag[0] == TAG_NAME && tag[1] == kind

        fun isIn(
            tag: Array<String>,
            kinds: Set<String>,
        ) = tag.has(1) && tag[0] == TAG_NAME && tag[1] in kinds

        @JvmStatic
        fun parse(tag: Array<String>): Int? {
            ensure(tag.has(1)) { return null }
            ensure(tag[0] == TAG_NAME) { return null }
            ensure(tag[1].isNotEmpty()) { return null }
            return tag[1].toInt()
        }

        fun assemble(kind: Int) = arrayOf(TAG_NAME, kind.toString())

        fun assemble(kinds: List<Int>): List<Array<String>> = kinds.map { assemble(it) }

        fun assemble(kinds: Set<Int>): List<Array<String>> = kinds.map { assemble(it) }
    }
}
