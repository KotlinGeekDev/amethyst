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
package com.vitorpamplona.amethyst.model.privacyOptions

import okhttp3.OkHttpClient
import java.net.InetSocketAddress

class EmptyRoleBasedHttpClientBuilder : IRoleBasedHttpClientBuilder {
    val rootOkHttpClient by lazy {
        OkHttpClient
            .Builder()
            .followRedirects(true)
            .followSslRedirects(true)
            .build()
    }

    override fun proxyPortForVideo(url: String) = (rootOkHttpClient.proxy?.address() as? InetSocketAddress)?.port

    override fun okHttpClientForNip05(url: String) = rootOkHttpClient

    override fun okHttpClientForUploads(url: String) = rootOkHttpClient

    override fun okHttpClientForImage(url: String) = rootOkHttpClient

    override fun okHttpClientForVideo(url: String) = rootOkHttpClient

    override fun okHttpClientForMoney(url: String) = rootOkHttpClient

    override fun okHttpClientForPreview(url: String) = rootOkHttpClient

    override fun okHttpClientForPushRegistration(url: String) = rootOkHttpClient
}
