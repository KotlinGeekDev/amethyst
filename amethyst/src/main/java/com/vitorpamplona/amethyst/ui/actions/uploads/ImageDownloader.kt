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
package com.vitorpamplona.amethyst.ui.actions.uploads

import com.vitorpamplona.ammolite.service.HttpClientManager
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

class ImageDownloader {
    suspend fun waitAndGetImage(
        imageUrl: String,
        forceProxy: Boolean,
    ): ByteArray? =
        withContext(Dispatchers.IO) {
            var imageData: ByteArray? = null
            var tentatives = 0

            // Servers are usually not ready.. so tries to download it for 15 times/seconds.
            while (imageData == null && tentatives < 15) {
                imageData =
                    try {
                        tryGetTheImage(imageUrl, forceProxy)
                    } catch (e: Exception) {
                        if (e is CancellationException) throw e
                        null
                    }

                if (imageData == null) {
                    tentatives++
                    delay(1000)
                }
            }

            return@withContext imageData
        }

    private suspend fun tryGetTheImage(
        imageUrl: String,
        forceProxy: Boolean,
    ): ByteArray? =
        withContext(Dispatchers.IO) {
            // TODO: Migrate to OkHttp
            HttpURLConnection.setFollowRedirects(true)
            var url = URL(imageUrl)
            var huc =
                if (forceProxy) {
                    url.openConnection(HttpClientManager.getCurrentProxy()) as HttpURLConnection
                } else {
                    url.openConnection() as HttpURLConnection
                }
            huc.instanceFollowRedirects = true
            var responseCode = huc.responseCode

            if (responseCode in 300..400) {
                val newUrl: String = huc.getHeaderField("Location")

                // open the new connnection again
                url = URL(newUrl)
                huc =
                    if (forceProxy) {
                        url.openConnection(HttpClientManager.getCurrentProxy()) as HttpURLConnection
                    } else {
                        url.openConnection() as HttpURLConnection
                    }
                responseCode = huc.responseCode
            }

            return@withContext if (responseCode in 200..300) {
                huc.inputStream.use { it.readBytes() }
            } else {
                null
            }
        }
}