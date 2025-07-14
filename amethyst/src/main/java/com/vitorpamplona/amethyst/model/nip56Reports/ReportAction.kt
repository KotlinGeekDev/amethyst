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
package com.vitorpamplona.amethyst.model.nip56Reports

import com.vitorpamplona.amethyst.model.Note
import com.vitorpamplona.amethyst.model.User
import com.vitorpamplona.quartz.nip01Core.signers.NostrSigner
import com.vitorpamplona.quartz.nip56Reports.ReportEvent
import com.vitorpamplona.quartz.nip56Reports.ReportType

class ReportAction {
    companion object {
        fun report(
            user: User,
            type: ReportType,
            by: User,
            signer: NostrSigner,
            onDone: (ReportEvent) -> Unit,
        ) {
            if (!signer.isWriteable()) return

            if (user.hasReport(by, type)) {
                // has already reported this note
                return
            }

            val template = ReportEvent.build(user.pubkeyHex, type)

            signer.sign(template, onDone)
        }

        suspend fun report(
            note: Note,
            type: ReportType,
            content: String = "",
            by: User,
            signer: NostrSigner,
            onDone: (ReportEvent) -> Unit,
        ) {
            if (!signer.isWriteable()) return

            if (note.hasReport(by, type)) {
                // has already reported this note
                return
            }

            note.event?.let {
                signer.sign(ReportEvent.build(it, type), onDone)
            }
        }
    }
}
