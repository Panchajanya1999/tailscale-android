// Copyright (c) Tailscale Inc & AUTHORS
// SPDX-License-Identifier: BSD-3-Clause


package com.tailscale.ipn.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tailscale.ipn.R
import com.tailscale.ipn.ui.util.Header
import com.tailscale.ipn.ui.util.defaultPaddingModifier
import com.tailscale.ipn.ui.util.settingsRowModifier
import com.tailscale.ipn.ui.viewModel.ComposableStringFormatter
import com.tailscale.ipn.ui.viewModel.Setting
import com.tailscale.ipn.ui.viewModel.SettingType
import com.tailscale.ipn.ui.viewModel.UserSwitcherViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserSwitcherView(viewModel: UserSwitcherViewModel) {
    val showDialog = remember { mutableStateOf<ErrorDialogType?>(null) }

    Surface(
            modifier = Modifier.fillMaxHeight(),
            color = MaterialTheme.colorScheme.background
    ) {
        Column(
                modifier = defaultPaddingModifier().fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            showDialog.value?.let {
                ErrorDialog(type = it,
                        action = { showDialog.value = null })
            }

            Header(title = R.string.accounts)

            Column(modifier = settingsRowModifier()) {
                val users = viewModel.profiles.collectAsState()
                val currentUser = viewModel.currentProfile.collectAsState()

                val nextUserId = remember { mutableStateOf<String?>(null) }

                users.value?.forEach { user ->
                    if (user.ID == currentUser.value?.ID) {
                        UserView(profile = user, actionState = UserActionState.CURRENT, onClick = {})
                    } else {
                        val state = if (user.ID == nextUserId.value) UserActionState.SWITCHING else UserActionState.NONE
                        UserView(profile = user, actionState = state, onClick = {
                            nextUserId.value = user.ID
                            viewModel.ipnManager.switchProfile(user) {
                                if (it.isFailure) {
                                    showDialog.value = ErrorDialogType.LOGOUT_FAILED
                                }
                            }
                        })
                    }
                }

                SettingsNavRow(setting = Setting(
                        title = ComposableStringFormatter(R.string.add_account),
                        type = SettingType.NAV,
                        onClick = {
                            viewModel.ipnManager.addProfile()
                        }))
            }

            Spacer(modifier = Modifier.height(8.dp))

            Column(modifier = settingsRowModifier()) {
                SettingsNavRow(setting = Setting(
                        title = ComposableStringFormatter(R.string.reauthenticate),
                        type = SettingType.NAV,
                        onClick = {
                            viewModel.ipnManager.login()
                        }))

                SettingsTextRow(setting = Setting(
                        title = ComposableStringFormatter(R.string.log_out),
                        destructive = true,
                        type = SettingType.TEXT,
                        onClick = {
                            viewModel.ipnManager.logout() {
                                if (it.isFailure) {
                                    showDialog.value = ErrorDialogType.LOGOUT_FAILED
                                }
                            }
                        }))
            }
        }
    }
}
