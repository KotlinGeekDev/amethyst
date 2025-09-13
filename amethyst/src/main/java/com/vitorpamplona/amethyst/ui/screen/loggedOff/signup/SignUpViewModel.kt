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
package com.vitorpamplona.amethyst.ui.screen.loggedOff.signup

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import com.vitorpamplona.amethyst.R
import com.vitorpamplona.amethyst.ui.screen.AccountStateViewModel
import com.vitorpamplona.amethyst.ui.screen.loggedOff.login.LoginErrorManager

class SignUpViewModel : ViewModel() {
    lateinit var accountStateViewModel: AccountStateViewModel

    val errorManager = LoginErrorManager()

    var displayName by mutableStateOf(TextFieldValue(""))

    var acceptedTerms by mutableStateOf(false)
    var termsAcceptanceIsRequiredError by mutableStateOf(false)

    fun init(accountStateViewModel: AccountStateViewModel) {
        this.accountStateViewModel = accountStateViewModel
    }

    fun updateDisplayName(value: TextFieldValue) {
        displayName = value
        errorManager.clearErrors()
    }

    fun updateAcceptedTerms(newAcceptedTerms: Boolean) {
        acceptedTerms = newAcceptedTerms
        if (newAcceptedTerms) {
            termsAcceptanceIsRequiredError = false
        }

        errorManager.clearErrors()
    }

    fun checkCanSignup(): Boolean {
        if (!acceptedTerms) {
            termsAcceptanceIsRequiredError = true
            return false
        }

        if (displayName.text.isBlank()) {
            errorManager.error(R.string.name_is_required)
            return false
        }

        return true
    }

    fun signup() {
        if (checkCanSignup()) {
            accountStateViewModel.newKey(displayName.text)
        }
    }
}
