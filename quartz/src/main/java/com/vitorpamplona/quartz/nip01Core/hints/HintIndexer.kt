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
package com.vitorpamplona.quartz.nip01Core.hints

import com.vitorpamplona.quartz.nip01Core.core.HexKey
import com.vitorpamplona.quartz.nip01Core.core.hexToByteArray
import com.vitorpamplona.quartz.nip01Core.hints.bloom.BloomFilterMurMur3

/**
 * Instead of having one bloom filter per relay per type, which could create
 * many large bloom filters for collections of very few items, this class uses
 * only one mega bloom filter per type and uses the hashcode of the relay uri
 * as seed differentiator in the hash function.
 */
class HintIndexer {
    private val eventHints = BloomFilterMurMur3(10_000_000, 5)
    private val addressHints = BloomFilterMurMur3(2_000_000, 5)
    private val pubKeyHints = BloomFilterMurMur3(10_000_000, 5)
    private val relayDB = mutableSetOf<String>()

    private fun add(
        id: ByteArray,
        relay: String,
        bloom: BloomFilterMurMur3,
    ) {
        relayDB.add(relay)
        bloom.add(id, relay.hashCode())
    }

    private fun get(
        id: ByteArray,
        bloom: BloomFilterMurMur3,
    ) = relayDB.filter { bloom.mightContain(id, it.hashCode()) }

    // --------------------
    // Event Host hints
    // --------------------
    fun addEvent(
        eventId: ByteArray,
        relay: String,
    ) = add(eventId, relay, eventHints)

    fun addEvent(
        eventId: HexKey,
        relay: String,
    ) = addEvent(eventId.hexToByteArray(), relay)

    fun getEvent(eventId: ByteArray) = get(eventId, eventHints)

    fun getEvent(eventId: HexKey) = getEvent(eventId.hexToByteArray())

    // --------------------
    // PubKeys Outbox hints
    // --------------------
    fun addAddress(
        addressId: ByteArray,
        relay: String,
    ) = add(addressId, relay, addressHints)

    fun addAddress(
        addressId: String,
        relay: String,
    ) = addAddress(addressId.toByteArray(), relay)

    fun getAddress(addressId: ByteArray) = get(addressId, addressHints)

    fun getAddress(addressId: String) = getAddress(addressId.toByteArray())

    // --------------------
    // PubKeys Outbox hints
    // --------------------
    fun addKey(
        key: ByteArray,
        relay: String,
    ) = add(key, relay, pubKeyHints)

    fun addKey(
        key: HexKey,
        relay: String,
    ) = addKey(key.hexToByteArray(), relay)

    fun getKey(key: ByteArray) = get(key, pubKeyHints)

    fun getKey(key: HexKey) = getKey(key.hexToByteArray())
}
