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
package com.vitorpamplona.amethyst.ui.screen.loggedIn.discover.nip72Communities.subassemblies

import com.vitorpamplona.amethyst.model.topNavFeeds.global.GlobalTopNavPerRelayFilterSet
import com.vitorpamplona.amethyst.service.relays.SincePerRelayMap
import com.vitorpamplona.quartz.nip01Core.relay.client.pool.RelayBasedFilter
import com.vitorpamplona.quartz.nip01Core.relay.filters.Filter
import com.vitorpamplona.quartz.nip72ModCommunities.approval.CommunityPostApprovalEvent
import com.vitorpamplona.quartz.nip72ModCommunities.definition.CommunityDefinitionEvent
import com.vitorpamplona.quartz.utils.TimeUtils

fun filterCommunitiesGlobal(
    relays: GlobalTopNavPerRelayFilterSet,
    since: SincePerRelayMap?,
    defaultSince: Long? = null,
): List<RelayBasedFilter> {
    if (relays.set.isEmpty()) return emptyList()

    return relays.set.flatMap {
        val since = since?.get(it.key)?.time ?: defaultSince
        listOf(
            RelayBasedFilter(
                relay = it.key,
                filter =
                    Filter(
                        kinds = listOf(CommunityDefinitionEvent.KIND),
                        limit = 500,
                        since = since,
                    ),
            ),
            RelayBasedFilter(
                relay = it.key,
                filter =
                    Filter(
                        kinds = CommunityPostApprovalEvent.KIND_LIST,
                        limit = 100,
                        since = since ?: TimeUtils.oneMonthAgo(),
                    ),
            ),
        )
    }
}
