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
package com.vitorpamplona.amethyst.ui.note

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.vitorpamplona.amethyst.model.User
import com.vitorpamplona.amethyst.ui.screen.loggedIn.AccountViewModel
import com.vitorpamplona.amethyst.ui.theme.DividerThickness

@Composable
fun ShowUserSuggestionList(
    userSuggestions: List<User>,
    onSelect: (User) -> Unit,
    accountViewModel: AccountViewModel,
    modifier: Modifier = Modifier.heightIn(0.dp, 200.dp),
) {
    if (userSuggestions.isNotEmpty()) {
        LazyColumn(
            contentPadding =
                PaddingValues(
                    top = 10.dp,
                ),
            modifier = modifier,
        ) {
            itemsIndexed(
                userSuggestions,
                key = { _, item -> item.pubkeyHex },
            ) { _, item ->
                UserLine(item, accountViewModel) { onSelect(item) }
                HorizontalDivider(
                    thickness = DividerThickness,
                )
            }
        }
    }
}

@Composable
fun UserLine(
    baseUser: User,
    accountViewModel: AccountViewModel,
    onClick: () -> Unit,
) {
    Row(
        modifier =
            Modifier.fillMaxWidth().clickable(onClick = onClick).padding(
                start = 12.dp,
                end = 12.dp,
                top = 10.dp,
                bottom = 10.dp,
            ),
    ) {
        ClickableUserPicture(baseUser, 55.dp, accountViewModel, Modifier, null)

        Column(
            modifier = Modifier.padding(start = 10.dp).weight(1f),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) { UsernameDisplay(baseUser, accountViewModel = accountViewModel) }

            AboutDisplay(baseUser)
        }
    }
}
