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
package com.vitorpamplona.amethyst.service.relays

import androidx.collection.LruCache
import com.vitorpamplona.amethyst.model.User
import com.vitorpamplona.ammolite.relays.filters.EOSETime

class EOSERelayList(
    var relayList: Map<String, EOSETime> = emptyMap(),
) {
    fun addOrUpdate(
        relayUrl: String,
        time: Long,
    ) {
        val eose = relayList[relayUrl]
        if (eose == null) {
            relayList = relayList + Pair(relayUrl, EOSETime(time))
        } else {
            eose.time = time
        }
    }
}

class EOSEFollowList(
    var followList: Map<String, EOSERelayList> = emptyMap(),
) {
    fun addOrUpdate(
        listCode: String,
        relayUrl: String,
        time: Long,
    ) {
        val relayList = followList[listCode]
        if (relayList == null) {
            val newList = EOSERelayList()
            newList.addOrUpdate(relayUrl, time)
            followList = followList + mapOf(listCode to newList)
        } else {
            relayList.addOrUpdate(relayUrl, time)
        }
    }
}

class EOSEAccount(
    var users: LruCache<User, EOSEFollowList> = LruCache<User, EOSEFollowList>(20),
) {
    fun addOrUpdate(
        user: User,
        listCode: String,
        relayUrl: String,
        time: Long,
    ) {
        val followList = users[user]
        if (followList == null) {
            val newList = EOSEFollowList()
            users.put(user, newList)
            newList.addOrUpdate(listCode, relayUrl, time)
        } else {
            followList.addOrUpdate(listCode, relayUrl, time)
        }
    }

    fun removeDataFor(user: User) {
        users.remove(user)
    }
}
