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
package com.vitorpamplona.amethyst.service

import com.vitorpamplona.amethyst.BuildConfig
import com.vitorpamplona.amethyst.service.okhttp.HttpClientManager
import com.vitorpamplona.quartz.nip05DnsIdentifiers.Nip05
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Request

class Nip05NostrAddressVerifier {
    suspend fun fetchNip05Json(
        nip05: String,
        forceProxy: (String) -> Boolean,
        onSuccess: suspend (String) -> Unit,
        onError: (String) -> Unit,
    ) = withContext(Dispatchers.IO) {
        checkNotInMainThread()

        val url = Nip05().assembleUrl(nip05)

        if (url == null) {
            onError("Could not assemble url from Nip05: \"${nip05}\". Check the user's setup")
            return@withContext
        }

        try {
            val request =
                Request
                    .Builder()
                    .header("User-Agent", "Amethyst/${BuildConfig.VERSION_NAME}")
                    .url(url)
                    .build()
            // Fetchers MUST ignore any HTTP redirects given by the /.well-known/nostr.json endpoint.
            HttpClientManager
                .getHttpClient(forceProxy(url))
                .newBuilder()
                .followRedirects(false)
                .build()
                .newCall(request)
                .execute()
                .use {
                    checkNotInMainThread()

                    if (it.isSuccessful) {
                        onSuccess(it.body.string())
                    } else {
                        onError(
                            "Could not resolve $nip05. Error: ${it.code}. Check if the server is up and if the address $nip05 is correct",
                        )
                    }
                }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            onError("Could not resolve NIP-05 $nip05 as URL $url: ${e.message}")
        }
    }

    suspend fun verifyNip05(
        nip05: String,
        forceProxy: (String) -> Boolean,
        onSuccess: suspend (String) -> Unit,
        onError: (String) -> Unit,
    ) {
        // check fails on tests
        checkNotInMainThread()

        fetchNip05Json(
            nip05,
            forceProxy,
            onSuccess = {
                checkNotInMainThread()

                Nip05().parseHexKeyFor(nip05, it.lowercase()).fold(
                    onSuccess = { hexKey ->
                        if (hexKey == null) {
                            onError("Username not found in the NIP05 JSON [$nip05]")
                        } else {
                            onSuccess(hexKey)
                        }
                    },
                    onFailure = {
                        onError("Error Parsing JSON from NIP-05 address: $nip05." + (it.message ?: it.localizedMessage ?: it.javaClass.simpleName))
                    },
                )
            },
            onError = onError,
        )
    }
}
