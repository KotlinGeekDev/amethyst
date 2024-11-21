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
package com.vitorpamplona.amethyst.ui.actions

import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.util.Log
import android.util.Size
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil3.compose.AsyncImage
import com.vitorpamplona.amethyst.R
import com.vitorpamplona.amethyst.ui.actions.mediaServers.ServerType
import com.vitorpamplona.amethyst.ui.components.SetDialogToEdgeToEdge
import com.vitorpamplona.amethyst.ui.components.VideoView
import com.vitorpamplona.amethyst.ui.navigation.INav
import com.vitorpamplona.amethyst.ui.screen.loggedIn.AccountViewModel
import com.vitorpamplona.amethyst.ui.screen.loggedIn.CloseButton
import com.vitorpamplona.amethyst.ui.screen.loggedIn.PostButton
import com.vitorpamplona.amethyst.ui.screen.loggedIn.SettingSwitchItem
import com.vitorpamplona.amethyst.ui.screen.loggedIn.TextSpinner
import com.vitorpamplona.amethyst.ui.screen.loggedIn.TitleExplainer
import com.vitorpamplona.amethyst.ui.stringRes
import com.vitorpamplona.amethyst.ui.theme.Size5dp
import com.vitorpamplona.amethyst.ui.theme.StdHorzSpacer
import com.vitorpamplona.amethyst.ui.theme.placeholderText
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewMediaView(
    uri: Uri,
    onClose: () -> Unit,
    postViewModel: NewMediaModel,
    accountViewModel: AccountViewModel,
    nav: INav,
) {
    val account = accountViewModel.account
    val resolver = LocalContext.current.contentResolver
    val context = LocalContext.current

    val scrollState = rememberScrollState()

    LaunchedEffect(uri) {
        val mediaType = resolver.getType(uri) ?: ""
        postViewModel.load(account, uri, mediaType)
    }

    var showRelaysDialog by remember { mutableStateOf(false) }
    var relayList = remember { accountViewModel.account.activeWriteRelays().toImmutableList() }

    // 0 = Low, 1 = Medium, 2 = High, 3=UNCOMPRESSED
    var mediaQualitySlider by remember { mutableIntStateOf(1) }

    Dialog(
        onDismissRequest = { onClose() },
        properties =
            DialogProperties(
                usePlatformDefaultWidth = false,
                dismissOnClickOutside = false,
                decorFitsSystemWindows = false,
            ),
    ) {
        SetDialogToEdgeToEdge()
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Spacer(modifier = StdHorzSpacer)

                            Box {
                                IconButton(
                                    modifier = Modifier.align(Alignment.Center),
                                    onClick = { showRelaysDialog = true },
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.relays),
                                        contentDescription = null,
                                        modifier = Modifier.height(25.dp),
                                        tint = MaterialTheme.colorScheme.onBackground,
                                    )
                                }
                            }

                            PostButton(
                                onPost = {
                                    onClose()
                                    postViewModel.upload(context, relayList, mediaQualitySlider) {
                                        accountViewModel.toast(stringRes(context, R.string.failed_to_upload_media_no_details), it)
                                    }
                                    postViewModel.selectedServer?.let {
                                        if (it.type != ServerType.NIP95) {
                                            account.settings.changeDefaultFileServer(it)
                                        }
                                    }
                                },
                                isActive = postViewModel.canPost(),
                            )
                        }
                    },
                    navigationIcon = {
                        Row {
                            Spacer(modifier = StdHorzSpacer)
                            CloseButton(
                                onPress = {
                                    postViewModel.cancel()
                                    onClose()
                                },
                            )
                        }
                    },
                    colors =
                        TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                        ),
                )
            },
        ) { pad ->
            if (showRelaysDialog) {
                RelaySelectionDialog(
                    preSelectedList = relayList,
                    onClose = { showRelaysDialog = false },
                    onPost = { relayList = it },
                    accountViewModel = accountViewModel,
                    nav = nav,
                )
            }

            Surface(
                modifier =
                    Modifier
                        .padding(pad)
                        .consumeWindowInsets(pad)
                        .imePadding(),
            ) {
                Column(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(start = 10.dp, end = 10.dp, bottom = 10.dp),
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().weight(1f).verticalScroll(scrollState),
                    ) {
                        ImageVideoPost(postViewModel, accountViewModel)

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                        ) {
                            Column(
                                modifier = Modifier.weight(1.0f),
                                verticalArrangement = Arrangement.spacedBy(Size5dp),
                            ) {
                                Text(
                                    text = stringRes(context, R.string.media_compression_quality_label),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                Text(
                                    text = stringRes(context, R.string.media_compression_quality_explainer),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray,
                                    maxLines = 5,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(modifier = Modifier.fillMaxWidth()) {
                                    Text(
                                        text =
                                            when (mediaQualitySlider) {
                                                0 -> stringRes(R.string.media_compression_quality_low)
                                                1 -> stringRes(R.string.media_compression_quality_medium)
                                                2 -> stringRes(R.string.media_compression_quality_high)
                                                3 -> stringRes(R.string.media_compression_quality_uncompressed)
                                                else -> stringRes(R.string.media_compression_quality_medium)
                                            },
                                        modifier = Modifier.align(Alignment.Center),
                                    )
                                }

                                Slider(
                                    value = mediaQualitySlider.toFloat(),
                                    onValueChange = { mediaQualitySlider = it.toInt() },
                                    valueRange = 0f..3f,
                                    steps = 2,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ImageVideoPost(
    postViewModel: NewMediaModel,
    accountViewModel: AccountViewModel,
) {
    val nip95description = stringRes(id = R.string.upload_server_relays_nip95)
    val fileServers by accountViewModel.account.liveServerList.collectAsState()

    val fileServerOptions =
        remember(fileServers) {
            fileServers
                .map {
                    if (it.type == ServerType.NIP95) {
                        TitleExplainer(it.name, nip95description)
                    } else {
                        TitleExplainer(it.name, it.baseUrl)
                    }
                }.toImmutableList()
        }

    val resolver = LocalContext.current.contentResolver

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp)
                .windowInsetsPadding(WindowInsets(0.dp, 0.dp, 0.dp, 0.dp)),
    ) {
        if (postViewModel.isImage() == true) {
            AsyncImage(
                model = postViewModel.galleryUri.toString(),
                contentDescription = postViewModel.galleryUri.toString(),
                contentScale = ContentScale.FillWidth,
                modifier =
                    Modifier
                        .padding(top = 4.dp)
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets(0.dp, 0.dp, 0.dp, 0.dp)),
            )
        } else if (postViewModel.isVideo() == true && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            var bitmap by remember { mutableStateOf<Bitmap?>(null) }

            LaunchedEffect(key1 = postViewModel.galleryUri) {
                launch(Dispatchers.IO) {
                    postViewModel.galleryUri?.let {
                        try {
                            bitmap = resolver.loadThumbnail(it, Size(1200, 1000), null)
                        } catch (e: Exception) {
                            if (e is CancellationException) throw e
                            Log.w("NewPostView", "Couldn't create thumbnail, but the video can be uploaded", e)
                        }
                    }
                }
            }

            bitmap?.let {
                Image(
                    bitmap = it.asImageBitmap(),
                    contentDescription = "some useful description",
                    contentScale = ContentScale.FillWidth,
                    modifier = Modifier.padding(top = 4.dp).fillMaxWidth(),
                )
            }
        } else {
            postViewModel.galleryUri?.let {
                VideoView(
                    videoUri = it.toString(),
                    mimeType = postViewModel.mediaType,
                    roundedCorner = false,
                    isFiniteHeight = false,
                    accountViewModel = accountViewModel,
                )
            }
        }
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth(),
    ) {
        TextSpinner(
            label = stringRes(id = R.string.file_server),
            placeholder =
                fileServers
                    .firstOrNull { it == accountViewModel.account.settings.defaultFileServer }
                    ?.name
                    ?: fileServers[0].name,
            options = fileServerOptions,
            onSelect = { postViewModel.selectedServer = fileServers[it] },
            modifier = Modifier.windowInsetsPadding(WindowInsets(0.dp, 0.dp, 0.dp, 0.dp)).weight(1f),
        )
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth(),
    ) {
        SettingSwitchItem(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            checked = postViewModel.sensitiveContent,
            onCheckedChange = { postViewModel.sensitiveContent = it },
            title = R.string.add_sensitive_content_label,
            description = R.string.add_sensitive_content_description,
        )
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().windowInsetsPadding(WindowInsets(0.dp, 0.dp, 0.dp, 0.dp)),
    ) {
        OutlinedTextField(
            label = { Text(text = stringRes(R.string.content_description)) },
            modifier = Modifier.fillMaxWidth().windowInsetsPadding(WindowInsets(0.dp, 0.dp, 0.dp, 0.dp)),
            value = postViewModel.alt,
            onValueChange = { postViewModel.alt = it },
            placeholder = {
                Text(
                    text = stringRes(R.string.content_description_example),
                    color = MaterialTheme.colorScheme.placeholderText,
                )
            },
            keyboardOptions =
                KeyboardOptions.Default.copy(
                    capitalization = KeyboardCapitalization.Sentences,
                ),
        )
    }
}
