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
package com.vitorpamplona.amethyst.ui.components

import android.content.Context
import android.net.Uri
import android.os.Looper
import com.abedelazizshe.lightcompressorlibrary.VideoCompressor
import com.vitorpamplona.amethyst.ui.components.util.MediaCompressorFileUtils
import id.zelory.compressor.Compressor
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.File

class MediaCompressorTest {
    @Before
    fun setUp() {
        // Mock compressors
        mockkStatic(VideoCompressor::class)
        mockkObject(Compressor)

        // mock out main thread check
        mockkStatic(Looper::class)
        every { Looper.myLooper() } returns mockk<Looper>()
        every { Looper.getMainLooper() } returns mockk<Looper>()
        MockKAnnotations.init(this)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `Compression level uncompressed should not compress media`() =
        runTest {
            // setup
            val mediaQuality = CompressorQuality.UNCOMPRESSED
            val uri = mockk<Uri>()
            val contentType = "video"

            // Execution
            MediaCompressor().compress(
                uri,
                contentType,
                applicationContext = mockk(),
                onReady = { _, _, _ -> },
                onError = { },
                mediaQuality = mediaQuality,
            )

            // Verify
            verify(exactly = 0) { VideoCompressor.start(any(), any(), any(), any(), any(), any(), any()) }
            coVerify(exactly = 0) { Compressor.compress(any(), any(), any(), any()) }
        }

    @Test
    fun `Unknown media type should be skipped`() =
        runTest {
            // setup
            val mediaQuality = CompressorQuality.MEDIUM
            val uri = mockk<Uri>()
            val contentType = "test"

            // Execution
            MediaCompressor().compress(
                uri,
                contentType,
                applicationContext = mockk(),
                onReady = { _, _, _ -> },
                onError = { },
                mediaQuality = mediaQuality,
            )

            // Verify
            verify(exactly = 0) { VideoCompressor.start(any(), any(), any(), any(), any(), any(), any()) }
            coVerify(exactly = 0) { Compressor.compress(any(), any(), any(), any()) }
        }

    @Test
    fun `Video media should invoke video compressor`() =
        runTest {
            // setup
            val mediaQuality = CompressorQuality.MEDIUM
            val uri = mockk<Uri>()
            val contentType = "video"

            every { VideoCompressor.start(any(), any(), any(), any(), any(), any(), any()) } returns Unit

            // Execution
            MediaCompressor().compress(
                uri,
                contentType,
                applicationContext = mockk(),
                onReady = { _, _, _ -> },
                onError = { },
                mediaQuality = mediaQuality,
            )

            // Verify
            verify(exactly = 1) { VideoCompressor.start(any(), any(), any(), any(), any(), any(), any()) }
        }

    @Test
    fun `Image media should invoke image compressor`() =
        runTest {
            // setup
            val mediaQuality = CompressorQuality.MEDIUM
            val uri = mockk<Uri>()
            val contentType = "image"

            mockkObject(MediaCompressorFileUtils)
            every { MediaCompressorFileUtils.from(any(), any()) } returns File("test")
            coEvery { Compressor.compress(any(), any(), any(), any()) } returns File("test")

            // Execution
            MediaCompressor().compress(
                uri,
                contentType,
                applicationContext = mockk<Context>(relaxed = true),
                onReady = { _, _, _ -> },
                onError = { },
                mediaQuality = mediaQuality,
            )

            // Verify
            coVerify(exactly = 1) { Compressor.compress(any(), any(), any(), any()) }
        }

    @Test
    fun `Image compression should return back same uri on exception`() =
        runTest {
            // setup
            val mockContext = mockk<Context>(relaxed = true)
            val mockUri = mockk<Uri>()
            val mockOnReady = mockk<(Uri, String?, Long?) -> Unit>(relaxed = true)

            mockkObject(MediaCompressorFileUtils)
            every { MediaCompressorFileUtils.from(any(), any()) } returns File("test")
            coEvery { Compressor.compress(any(), any<File>(), any(), any()) } throws Exception("Compression error")

            // Execute
            MediaCompressor().compress(
                uri = mockUri,
                contentType = "image/jpeg",
                applicationContext = mockContext,
                onReady = mockOnReady,
                onError = { },
                mediaQuality = CompressorQuality.MEDIUM,
            )

            // Verify: onReady should be called with original uri, content type, and null size
            verify { mockOnReady.invoke(mockUri, "image/jpeg", null) }
        }
}
