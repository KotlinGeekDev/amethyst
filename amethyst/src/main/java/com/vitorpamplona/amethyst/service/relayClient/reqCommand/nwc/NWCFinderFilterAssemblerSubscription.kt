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
package com.vitorpamplona.amethyst.service.relayClient.reqCommand.nwc

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.vitorpamplona.amethyst.model.Note
import com.vitorpamplona.amethyst.service.relayClient.KeyDataSourceSubscription
import com.vitorpamplona.amethyst.ui.screen.loggedIn.AccountViewModel
import com.vitorpamplona.quartz.nip47WalletConnect.LnZapPaymentRequestEvent

@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun NWCFinderFilterAssemblerSubscription(
    note: Note,
    accountViewModel: AccountViewModel,
) = NWCFinderFilterAssemblerSubscription(
    note,
    accountViewModel.dataSources().nwc,
)

@Composable
fun NWCFinderFilterAssemblerSubscription(
    note: Note,
    dataSource: NWCPaymentFilterAssembler,
) {
    // different screens get different states
    // even if they are tracking the same tag.
    val states =
        remember(note) {
            val zapPaymentRequestNote = note
            (zapPaymentRequestNote.event as? LnZapPaymentRequestEvent)?.let { noteEvent ->
                noteEvent.walletServicePubKey()?.let { serviceId ->
                    zapPaymentRequestNote.relays.map {
                        NWCPaymentQueryState(
                            fromServiceHex = serviceId,
                            toUserHex = noteEvent.pubKey,
                            replyingToHex = noteEvent.id,
                            relay = it,
                        )
                    }
                }
            }
        }

    states?.forEach {
        KeyDataSourceSubscription(it, dataSource)
    }
}
