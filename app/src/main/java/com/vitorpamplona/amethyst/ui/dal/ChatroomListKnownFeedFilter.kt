package com.vitorpamplona.amethyst.ui.dal

import com.vitorpamplona.amethyst.model.Account
import com.vitorpamplona.amethyst.model.ChatroomKey
import com.vitorpamplona.amethyst.model.LocalCache
import com.vitorpamplona.amethyst.model.Note
import com.vitorpamplona.amethyst.service.model.ChannelMessageEvent
import com.vitorpamplona.amethyst.service.model.ChatroomKeyable
import com.vitorpamplona.amethyst.ui.actions.updated
import kotlin.time.ExperimentalTime
import kotlin.time.measureTimedValue

class ChatroomListKnownFeedFilter(val account: Account) : AdditiveFeedFilter<Note>() {

    override fun feedKey(): String {
        return account.userProfile().pubkeyHex
    }

    // returns the last Note of each user.
    override fun feed(): List<Note> {
        val me = account.userProfile()
        val followingKeySet = account.followingKeySet()

        val privateChatrooms = me.privateChatrooms
        val messagingWith = privateChatrooms.keys.filter {
            (
                privateChatrooms[it]?.senderIntersects(followingKeySet) == true ||
                    me.hasSentMessagesTo(it)
                ) && !account.isAllHidden(it.users)
        }

        val privateMessages = messagingWith.mapNotNull { it ->
            privateChatrooms[it]
                ?.roomMessages
                ?.sortedWith(compareBy({ it.createdAt() }, { it.idHex }))
                ?.lastOrNull { it.event != null }
        }

        val publicChannels = account.selectedChatsFollowList().mapNotNull {
            LocalCache.getChannelIfExists(it)
        }.mapNotNull { it ->
            it.notes.values
                .filter { account.isAcceptable(it) && it.event != null }
                .sortedWith(compareBy({ it.createdAt() }, { it.idHex }))
                .lastOrNull()
        }

        return (privateMessages + publicChannels)
            .sortedWith(compareBy({ it.createdAt() }, { it.idHex }))
            .reversed()
    }

    @OptIn(ExperimentalTime::class)
    override fun updateListWith(oldList: List<Note>, newItems: Set<Note>): List<Note> {
        val (feed, elapsed) = measureTimedValue {
            val me = account.userProfile()

            // Gets the latest message by channel from the new items.
            val newRelevantPublicMessages = filterRelevantPublicMessages(newItems, account)

            // Gets the latest message by room from the new items.
            val newRelevantPrivateMessages = filterRelevantPrivateMessages(newItems, account)

            if (newRelevantPrivateMessages.isEmpty() && newRelevantPublicMessages.isEmpty()) {
                return oldList
            }

            var myNewList = oldList

            newRelevantPublicMessages.forEach { newNotePair ->
                oldList.forEach { oldNote ->
                    if (
                        (newNotePair.key == oldNote.channelHex()) && (newNotePair.value.createdAt() ?: 0) > (oldNote.createdAt() ?: 0)
                    ) {
                        myNewList = myNewList.updated(oldNote, newNotePair.value)
                    }
                }
            }

            newRelevantPrivateMessages.forEach { newNotePair ->
                oldList.forEach { oldNote ->
                    val oldRoom = (oldNote.event as? ChatroomKeyable)?.chatroomKey(me.pubkeyHex)

                    if (
                        (newNotePair.key == oldRoom) && (newNotePair.value.createdAt() ?: 0) > (oldNote.createdAt() ?: 0)
                    ) {
                        myNewList = myNewList.updated(oldNote, newNotePair.value)
                    }
                }
            }

            sort(myNewList.toSet()).take(1000)
        }

        // Log.d("Time", "${this.javaClass.simpleName} Modified Additive Feed in $elapsed with ${feed.size} objects")
        return feed
    }

    override fun applyFilter(newItems: Set<Note>): Set<Note> {
        // Gets the latest message by channel from the new items.
        val newRelevantPublicMessages = filterRelevantPublicMessages(newItems, account)

        // Gets the latest message by room from the new items.
        val newRelevantPrivateMessages = filterRelevantPrivateMessages(newItems, account)

        return if (newRelevantPrivateMessages.isEmpty() && newRelevantPublicMessages.isEmpty()) {
            emptySet()
        } else {
            (newRelevantPrivateMessages.values + newRelevantPublicMessages.values).toSet()
        }
    }

    private fun filterRelevantPublicMessages(newItems: Set<Note>, account: Account): MutableMap<String, Note> {
        val followingChannels = account.userProfile().latestContactList?.taggedEvents()?.toSet() ?: emptySet()
        val newRelevantPublicMessages = mutableMapOf<String, Note>()
        newItems.filter { it.event is ChannelMessageEvent }.forEach { newNote ->
            newNote.channelHex()?.let { channelHex ->
                if (channelHex in followingChannels && account.isAcceptable(newNote)) {
                    val lastNote = newRelevantPublicMessages.get(channelHex)
                    if (lastNote != null) {
                        if ((newNote.createdAt() ?: 0) > (lastNote.createdAt() ?: 0)) {
                            newRelevantPublicMessages.put(channelHex, newNote)
                        }
                    } else {
                        newRelevantPublicMessages.put(channelHex, newNote)
                    }
                }
            }
        }
        return newRelevantPublicMessages
    }

    private fun filterRelevantPrivateMessages(newItems: Set<Note>, account: Account): MutableMap<ChatroomKey, Note> {
        val me = account.userProfile()
        val followingKeySet = account.followingKeySet()

        val newRelevantPrivateMessages = mutableMapOf<ChatroomKey, Note>()
        newItems.filter { it.event is ChatroomKeyable }.forEach { newNote ->
            val roomKey = (newNote.event as? ChatroomKeyable)?.chatroomKey(me.pubkeyHex)
            val room = account.userProfile().privateChatrooms[roomKey]

            if (roomKey != null && room != null) {
                if ((newNote.author?.pubkeyHex == me.pubkeyHex || room.senderIntersects(followingKeySet) || me.hasSentMessagesTo(roomKey)) && !account.isAllHidden(roomKey.users)) {
                    val lastNote = newRelevantPrivateMessages.get(roomKey)
                    if (lastNote != null) {
                        if ((newNote.createdAt() ?: 0) > (lastNote.createdAt() ?: 0)) {
                            newRelevantPrivateMessages.put(roomKey, newNote)
                        }
                    } else {
                        newRelevantPrivateMessages.put(roomKey, newNote)
                    }
                }
            }
        }
        return newRelevantPrivateMessages
    }

    override fun sort(collection: Set<Note>): List<Note> {
        return collection
            .sortedWith(compareBy({ it.createdAt() }, { it.idHex }))
            .reversed()
    }
}
