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
package com.vitorpamplona.amethyst.ui.screen.loggedIn.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vitorpamplona.amethyst.ui.navigation.FollowListWithRoutes
import com.vitorpamplona.amethyst.ui.navigation.GenericMainTopBar
import com.vitorpamplona.amethyst.ui.navigation.INav
import com.vitorpamplona.amethyst.ui.screen.loggedIn.AccountViewModel

@Composable
fun HomeTopBar(
    accountViewModel: AccountViewModel,
    nav: INav,
) {
    GenericMainTopBar(accountViewModel, nav) {
        val list by accountViewModel.account.settings.defaultHomeFollowList
            .collectAsStateWithLifecycle()

        FollowListWithRoutes(
            followListsModel = accountViewModel.feedStates.feedListOptions,
            listName = list,
            accountViewModel = accountViewModel,
        ) { listName ->
            if (listName.route != null) {
                nav.nav(listName.route)
            } else {
                accountViewModel.account.settings.changeDefaultHomeFollowList(listName.code)
            }
        }
    }
}
