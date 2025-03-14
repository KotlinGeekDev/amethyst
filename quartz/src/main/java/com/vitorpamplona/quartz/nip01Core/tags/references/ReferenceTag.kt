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
package com.vitorpamplona.quartz.nip01Core.tags.references

import com.vitorpamplona.quartz.nip01Core.core.has
import com.vitorpamplona.quartz.nip96FileStorage.HttpUrlFormatter
import com.vitorpamplona.quartz.utils.ensure

class ReferenceTag {
    companion object {
        const val TAG_NAME = "r"

        @JvmStatic
        fun isTagged(
            tag: Array<String>,
            reference: String,
        ): Boolean = tag.has(1) && tag[0] == TAG_NAME && tag[1] == reference

        @JvmStatic
        fun isIn(
            tag: Array<String>,
            references: Set<String>,
        ): Boolean = tag.has(1) && tag[0] == TAG_NAME && tag[1] in references

        @JvmStatic
        fun hasReference(tag: Array<String>): Boolean {
            ensure(tag.has(1)) { return false }
            ensure(tag[0] == TAG_NAME) { return false }
            return tag[1].isNotEmpty()
        }

        @JvmStatic
        fun parse(tag: Array<String>): String? {
            ensure(tag.has(1)) { return null }
            ensure(tag[0] == TAG_NAME) { return null }
            ensure(tag[1].isNotEmpty()) { return null }
            return tag[1]
        }

        @JvmStatic
        fun assemble(url: String) = arrayOf(TAG_NAME, HttpUrlFormatter.normalize(url))

        @JvmStatic
        fun assemble(urls: List<String>): List<Array<String>> = urls.mapTo(HashSet()) { HttpUrlFormatter.normalize(it) }.map { arrayOf(TAG_NAME, it) }
    }
}
