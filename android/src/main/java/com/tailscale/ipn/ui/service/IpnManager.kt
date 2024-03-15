// Copyright (c) Tailscale Inc & AUTHORS
// SPDX-License-Identifier: BSD-3-Clause

package com.tailscale.ipn.ui.service


import android.content.Intent
import com.tailscale.ipn.App
import com.tailscale.ipn.IPNReceiver
import com.tailscale.ipn.mdm.MDMSettings
import com.tailscale.ipn.ui.localapi.LocalApiClient
import com.tailscale.ipn.ui.model.Ipn
import com.tailscale.ipn.ui.model.IpnLocal
import com.tailscale.ipn.ui.notifier.Notifier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

typealias PrefChangeCallback = (Result<Boolean>) -> Unit

// Abstracts the actions that can be taken by the UI so that the concept of an IPNManager
// itself is hidden from the viewModel implementations.
interface IpnActions {
    fun startVPN()
    fun stopVPN()
    fun login(completionHandler: (Result<String>) -> Unit = {})
    fun logout(completionHandler: (Result<String>) -> Unit = {})
    fun deleteProfile(profile: IpnLocal.LoginProfile, completionHandler: (Result<String>) -> Unit = {})
    fun addProfile(completionHandler: (Result<String>) -> Unit = {})
    fun switchProfile(profile: IpnLocal.LoginProfile, completionHandler: (Result<String>) -> Unit = {})
    fun updatePrefs(prefs: Ipn.MaskedPrefs, callback: PrefChangeCallback)
}

class IpnManager(val scope: CoroutineScope) : IpnActions {
    private var notifier = Notifier()

    var apiClient = LocalApiClient(scope)
    var mdmSettings = MDMSettings()
    val model = IpnModel(notifier, apiClient, scope)

    override fun startVPN() {
        val context = App.getApplication().applicationContext
        val intent = Intent(context, IPNReceiver::class.java)
        intent.action = IPNReceiver.INTENT_CONNECT_VPN
        context.sendBroadcast(intent)
    }

    override fun stopVPN() {
        val context = App.getApplication().applicationContext
        val intent = Intent(context, IPNReceiver::class.java)
        intent.action = IPNReceiver.INTENT_DISCONNECT_VPN
        context.sendBroadcast(intent)
    }


    override fun login(completionHandler: (Result<String>) -> Unit) {
        apiClient.startLoginInteractive(completionHandler)
    }

    override fun logout(completionHandler: (Result<String>) -> Unit) {
        apiClient.logout {
            if (it.isSuccess) {
                model.loggedInUser.set(null)
            }
            completionHandler(it)
        }
    }

    override fun switchProfile(profile: IpnLocal.LoginProfile, completionHandler: (Result<String>) -> Unit) {
        apiClient.switchProfile(profile) {
            scope.launch { model.loadUserProfiles() }
            completionHandler(it)
        }
    }

    override fun addProfile(completionHandler: (Result<String>) -> Unit) {
        apiClient.addProfile {
            if (it.isSuccess) {
                login()
            }
            completionHandler(it)
        }
    }

    override fun deleteProfile(profile: IpnLocal.LoginProfile, completionHandler: (Result<String>) -> Unit) {
        apiClient.deleteProfile(profile) {
            scope.launch { model.loadUserProfiles() }
            completionHandler(it)
        }
    }

    override fun updatePrefs(prefs: Ipn.MaskedPrefs, callback: PrefChangeCallback) {
        apiClient.editPrefs(prefs) { result ->
            callback(Result.success(result.isSuccess))
        }
    }
}
