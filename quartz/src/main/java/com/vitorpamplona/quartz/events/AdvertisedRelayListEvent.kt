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
package com.vitorpamplona.quartz.events

import androidx.compose.runtime.Immutable
import com.vitorpamplona.quartz.encoders.ATag
import com.vitorpamplona.quartz.encoders.HexKey
import com.vitorpamplona.quartz.signers.NostrSigner
import com.vitorpamplona.quartz.signers.NostrSignerSync
import com.vitorpamplona.quartz.utils.TimeUtils

@Immutable
class AdvertisedRelayListEvent(
    id: HexKey,
    pubKey: HexKey,
    createdAt: Long,
    tags: Array<Array<String>>,
    content: String,
    sig: HexKey,
) : BaseAddressableEvent(id, pubKey, createdAt, KIND, tags, content, sig) {
    override fun dTag() = FIXED_D_TAG

    fun relays(): List<AdvertisedRelayInfo> =
        tags.mapNotNull {
            if (it.size > 1 && it[0] == "r") {
                val type =
                    when (it.getOrNull(2)) {
                        "read" -> AdvertisedRelayType.READ
                        "write" -> AdvertisedRelayType.WRITE
                        else -> AdvertisedRelayType.BOTH
                    }

                AdvertisedRelayInfo(it[1], type)
            } else {
                null
            }
        }

    fun readRelays(): List<String>? =
        tags
            .mapNotNull {
                if (it.size > 1 && it[0] == "r") {
                    when (it.getOrNull(2)) {
                        "read" -> it[1]
                        "write" -> null
                        else -> it[1]
                    }
                } else {
                    null
                }
            }.ifEmpty { null }

    fun writeRelays(): List<String> =
        tags.mapNotNull {
            if (it.size > 1 && it[0] == "r") {
                when (it.getOrNull(2)) {
                    "read" -> null
                    "write" -> it[1]
                    else -> it[1]
                }
            } else {
                null
            }
        }

    companion object {
        const val KIND = 10002
        const val FIXED_D_TAG = ""
        const val ALT = "Relay list to discover the user's content"

        fun createAddressATag(pubKey: HexKey): ATag = ATag(KIND, pubKey, FIXED_D_TAG, null)

        fun createAddressTag(pubKey: HexKey): String = ATag.assembleATag(KIND, pubKey, FIXED_D_TAG)

        fun updateRelayList(
            earlierVersion: AdvertisedRelayListEvent,
            relays: List<AdvertisedRelayInfo>,
            signer: NostrSigner,
            createdAt: Long = TimeUtils.now(),
            onReady: (AdvertisedRelayListEvent) -> Unit,
        ) {
            val tags =
                earlierVersion.tags
                    .filter { it[0] != "r" }
                    .plus(
                        relays.map(::createRelayTag),
                    ).toTypedArray()

            signer.sign(createdAt, KIND, tags, earlierVersion.content, onReady)
        }

        fun createFromScratch(
            relays: List<AdvertisedRelayInfo>,
            signer: NostrSigner,
            createdAt: Long = TimeUtils.now(),
            onReady: (AdvertisedRelayListEvent) -> Unit,
        ) {
            create(relays, signer, createdAt, onReady)
        }

        fun createRelayTag(relay: AdvertisedRelayInfo): Array<String> =
            if (relay.type == AdvertisedRelayType.BOTH) {
                arrayOf("r", relay.relayUrl)
            } else {
                arrayOf("r", relay.relayUrl, relay.type.code)
            }

        fun createTagArray(relays: List<AdvertisedRelayInfo>): Array<Array<String>> =
            relays
                .map(::createRelayTag)
                .plusElement(arrayOf("alt", ALT))
                .toTypedArray()

        fun create(
            list: List<AdvertisedRelayInfo>,
            signer: NostrSigner,
            createdAt: Long = TimeUtils.now(),
            onReady: (AdvertisedRelayListEvent) -> Unit,
        ) {
            val tags = createTagArray(list)
            val msg = ""

            signer.sign(createdAt, KIND, tags, msg, onReady)
        }

        fun create(
            list: List<AdvertisedRelayInfo>,
            signer: NostrSignerSync,
            createdAt: Long = TimeUtils.now(),
        ): AdvertisedRelayListEvent? {
            val tags = createTagArray(list)
            val msg = ""

            return signer.sign(createdAt, KIND, tags, msg)
        }
    }

    @Immutable data class AdvertisedRelayInfo(
        val relayUrl: String,
        val type: AdvertisedRelayType,
    )

    @Immutable
    enum class AdvertisedRelayType(
        val code: String,
    ) {
        BOTH(""),
        READ("read"),
        WRITE("write"),
    }
}
