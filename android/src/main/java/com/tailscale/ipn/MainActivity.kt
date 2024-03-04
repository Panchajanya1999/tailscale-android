// Copyright (c) 2024 Tailscale Inc & AUTHORS All rights reserved.
// Use of this source code is governed by a BSD-style
// license that can be found in the LICENSE file.

package com.tailscale.ipn

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.tailscale.ipn.ui.service.IpnManager
import com.tailscale.ipn.ui.view.MainView
import com.tailscale.ipn.ui.view.PeerDetails
import com.tailscale.ipn.ui.view.Settings
import com.tailscale.ipn.ui.viewModel.MainViewModel


class MainActivity : ComponentActivity() {
    val model = IpnManager.getInstance().model
    private val viewModel = MainViewModel(model)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val navController = rememberNavController()
            NavHost(navController = navController, startDestination = "main") {
                composable("main") {
                    MainView(viewModel = viewModel,
                            onNavigateToSettings = { navController.navigate("settings") },
                            onNavigateToPeerDetails = { navController.navigate("peerDetails") }) }
                composable("settings") { Settings() }
                composable("peerDetails") { PeerDetails() }
            }
        }
    }
}

