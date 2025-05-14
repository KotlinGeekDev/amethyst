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
package com.vitorpamplona.quartz.benchmark

import androidx.benchmark.junit4.BenchmarkRule
import androidx.benchmark.junit4.measureRepeated
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.vitorpamplona.quartz.nip01Core.crypto.KeyPair
import com.vitorpamplona.quartz.nip01Core.signers.NostrSignerInternal
import com.vitorpamplona.quartz.nip01Core.tags.people.PTag
import com.vitorpamplona.quartz.nip17Dm.messages.ChatMessageEvent
import com.vitorpamplona.quartz.nip17Dm.messages.changeSubject
import com.vitorpamplona.quartz.nip36SensitiveContent.contentWarning
import com.vitorpamplona.quartz.nip57Zaps.zapraiser.zapraiser
import com.vitorpamplona.quartz.nip59Giftwrap.seals.SealedRumorEvent
import com.vitorpamplona.quartz.nip59Giftwrap.wraps.GiftWrapEvent
import junit.framework.TestCase.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * Benchmark, which will execute on an Android device.
 *
 * The body of [BenchmarkRule.measureRepeated] is measured in a loop, and Studio will output the
 * result. Modify your code to see how it affects performance.
 */
@RunWith(AndroidJUnit4::class)
class GiftWrapSigningBenchmark {
    @get:Rule val benchmarkRule = BenchmarkRule()

    @Test
    fun createMessageEvent() {
        val sender = NostrSignerInternal(KeyPair())
        val receiver = NostrSignerInternal(KeyPair())

        benchmarkRule.measureRepeated {
            val countDownLatch = CountDownLatch(1)

            sender.sign(
                ChatMessageEvent.build(
                    msg = "Hi there! This is a test message",
                    to =
                        listOf(
                            PTag(receiver.pubKey),
                        ),
                ) {
                    changeSubject("Party Tonight")
                    zapraiser(10000)
                    contentWarning("nsfw")
                },
            ) {
                countDownLatch.countDown()
            }

            assertTrue(countDownLatch.await(1, TimeUnit.SECONDS))
        }
    }

    @Test
    fun sealMessage() {
        val sender = NostrSignerInternal(KeyPair())
        val receiver = NostrSignerInternal(KeyPair())

        val countDownLatch = CountDownLatch(1)

        var msg: ChatMessageEvent? = null

        sender.sign(
            ChatMessageEvent.build(
                msg = "Hi there! This is a test message",
                to =
                    listOf(
                        PTag(receiver.pubKey),
                    ),
            ) {
                changeSubject("Party Tonight")
                zapraiser(10000)
                contentWarning("nsfw")
            },
        ) {
            msg = it
            countDownLatch.countDown()
        }

        assertTrue(countDownLatch.await(1, TimeUnit.SECONDS))

        benchmarkRule.measureRepeated {
            val countDownLatch2 = CountDownLatch(1)
            SealedRumorEvent.create(
                event = msg!!,
                encryptTo = receiver.pubKey,
                signer = sender,
            ) {
                countDownLatch2.countDown()
            }

            assertTrue(countDownLatch2.await(1, TimeUnit.SECONDS))
        }
    }

    @Test
    fun wrapSeal() {
        val sender = NostrSignerInternal(KeyPair())
        val receiver = NostrSignerInternal(KeyPair())

        val countDownLatch = CountDownLatch(1)

        var seal: SealedRumorEvent? = null

        sender.sign(
            ChatMessageEvent.build(
                msg = "Hi there! This is a test message",
                to =
                    listOf(
                        PTag(receiver.pubKey),
                    ),
            ) {
                changeSubject("Party Tonight")
                zapraiser(10000)
                contentWarning("nsfw")
            },
        ) {
            SealedRumorEvent.create(
                event = it,
                encryptTo = receiver.pubKey,
                signer = sender,
            ) {
                seal = it
                countDownLatch.countDown()
            }
        }

        assertTrue(countDownLatch.await(1, TimeUnit.SECONDS))

        benchmarkRule.measureRepeated {
            val countDownLatch2 = CountDownLatch(1)
            GiftWrapEvent.create(
                event = seal!!,
                recipientPubKey = receiver.pubKey,
            ) {
                countDownLatch2.countDown()
            }
            assertTrue(countDownLatch2.await(1, TimeUnit.SECONDS))
        }
    }

    @Test
    fun wrapToString() {
        val sender = NostrSignerInternal(KeyPair())
        val receiver = NostrSignerInternal(KeyPair())

        val countDownLatch = CountDownLatch(1)

        var wrap: GiftWrapEvent? = null

        sender.sign(
            ChatMessageEvent.build(
                msg = "Hi there! This is a test message",
                to =
                    listOf(
                        PTag(receiver.pubKey),
                    ),
            ) {
                changeSubject("Party Tonight")
                zapraiser(10000)
                contentWarning("nsfw")
            },
        ) {
            SealedRumorEvent.create(
                event = it,
                encryptTo = receiver.pubKey,
                signer = sender,
            ) {
                GiftWrapEvent.create(
                    event = it,
                    recipientPubKey = receiver.pubKey,
                ) {
                    wrap = it
                    countDownLatch.countDown()
                }
            }
        }

        assertTrue(countDownLatch.await(1, TimeUnit.SECONDS))

        benchmarkRule.measureRepeated { wrap!!.toJson() }
    }
}
