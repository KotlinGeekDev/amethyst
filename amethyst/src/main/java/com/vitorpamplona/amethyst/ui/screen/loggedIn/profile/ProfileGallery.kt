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
package com.vitorpamplona.amethyst.ui.screen.loggedIn.profile

import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment.Companion.BottomStart
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.map
import com.vitorpamplona.amethyst.commons.richtext.MediaUrlImage
import com.vitorpamplona.amethyst.commons.richtext.MediaUrlVideo
import com.vitorpamplona.amethyst.commons.richtext.RichTextParser.Companion.isVideoUrl
import com.vitorpamplona.amethyst.model.Note
import com.vitorpamplona.amethyst.ui.actions.CrossfadeIfEnabled
import com.vitorpamplona.amethyst.ui.components.GalleryContentView
import com.vitorpamplona.amethyst.ui.components.LoadNote
import com.vitorpamplona.amethyst.ui.components.SensitivityWarning
import com.vitorpamplona.amethyst.ui.feeds.FeedEmpty
import com.vitorpamplona.amethyst.ui.feeds.FeedError
import com.vitorpamplona.amethyst.ui.feeds.FeedState
import com.vitorpamplona.amethyst.ui.feeds.LoadingFeed
import com.vitorpamplona.amethyst.ui.navigation.INav
import com.vitorpamplona.amethyst.ui.note.CheckHiddenFeedWatchBlockAndReport
import com.vitorpamplona.amethyst.ui.note.ClickableNote
import com.vitorpamplona.amethyst.ui.note.LongPressToQuickActionGallery
import com.vitorpamplona.amethyst.ui.note.WatchAuthor
import com.vitorpamplona.amethyst.ui.note.WatchNoteEvent
import com.vitorpamplona.amethyst.ui.note.calculateBackgroundColor
import com.vitorpamplona.amethyst.ui.note.elements.BannerImage
import com.vitorpamplona.amethyst.ui.screen.FeedViewModel
import com.vitorpamplona.amethyst.ui.screen.loggedIn.AccountViewModel
import com.vitorpamplona.amethyst.ui.screen.loggedIn.mockAccountViewModel
import com.vitorpamplona.amethyst.ui.theme.FeedPadding
import com.vitorpamplona.amethyst.ui.theme.HalfPadding
import com.vitorpamplona.amethyst.ui.theme.QuoteBorder
import com.vitorpamplona.quartz.events.PictureEvent
import com.vitorpamplona.quartz.events.ProfileGalleryEntryEvent
import com.vitorpamplona.quartz.events.VideoEvent

@Composable
fun RenderGalleryFeed(
    viewModel: FeedViewModel,
    routeForLastRead: String?,
    listState: LazyGridState,
    accountViewModel: AccountViewModel,
    nav: INav,
) {
    val feedState by viewModel.feedState.feedContent.collectAsStateWithLifecycle()
    CrossfadeIfEnabled(
        targetState = feedState,
        animationSpec = tween(durationMillis = 100),
        label = "RenderDiscoverFeed",
        accountViewModel = accountViewModel,
    ) { state ->
        when (state) {
            is FeedState.Empty -> {
                FeedEmpty { viewModel.invalidateData() }
            }
            is FeedState.FeedError -> {
                FeedError(state.errorMessage) { viewModel.invalidateData() }
            }
            is FeedState.Loaded -> {
                GalleryFeedLoaded(
                    state,
                    routeForLastRead,
                    listState,
                    accountViewModel,
                    nav,
                )
            }
            is FeedState.Loading -> {
                LoadingFeed()
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun GalleryFeedLoaded(
    loaded: FeedState.Loaded,
    routeForLastRead: String?,
    listState: LazyGridState,
    accountViewModel: AccountViewModel,
    nav: INav,
) {
    val items by loaded.feed.collectAsStateWithLifecycle()

    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        contentPadding = FeedPadding,
        state = listState,
    ) {
        itemsIndexed(items.list, key = { _, item -> item.idHex }) { _, item ->
            Row(Modifier.fillMaxWidth().animateItemPlacement()) {
                GalleryCardCompose(
                    baseNote = item,
                    routeForLastRead = routeForLastRead,
                    modifier = HalfPadding,
                    accountViewModel = accountViewModel,
                    nav = nav,
                )
            }
        }
    }
}

@Composable
fun GalleryCardCompose(
    baseNote: Note,
    routeForLastRead: String? = null,
    modifier: Modifier = Modifier,
    parentBackgroundColor: MutableState<Color>? = null,
    isHiddenFeed: Boolean = false,
    accountViewModel: AccountViewModel,
    nav: INav,
) {
    WatchNoteEvent(baseNote = baseNote, accountViewModel = accountViewModel, shortPreview = true) {
        CheckHiddenFeedWatchBlockAndReport(
            note = baseNote,
            modifier = modifier,
            ignoreAllBlocksAndReports = isHiddenFeed,
            showHiddenWarning = false,
            accountViewModel = accountViewModel,
            nav = nav,
        ) { canPreview ->
            val (url, sourceEvent) =
                when (val galleryEvent = baseNote.event) {
                    is ProfileGalleryEntryEvent -> Pair(galleryEvent.url(), galleryEvent.fromEvent())
                    is PictureEvent -> Pair(galleryEvent.imetaTags().getOrNull(0)?.url, galleryEvent.id())
                    is VideoEvent -> Pair(galleryEvent.imetaTags().getOrNull(0)?.url, galleryEvent.id())
                    else -> Pair(null, null)
                }

            url?.let { imageUrl ->
                if (sourceEvent != null) {
                    LoadNote(baseNoteHex = sourceEvent, accountViewModel = accountViewModel) { sourceNote ->
                        if (sourceNote != null) {
                            ClickableGalleryCard(
                                galleryNote = baseNote,
                                baseNote = sourceNote,
                                image = imageUrl,
                                modifier = modifier,
                                parentBackgroundColor = parentBackgroundColor,
                                accountViewModel = accountViewModel,
                                nav = nav,
                            )
                        } else {
                            GalleryCard(
                                galleryNote = baseNote,
                                image = imageUrl,
                                modifier = modifier,
                                accountViewModel = accountViewModel,
                                nav = nav,
                            )
                        }
                    }
                } else {
                    GalleryCard(
                        galleryNote = baseNote,
                        image = imageUrl,
                        modifier = modifier,
                        accountViewModel = accountViewModel,
                        nav = nav,
                    )
                }
            }
        }
    }
}

@Composable
fun ClickableGalleryCard(
    galleryNote: Note,
    baseNote: Note,
    image: String,
    modifier: Modifier = Modifier,
    parentBackgroundColor: MutableState<Color>? = null,
    accountViewModel: AccountViewModel,
    nav: INav,
) {
    // baseNote.event?.let { Text(text = it.pubKey()) }
    LongPressToQuickActionGallery(baseNote = galleryNote, accountViewModel = accountViewModel) { showPopup ->
        val backgroundColor =
            calculateBackgroundColor(
                createdAt = baseNote.createdAt(),
                parentBackgroundColor = parentBackgroundColor,
                accountViewModel = accountViewModel,
            )

        ClickableNote(
            baseNote = baseNote,
            backgroundColor = backgroundColor,
            modifier = modifier,
            accountViewModel = accountViewModel,
            showPopup = showPopup,
            nav = nav,
        ) {
            InnerGalleryCardBox(galleryNote, image, accountViewModel, nav)
        }
    }
}

@Composable
fun GalleryCard(
    galleryNote: Note,
    image: String,
    modifier: Modifier = Modifier,
    accountViewModel: AccountViewModel,
    nav: INav,
) {
    LongPressToQuickActionGallery(baseNote = galleryNote, accountViewModel = accountViewModel) { showPopup ->
        Column(modifier = modifier) {
            InnerGalleryCardBox(galleryNote, image, accountViewModel, nav)
        }
    }
}

@Composable
fun InnerGalleryCardBox(
    baseNote: Note,
    image: String,
    accountViewModel: AccountViewModel,
    nav: INav,
) {
    SensitivityWarning(
        note = baseNote,
        accountViewModel = accountViewModel,
    ) {
        RenderGalleryThumb(baseNote, image, accountViewModel, nav)
    }
}

@Immutable
data class GalleryThumb(
    val id: String?,
    val image: String?,
    val title: String?,
)

@Composable
fun RenderGalleryThumb(
    baseNote: Note,
    image: String,
    accountViewModel: AccountViewModel,
    nav: INav,
) {
    val card by
        baseNote
            .live()
            .metadata
            .map {
                GalleryThumb(
                    id = "",
                    image = image,
                    title = "",
                    // noteEvent?.title(),
                )
            }.distinctUntilChanged()
            .observeAsState(
                GalleryThumb(
                    id = "",
                    image = image,
                    title = "",
                ),
            )

    InnerRenderGalleryThumb(card, baseNote, accountViewModel)
}

@Preview
@Composable
fun RenderGalleryThumbPreview() {
    val accountViewModel = mockAccountViewModel()

    Surface(Modifier.size(200.dp)) {
        InnerRenderGalleryThumb(
            card =
                GalleryThumb(
                    id = "",
                    image = null,
                    title = "Like New",
                ),
            note = Note("hex"),
            accountViewModel = accountViewModel,
        )
    }
}

@Composable
fun InnerRenderGalleryThumb(
    card: GalleryThumb,
    note: Note,
    accountViewModel: AccountViewModel,
) {
    val noteEvent = note.event
    val content =
        if (card.image == null) {
            null
        } else if (noteEvent is ProfileGalleryEntryEvent) {
            if (isVideoUrl(card.image)) {
                MediaUrlVideo(
                    url = card.image,
                    description = noteEvent.content,
                    hash = null,
                    blurhash = noteEvent.blurhash(),
                    dim = noteEvent.dimensions(),
                    uri = null,
                    mimeType = noteEvent.mimeType(),
                )
            } else {
                MediaUrlImage(
                    url = card.image,
                    description = noteEvent.content,
                    hash = null, // We don't want to show the hash banner here
                    blurhash = noteEvent.blurhash(),
                    dim = noteEvent.dimensions(),
                    uri = null,
                    mimeType = noteEvent.mimeType(),
                )
            }
        } else if (noteEvent is PictureEvent) {
            val imeta = noteEvent.imetaTags().firstOrNull()
            MediaUrlImage(
                url = card.image,
                description = noteEvent.content,
                hash = null, // We don't want to show the hash banner here
                blurhash = imeta?.blurhash,
                dim = imeta?.dimension,
                uri = null,
                mimeType = imeta?.mimeType,
            )
        } else if (noteEvent is VideoEvent) {
            val imeta = noteEvent.imetaTags().firstOrNull()
            MediaUrlVideo(
                url = card.image,
                description = noteEvent.content,
                hash = null, // We don't want to show the hash banner here
                blurhash = imeta?.blurhash,
                dim = imeta?.dimension,
                uri = null,
                mimeType = imeta?.mimeType,
            )
        } else {
            null
        }

    Box(Modifier.fillMaxWidth().aspectRatio(1f), BottomStart) {
        if (content != null) {
            GalleryContentView(
                content = content,
                roundedCorner = false,
                isFiniteHeight = false,
                accountViewModel = accountViewModel,
            )
        } else {
            DisplayGalleryAuthorBanner(note)
        }
    }
}

@Composable
fun DisplayGalleryAuthorBanner(note: Note) {
    WatchAuthor(note) {
        BannerImage(
            it,
            Modifier
                .fillMaxSize()
                .clip(QuoteBorder),
        )
    }
}
