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
package com.vitorpamplona.amethyst.ui.screen.loggedIn.chatlist.private

import android.content.Context
import android.util.Log
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vitorpamplona.amethyst.R
import com.vitorpamplona.amethyst.commons.compose.currentWord
import com.vitorpamplona.amethyst.commons.compose.insertUrlAtCursor
import com.vitorpamplona.amethyst.commons.compose.replaceCurrentWord
import com.vitorpamplona.amethyst.commons.richtext.RichTextParser
import com.vitorpamplona.amethyst.model.Account
import com.vitorpamplona.amethyst.model.LocalCache
import com.vitorpamplona.amethyst.model.Note
import com.vitorpamplona.amethyst.model.User
import com.vitorpamplona.amethyst.service.NostrSearchEventOrUserDataSource
import com.vitorpamplona.amethyst.service.uploads.MediaCompressor
import com.vitorpamplona.amethyst.service.uploads.MultiOrchestrator
import com.vitorpamplona.amethyst.service.uploads.UploadOrchestrator
import com.vitorpamplona.amethyst.ui.actions.NewMessageTagger
import com.vitorpamplona.amethyst.ui.actions.UserSuggestionAnchor
import com.vitorpamplona.amethyst.ui.actions.mediaServers.ServerName
import com.vitorpamplona.amethyst.ui.actions.uploads.SelectedMedia
import com.vitorpamplona.amethyst.ui.actions.uploads.SelectedMediaProcessing
import com.vitorpamplona.amethyst.ui.components.Split
import com.vitorpamplona.amethyst.ui.screen.loggedIn.AccountViewModel
import com.vitorpamplona.amethyst.ui.stringRes
import com.vitorpamplona.quartz.nip01Core.core.AddressableEvent
import com.vitorpamplona.quartz.nip01Core.tags.hashtags.hashtags
import com.vitorpamplona.quartz.nip01Core.tags.references.references
import com.vitorpamplona.quartz.nip04Dm.messages.PrivateDmEvent
import com.vitorpamplona.quartz.nip10Notes.content.findHashtags
import com.vitorpamplona.quartz.nip10Notes.content.findNostrUris
import com.vitorpamplona.quartz.nip10Notes.content.findURLs
import com.vitorpamplona.quartz.nip14Subject.subject
import com.vitorpamplona.quartz.nip17Dm.base.BaseDMGroupEvent
import com.vitorpamplona.quartz.nip17Dm.base.ChatroomKey
import com.vitorpamplona.quartz.nip17Dm.base.NIP17Group
import com.vitorpamplona.quartz.nip17Dm.messages.ChatMessageEvent
import com.vitorpamplona.quartz.nip18Reposts.quotes.quotes
import com.vitorpamplona.quartz.nip19Bech32.toNpub
import com.vitorpamplona.quartz.nip30CustomEmoji.CustomEmoji
import com.vitorpamplona.quartz.nip30CustomEmoji.EmojiUrlTag
import com.vitorpamplona.quartz.nip30CustomEmoji.emojis
import com.vitorpamplona.quartz.nip36SensitiveContent.isSensitive
import com.vitorpamplona.quartz.nip37Drafts.DraftEvent
import com.vitorpamplona.quartz.nip57Zaps.splits.ZapSplitSetup
import com.vitorpamplona.quartz.nip57Zaps.splits.zapSplitSetup
import com.vitorpamplona.quartz.nip57Zaps.zapraiser.zapraiserAmount
import com.vitorpamplona.quartz.nip65RelayList.AdvertisedRelayListEvent
import com.vitorpamplona.quartz.nip92IMeta.imetas
import com.vitorpamplona.quartz.utils.Hex
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

@Stable
open class ChatNewMessageViewModel : ViewModel() {
    var draftTag: String by mutableStateOf(UUID.randomUUID().toString())

    var accountViewModel: AccountViewModel? = null
    var account: Account? = null
    var room: ChatroomKey? = null

    var requiresNIP17: Boolean = false

    val replyTo = mutableStateOf<Note?>(null)

    val iMetaAttachments = IMetaAttachments()

    var message by mutableStateOf(TextFieldValue(""))
    var urlPreview by mutableStateOf<String?>(null)
    var isUploadingImage by mutableStateOf(false)

    val userSuggestions = UserSuggestions()
    var userSuggestionsMainMessage: UserSuggestionAnchor? = null

    val emojiSearch: MutableStateFlow<String> = MutableStateFlow("")
    val emojiSuggestions: StateFlow<List<Account.EmojiMedia>> by lazy {
        account!!
            .myEmojis
            .combine(emojiSearch) { list, search ->
                if (search.length == 1) {
                    list
                } else if (search.isNotEmpty()) {
                    val code = search.removePrefix(":")
                    list.filter { it.code.startsWith(code) }
                } else {
                    emptyList()
                }
            }.flowOn(Dispatchers.Default)
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                emptyList(),
            )
    }

    var toUsers by mutableStateOf(TextFieldValue(""))
    var subject by mutableStateOf(TextFieldValue(""))

    // Images and Videos
    var multiOrchestrator by mutableStateOf<MultiOrchestrator?>(null)

    // Invoices
    var canAddInvoice by mutableStateOf(false)
    var wantsInvoice by mutableStateOf(false)

    // Forward Zap to
    var wantsForwardZapTo by mutableStateOf(false)
    var forwardZapTo by mutableStateOf<Split<User>>(Split())
    var forwardZapToEditting by mutableStateOf(TextFieldValue(""))

    // NSFW, Sensitive
    var wantsToMarkAsSensitive by mutableStateOf(false)

    // ZapRaiser
    var canAddZapRaiser by mutableStateOf(false)
    var wantsZapraiser by mutableStateOf(false)
    var zapRaiserAmount by mutableStateOf<Long?>(null)

    // NIP17 Wrapped DMs / Group messages
    var nip17 by mutableStateOf(false)

    val draftTextChanges = Channel<String>(Channel.CONFLATED)

    fun lnAddress(): String? = account?.userProfile()?.info?.lnAddress()

    fun hasLnAddress(): Boolean = account?.userProfile()?.info?.lnAddress() != null

    fun user(): User? = account?.userProfile()

    open fun init(accountVM: AccountViewModel) {
        this.accountViewModel = accountVM
        this.account = accountVM.account
        this.canAddInvoice = accountVM.userProfile().info?.lnAddress() != null
        this.canAddZapRaiser = accountVM.userProfile().info?.lnAddress() != null
    }

    open fun load(room: ChatroomKey) {
        this.room = room
        this.requiresNIP17 = room.users.size > 1
        if (this.requiresNIP17) {
            this.nip17 = true
        }
    }

    open fun reply(replyNote: Note) {
        replyTo.value = replyNote
    }

    open fun quote(quote: Note) {
        message = TextFieldValue(message.text + "\nnostr:${quote.toNEvent()}")
        urlPreview = findUrlInMessage()

        // creates a split with that author.
        val accountViewModel = accountViewModel ?: return
        val quotedAuthor = quote.author ?: return

        if (quotedAuthor.pubkeyHex != accountViewModel.userProfile().pubkeyHex) {
            if (forwardZapTo.items.none { it.key.pubkeyHex == quotedAuthor.pubkeyHex }) {
                forwardZapTo.addItem(quotedAuthor)
            }
            if (forwardZapTo.items.none { it.key.pubkeyHex == accountViewModel.userProfile().pubkeyHex }) {
                forwardZapTo.addItem(accountViewModel.userProfile())
            }

            val pos = forwardZapTo.items.indexOfFirst { it.key.pubkeyHex == quotedAuthor.pubkeyHex }
            forwardZapTo.updatePercentage(pos, 0.9f)

            wantsForwardZapTo = true
        }
    }

    open fun editFromDraft(draft: Note) {
        val noteEvent = draft.event
        val noteAuthor = draft.author

        if (noteEvent is DraftEvent && noteAuthor != null) {
            viewModelScope.launch(Dispatchers.IO) {
                accountViewModel?.createTempDraftNote(noteEvent) { innerNote ->
                    if (innerNote != null) {
                        val oldTag = (draft.event as? AddressableEvent)?.dTag()
                        if (oldTag != null) {
                            draftTag = oldTag
                        }
                        loadFromDraft(innerNote)
                    }
                }
            }
        }
    }

    private fun loadFromDraft(draft: Note) {
        Log.d("draft", draft.event!!.toJson())

        val draftEvent = draft.event ?: return
        val accountViewModel = accountViewModel ?: return

        val localfowardZapTo = draftEvent.tags.zapSplitSetup()
        val totalWeight = localfowardZapTo.sumOf { it.weight }
        forwardZapTo = Split()
        localfowardZapTo.forEach {
            if (it is ZapSplitSetup) {
                val user = LocalCache.getOrCreateUser(it.pubKeyHex)
                forwardZapTo.addItem(user, (it.weight / totalWeight).toFloat())
            }
            // don't support edditing old-style splits.
        }
        forwardZapToEditting = TextFieldValue("")
        wantsForwardZapTo = localfowardZapTo.isNotEmpty()

        wantsToMarkAsSensitive = draftEvent.isSensitive()

        val zapraiser = draftEvent.zapraiserAmount()
        wantsZapraiser = zapraiser != null
        zapRaiserAmount = null
        if (zapraiser != null) {
            zapRaiserAmount = zapraiser
        }

        if (forwardZapTo.items.isNotEmpty()) {
            wantsForwardZapTo = true
        }

        draftEvent.subject()?.let {
            subject = TextFieldValue()
        }

        if (draftEvent is NIP17Group) {
            toUsers =
                TextFieldValue(
                    draftEvent.groupMembers().mapNotNull { runCatching { Hex.decode(it).toNpub() }.getOrNull() }.joinToString(", ") { "@$it" },
                )
        } else if (draftEvent is PrivateDmEvent) {
            val recepientNpub = draftEvent.verifiedRecipientPubKey()?.let { Hex.decode(it).toNpub() }
            toUsers = TextFieldValue("@$recepientNpub")
        }

        message =
            if (draftEvent is PrivateDmEvent) {
                TextFieldValue(draftEvent.cachedContentFor(accountViewModel.account.signer) ?: "")
            } else {
                TextFieldValue(draftEvent.content)
            }

        requiresNIP17 = draftEvent is NIP17Group
        nip17 = draftEvent is NIP17Group

        urlPreview = findUrlInMessage()
    }

    fun sendPost() {
        viewModelScope.launch(Dispatchers.IO) {
            innerSendPost(null)
            accountViewModel?.deleteDraft(draftTag)
            cancel()
        }
    }

    suspend fun sendPostSync() {
        innerSendPost(null)
        accountViewModel?.deleteDraft(draftTag)
        cancel()
    }

    fun sendDraft() {
        viewModelScope.launch(Dispatchers.IO) {
            sendDraftSync()
        }
    }

    suspend fun sendDraftSync() {
        if (message.text.isBlank()) {
            account?.deleteDraft(draftTag)
        } else {
            innerSendPost(draftTag)
        }
    }

    private fun innerSendPost(dTag: String?) {
        val room = room ?: return
        val accountViewModel = accountViewModel ?: return

        val urls = findURLs(message.text)
        val usedAttachments = iMetaAttachments.filterIsIn(urls.toSet())
        val emojis = findEmoji(message.text, accountViewModel.account.myEmojis.value)

        val message = message.text

        if (nip17 || room.users.size > 1 || replyTo.value?.event is NIP17Group) {
            val replyHint = replyTo.value?.toEventHint<BaseDMGroupEvent>()

            val template =
                if (replyHint == null) {
                    ChatMessageEvent.build(message, room.users.map { LocalCache.getOrCreateUser(it).toPTag() }) {
                        hashtags(findHashtags(message))
                        references(findURLs(message))
                        quotes(findNostrUris(message))

                        emojis(emojis)
                        imetas(usedAttachments)
                    }
                } else {
                    ChatMessageEvent.reply(message, replyHint) {
                        hashtags(findHashtags(message))
                        references(findURLs(message))
                        quotes(findNostrUris(message))

                        emojis(emojis)
                        imetas(usedAttachments)
                    }
                }

            accountViewModel.account.sendNIP17PrivateMessage(template, dTag)
        } else {
            accountViewModel.account.sendPrivateMessage(
                message = message,
                toUser = room.users.first().let { LocalCache.getOrCreateUser(it).toPTag() },
                replyingTo = replyTo.value,
                contentWarningReason = null,
                imetas = usedAttachments,
                draftTag = dTag,
            )
        }
    }

    fun findEmoji(
        message: String,
        myEmojiSet: List<Account.EmojiMedia>?,
    ): List<EmojiUrlTag> {
        if (myEmojiSet == null) return emptyList()
        return CustomEmoji.findAllEmojiCodes(message).mapNotNull { possibleEmoji ->
            myEmojiSet.firstOrNull { it.code == possibleEmoji }?.let { EmojiUrlTag(it.code, it.url.url) }
        }
    }

    fun upload(
        alt: String?,
        contentWarningReason: String?,
        mediaQuality: Int,
        isPrivate: Boolean = false,
        server: ServerName,
        onError: (title: String, message: String) -> Unit,
        context: Context,
    ) {
        viewModelScope.launch(Dispatchers.Default) {
            val myAccount = account ?: return@launch

            val myMultiOrchestrator = multiOrchestrator ?: return@launch

            isUploadingImage = true

            val results =
                myMultiOrchestrator.upload(
                    viewModelScope,
                    alt,
                    contentWarningReason,
                    MediaCompressor.intToCompressorQuality(mediaQuality),
                    server,
                    myAccount,
                    context,
                )

            if (results.allGood) {
                results.successful.forEach {
                    if (it.result is UploadOrchestrator.OrchestratorResult.ServerResult) {
                        iMetaAttachments.add(it.result, alt, contentWarningReason)

                        message = message.insertUrlAtCursor(it.result.url)
                        urlPreview = findUrlInMessage()
                    }
                }

                multiOrchestrator = null
            } else {
                val errorMessages = results.errors.map { stringRes(context, it.errorResource, *it.params) }.distinct()

                onError(stringRes(context, R.string.failed_to_upload_media_no_details), errorMessages.joinToString(".\n"))
            }

            isUploadingImage = false
        }
    }

    open fun cancel() {
        message = TextFieldValue("")
        toUsers = TextFieldValue("")
        subject = TextFieldValue("")

        replyTo.value = null

        multiOrchestrator = null
        urlPreview = null
        isUploadingImage = false

        wantsInvoice = false
        wantsZapraiser = false
        zapRaiserAmount = null

        wantsForwardZapTo = false
        wantsToMarkAsSensitive = false

        forwardZapTo = Split()
        forwardZapToEditting = TextFieldValue("")

        userSuggestions.reset()
        userSuggestionsMainMessage = null

        if (emojiSearch.value.isNotEmpty()) {
            emojiSearch.tryEmit("")
        }

        draftTag = UUID.randomUUID().toString()

        NostrSearchEventOrUserDataSource.clear()
    }

    fun deleteDraft() {
        viewModelScope.launch(Dispatchers.IO) {
            accountViewModel?.deleteDraft(draftTag)
        }
    }

    fun deleteMediaToUpload(selected: SelectedMediaProcessing) {
        this.multiOrchestrator?.remove(selected)
    }

    open fun findUrlInMessage(): String? = RichTextParser().parseValidUrls(message.text).firstOrNull()

    private fun saveDraft() {
        draftTextChanges.trySend("")
    }

    open fun addToMessage(it: String) {
        updateMessage(TextFieldValue(message.text + " " + it))
    }

    open fun updateMessage(newMessage: TextFieldValue) {
        message = newMessage
        urlPreview = findUrlInMessage()

        if (newMessage.selection.collapsed) {
            val lastWord = newMessage.currentWord()

            userSuggestionsMainMessage = UserSuggestionAnchor.MAIN_MESSAGE

            accountViewModel?.let {
                userSuggestions.processCurrentWord(lastWord, it)
            }

            if (lastWord.startsWith(":")) {
                emojiSearch.tryEmit(lastWord)
            } else {
                if (emojiSearch.value.isNotBlank()) {
                    emojiSearch.tryEmit("")
                }
            }
        }

        saveDraft()
    }

    open fun updateToUsers(newToUsersValue: TextFieldValue) {
        toUsers = newToUsersValue

        if (newToUsersValue.selection.collapsed) {
            val lastWord = newToUsersValue.currentWord()
            userSuggestionsMainMessage = UserSuggestionAnchor.TO_USERS

            accountViewModel?.let {
                userSuggestions.processCurrentWord(lastWord, it)
            }
        }
        saveDraft()
    }

    open fun updateSubject(it: TextFieldValue) {
        subject = it
        saveDraft()
    }

    open fun updateZapForwardTo(newZapForwardTo: TextFieldValue) {
        forwardZapToEditting = newZapForwardTo
        if (newZapForwardTo.selection.collapsed) {
            val lastWord = newZapForwardTo.text
            userSuggestionsMainMessage = UserSuggestionAnchor.FORWARD_ZAPS
            accountViewModel?.let {
                userSuggestions.processCurrentWord(lastWord, it)
            }
        }
    }

    open fun autocompleteWithUser(item: User) {
        if (userSuggestionsMainMessage == UserSuggestionAnchor.MAIN_MESSAGE) {
            val lastWord = message.currentWord()
            message = userSuggestions.replaceCurrentWord(message, lastWord, item)
        } else if (userSuggestionsMainMessage == UserSuggestionAnchor.FORWARD_ZAPS) {
            forwardZapTo.addItem(item)
            forwardZapToEditting = TextFieldValue("")
        } else if (userSuggestionsMainMessage == UserSuggestionAnchor.TO_USERS) {
            val lastWord = toUsers.currentWord()
            toUsers = userSuggestions.replaceCurrentWord(toUsers, lastWord, item)

            val relayList = (LocalCache.getAddressableNoteIfExists(AdvertisedRelayListEvent.createAddressTag(item.pubkeyHex))?.event as? AdvertisedRelayListEvent)?.readRelays()
            nip17 = relayList != null
        }

        userSuggestionsMainMessage = null
        userSuggestions.reset()

        saveDraft()
    }

    open fun autocompleteWithEmoji(item: Account.EmojiMedia) {
        val wordToInsert = ":${item.code}:"
        message = message.replaceCurrentWord(wordToInsert)

        emojiSearch.tryEmit("")

        saveDraft()
    }

    open fun autocompleteWithEmojiUrl(item: Account.EmojiMedia) {
        val wordToInsert = item.url.url + " "

        viewModelScope.launch(Dispatchers.IO) {
            iMetaAttachments.downloadAndPrepare(
                item.url.url,
                accountViewModel?.account?.shouldUseTorForImageDownload() ?: false,
            )
        }

        message = message.replaceCurrentWord(wordToInsert)

        emojiSearch.tryEmit("")

        urlPreview = findUrlInMessage()

        saveDraft()
    }

    private fun newStateMapPollOptions(): SnapshotStateMap<Int, String> = mutableStateMapOf(Pair(0, ""), Pair(1, ""))

    fun canPost(): Boolean =
        message.text.isNotBlank() &&
            !isUploadingImage &&
            !wantsInvoice &&
            (!wantsZapraiser || zapRaiserAmount != null) &&
            (toUsers.text.isNotBlank()) &&
            multiOrchestrator == null

    fun insertAtCursor(newElement: String) {
        message = message.insertUrlAtCursor(newElement)
    }

    fun selectImage(uris: ImmutableList<SelectedMedia>) {
        multiOrchestrator = MultiOrchestrator(uris)
    }

    override fun onCleared() {
        super.onCleared()
        Log.d("Init", "OnCleared: ${this.javaClass.simpleName}")
    }

    fun toggleNIP04And24() {
        if (requiresNIP17) {
            nip17 = true
        } else {
            nip17 = !nip17
        }
        if (message.text.isNotBlank()) {
            saveDraft()
        }
    }

    fun updateZapPercentage(
        index: Int,
        sliderValue: Float,
    ) {
        forwardZapTo.updatePercentage(index, sliderValue)
    }

    fun updateZapFromText() {
        viewModelScope.launch(Dispatchers.Default) {
            val tagger = NewMessageTagger(message.text, emptyList(), emptyList(), null, accountViewModel!!)
            tagger.run()
            tagger.pTags?.forEach { taggedUser ->
                if (!forwardZapTo.items.any { it.key == taggedUser }) {
                    forwardZapTo.addItem(taggedUser)
                }
            }
        }
    }

    fun updateZapRaiserAmount(newAmount: Long?) {
        zapRaiserAmount = newAmount
        saveDraft()
    }

    fun toggleMarkAsSensitive() {
        wantsToMarkAsSensitive = !wantsToMarkAsSensitive
        saveDraft()
    }
}
