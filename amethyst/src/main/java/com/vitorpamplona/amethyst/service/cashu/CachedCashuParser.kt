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
package com.vitorpamplona.amethyst.service.cashu

import android.util.LruCache
import com.vitorpamplona.amethyst.ui.components.GenericLoadable
import kotlinx.collections.immutable.ImmutableList

object CachedCashuParser {
    val cashuCache = LruCache<String, GenericLoadable<ImmutableList<CashuToken>>>(20)

    fun cached(token: String): GenericLoadable<ImmutableList<CashuToken>> = cashuCache[token] ?: GenericLoadable.Loading()

    fun parse(token: String): GenericLoadable<ImmutableList<CashuToken>> {
        if (cashuCache[token] !is GenericLoadable.Loaded) {
            val newCachuData = CashuParser().parse(token)
            cashuCache.put(token, newCachuData)
        }

        return cashuCache[token]
    }
}
