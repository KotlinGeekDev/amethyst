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
package com.vitorpamplona.amethyst

import android.app.ActivityManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.os.Debug
import android.util.Log
import androidx.core.content.getSystemService
import com.vitorpamplona.amethyst.model.LocalCache
import com.vitorpamplona.amethyst.service.NostrAccountDataSource
import com.vitorpamplona.amethyst.service.NostrChannelDataSource
import com.vitorpamplona.amethyst.service.NostrChatroomDataSource
import com.vitorpamplona.amethyst.service.NostrChatroomListDataSource
import com.vitorpamplona.amethyst.service.NostrCommunityDataSource
import com.vitorpamplona.amethyst.service.NostrDiscoveryDataSource
import com.vitorpamplona.amethyst.service.NostrGeohashDataSource
import com.vitorpamplona.amethyst.service.NostrHashtagDataSource
import com.vitorpamplona.amethyst.service.NostrHomeDataSource
import com.vitorpamplona.amethyst.service.NostrSearchEventOrUserDataSource
import com.vitorpamplona.amethyst.service.NostrSingleChannelDataSource
import com.vitorpamplona.amethyst.service.NostrSingleEventDataSource
import com.vitorpamplona.amethyst.service.NostrSingleUserDataSource
import com.vitorpamplona.amethyst.service.NostrThreadDataSource
import com.vitorpamplona.amethyst.service.NostrUserProfileDataSource
import com.vitorpamplona.amethyst.service.NostrVideoDataSource
import com.vitorpamplona.ammolite.relays.Client
import com.vitorpamplona.ammolite.relays.RelayPool

fun debugState(context: Context) {
    Client
        .allSubscriptions()
        .forEach { Log.d("STATE DUMP", "${it.key} ${it.value.joinToString { it.filter.toDebugJson() }}") }

    NostrAccountDataSource.printCounter()
    NostrChannelDataSource.printCounter()
    NostrChatroomDataSource.printCounter()
    NostrChatroomListDataSource.printCounter()
    NostrCommunityDataSource.printCounter()
    NostrDiscoveryDataSource.printCounter()
    NostrHashtagDataSource.printCounter()
    NostrGeohashDataSource.printCounter()
    NostrHomeDataSource.printCounter()
    NostrSearchEventOrUserDataSource.printCounter()
    NostrSingleChannelDataSource.printCounter()
    NostrSingleEventDataSource.printCounter()
    NostrSingleUserDataSource.printCounter()
    NostrThreadDataSource.printCounter()
    NostrUserProfileDataSource.printCounter()
    NostrVideoDataSource.printCounter()

    val totalMemoryMb = Runtime.getRuntime().totalMemory() / (1024 * 1024)
    val freeMemoryMb = Runtime.getRuntime().freeMemory() / (1024 * 1024)
    val maxMemoryMb = Runtime.getRuntime().maxMemory() / (1024 * 1024)

    val jvmHeapAllocatedMb = totalMemoryMb - freeMemoryMb

    Log.d("STATE DUMP", "Total Heap Allocated: " + jvmHeapAllocatedMb + "/" + maxMemoryMb + " MB")

    val nativeHeap = Debug.getNativeHeapAllocatedSize() / (1024 * 1024)
    val maxNative = Debug.getNativeHeapSize() / (1024 * 1024)

    Log.d("STATE DUMP", "Total Native Heap Allocated: " + nativeHeap + "/" + maxNative + " MB")

    val activityManager: ActivityManager? = context.getSystemService()
    if (activityManager != null) {
        val isLargeHeap = (context.applicationInfo.flags and ApplicationInfo.FLAG_LARGE_HEAP) != 0
        val memClass = if (isLargeHeap) activityManager.largeMemoryClass else activityManager.memoryClass

        Log.d("STATE DUMP", "Memory Class " + memClass + " MB (largeHeap $isLargeHeap)")
    }

    Log.d("STATE DUMP", "Connected Relays: " + RelayPool.connectedRelays())

    Log.d(
        "STATE DUMP",
        "Image Disk Cache ${(Amethyst.instance.coilCache.size) / (1024 * 1024)}/${(Amethyst.instance.coilCache.maxSize) / (1024 * 1024)} MB",
    )
    Log.d(
        "STATE DUMP",
        "Image Memory Cache ${(Amethyst.instance.memoryCache.size) / (1024 * 1024)}/${(Amethyst.instance.memoryCache.size) / (1024 * 1024)} MB",
    )

    Log.d(
        "STATE DUMP",
        "Notes: " +
            LocalCache.notes.filter { _, it -> it.liveSet != null }.size +
            " / " +
            LocalCache.notes.filter { _, it -> it.flowSet != null }.size +
            " / " +
            LocalCache.notes.filter { _, it -> it.event != null }.size +
            " / " +
            LocalCache.notes.size(),
    )
    Log.d(
        "STATE DUMP",
        "Addressables: " +
            LocalCache.addressables.filter { _, it -> it.liveSet != null }.size +
            " / " +
            LocalCache.addressables.filter { _, it -> it.flowSet != null }.size +
            " / " +
            LocalCache.addressables.filter { _, it -> it.event != null }.size +
            " / " +
            LocalCache.addressables.size(),
    )
    Log.d(
        "STATE DUMP",
        "Users: " +
            LocalCache.users.filter { _, it -> it.liveSet != null }.size +
            " / " +
            LocalCache.users.filter { _, it -> it.flowSet != null }.size +
            " / " +
            LocalCache.users.filter { _, it -> it.latestMetadata != null }.size +
            " / " +
            LocalCache.users.size(),
    )
    Log.d(
        "STATE DUMP",
        "Deletion Events: " +
            LocalCache.deletionIndex.size(),
    )
    Log.d(
        "STATE DUMP",
        "Observable Events: " +
            LocalCache.observablesByKindAndETag.size +
            " / " +
            LocalCache.observablesByKindAndAuthor.size,
    )

    Log.d(
        "STATE DUMP",
        "Spam: " +
            LocalCache.antiSpam.spamMessages.size() + " / " + LocalCache.antiSpam.recentMessages.size(),
    )

    Log.d(
        "STATE DUMP",
        "Memory used by Events: " +
            LocalCache.notes.sumOfLong { _, note -> note.event?.countMemory() ?: 0L } / (1024 * 1024) +
            " MB",
    )

    val qttNotes = LocalCache.notes.countByGroup { _, it -> it.event?.kind() }
    val qttAddressables = LocalCache.addressables.countByGroup { _, it -> it.event?.kind() }

    val bytesNotes =
        LocalCache.notes
            .sumByGroup(groupMap = { _, it -> it.event?.kind() }, sumOf = { _, it -> it.event?.countMemory() ?: 0L })
    val bytesAddressables =
        LocalCache.addressables
            .sumByGroup(groupMap = { _, it -> it.event?.kind() }, sumOf = { _, it -> it.event?.countMemory() ?: 0L })

    qttNotes.toList().sortedByDescending { bytesNotes.get(it.first) }.forEach { (kind, qtt) ->
        Log.d("STATE DUMP", "Kind ${kind.toString().padStart(5,' ')}:\t${qtt.toString().padStart(6,' ')} elements\t${bytesNotes.get(kind)?.div((1024 * 1024))}MB ")
    }
    qttAddressables.toList().sortedByDescending { bytesNotes.get(it.first) }.forEach { (kind, qtt) ->
        Log.d("STATE DUMP", "Kind ${kind.toString().padStart(5,' ')}:\t${qtt.toString().padStart(6,' ')} elements\t${bytesAddressables.get(kind)?.div((1024 * 1024))}MB ")
    }
}
