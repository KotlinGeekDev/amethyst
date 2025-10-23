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

import com.vitorpamplona.amethyst.model.serverList.MergedFollowListsState
import com.vitorpamplona.amethyst.model.topNavFeeds.IFeedFlowsType
import com.vitorpamplona.amethyst.model.topNavFeeds.IFeedTopNavFilter
import com.vitorpamplona.quartz.nip01Core.relay.normalizer.NormalizedRelayUrl
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine

class AllFollowsFeedFlow(
    val allFollows: StateFlow<MergedFollowListsState.AllFollows?>,
    val followsRelays: StateFlow<Set<NormalizedRelayUrl>>,
    val blockedRelays: StateFlow<Set<NormalizedRelayUrl>>,
    val proxyRelays: StateFlow<Set<NormalizedRelayUrl>>,
) : IFeedFlowsType {
    fun convert(
        allFollows: MergedFollowListsState.AllFollows?,
        proxyRelays: Set<NormalizedRelayUrl>,
    ): IFeedTopNavFilter =
        if (allFollows != null) {
            if (proxyRelays.isEmpty()) {
                AllFollowsByOutboxTopNavFilter(
                    authors = allFollows.authors,
                    hashtags = allFollows.hashtags,
                    geotags = allFollows.geotags,
                    communities = allFollows.communities,
                    defaultRelays = followsRelays,
                    blockedRelays = blockedRelays,
                )
            } else {
                AllFollowsByProxyTopNavFilter(
                    authors = allFollows.authors,
                    hashtags = allFollows.hashtags,
                    geotags = allFollows.geotags,
                    communities = allFollows.communities,
                    proxyRelays = proxyRelays,
                )
            }
        } else {
            AllFollowsByOutboxTopNavFilter(
                authors = emptySet(),
                defaultRelays = followsRelays,
                blockedRelays = blockedRelays,
            )
        }

    override fun flow() = combine(allFollows, proxyRelays, ::convert)

    override fun startValue(): IFeedTopNavFilter = convert(allFollows.value, proxyRelays.value)

    override suspend fun startValue(collector: FlowCollector<IFeedTopNavFilter>) {
        collector.emit(startValue())
    }
}
