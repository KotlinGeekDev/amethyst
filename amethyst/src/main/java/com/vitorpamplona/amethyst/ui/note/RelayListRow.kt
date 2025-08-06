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
package com.vitorpamplona.amethyst.ui.note

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vitorpamplona.amethyst.R
import com.vitorpamplona.amethyst.model.FeatureSetType
import com.vitorpamplona.amethyst.model.Note
import com.vitorpamplona.amethyst.ui.components.ClickableBox
import com.vitorpamplona.amethyst.ui.components.RobohashFallbackAsyncImage
import com.vitorpamplona.amethyst.ui.navigation.navs.INav
import com.vitorpamplona.amethyst.ui.navigation.routes.Route
import com.vitorpamplona.amethyst.ui.screen.loggedIn.AccountViewModel
import com.vitorpamplona.amethyst.ui.screen.loggedIn.chats.publicChannels.ephemChat.header.loadRelayInfo
import com.vitorpamplona.amethyst.ui.stringRes
import com.vitorpamplona.amethyst.ui.theme.LargeRelayIconModifier
import com.vitorpamplona.amethyst.ui.theme.RelayIconFilter
import com.vitorpamplona.amethyst.ui.theme.Size15Modifier
import com.vitorpamplona.amethyst.ui.theme.Size17dp
import com.vitorpamplona.amethyst.ui.theme.StdStartPadding
import com.vitorpamplona.amethyst.ui.theme.ThemeComparisonColumn
import com.vitorpamplona.amethyst.ui.theme.allGoodColor
import com.vitorpamplona.amethyst.ui.theme.placeholderText
import com.vitorpamplona.amethyst.ui.theme.redColorOnSecondSurface
import com.vitorpamplona.amethyst.ui.theme.relayIconModifier
import com.vitorpamplona.amethyst.ui.theme.ripple24dp
import com.vitorpamplona.amethyst.ui.theme.warningColorOnSecondSurface
import com.vitorpamplona.quartz.nip01Core.relay.normalizer.NormalizedRelayUrl

@Composable
public fun RelayBadgesHorizontal(
    baseNote: Note,
    accountViewModel: AccountViewModel,
    nav: INav,
) {
    val expanded = remember { mutableStateOf(false) }

    if (expanded.value) {
        RenderAllRelayList(baseNote, StdStartPadding, verticalArrangement = Arrangement.Center, accountViewModel, nav)
    } else {
        RenderClosedRelayList(baseNote, StdStartPadding, verticalAlignment = Alignment.CenterVertically, accountViewModel = accountViewModel, nav = nav)
        ShouldShowExpandButton(baseNote, accountViewModel) {
            ChatRelayExpandButton { expanded.value = true }
        }
    }
}

@Composable
fun ShouldShowExpandButton(
    baseNote: Note,
    accountViewModel: AccountViewModel,
    content: @Composable () -> Unit,
) {
    val showExpandButton by accountViewModel.createMustShowExpandButtonFlows(baseNote).collectAsStateWithLifecycle()

    if (showExpandButton) {
        content()
    }
}

@Composable
fun ChatRelayExpandButton(onClick: () -> Unit) {
    ClickableBox(
        modifier = Size15Modifier,
        onClick = onClick,
    ) {
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = stringRes(id = R.string.expand_relay_list),
            modifier = Size15Modifier,
            tint = MaterialTheme.colorScheme.placeholderText,
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RenderRelay(
    relay: NormalizedRelayUrl,
    accountViewModel: AccountViewModel,
    nav: INav,
) {
    val relayInfo by loadRelayInfo(relay, accountViewModel)

    val clipboardManager = LocalClipboardManager.current
    val clickableModifier =
        remember(relay) {
            Modifier
                .size(Size17dp)
                .combinedClickable(
                    indication = ripple24dp,
                    interactionSource = MutableInteractionSource(),
                    onLongClick = {
                        clipboardManager.setText(AnnotatedString(relay.url))
                    },
                    onClick = { nav.nav(Route.RelayInfo(relay.url)) },
                )
        }

    Box(
        modifier = clickableModifier,
        contentAlignment = Alignment.Center,
    ) {
        RenderRelayIcon(
            displayUrl = relayInfo.id ?: relay.url,
            iconUrl = relayInfo.icon,
            loadProfilePicture = accountViewModel.settings.showProfilePictures.value,
            pingInMs = 0,
            loadRobohash = accountViewModel.settings.featureSet != FeatureSetType.PERFORMANCE,
        )
    }
}

@Preview
@Composable
fun RenderRelayIconPreview() {
    ThemeComparisonColumn {
        Row {
            RenderRelayIcon(
                displayUrl = "wss://relay.damus.io",
                iconUrl = "wss://relay.damus.io",
                loadProfilePicture = true,
                pingInMs = 100,
                loadRobohash = true,
                iconModifier = LargeRelayIconModifier,
            )
            RenderRelayIcon(
                displayUrl = "wss://relay.damus.io",
                iconUrl = "wss://relay.damus.io",
                loadProfilePicture = true,
                pingInMs = 300,
                loadRobohash = true,
                iconModifier = LargeRelayIconModifier,
            )
            RenderRelayIcon(
                displayUrl = "wss://relay.damus.io",
                iconUrl = "wss://relay.damus.io",
                loadProfilePicture = true,
                pingInMs = 500,
                loadRobohash = true,
                iconModifier = LargeRelayIconModifier,
            )
        }
    }
}

@Composable
fun RenderRelayIcon(
    displayUrl: String,
    iconUrl: String?,
    loadProfilePicture: Boolean,
    loadRobohash: Boolean,
    pingInMs: Long,
    iconModifier: Modifier = MaterialTheme.colorScheme.relayIconModifier,
) {
    val green = MaterialTheme.colorScheme.allGoodColor
    val yellow = MaterialTheme.colorScheme.warningColorOnSecondSurface
    val red = MaterialTheme.colorScheme.redColorOnSecondSurface
    Box(
        contentAlignment = Alignment.TopEnd,
    ) {
        RobohashFallbackAsyncImage(
            robot = displayUrl,
            model = iconUrl,
            contentDescription = stringRes(id = R.string.relay_info, displayUrl),
            colorFilter = RelayIconFilter,
            modifier = iconModifier,
            loadProfilePicture = loadProfilePicture,
            loadRobohash = loadRobohash,
        )

        val textStyle =
            remember(pingInMs) {
                TextStyle(
                    color =
                        if (pingInMs <= 150) {
                            green
                        } else if (pingInMs <= 300) {
                            yellow
                        } else {
                            red
                        },
                )
            }

        if (pingInMs > 0) {
            Box(
                modifier =
                    Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            MaterialTheme.colorScheme.secondaryContainer,
                        ),
            ) {
                Text(
                    modifier = Modifier.padding(3.dp),
                    style = textStyle,
                    text = "$pingInMs",
                    fontSize = 10.sp,
                )
            }
        }
    }
}
