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
package com.vitorpamplona.amethyst.model.nip28PublicChats

import com.vitorpamplona.quartz.nip01Core.signers.NostrSigner
import com.vitorpamplona.quartz.nip28PublicChat.list.ChannelListEvent
import com.vitorpamplona.quartz.nip28PublicChat.list.channelSet
import com.vitorpamplona.quartz.nip28PublicChat.list.channels
import com.vitorpamplona.quartz.nip51Lists.PrivateTagArrayEventCache

class PublicChatListDecryptionCache(
    val signer: NostrSigner,
) {
    val cachedPrivateLists = PrivateTagArrayEventCache<ChannelListEvent>(signer)

    fun cachedChannelSet(event: ChannelListEvent) = cachedPrivateLists.mergeTagListPrecached(event).channelSet()

    fun cachedChannels(event: ChannelListEvent) = cachedPrivateLists.mergeTagListPrecached(event).channels()

    suspend fun channelSet(event: ChannelListEvent) = cachedPrivateLists.mergeTagList(event).channelSet()

    suspend fun channels(event: ChannelListEvent) = cachedPrivateLists.mergeTagList(event).channels()
}
