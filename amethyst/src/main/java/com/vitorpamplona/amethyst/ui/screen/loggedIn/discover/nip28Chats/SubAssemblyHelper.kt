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
package com.vitorpamplona.amethyst.ui.screen.loggedIn.discover.nip28Chats

import com.vitorpamplona.amethyst.model.topNavFeeds.IFeedTopNavPerRelayFilterSet
import com.vitorpamplona.amethyst.model.topNavFeeds.allFollows.AllFollowsByOutboxTopNavPerRelayFilterSet
import com.vitorpamplona.amethyst.model.topNavFeeds.aroundMe.LocationTopNavPerRelayFilterSet
import com.vitorpamplona.amethyst.model.topNavFeeds.global.GlobalTopNavPerRelayFilterSet
import com.vitorpamplona.amethyst.model.topNavFeeds.hashtag.HashtagTopNavPerRelayFilterSet
import com.vitorpamplona.amethyst.model.topNavFeeds.noteBased.allcommunities.AllCommunitiesTopNavPerRelayFilterSet
import com.vitorpamplona.amethyst.model.topNavFeeds.noteBased.author.AuthorsByOutboxTopNavPerRelayFilterSet
import com.vitorpamplona.amethyst.model.topNavFeeds.noteBased.community.SingleCommunityTopNavPerRelayFilterSet
import com.vitorpamplona.amethyst.model.topNavFeeds.noteBased.muted.MutedAuthorsByOutboxTopNavPerRelayFilterSet
import com.vitorpamplona.amethyst.service.relays.SincePerRelayMap
import com.vitorpamplona.amethyst.ui.screen.loggedIn.discover.nip28Chats.subassemblies.filterPublicChatsByAllCommunities
import com.vitorpamplona.amethyst.ui.screen.loggedIn.discover.nip28Chats.subassemblies.filterPublicChatsByAuthors
import com.vitorpamplona.amethyst.ui.screen.loggedIn.discover.nip28Chats.subassemblies.filterPublicChatsByCommunity
import com.vitorpamplona.amethyst.ui.screen.loggedIn.discover.nip28Chats.subassemblies.filterPublicChatsByFollows
import com.vitorpamplona.amethyst.ui.screen.loggedIn.discover.nip28Chats.subassemblies.filterPublicChatsByGeohash
import com.vitorpamplona.amethyst.ui.screen.loggedIn.discover.nip28Chats.subassemblies.filterPublicChatsByHashtag
import com.vitorpamplona.amethyst.ui.screen.loggedIn.discover.nip28Chats.subassemblies.filterPublicChatsGlobal
import com.vitorpamplona.quartz.nip01Core.relay.client.pool.RelayBasedFilter

fun makePublicChatsFilter(
    feedSettings: IFeedTopNavPerRelayFilterSet,
    since: SincePerRelayMap?,
): List<RelayBasedFilter> =
    when (feedSettings) {
        is AllCommunitiesTopNavPerRelayFilterSet -> filterPublicChatsByAllCommunities(feedSettings, since)
        is AllFollowsByOutboxTopNavPerRelayFilterSet -> filterPublicChatsByFollows(feedSettings, since)
        is AuthorsByOutboxTopNavPerRelayFilterSet -> filterPublicChatsByAuthors(feedSettings, since)
        is GlobalTopNavPerRelayFilterSet -> filterPublicChatsGlobal(feedSettings, since)
        is HashtagTopNavPerRelayFilterSet -> filterPublicChatsByHashtag(feedSettings, since)
        is LocationTopNavPerRelayFilterSet -> filterPublicChatsByGeohash(feedSettings, since)
        is MutedAuthorsByOutboxTopNavPerRelayFilterSet -> filterPublicChatsByAuthors(feedSettings, since)
        is SingleCommunityTopNavPerRelayFilterSet -> filterPublicChatsByCommunity(feedSettings, since)
        else -> emptyList()
    }
