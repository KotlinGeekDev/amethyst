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
package com.vitorpamplona.amethyst.model.topNavFeeds.allFollows

import androidx.compose.runtime.Immutable
import com.vitorpamplona.amethyst.model.topNavFeeds.IFeedTopNavPerRelayFilter
import com.vitorpamplona.quartz.nip73ExternalIds.location.GeohashId
import com.vitorpamplona.quartz.nip73ExternalIds.topics.HashtagId

/**
 * This is a big OR filter.
 */
@Immutable
class AllFollowsByOutboxTopNavPerRelayFilter(
    val authors: Set<String>? = null,
    val hashtags: Set<String>? = null,
    val geotags: Set<String>? = null,
    val communities: Set<String>? = null,
) : IFeedTopNavPerRelayFilter {
    val geotagScopes: Set<String>? = geotags?.mapTo(mutableSetOf<String>()) { GeohashId.toScope(it) }
    val hashtagScopes: Set<String>? = hashtags?.mapTo(mutableSetOf<String>()) { HashtagId.toScope(it) }
}
