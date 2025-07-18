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
package com.vitorpamplona.amethyst.model.topNavFeeds.noteBased

import com.vitorpamplona.amethyst.model.NoteState
import com.vitorpamplona.amethyst.model.topNavFeeds.FeedDecryptionCaches
import com.vitorpamplona.amethyst.model.topNavFeeds.IFeedFlowsType
import com.vitorpamplona.amethyst.model.topNavFeeds.IFeedTopNavFilter
import com.vitorpamplona.amethyst.model.topNavFeeds.aroundMe.LocationTopNavFilter
import com.vitorpamplona.amethyst.model.topNavFeeds.hashtag.HashtagTopNavFilter
import com.vitorpamplona.amethyst.model.topNavFeeds.noteBased.allcommunities.AllCommunitiesTopNavFilter
import com.vitorpamplona.amethyst.model.topNavFeeds.noteBased.author.AuthorsByOutboxTopNavFilter
import com.vitorpamplona.amethyst.model.topNavFeeds.noteBased.community.SingleCommunityTopNavFilter
import com.vitorpamplona.amethyst.model.topNavFeeds.noteBased.muted.MutedAuthorsByOutboxTopNavFilter
import com.vitorpamplona.quartz.nip01Core.core.Event
import com.vitorpamplona.quartz.nip01Core.relay.normalizer.NormalizedRelayUrl
import com.vitorpamplona.quartz.nip01Core.signers.NostrSigner
import com.vitorpamplona.quartz.nip51Lists.followList.FollowListEvent
import com.vitorpamplona.quartz.nip51Lists.geohashList.GeohashListEvent
import com.vitorpamplona.quartz.nip51Lists.hashtagList.HashtagListEvent
import com.vitorpamplona.quartz.nip51Lists.muteList.MuteListEvent
import com.vitorpamplona.quartz.nip51Lists.peopleList.PeopleListEvent
import com.vitorpamplona.quartz.nip72ModCommunities.definition.CommunityDefinitionEvent
import com.vitorpamplona.quartz.nip72ModCommunities.follow.CommunityListEvent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.transformLatest

class NoteFeedFlow(
    val metadataFlow: StateFlow<NoteState?>,
    val signer: NostrSigner,
    val allFollowRelays: StateFlow<Set<NormalizedRelayUrl>>,
    val blockedRelays: StateFlow<Set<NormalizedRelayUrl>>,
    val caches: FeedDecryptionCaches,
) : IFeedFlowsType {
    fun process(noteEvent: Event): IFeedTopNavFilter =
        when (noteEvent) {
            is PeopleListEvent -> {
                if (noteEvent.dTag() == PeopleListEvent.Companion.BLOCK_LIST_D_TAG) {
                    MutedAuthorsByOutboxTopNavFilter(caches.peopleListCache.cachedUserIdSet(noteEvent), blockedRelays)
                } else {
                    AuthorsByOutboxTopNavFilter(caches.peopleListCache.cachedUserIdSet(noteEvent), blockedRelays)
                }
            }
            is MuteListEvent -> {
                MutedAuthorsByOutboxTopNavFilter(caches.muteListCache.cachedUserIdSet(noteEvent), blockedRelays)
            }
            is FollowListEvent -> {
                AuthorsByOutboxTopNavFilter(noteEvent.followIdSet(), blockedRelays)
            }
            is CommunityListEvent -> {
                AllCommunitiesTopNavFilter(caches.communityListCache.cachedCommunityIdSet(noteEvent), blockedRelays)
            }
            is HashtagListEvent -> {
                HashtagTopNavFilter(caches.hashtagCache.cachedHashtags(noteEvent), allFollowRelays)
            }
            is GeohashListEvent -> {
                LocationTopNavFilter(caches.geohashCache.cachedGeohashes(noteEvent), allFollowRelays)
            }
            is CommunityDefinitionEvent -> {
                SingleCommunityTopNavFilter(
                    community = noteEvent.addressTag(),
                    authors = noteEvent.moderatorKeys().toSet().ifEmpty { null },
                    relays = noteEvent.relayUrls().toSet(),
                    blockedRelays = blockedRelays,
                )
            }
            else -> AuthorsByOutboxTopNavFilter(emptySet(), blockedRelays)
        }

    suspend fun FlowCollector<IFeedTopNavFilter>.process(noteEvent: Event) {
        when (noteEvent) {
            is PeopleListEvent -> {
                if (noteEvent.dTag() == PeopleListEvent.Companion.BLOCK_LIST_D_TAG) {
                    emit(MutedAuthorsByOutboxTopNavFilter(caches.peopleListCache.userIdSet(noteEvent), blockedRelays))
                } else {
                    emit(AuthorsByOutboxTopNavFilter(caches.peopleListCache.userIdSet(noteEvent), blockedRelays))
                }
            }
            is MuteListEvent -> {
                emit(MutedAuthorsByOutboxTopNavFilter(caches.muteListCache.mutedUserIdSet(noteEvent), blockedRelays))
            }
            is FollowListEvent -> {
                emit(AuthorsByOutboxTopNavFilter(noteEvent.followIdSet(), blockedRelays))
            }
            is CommunityListEvent -> {
                emit(AllCommunitiesTopNavFilter(caches.communityListCache.communityIdSet(noteEvent), blockedRelays))
            }
            is HashtagListEvent -> {
                emit(HashtagTopNavFilter(caches.hashtagCache.hashtags(noteEvent), allFollowRelays))
            }
            is GeohashListEvent -> {
                emit(LocationTopNavFilter(caches.geohashCache.geohashes(noteEvent), allFollowRelays))
            }
            is CommunityDefinitionEvent -> {
                emit(
                    SingleCommunityTopNavFilter(
                        community = noteEvent.addressTag(),
                        authors = noteEvent.moderatorKeys().toSet().ifEmpty { null },
                        relays = noteEvent.relayUrls().toSet(),
                        blockedRelays = blockedRelays,
                    ),
                )
            }
            else ->
                emit(
                    AuthorsByOutboxTopNavFilter(emptySet(), blockedRelays),
                )
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun flow() =
        metadataFlow.transformLatest { noteState ->
            val noteEvent = noteState?.note?.event
            if (noteEvent == null) {
                AuthorsByOutboxTopNavFilter(emptySet(), blockedRelays)
            } else {
                process(noteEvent)
            }
        }

    override fun startValue(): IFeedTopNavFilter {
        val noteEvent = metadataFlow.value?.note?.event
        if (noteEvent == null) {
            return AuthorsByOutboxTopNavFilter(emptySet(), blockedRelays)
        } else {
            return process(noteEvent)
        }
    }

    override suspend fun startValue(collector: FlowCollector<IFeedTopNavFilter>) {
        val noteEvent = metadataFlow.value?.note?.event
        if (noteEvent == null) {
            collector.emit(AuthorsByOutboxTopNavFilter(emptySet(), blockedRelays))
        } else {
            collector.process(noteEvent)
        }
    }
}
