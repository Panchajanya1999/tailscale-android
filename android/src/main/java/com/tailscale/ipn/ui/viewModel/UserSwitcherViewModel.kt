// Copyright (c) Tailscale Inc & AUTHORS
// SPDX-License-Identifier: BSD-3-Clause


package com.tailscale.ipn.ui.viewModel

import androidx.lifecycle.ViewModel
import com.tailscale.ipn.ui.service.IpnManager


class UserSwitcherViewModel(val ipnManager: IpnManager) : ViewModel() {
    val model = ipnManager.model

    val profiles = model.loginProfiles
    val currentProfile = model.loggedInUser
}
