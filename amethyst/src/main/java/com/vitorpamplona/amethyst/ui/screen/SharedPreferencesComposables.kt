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
package com.vitorpamplona.amethyst.ui.screen

import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.adaptive.calculateDisplayFeatures
import com.vitorpamplona.amethyst.Amethyst
import com.vitorpamplona.amethyst.service.connectivity.ConnectivityStatus
import com.vitorpamplona.amethyst.ui.components.getActivity

@Composable
fun prepareSharedViewModel(): SharedPreferencesViewModel {
    val sharedPreferencesViewModel: SharedPreferencesViewModel = viewModel()

    LaunchedEffect(key1 = sharedPreferencesViewModel) {
        sharedPreferencesViewModel.init()
    }

    MonitorDisplaySize(sharedPreferencesViewModel)
    ManageConnectivity(sharedPreferencesViewModel)

    return sharedPreferencesViewModel
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun MonitorDisplaySize(sharedPreferencesViewModel: SharedPreferencesViewModel) {
    val act = LocalContext.current.getActivity()

    val displayFeatures = calculateDisplayFeatures(act)
    val windowSizeClass = calculateWindowSizeClass(act)

    LaunchedEffect(sharedPreferencesViewModel, displayFeatures, windowSizeClass) {
        sharedPreferencesViewModel.updateDisplaySettings(windowSizeClass, displayFeatures)
    }
}

@Composable
fun ManageConnectivity(sharedPreferencesViewModel: SharedPreferencesViewModel) {
    val status =
        Amethyst.instance.connManager.status
            .collectAsStateWithLifecycle()

    (status.value as? ConnectivityStatus.Active)?.let {
        sharedPreferencesViewModel.updateNetworkState(it.networkId)
        sharedPreferencesViewModel.updateConnectivityStatusState(it.isMobile)
    }
}
