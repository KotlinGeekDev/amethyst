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
package com.vitorpamplona.amethyst.ui.screen.loggedIn.lists.followsets

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.recalculateWindowInsets
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistAddCheck
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vitorpamplona.amethyst.R
import com.vitorpamplona.amethyst.model.Account
import com.vitorpamplona.amethyst.model.LocalCache
import com.vitorpamplona.amethyst.ui.navigation.navs.INav
import com.vitorpamplona.amethyst.ui.note.ArrowBackIcon
import com.vitorpamplona.amethyst.ui.screen.loggedIn.lists.FollowSetState
import com.vitorpamplona.amethyst.ui.screen.loggedIn.lists.ListVisibility
import com.vitorpamplona.amethyst.ui.screen.loggedIn.lists.NewSetCreationDialog
import com.vitorpamplona.amethyst.ui.screen.loggedIn.lists.NostrUserListFeedViewModel
import com.vitorpamplona.amethyst.ui.stringRes
import com.vitorpamplona.amethyst.ui.theme.ButtonBorder
import com.vitorpamplona.amethyst.ui.theme.StdHorzSpacer
import com.vitorpamplona.amethyst.ui.theme.StdVertSpacer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FollowSetsManagementDialog(
    userHex: String,
    account: Account,
    followSetsViewModel: NostrUserListFeedViewModel,
    navigator: INav,
) {
    val followSetsState by followSetsViewModel.feedContent.collectAsState()
    val userInfo by remember { derivedStateOf { LocalCache.getOrCreateUser(userHex) } }

    Scaffold(
        modifier =
            Modifier
                .fillMaxSize()
                .recalculateWindowInsets(),
        containerColor = AlertDialogDefaults.containerColor,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = stringRes(R.string.follow_set_man_dialog_title),
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = { navigator.popBack() },
                    ) {
                        ArrowBackIcon()
                    }
                },
                colors =
                    TopAppBarDefaults
                        .topAppBarColors(
                            containerColor = AlertDialogDefaults.containerColor,
                        ),
            )
        },
        floatingActionButton = {
            BackActionButton { navigator.popBack() }
        },
    ) { contentPadding ->
        Column(
            modifier =
                Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(
                        start = 10.dp,
                        end = 10.dp,
                        top = contentPadding.calculateTopPadding(),
                        bottom = contentPadding.calculateBottomPadding(),
                    ).consumeWindowInsets(contentPadding)
                    .imePadding(),
        ) {
            when (followSetsState) {
                is FollowSetState.Loaded -> {
                    val lists = (followSetsState as FollowSetState.Loaded).feed

                    lists.forEachIndexed { index, list ->
                        Spacer(StdVertSpacer)
                        FollowSetItem(
                            modifier = Modifier.fillMaxWidth(),
                            listHeader = list.title,
                            listVisibility = list.visibility,
                            userName = userInfo.toBestDisplayName(),
                            isUserInList = list.profileList.contains(userHex),
                            onRemoveUser = {
                                Log.d(
                                    "Amethyst",
                                    "ProfileActions: Removing item from list ...",
                                )
                                followSetsViewModel.removeUserFromSet(
                                    userHex,
                                    list,
                                    account,
                                )
                                Log.d(
                                    "Amethyst",
                                    "Updated List. New size: ${list.profileList.size}",
                                )
                            },
                            onAddUser = {
                                Log.d(
                                    "Amethyst",
                                    "ProfileActions: Adding item to list ...",
                                )
                                followSetsViewModel.addUserToSet(userHex, list, account)
                                Log.d(
                                    "Amethyst",
                                    "Updated List. New size: ${list.profileList.size}",
                                )
                            },
                        )
                    }
                }

                FollowSetState.Empty -> {
                    EmptyOrNoneFound { followSetsViewModel.refresh() }
                }

                is FollowSetState.FeedError -> {
                    val errorMsg = (followSetsState as FollowSetState.FeedError).errorMessage
                    ErrorMessage(errorMsg) { followSetsViewModel.refresh() }
                }

                FollowSetState.Loading -> {
                    Loading()
                }
            }

            if (followSetsState != FollowSetState.Loading) {
                FollowSetsCreationMenu(
                    userName = userInfo.toBestDisplayName(),
                    onSetCreate = { setName, setIsPrivate, description ->
                        followSetsViewModel.addFollowSet(
                            setName = setName,
                            setDescription = description,
                            isListPrivate = setIsPrivate,
                            optionalFirstMemberHex = userHex,
                            account = account,
                        )
                    },
                )
            }
        }
    }
}

@Composable
fun BackActionButton(
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
) {
    OutlinedButton(
        onClick = onBack,
        shape = ButtonBorder,
        colors = ButtonDefaults.filledTonalButtonColors(),
        elevation = ButtonDefaults.elevatedButtonElevation(defaultElevation = 6.0.dp),
    ) {
        Text(text = stringRes(R.string.back), fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun Loading() {
    Column(
        Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.5f),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        CircularProgressIndicator()
        Text(stringRes(R.string.loading_feed))
    }
}

@Composable
private fun EmptyOrNoneFound(onRefresh: () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.5f),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(text = stringRes(R.string.follow_set_empty_dialog_msg))
        Spacer(modifier = StdVertSpacer)
        OutlinedButton(onClick = onRefresh) { Text(text = stringRes(R.string.refresh)) }
    }
}

@Composable
private fun ErrorMessage(
    errorMsg: String,
    onRefresh: () -> Unit,
) {
    Column(
        Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.5f),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(text = stringRes(R.string.follow_set_error_dialog_msg, errorMsg))
        Spacer(modifier = StdVertSpacer)
        OutlinedButton(onClick = onRefresh) { Text(text = stringRes(R.string.refresh)) }
    }
}

@Composable
fun FollowSetItem(
    modifier: Modifier = Modifier,
    listHeader: String,
    listVisibility: ListVisibility,
    userName: String,
    isUserInList: Boolean,
    onAddUser: () -> Unit,
    onRemoveUser: () -> Unit,
) {
    val context = LocalContext.current
    Row(
        modifier =
            modifier
                .border(
                    width = Dp.Hairline,
                    color = Color.Gray,
                    shape = RoundedCornerShape(percent = 20),
                ).padding(all = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = modifier.weight(1f),
            verticalArrangement = Arrangement.Center,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(listHeader, fontWeight = FontWeight.Bold)
                Spacer(modifier = StdHorzSpacer)
                listVisibility.let {
                    val text by derivedStateOf {
                        when (it) {
                            ListVisibility.Public -> stringRes(context, R.string.follow_set_type_public)
                            ListVisibility.Private -> stringRes(context, R.string.follow_set_type_private)
                            ListVisibility.Mixed -> stringRes(context, R.string.follow_set_type_mixed)
                        }
                    }
                    Icon(
                        painter =
                            painterResource(
                                when (listVisibility) {
                                    ListVisibility.Public -> R.drawable.ic_public
                                    ListVisibility.Private -> R.drawable.lock
                                    ListVisibility.Mixed -> R.drawable.format_list_bulleted_type
                                },
                            ),
                        contentDescription = stringRes(R.string.follow_set_type_description, text),
                    )
                }
            }

            Spacer(modifier = StdVertSpacer)
            Row {
                FilterChip(
                    selected = isUserInList,
                    enabled = isUserInList,
                    onClick = {},
                    label = {
                        Text(
                            text =
                                if (isUserInList) {
                                    stringRes(R.string.follow_set_presence_indicator, userName)
                                } else {
                                    stringRes(R.string.follow_set_absence_indicator, userName)
                                },
                        )
                    },
                    leadingIcon =
                        if (isUserInList) {
                            {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.PlaylistAddCheck,
                                    contentDescription = null,
                                )
                            }
                        } else {
                            null
                        },
                    shape = ButtonBorder,
                )
                Spacer(modifier = StdHorzSpacer)
            }
        }

        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            IconButton(
                onClick = {
                    if (isUserInList) onRemoveUser() else onAddUser()
                },
                modifier =
                    Modifier
                        .background(
                            color =
                                if (isUserInList) {
                                    MaterialTheme.colorScheme.errorContainer
                                } else {
                                    ButtonDefaults.filledTonalButtonColors().containerColor
                                },
                            shape = RoundedCornerShape(percent = 80),
                        ),
            ) {
                if (isUserInList) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onBackground,
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onBackground,
                    )
                }
            }
            Text(text = stringRes(if (isUserInList) R.string.remove else R.string.add), color = Color.Gray)
        }
    }
}

@Composable
fun FollowSetsCreationMenu(
    modifier: Modifier = Modifier,
    userName: String,
    onSetCreate: (setName: String, setIsPrivate: Boolean, description: String?) -> Unit,
) {
    val isListAdditionDialogOpen = remember { mutableStateOf(false) }
    val isPrivateOptionTapped = remember { mutableStateOf(false) }

    Column(
        modifier = modifier.padding(vertical = 30.dp),
    ) {
        Text(
            stringRes(R.string.follow_set_creation_menu_title),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Start,
        )
        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(0.5f),
            thickness = 3.dp,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(modifier = StdVertSpacer)
        FollowSetCreationItem(
            setIsPrivate = false,
            userName = userName,
            onClick = {
                isListAdditionDialogOpen.value = true
            },
        )
        FollowSetCreationItem(
            setIsPrivate = true,
            userName = userName,
            onClick = {
                isPrivateOptionTapped.value = true
                isListAdditionDialogOpen.value = true
            },
        )
    }

    if (isListAdditionDialogOpen.value) {
        NewSetCreationDialog(
            onDismiss = {
                isListAdditionDialogOpen.value = false
                isPrivateOptionTapped.value = false
            },
            shouldBePrivate = isPrivateOptionTapped.value,
            onCreateList = { name, description ->
                onSetCreate(name, isPrivateOptionTapped.value, description)
            },
        )
    }
}

@Composable
fun FollowSetCreationItem(
    modifier: Modifier = Modifier,
    setIsPrivate: Boolean,
    userName: String,
    onClick: () -> Unit,
) {
    val context = LocalContext.current
    val setTypeLabel = stringRes(context, if (setIsPrivate) R.string.follow_set_type_private else R.string.follow_set_type_public)

    HorizontalDivider()
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .background(
                    color =
                        ButtonDefaults
                            .filledTonalButtonColors()
                            .containerColor
                            .copy(alpha = 0.2f),
                ).padding(vertical = 15.dp)
                .clickable(role = Role.Button, onClick = onClick),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text =
                    stringRes(
                        R.string.follow_set_creation_item_label,
                        setTypeLabel,
                    ),
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(modifier = StdHorzSpacer)
            Icon(
                painter =
                    painterResource(
                        if (setIsPrivate) R.drawable.lock_plus else R.drawable.earth_plus,
                    ),
                contentDescription = null,
            )
        }
        Spacer(modifier = StdVertSpacer)
        Text(
            stringRes(R.string.follow_set_creation_item_description, setTypeLabel, userName),
            fontWeight = FontWeight.Light,
            overflow = TextOverflow.Ellipsis,
            maxLines = 2,
            color = Color.Gray,
        )
    }
    HorizontalDivider()
}
