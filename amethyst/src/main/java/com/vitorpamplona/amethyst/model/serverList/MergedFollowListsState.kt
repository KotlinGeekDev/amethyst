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
package com.vitorpamplona.amethyst.model.serverList

import androidx.compose.runtime.Immutable
import com.vitorpamplona.amethyst.model.nip02FollowLists.Kind3FollowListState
import com.vitorpamplona.amethyst.model.nip51Lists.followSets.FollowSetState
import com.vitorpamplona.amethyst.model.nip51Lists.geohashLists.GeohashListState
import com.vitorpamplona.amethyst.model.nip51Lists.hashtagLists.HashtagListState
import com.vitorpamplona.amethyst.model.nip72Communities.CommunityListState
import com.vitorpamplona.quartz.nip72ModCommunities.follow.tags.CommunityTag
import com.vitorpamplona.quartz.nip73ExternalIds.location.GeohashId
import com.vitorpamplona.quartz.nip73ExternalIds.topics.HashtagId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.sample
import kotlinx.coroutines.flow.stateIn

class MergedFollowListsState(
    val kind3List: Kind3FollowListState,
    val followSetList: FollowSetState,
    val hashtagList: HashtagListState,
    val geohashList: GeohashListState,
    val communityList: CommunityListState,
    val scope: CoroutineScope,
) {
    /**
     This contains a big OR of everything the user wants to see in the a single feed.
     */
    @Immutable
    class AllFollows(
        val authors: Set<String> = emptySet(),
        val hashtags: Set<String> = emptySet(),
        val geotags: Set<String> = emptySet(),
        val communities: Set<String> = emptySet(),
    ) {
        val geotagScopes: Set<String> = geotags.mapTo(mutableSetOf<String>()) { GeohashId.toScope(it) }
        val hashtagScopes: Set<String> = hashtags.mapTo(mutableSetOf<String>()) { HashtagId.toScope(it) }
    }

    fun mergeLists(
        kind3: Kind3FollowListState.Kind3Follows,
        followSetProfiles: Set<String>,
        hashtags: Set<String>,
        geohashes: Set<String>,
        community: Set<CommunityTag>,
    ): AllFollows =
        AllFollows(
            authors = kind3.authors + followSetProfiles,
            hashtags = hashtags,
            geotags = geohashes,
            communities = community.mapTo(mutableSetOf()) { it.address.toValue() },
        )

    val flow: StateFlow<AllFollows> =
        combine(
            kind3List.flow,
            followSetList.profilesFlow,
            hashtagList.flow,
            geohashList.flow,
            communityList.flow,
            ::mergeLists,
        ).onStart {
            emit(
                mergeLists(
                    kind3List.flow.value,
                    followSetList.profilesFlow.value,
                    hashtagList.flow.value,
                    geohashList.flow.value,
                    communityList.flow.value,
                ),
            )
        }.sample(200)
            .flowOn(Dispatchers.IO)
            .stateIn(
                scope,
                SharingStarted.Eagerly,
                mergeLists(
                    kind3List.flow.value,
                    followSetList.profilesFlow.value,
                    hashtagList.flow.value,
                    geohashList.flow.value,
                    communityList.flow.value,
                ),
            )
}
