// Copyright (c) 2024 Tailscale Inc & AUTHORS All rights reserved.
// Use of this source code is governed by a BSD-style
// license that can be found in the LICENSE file.

package com.tailscale.ipn.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tailscale.ipn.ui.model.Ipn
import com.tailscale.ipn.ui.model.IpnLocal
import com.tailscale.ipn.ui.model.Tailcfg
import com.tailscale.ipn.ui.viewModel.MainViewModel
import kotlinx.coroutines.flow.StateFlow


@Composable
fun MainView(viewModel: MainViewModel, onNavigateToSettings: () -> Unit, onNavigateToPeerDetails: () -> Unit) {
    Surface(color = MaterialTheme.colorScheme.primaryContainer) {
        Column(
                modifier = Modifier.fillMaxWidth(fraction = 1.0f),
                verticalArrangement = Arrangement.Center
        ) {
            val state = viewModel.ipnState.collectAsState(initial = Ipn.State.NoState)
            val user = viewModel.loggedInUser.collectAsState(initial = null)

            Row(modifier = Modifier
                    .padding(6.dp)
                    .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically) {
                val isOn = viewModel.vpnToggleState.collectAsState(initial = false)

                Switch(onCheckedChange = { viewModel.toggleVpn() }, checked = isOn.value)
                StateDisplay(viewModel.stateStr, viewModel.userName)
                Spacer(modifier = Modifier)
                SettingsButton(user.value, { onNavigateToSettings() })
            }

            when (state.value) {
                Ipn.State.Running -> PeerList(peers = viewModel.peers, onNavigateToPeerDetails = onNavigateToPeerDetails)
                Ipn.State.Starting -> StartingView()
                else ->
                    ConnectView(
                            user.value,
                            { viewModel.toggleVpn() },
                            { viewModel.login() }
                    )
            }

        }
    }
}

@Composable
fun StateDisplay(state: StateFlow<String>, tailnet: String) {
    val stateStr = state.collectAsState(initial = "--")

    Column(modifier = Modifier.padding(6.dp)) {
        Text(text = "${tailnet}", style = MaterialTheme.typography.titleMedium)
        Text(text = "${stateStr.value}", style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun SettingsButton(user: IpnLocal.LoginProfile?, action: () -> Unit) {
    IconButton(
            modifier = Modifier.size(24.dp),
            onClick = { action() }
    ) {
        Icon(
                Icons.Outlined.Settings,
                null,
        )
    }
}

@Composable
fun StartingView() {
    // (jonathan) TODO: On iOS this is the game-of-life Tailscale animation.  It would
    // be nice to do the same thing here.
    Column(
            modifier =
            Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.secondaryContainer),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
    ) { Text(text = "Starting...", style = MaterialTheme.typography.titleMedium) }
}

@Composable
fun ConnectView(user: IpnLocal.LoginProfile?, connectAction: () -> Unit, loginAction: () -> Unit) {
    Column(
            modifier =
            Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.secondaryContainer),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Not Connected", style = MaterialTheme.typography.titleMedium)
        if (user != null) {
            val tailnetName = user.NetworkProfile?.DomainName ?: ""
            Text(
                    "Connect to your ${tailnetName} tailnet",
                    style = MaterialTheme.typography.bodyMedium
            )
            Button(onClick = connectAction) { Text(text = "Connect") }
        } else {
            Button(onClick = loginAction) { Text(text = "Log In") }
        }
    }
}

@Composable
fun PeerList(peers: StateFlow<List<Tailcfg.Node>>, onNavigateToPeerDetails: () -> Unit) {
    val peerList = peers.collectAsState(initial = emptyList<Tailcfg.Node>())

    Column(
            modifier =
            Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.secondaryContainer),
    ) {
        peerList.value.forEach { peer ->
            ListItem(
                    headlineContent = {
                        Text(text = peer.ComputedName, style = MaterialTheme.typography.titleMedium)
                    },
                    supportingContent = {
                        Text(
                                text = peer.Addresses?.first() ?: "",
                                style = MaterialTheme.typography.bodyMedium
                        )
                    }
            )
        }
    }
}