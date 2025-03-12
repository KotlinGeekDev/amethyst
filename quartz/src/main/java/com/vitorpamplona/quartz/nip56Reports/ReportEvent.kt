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
package com.vitorpamplona.quartz.nip56Reports

import androidx.compose.runtime.Immutable
import com.vitorpamplona.quartz.nip01Core.core.AddressableEvent
import com.vitorpamplona.quartz.nip01Core.core.Event
import com.vitorpamplona.quartz.nip01Core.core.HexKey
import com.vitorpamplona.quartz.nip01Core.signers.NostrSigner
import com.vitorpamplona.quartz.nip01Core.signers.eventTemplate
import com.vitorpamplona.quartz.nip31Alts.AltTag
import com.vitorpamplona.quartz.nip31Alts.alt
import com.vitorpamplona.quartz.nip56Reports.tags.DefaultReportTag
import com.vitorpamplona.quartz.nip56Reports.tags.ReportedAddressTag
import com.vitorpamplona.quartz.nip56Reports.tags.ReportedAuthorTag
import com.vitorpamplona.quartz.nip56Reports.tags.ReportedEventTag
import com.vitorpamplona.quartz.utils.TimeUtils

// NIP 56 event.
@Immutable
class ReportEvent(
    id: HexKey,
    pubKey: HexKey,
    createdAt: Long,
    tags: Array<Array<String>>,
    content: String,
    sig: HexKey,
) : Event(id, pubKey, createdAt, KIND, tags, content, sig) {
    @Transient
    private var defaultType: ReportType? = null

    private fun defaultReportTypes() = tags.mapNotNull(DefaultReportTag::parse)

    private fun defaultReportType(): ReportType {
        defaultType?.let { return it }

        // Works with old and new structures for report.
        var reportType = defaultReportTypes().firstOrNull()
        if (reportType == null) {
            reportType = tags.mapNotNull { it.getOrNull(2) }.map { ReportType.parseOrNull(it, emptyArray()) }.firstOrNull()
        }
        if (reportType == null) {
            reportType = ReportType.SPAM
        }
        defaultType = reportType
        return reportType
    }

    fun reportedPost() = tags.mapNotNull { ReportedEventTag.parse(it, defaultReportType()) }

    fun reportedAddresses() = tags.mapNotNull { ReportedAddressTag.parse(it, defaultReportType()) }

    fun reportedAuthor() = tags.mapNotNull { ReportedAuthorTag.parse(it, defaultReportType()) }

    companion object {
        const val KIND = 1984
        const val ALT_PREFIX = "Report for "

        fun create(
            reportedPost: Event,
            type: ReportType,
            signer: NostrSigner,
            content: String = "",
            createdAt: Long = TimeUtils.now(),
            onReady: (ReportEvent) -> Unit,
        ) {
            val reportPostTag = arrayOf("e", reportedPost.id, type.name.lowercase())
            val reportAuthorTag = arrayOf("p", reportedPost.pubKey, type.name.lowercase())

            var tags: Array<Array<String>> = arrayOf(reportPostTag, reportAuthorTag)

            if (reportedPost is AddressableEvent) {
                tags += listOf(arrayOf("a", reportedPost.aTag().toTag()))
            }

            tags += listOf(AltTag.assemble("Report for ${type.name}"))

            signer.sign(createdAt, KIND, tags, content, onReady)
        }

        fun build(
            reportedPost: Event,
            type: ReportType,
            createdAt: Long = TimeUtils.now(),
        ) = eventTemplate(KIND, "", createdAt) {
            alt(ALT_PREFIX + type.code)
            event(reportedPost.id, type)
            user(reportedPost.pubKey, type)

            if (reportedPost is AddressableEvent) {
                address(reportedPost.address(), type)
            }
        }

        fun build(
            reportedUser: HexKey,
            type: ReportType,
            createdAt: Long = TimeUtils.now(),
        ) = eventTemplate(KIND, "", createdAt) {
            alt(ALT_PREFIX + type.code)
            user(reportedUser, type)
        }
    }
}
