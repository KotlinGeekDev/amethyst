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
package com.vitorpamplona.amethyst.ui.screen.loggedIn.chatrooms

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.map
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vitorpamplona.amethyst.R
import com.vitorpamplona.amethyst.model.Note
import com.vitorpamplona.amethyst.model.User
import com.vitorpamplona.amethyst.service.NostrChatroomDataSource
import com.vitorpamplona.amethyst.ui.actions.CrossfadeIfEnabled
import com.vitorpamplona.amethyst.ui.actions.NewPostViewModel
import com.vitorpamplona.amethyst.ui.actions.ServerOption
import com.vitorpamplona.amethyst.ui.actions.UploadFromGallery
import com.vitorpamplona.amethyst.ui.actions.UrlUserTagTransformation
import com.vitorpamplona.amethyst.ui.components.CompressorQuality
import com.vitorpamplona.amethyst.ui.components.MediaCompressor
import com.vitorpamplona.amethyst.ui.navigation.INav
import com.vitorpamplona.amethyst.ui.navigation.TopBarExtensibleWithBackButton
import com.vitorpamplona.amethyst.ui.note.ClickableUserPicture
import com.vitorpamplona.amethyst.ui.note.IncognitoIconOff
import com.vitorpamplona.amethyst.ui.note.IncognitoIconOn
import com.vitorpamplona.amethyst.ui.note.NonClickableUserPictures
import com.vitorpamplona.amethyst.ui.note.QuickActionAlertDialog
import com.vitorpamplona.amethyst.ui.note.UserCompose
import com.vitorpamplona.amethyst.ui.note.UsernameDisplay
import com.vitorpamplona.amethyst.ui.note.elements.ObserveRelayListForDMs
import com.vitorpamplona.amethyst.ui.note.elements.ObserveRelayListForDMsAndDisplayIfNotFound
import com.vitorpamplona.amethyst.ui.screen.NostrChatroomFeedViewModel
import com.vitorpamplona.amethyst.ui.screen.loggedIn.AccountViewModel
import com.vitorpamplona.amethyst.ui.screen.loggedIn.CloseButton
import com.vitorpamplona.amethyst.ui.screen.loggedIn.DisappearingScaffold
import com.vitorpamplona.amethyst.ui.screen.loggedIn.PostButton
import com.vitorpamplona.amethyst.ui.screen.loggedIn.search.UserLine
import com.vitorpamplona.amethyst.ui.stringRes
import com.vitorpamplona.amethyst.ui.theme.BottomTopHeight
import com.vitorpamplona.amethyst.ui.theme.DividerThickness
import com.vitorpamplona.amethyst.ui.theme.DoubleHorzSpacer
import com.vitorpamplona.amethyst.ui.theme.EditFieldBorder
import com.vitorpamplona.amethyst.ui.theme.EditFieldModifier
import com.vitorpamplona.amethyst.ui.theme.EditFieldTrailingIconModifier
import com.vitorpamplona.amethyst.ui.theme.Size20Modifier
import com.vitorpamplona.amethyst.ui.theme.Size30Modifier
import com.vitorpamplona.amethyst.ui.theme.Size34dp
import com.vitorpamplona.amethyst.ui.theme.StdPadding
import com.vitorpamplona.amethyst.ui.theme.ZeroPadding
import com.vitorpamplona.amethyst.ui.theme.placeholderText
import com.vitorpamplona.quartz.events.ChatMessageEvent
import com.vitorpamplona.quartz.events.ChatroomKey
import com.vitorpamplona.quartz.events.findURLs
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

@Composable
fun ChatroomScreen(
    roomId: String?,
    draftMessage: String? = null,
    accountViewModel: AccountViewModel,
    nav: INav,
) {
    if (roomId == null) return

    DisappearingScaffold(
        isInvertedLayout = true,
        topBar = {
            RoomTopBar(roomId, accountViewModel, nav)
        },
        accountViewModel = accountViewModel,
    ) {
        Column(Modifier.padding(it)) {
            Chatroom(roomId, draftMessage, accountViewModel, nav)
        }
    }
}

@Composable
private fun RoomTopBar(
    id: String,
    accountViewModel: AccountViewModel,
    nav: INav,
) {
    LoadRoom(roomId = id, accountViewModel) { room ->
        if (room != null) {
            RenderRoomTopBar(room, accountViewModel, nav)
        } else {
            Spacer(BottomTopHeight)
        }
    }
}

@Composable
private fun RenderRoomTopBar(
    room: ChatroomKey,
    accountViewModel: AccountViewModel,
    nav: INav,
) {
    if (room.users.size == 1) {
        TopBarExtensibleWithBackButton(
            title = {
                LoadUser(baseUserHex = room.users.first(), accountViewModel) { baseUser ->
                    if (baseUser != null) {
                        ClickableUserPicture(
                            baseUser = baseUser,
                            accountViewModel = accountViewModel,
                            size = Size34dp,
                        )

                        Spacer(modifier = DoubleHorzSpacer)

                        UsernameDisplay(baseUser, Modifier.weight(1f), fontWeight = FontWeight.Normal, accountViewModel = accountViewModel)
                    }
                }
            },
            extendableRow = {
                LoadUser(baseUserHex = room.users.first(), accountViewModel) {
                    if (it != null) {
                        UserCompose(
                            baseUser = it,
                            accountViewModel = accountViewModel,
                            nav = nav,
                        )
                    }
                }
            },
            popBack = nav::popBack,
        )
    } else {
        TopBarExtensibleWithBackButton(
            title = {
                NonClickableUserPictures(
                    room = room,
                    accountViewModel = accountViewModel,
                    size = Size34dp,
                )

                RoomNameOnlyDisplay(
                    room,
                    Modifier
                        .padding(start = 10.dp)
                        .weight(1f),
                    fontWeight = FontWeight.Normal,
                    accountViewModel,
                )
            },
            extendableRow = {
                LongRoomHeader(room = room, accountViewModel = accountViewModel, nav = nav)
            },
            popBack = nav::popBack,
        )
    }
}

@Composable
fun Chatroom(
    roomId: String?,
    draftMessage: String? = null,
    accountViewModel: AccountViewModel,
    nav: INav,
) {
    if (roomId == null) return

    LoadRoom(roomId, accountViewModel) {
        it?.let {
            PrepareChatroomViewModels(
                room = it,
                draftMessage = draftMessage,
                accountViewModel = accountViewModel,
                nav = nav,
            )
        }
    }
}

@Composable
fun ChatroomScreenByAuthor(
    authorPubKeyHex: String?,
    draftMessage: String? = null,
    accountViewModel: AccountViewModel,
    nav: INav,
) {
    if (authorPubKeyHex == null) return

    DisappearingScaffold(
        isInvertedLayout = true,
        topBar = {
            RoomByAuthorTopBar(authorPubKeyHex, accountViewModel, nav)
        },
        accountViewModel = accountViewModel,
    ) {
        Column(Modifier.padding(it)) {
            ChatroomByAuthor(authorPubKeyHex, draftMessage, accountViewModel, nav)
        }
    }
}

@Composable
private fun RoomByAuthorTopBar(
    authorPubKeyHex: String,
    accountViewModel: AccountViewModel,
    nav: INav,
) {
    LoadRoomByAuthor(authorPubKeyHex = authorPubKeyHex, accountViewModel) { room ->
        if (room != null) {
            RenderRoomTopBar(room, accountViewModel, nav)
        } else {
            Spacer(BottomTopHeight)
        }
    }
}

@Composable
fun ChatroomByAuthor(
    authorPubKeyHex: String?,
    draftMessage: String? = null,
    accountViewModel: AccountViewModel,
    nav: INav,
) {
    if (authorPubKeyHex == null) return

    LoadRoomByAuthor(authorPubKeyHex, accountViewModel) {
        it?.let {
            PrepareChatroomViewModels(
                room = it,
                draftMessage = draftMessage,
                accountViewModel = accountViewModel,
                nav = nav,
            )
        }
    }
}

@Composable
fun LoadRoom(
    roomId: String,
    accountViewModel: AccountViewModel,
    content: @Composable (ChatroomKey?) -> Unit,
) {
    var room by remember(roomId) { mutableStateOf<ChatroomKey?>(null) }

    if (room == null) {
        LaunchedEffect(key1 = roomId) {
            launch(Dispatchers.IO) {
                val newRoom =
                    accountViewModel.userProfile().privateChatrooms.keys.firstOrNull {
                        it.hashCode().toString() == roomId
                    }
                if (room != newRoom) {
                    room = newRoom
                }
            }
        }
    }

    content(room)
}

@Composable
fun LoadRoomByAuthor(
    authorPubKeyHex: String,
    accountViewModel: AccountViewModel,
    content: @Composable (ChatroomKey?) -> Unit,
) {
    val room by
        remember(authorPubKeyHex) {
            mutableStateOf<ChatroomKey?>(ChatroomKey(persistentSetOf(authorPubKeyHex)))
        }

    content(room)
}

@Composable
fun PrepareChatroomViewModels(
    room: ChatroomKey,
    draftMessage: String?,
    accountViewModel: AccountViewModel,
    nav: INav,
) {
    val feedViewModel: NostrChatroomFeedViewModel =
        viewModel(
            key = room.hashCode().toString() + "ChatroomViewModels",
            factory =
                NostrChatroomFeedViewModel.Factory(
                    room,
                    accountViewModel.account,
                ),
        )

    val newPostModel: NewPostViewModel = viewModel()
    newPostModel.accountViewModel = accountViewModel
    newPostModel.account = accountViewModel.account
    newPostModel.requiresNIP17 = room.users.size > 1

    if (newPostModel.requiresNIP17) {
        newPostModel.nip17 = true
    } else {
        if (room.users.size == 1) {
            ObserveRelayListForDMs(pubkey = room.users.first(), accountViewModel = accountViewModel) {
                if (it?.relays().isNullOrEmpty()) {
                    newPostModel.nip17 = false
                } else {
                    newPostModel.nip17 = true
                }
            }
        }
    }

    if (draftMessage != null) {
        LaunchedEffect(key1 = draftMessage) { newPostModel.updateMessage(TextFieldValue(draftMessage)) }
    }

    ChatroomScreen(
        room = room,
        feedViewModel = feedViewModel,
        newPostModel = newPostModel,
        accountViewModel = accountViewModel,
        nav = nav,
    )
}

@Composable
fun ChatroomScreen(
    room: ChatroomKey,
    feedViewModel: NostrChatroomFeedViewModel,
    newPostModel: NewPostViewModel,
    accountViewModel: AccountViewModel,
    nav: INav,
) {
    val context = LocalContext.current

    NostrChatroomDataSource.loadMessagesBetween(accountViewModel.account, room)

    val lifeCycleOwner = LocalLifecycleOwner.current

    DisposableEffect(room, accountViewModel) {
        NostrChatroomDataSource.loadMessagesBetween(accountViewModel.account, room)
        NostrChatroomDataSource.start()
        feedViewModel.invalidateData()

        onDispose { NostrChatroomDataSource.stop() }
    }

    DisposableEffect(lifeCycleOwner) {
        val observer =
            LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME) {
                    println("Private Message Start")
                    NostrChatroomDataSource.start()
                    feedViewModel.invalidateData()
                }
                if (event == Lifecycle.Event.ON_PAUSE) {
                    println("Private Message Stop")
                    NostrChatroomDataSource.stop()
                }
            }

        lifeCycleOwner.lifecycle.addObserver(observer)
        onDispose { lifeCycleOwner.lifecycle.removeObserver(observer) }
    }

    Column(Modifier.fillMaxHeight()) {
        val replyTo = remember { mutableStateOf<Note?>(null) }
        ObserveRelayListForDMsAndDisplayIfNotFound(accountViewModel, nav)

        Column(
            modifier =
                Modifier
                    .fillMaxHeight()
                    .padding(vertical = 0.dp)
                    .weight(1f, true),
        ) {
            RefreshingChatroomFeedView(
                viewModel = feedViewModel,
                accountViewModel = accountViewModel,
                nav = nav,
                routeForLastRead = "Room/${room.hashCode()}",
                avoidDraft = newPostModel.draftTag,
                onWantsToReply = {
                    replyTo.value = it
                },
                onWantsToEditDraft = {
                    newPostModel.load(accountViewModel, null, null, null, null, it)
                },
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        replyTo.value?.let { DisplayReplyingToNote(it, accountViewModel, nav) { replyTo.value = null } }

        val scope = rememberCoroutineScope()

        LaunchedEffect(key1 = newPostModel.draftTag) {
            launch(Dispatchers.IO) {
                newPostModel.draftTextChanges
                    .receiveAsFlow()
                    .debounce(1000)
                    .collectLatest {
                        innerSendPost(newPostModel, room, replyTo, accountViewModel, newPostModel.draftTag)
                    }
            }
        }

        // LAST ROW
        PrivateMessageEditFieldRow(newPostModel, isPrivate = true, accountViewModel) {
            scope.launch(Dispatchers.IO) {
                innerSendPost(newPostModel, room, replyTo, accountViewModel, null)

                accountViewModel.deleteDraft(newPostModel.draftTag)

                newPostModel.message = TextFieldValue("")

                replyTo.value = null
                feedViewModel.sendToTop()
            }
        }
    }
}

private fun innerSendPost(
    newPostModel: NewPostViewModel,
    room: ChatroomKey,
    replyTo: MutableState<Note?>,
    accountViewModel: AccountViewModel,
    dTag: String?,
) {
    val urls = findURLs(newPostModel.message.text)
    val usedAttachments = newPostModel.nip94attachments.filter { it.urls().intersect(urls.toSet()).isNotEmpty() }

    if (newPostModel.nip17 || room.users.size > 1 || replyTo.value?.event is ChatMessageEvent) {
        accountViewModel.account.sendNIP17PrivateMessage(
            message = newPostModel.message.text,
            toUsers = room.users.toList(),
            replyingTo = replyTo.value,
            mentions = null,
            wantsToMarkAsSensitive = false,
            nip94attachments = usedAttachments,
            draftTag = dTag,
        )
    } else {
        accountViewModel.account.sendPrivateMessage(
            message = newPostModel.message.text,
            toUser = room.users.first(),
            replyingTo = replyTo.value,
            mentions = null,
            wantsToMarkAsSensitive = false,
            nip94attachments = usedAttachments,
            draftTag = dTag,
        )
    }
}

@Composable
fun PrivateMessageEditFieldRow(
    channelScreenModel: NewPostViewModel,
    isPrivate: Boolean,
    accountViewModel: AccountViewModel,
    onSendNewMessage: () -> Unit,
) {
    Column(
        modifier = EditFieldModifier,
    ) {
        val context = LocalContext.current

        ShowUserSuggestionList(channelScreenModel, accountViewModel)

        MyTextField(
            value = channelScreenModel.message,
            onValueChange = { channelScreenModel.updateMessage(it) },
            keyboardOptions =
                KeyboardOptions.Default.copy(
                    capitalization = KeyboardCapitalization.Sentences,
                ),
            shape = EditFieldBorder,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    text = stringRes(R.string.reply_here),
                    color = MaterialTheme.colorScheme.placeholderText,
                )
            },
            trailingIcon = {
                ThinSendButton(
                    isActive =
                        channelScreenModel.message.text.isNotBlank() && !channelScreenModel.isUploadingImage,
                    modifier = EditFieldTrailingIconModifier,
                ) {
                    onSendNewMessage()
                }
            },
            leadingIcon = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 6.dp),
                ) {
                    UploadFromGallery(
                        isUploading = channelScreenModel.isUploadingImage,
                        tint = MaterialTheme.colorScheme.placeholderText,
                        modifier =
                            Modifier
                                .size(30.dp)
                                .padding(start = 2.dp),
                    ) {
                        channelScreenModel.upload(
                            galleryUri = it,
                            alt = null,
                            sensitiveContent = false,
                            // use MEDIUM quality
                            mediaQuality = MediaCompressor().compressorQualityToInt(CompressorQuality.MEDIUM),
                            isPrivate = isPrivate,
                            server = ServerOption(accountViewModel.account.settings.defaultFileServer, false),
                            onError = accountViewModel::toast,
                            context = context,
                        )
                    }

                    var wantsToActivateNIP17 by remember { mutableStateOf(false) }

                    if (wantsToActivateNIP17) {
                        NewFeatureNIP17AlertDialog(
                            accountViewModel = accountViewModel,
                            onConfirm = { channelScreenModel.toggleNIP04And24() },
                            onDismiss = { wantsToActivateNIP17 = false },
                        )
                    }

                    IconButton(
                        modifier = Size30Modifier,
                        onClick = {
                            if (
                                !accountViewModel.account.settings.hideNIP17WarningDialog &&
                                !channelScreenModel.nip17 &&
                                !channelScreenModel.requiresNIP17
                            ) {
                                wantsToActivateNIP17 = true
                            } else {
                                channelScreenModel.toggleNIP04And24()
                            }
                        },
                    ) {
                        if (channelScreenModel.nip17) {
                            IncognitoIconOn(
                                modifier =
                                    Modifier
                                        .padding(top = 2.dp)
                                        .size(18.dp),
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        } else {
                            IncognitoIconOff(
                                modifier =
                                    Modifier
                                        .padding(top = 2.dp)
                                        .size(18.dp),
                                tint = MaterialTheme.colorScheme.placeholderText,
                            )
                        }
                    }
                }
            },
            colors =
                TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                ),
            visualTransformation = UrlUserTagTransformation(MaterialTheme.colorScheme.primary),
        )
    }
}

@Composable
fun ShowUserSuggestionList(
    channelScreenModel: NewPostViewModel,
    accountViewModel: AccountViewModel,
    modifier: Modifier = Modifier.heightIn(0.dp, 200.dp),
) {
    val userSuggestions = channelScreenModel.userSuggestions
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
                UserLine(item, accountViewModel) { channelScreenModel.autocompleteWithUser(item) }
                HorizontalDivider(
                    thickness = DividerThickness,
                )
            }
        }
    }
}

@Composable
fun NewFeatureNIP17AlertDialog(
    accountViewModel: AccountViewModel,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    val scope = rememberCoroutineScope()

    QuickActionAlertDialog(
        title = stringRes(R.string.new_feature_nip17_might_not_be_available_title),
        textContent = stringRes(R.string.new_feature_nip17_might_not_be_available_description),
        buttonIconResource = R.drawable.incognito,
        buttonText = stringRes(R.string.new_feature_nip17_activate),
        onClickDoOnce = {
            scope.launch { onConfirm() }
            onDismiss()
        },
        onClickDontShowAgain = {
            scope.launch {
                onConfirm()
                accountViewModel.account.settings.setHideNIP17WarningDialog()
            }
            onDismiss()
        },
        onDismiss = onDismiss,
    )
}

@Composable
fun ThinSendButton(
    isActive: Boolean,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    IconButton(
        enabled = isActive,
        modifier = modifier,
        onClick = onClick,
    ) {
        Icon(
            imageVector = Icons.Default.Send,
            contentDescription = stringRes(id = R.string.accessibility_send),
            modifier = Size20Modifier,
        )
    }
}

@Composable
fun ChatroomHeader(
    room: ChatroomKey,
    modifier: Modifier = StdPadding,
    accountViewModel: AccountViewModel,
    onClick: () -> Unit,
) {
    if (room.users.size == 1) {
        LoadUser(baseUserHex = room.users.first(), accountViewModel) { baseUser ->
            if (baseUser != null) {
                ChatroomHeader(
                    baseUser = baseUser,
                    modifier = modifier,
                    accountViewModel = accountViewModel,
                    onClick = onClick,
                )
            }
        }
    } else {
        GroupChatroomHeader(
            room = room,
            modifier = modifier,
            accountViewModel = accountViewModel,
            onClick = onClick,
        )
    }
}

@Composable
fun ChatroomHeader(
    baseUser: User,
    modifier: Modifier = StdPadding,
    accountViewModel: AccountViewModel,
    onClick: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(
                    onClick = onClick,
                ),
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            modifier = modifier,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ClickableUserPicture(
                    baseUser = baseUser,
                    accountViewModel = accountViewModel,
                    size = Size34dp,
                )

                Column(modifier = Modifier.padding(start = 10.dp)) {
                    UsernameDisplay(baseUser, accountViewModel = accountViewModel)
                }
            }
        }
    }
}

@Composable
fun GroupChatroomHeader(
    room: ChatroomKey,
    modifier: Modifier = StdPadding,
    accountViewModel: AccountViewModel,
    onClick: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick),
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            modifier = modifier,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                NonClickableUserPictures(
                    room = room,
                    accountViewModel = accountViewModel,
                    size = Size34dp,
                )

                Column(modifier = Modifier.padding(start = 10.dp)) {
                    RoomNameOnlyDisplay(room, Modifier, FontWeight.Bold, accountViewModel)
                    DisplayUserSetAsSubject(room, accountViewModel, FontWeight.Normal)
                }
            }
        }
    }
}

@Composable
private fun EditRoomSubjectButton(
    room: ChatroomKey,
    accountViewModel: AccountViewModel,
) {
    var wantsToPost by remember { mutableStateOf(false) }

    if (wantsToPost) {
        NewSubjectView({ wantsToPost = false }, accountViewModel, room)
    }

    Button(
        modifier =
            Modifier
                .padding(horizontal = 3.dp)
                .width(50.dp),
        onClick = { wantsToPost = true },
        contentPadding = ZeroPadding,
    ) {
        Icon(
            tint = Color.White,
            imageVector = Icons.Default.EditNote,
            contentDescription = stringRes(R.string.edits_the_channel_metadata),
        )
    }
}

@Composable
fun NewSubjectView(
    onClose: () -> Unit,
    accountViewModel: AccountViewModel,
    room: ChatroomKey,
) {
    Dialog(
        onDismissRequest = { onClose() },
        properties =
            DialogProperties(
                dismissOnClickOutside = false,
            ),
    ) {
        Surface {
            val groupName =
                remember {
                    mutableStateOf<String>(accountViewModel.userProfile().privateChatrooms[room]?.subject ?: "")
                }
            val message = remember { mutableStateOf<String>("") }
            val scope = rememberCoroutineScope()

            Column(
                modifier =
                    Modifier
                        .padding(10.dp)
                        .verticalScroll(rememberScrollState()),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CloseButton(onPress = { onClose() })

                    PostButton(
                        onPost = {
                            scope.launch(Dispatchers.IO) {
                                accountViewModel.account.sendNIP17PrivateMessage(
                                    message = message.value,
                                    toUsers = room.users.toList(),
                                    subject = groupName.value.ifBlank { null },
                                    replyingTo = null,
                                    mentions = null,
                                    wantsToMarkAsSensitive = false,
                                )
                            }

                            onClose()
                        },
                        true,
                    )
                }

                Spacer(modifier = Modifier.height(15.dp))

                OutlinedTextField(
                    label = { Text(text = stringRes(R.string.messages_new_message_subject)) },
                    modifier = Modifier.fillMaxWidth(),
                    value = groupName.value,
                    onValueChange = { groupName.value = it },
                    placeholder = {
                        Text(
                            text = stringRes(R.string.messages_new_message_subject_caption),
                            color = MaterialTheme.colorScheme.placeholderText,
                        )
                    },
                    keyboardOptions =
                        KeyboardOptions.Default.copy(
                            capitalization = KeyboardCapitalization.Sentences,
                        ),
                    textStyle = LocalTextStyle.current.copy(textDirection = TextDirection.Content),
                )

                Spacer(modifier = Modifier.height(15.dp))

                OutlinedTextField(
                    label = { Text(text = stringRes(R.string.messages_new_subject_message)) },
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                    value = message.value,
                    onValueChange = { message.value = it },
                    placeholder = {
                        Text(
                            text = stringRes(R.string.messages_new_subject_message_placeholder),
                            color = MaterialTheme.colorScheme.placeholderText,
                        )
                    },
                    keyboardOptions =
                        KeyboardOptions.Default.copy(
                            capitalization = KeyboardCapitalization.Sentences,
                        ),
                    textStyle = LocalTextStyle.current.copy(textDirection = TextDirection.Content),
                    maxLines = 10,
                )
            }
        }
    }
}

@Composable
fun LongRoomHeader(
    room: ChatroomKey,
    lineModifier: Modifier = StdPadding,
    accountViewModel: AccountViewModel,
    nav: INav,
) {
    val list = remember(room) { room.users.toPersistentList() }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringRes(id = R.string.messages_group_descriptor),
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center,
        )

        EditRoomSubjectButton(room, accountViewModel)
    }

    LazyColumn(
        modifier = Modifier,
        state = rememberLazyListState(),
    ) {
        itemsIndexed(list, key = { _, item -> item }) { _, item ->
            LoadUser(baseUserHex = item, accountViewModel) {
                if (it != null) {
                    UserCompose(
                        baseUser = it,
                        overallModifier = lineModifier,
                        accountViewModel = accountViewModel,
                        nav = nav,
                    )
                    HorizontalDivider(
                        thickness = DividerThickness,
                    )
                }
            }
        }
    }
}

@Composable
fun RoomNameOnlyDisplay(
    room: ChatroomKey,
    modifier: Modifier,
    fontWeight: FontWeight = FontWeight.Bold,
    accountViewModel: AccountViewModel,
) {
    val roomSubject by
        accountViewModel
            .userProfile()
            .live()
            .messages
            .map { it.user.privateChatrooms[room]?.subject }
            .distinctUntilChanged()
            .observeAsState(accountViewModel.userProfile().privateChatrooms[room]?.subject)

    CrossfadeIfEnabled(targetState = roomSubject, modifier, accountViewModel = accountViewModel) {
        if (it != null && it.isNotBlank()) {
            DisplayRoomSubject(it, fontWeight)
        }
    }
}
