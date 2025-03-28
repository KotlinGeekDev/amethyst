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
package com.vitorpamplona.amethyst.ui.note.creators.emojiSuggestions

import com.vitorpamplona.amethyst.model.Account
import com.vitorpamplona.amethyst.ui.screen.loggedIn.AccountViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn

class EmojiSuggestionState(
    val accountViewModel: AccountViewModel,
) {
    val search: MutableStateFlow<String> = MutableStateFlow("")
    val results: Flow<List<Account.EmojiMedia>> =
        accountViewModel.account
            .myEmojis
            .combine(search) { list, search ->
                if (search.length == 1) {
                    list
                } else if (search.isNotEmpty()) {
                    val code = search.removePrefix(":")
                    list.filter { it.code.startsWith(code) }
                } else {
                    emptyList()
                }
            }.flowOn(Dispatchers.Default)

    fun reset() {
        if (search.value.isNotEmpty()) {
            search.tryEmit("")
        }
    }

    fun processCurrentWord(currentWord: String) {
        if (currentWord.startsWith(":")) {
            search.tryEmit(currentWord)
        } else {
            if (search.value.isNotBlank()) {
                search.tryEmit("")
            }
        }
    }
}
