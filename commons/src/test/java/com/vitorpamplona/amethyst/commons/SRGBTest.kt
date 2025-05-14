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
package com.vitorpamplona.amethyst.commons

import com.vitorpamplona.amethyst.commons.blurhash.SRGB
import junit.framework.TestCase.assertEquals
import org.junit.Test
import kotlin.math.round

class SRGBTest {
    @Test
    fun testEncodeDecode() {
        for (i in 0..255) {
            assertEquals("$i encode decode", i, SRGB.linearToSrgb(SRGB.srgbToLinear(i)))
        }

        for (i in 0..100) {
            val srgb = SRGB.linearToSrgb(i / 100.0f)
            val linear = round(SRGB.srgbToLinear(srgb) * 100).toInt()

            assertEquals("$i decode encode", i, linear)
        }
    }
}
