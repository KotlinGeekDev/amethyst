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
package com.vitorpamplona.quartz.nip51Lists.encryption

import android.util.Log
import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.module.kotlin.readValue
import com.vitorpamplona.quartz.nip01Core.core.TagArray
import com.vitorpamplona.quartz.nip01Core.jackson.JsonMapper
import com.vitorpamplona.quartz.nip01Core.signers.NostrSigner
import com.vitorpamplona.quartz.nip01Core.signers.NostrSignerSync

class PrivateTagsInContent {
    companion object {
        fun decode(content: String) = JsonMapper.mapper.readValue<Array<Array<String>>>(content)

        fun encode(privateTags: Array<Array<String>>): String = JsonMapper.mapper.writeValueAsString(privateTags)

        suspend fun decrypt(
            content: String,
            signer: NostrSigner,
        ): TagArray {
            if (content.isBlank()) return emptyArray()
            val json = signer.decrypt(content, signer.pubKey)
            return try {
                decode(json)
            } catch (e: JsonParseException) {
                Log.w("DraftEvent", "Unable to parse inner event of a draft: $json")
                throw e
            }
        }

        suspend fun encryptNip44(
            privateTags: Array<Array<String>>? = null,
            signer: NostrSigner,
        ): String =
            signer.nip44Encrypt(
                if (privateTags.isNullOrEmpty()) "" else encode(privateTags),
                signer.pubKey,
            )

        suspend fun decrypt(
            content: String,
            signer: NostrSignerSync,
        ): TagArray {
            val json = signer.decrypt(content, signer.pubKey)
            return decode(json)
        }

        fun encryptNip44(
            privateTags: Array<Array<String>>? = null,
            signer: NostrSignerSync,
        ): String =
            signer.nip44Encrypt(
                if (privateTags.isNullOrEmpty()) "" else encode(privateTags),
                signer.pubKey,
            )
    }
}
