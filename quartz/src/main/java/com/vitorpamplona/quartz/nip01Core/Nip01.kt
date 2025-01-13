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
package com.vitorpamplona.quartz.nip01Core

import com.vitorpamplona.quartz.crypto.CryptoUtils
import com.vitorpamplona.quartz.crypto.nextBytes
import com.vitorpamplona.quartz.crypto.sha256Hash
import fr.acinq.secp256k1.Secp256k1
import java.security.SecureRandom

class Nip01(
    val secp256k1: Secp256k1,
    val random: SecureRandom,
) {
    /** Provides a 32B "private key" aka random number */
    fun privkeyCreate() = random.nextBytes(32)

    fun compressedPubkeyCreate(privKey: ByteArray) = secp256k1.pubKeyCompress(secp256k1.pubkeyCreate(privKey))

    fun pubkeyCreate(privKey: ByteArray) = compressedPubkeyCreate(privKey).copyOfRange(1, 33)

    fun sign(
        data: ByteArray,
        privKey: ByteArray,
        nonce: ByteArray? = random.nextBytes(32),
    ): ByteArray = secp256k1.signSchnorr(data, privKey, nonce)

    fun signDeterministic(
        data: ByteArray,
        privKey: ByteArray,
    ): ByteArray = secp256k1.signSchnorr(data, privKey, null)

    fun verify(
        signature: ByteArray,
        hash: ByteArray,
        pubKey: ByteArray,
    ): Boolean = secp256k1.verifySchnorr(signature, hash, pubKey)

    fun sha256(data: ByteArray) = sha256Hash(data)

    fun signString(
        message: String,
        privKey: ByteArray,
        nonce: ByteArray = random.nextBytes(32),
    ): ByteArray = sign(CryptoUtils.sha256(message.toByteArray()), privKey, nonce)
}
