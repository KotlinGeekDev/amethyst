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
package com.vitorpamplona.quartz.utils

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.refTo
import platform.Security.SecRandomCopyBytes
import platform.Security.kSecRandomDefault

actual class SecureRandom {
    actual fun nextInt(bound: Int): Int {
        require(bound > 0) { throw IllegalArgumentException("Bad Bound $bound") }

        var intValue = nextPositiveInt()

        val m = bound - 1
        if ((bound and m) == 0) {
            // i.e., bound is a power of 2
            intValue = ((bound * intValue.toLong()) shr 31).toInt()
        } else { // reject over-represented candidates
            var u: Int = intValue
            intValue = u % bound
            while (u - intValue + m < 0) {
                u = nextPositiveInt()
                intValue = u % bound
            }
        }

        return intValue
    }

    fun nextPositiveInt(): Int {
        val bytes = ByteArray(4)
        nextBytes(bytes)
        val value =
            ((bytes[0].toInt() and 0xFF) shl 24) or
                ((bytes[1].toInt() and 0xFF) shl 16) or
                ((bytes[2].toInt() and 0xFF) shl 8) or
                (bytes[3].toInt() and 0xFF)
        return if (value > 0) {
            value
        } else {
            -value
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    actual fun nextBytes(output: ByteArray) {
        val status = SecRandomCopyBytes(kSecRandomDefault, output.size.toULong(), output.refTo(0))
        if (status != 0) {
            // Handle error, e.g., throw an exception
            throw IllegalStateException("Failed to generate secure random bytes: $status")
        }
    }
}
