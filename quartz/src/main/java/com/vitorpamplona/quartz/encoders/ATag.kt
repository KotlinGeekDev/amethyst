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
package com.vitorpamplona.quartz.encoders

import android.util.Log
import androidx.compose.runtime.Immutable
import com.vitorpamplona.quartz.utils.bytesUsedInMemory
import com.vitorpamplona.quartz.utils.pointerSizeInBytes
import com.vitorpamplona.quartz.utils.removeTrailingNullsAndEmptyOthers

@Immutable
data class ATag(
    val kind: Int,
    val pubKeyHex: String,
    val dTag: String,
) {
    var relay: String? = null

    constructor(
        kind: Int,
        pubKeyHex: String,
        dTag: String,
        relayHint: String?,
    ) : this(kind, pubKeyHex, dTag) {
        this.relay = relayHint
    }

    fun countMemory(): Long =
        5 * pointerSizeInBytes + // 7 fields, 4 bytes each reference (32bit)
            8L + // kind
            pubKeyHex.bytesUsedInMemory() +
            dTag.bytesUsedInMemory() +
            (relay?.bytesUsedInMemory() ?: 0)

    fun toTag() = assembleATag(kind, pubKeyHex, dTag)

    fun toATagArray() = removeTrailingNullsAndEmptyOthers("a", toTag(), relay)

    fun toQTagArray() = removeTrailingNullsAndEmptyOthers("q", toTag(), relay)

    fun toNAddr(overrideRelay: String? = relay): String =
        TlvBuilder()
            .apply {
                addString(Nip19Bech32.TlvTypes.SPECIAL, dTag)
                addStringIfNotNull(Nip19Bech32.TlvTypes.RELAY, overrideRelay ?: relay)
                addHex(Nip19Bech32.TlvTypes.AUTHOR, pubKeyHex)
                addInt(Nip19Bech32.TlvTypes.KIND, kind)
            }.build()
            .toNAddress()

    companion object {
        fun assembleATag(
            kind: Int,
            pubKeyHex: String,
            dTag: String,
        ) = "$kind:$pubKeyHex:$dTag"

        fun isATag(key: String): Boolean = key.startsWith("naddr1") || key.contains(":")

        fun parse(
            address: String,
            relay: String?,
        ): ATag? =
            if (address.startsWith("naddr") || address.startsWith("nostr:naddr")) {
                parseNAddr(address)
            } else {
                parseAtag(address, relay)
            }

        fun parseAtag(
            atag: String,
            relay: String?,
        ): ATag? =
            try {
                val parts = atag.split(":", limit = 3)
                Hex.decode(parts[1])
                ATag(parts[0].toInt(), parts[1], parts[2], relay)
            } catch (t: Throwable) {
                Log.w("ATag", "Error parsing A Tag: $atag: ${t.message}")
                null
            }

        fun parseAtagUnckecked(atag: String): ATag? =
            try {
                val parts = atag.split(":")
                ATag(parts[0].toInt(), parts[1], parts[2], null)
            } catch (t: Throwable) {
                null
            }

        fun parseNAddr(naddr: String): ATag? {
            try {
                val key = naddr.removePrefix("nostr:")

                if (key.startsWith("naddr")) {
                    val tlv = Tlv.parse(key.bechToBytes())

                    val d = tlv.firstAsString(Nip19Bech32.TlvTypes.SPECIAL) ?: ""
                    val relay = tlv.firstAsString(Nip19Bech32.TlvTypes.RELAY)
                    val author = tlv.firstAsHex(Nip19Bech32.TlvTypes.AUTHOR)
                    val kind = tlv.firstAsInt(Nip19Bech32.TlvTypes.KIND)

                    if (kind != null && author != null) {
                        return ATag(kind, author, d, relay)
                    }
                }
            } catch (e: Throwable) {
                Log.w("ATag", "Issue trying to Decode NIP19 $this: ${e.message}")
                // e.printStackTrace()
            }

            return null
        }
    }
}
