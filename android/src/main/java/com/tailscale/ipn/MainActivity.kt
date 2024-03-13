// Copyright (c) Tailscale Inc & AUTHORS
// SPDX-License-Identifier: BSD-3-Clause

package com.tailscale.ipn

import android.content.Context
import android.content.Intent
import android.content.RestrictionsManager
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.tailscale.ipn.mdm.MDMSettings
import com.tailscale.ipn.ui.service.IpnManager
import com.tailscale.ipn.ui.theme.AppTheme
import com.tailscale.ipn.ui.view.AboutView
import com.tailscale.ipn.ui.view.BugReportView
import com.tailscale.ipn.ui.view.ExitNodePicker
import com.tailscale.ipn.ui.view.MDMSettingsDebugView
import com.tailscale.ipn.ui.view.MainView
import com.tailscale.ipn.ui.view.MainViewNavigation
import com.tailscale.ipn.ui.view.PeerDetails
import com.tailscale.ipn.ui.view.Settings
import com.tailscale.ipn.ui.view.SettingsNav
import com.tailscale.ipn.ui.viewModel.BugReportViewModel
import com.tailscale.ipn.ui.viewModel.ExitNodePickerViewModel
import com.tailscale.ipn.ui.viewModel.MainViewModel
import com.tailscale.ipn.ui.viewModel.PeerDetailsViewModel
import com.tailscale.ipn.ui.viewModel.SettingsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class MainActivity : ComponentActivity() {
    companion object {
        public const val WRITE_STORAGE_RESULT = 1000
    }

    private val manager = IpnManager(lifecycleScope)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AppTheme {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = "main") {
                    val mainViewNav = MainViewNavigation(
                            onNavigateToSettings = { navController.navigate("settings") },
                            onNavigateToPeerDetails = {
                                navController.navigate("peerDetails/${it.StableID}")
                            },
                            onNavigateToExitNodes = { navController.navigate("exitNodes") }
                    )

                    val settingsNav = SettingsNav(
                            onNavigateToBugReport = { navController.navigate("bugReport") },
                            onNavigateToAbout = { navController.navigate("about") },
                            onNavigateToMDMSettings = { navController.navigate("mdmSettings") }
                    )

                    composable("main") {
                        MainView(
                                viewModel = MainViewModel(manager.model, manager),
                                navigation = mainViewNav
                        )
                    }
                    composable("settings") {
                        Settings(SettingsViewModel(manager.model, manager, settingsNav))
                    }
                    composable("exitNodes") {
                        ExitNodePicker(ExitNodePickerViewModel(manager.model))
                    }
                    composable(
                            "peerDetails/{nodeId}",
                            arguments = listOf(navArgument("nodeId") { type = NavType.StringType })
                    ) {
                        PeerDetails(
                                PeerDetailsViewModel(
                                        manager.model, nodeId = it.arguments?.getString("nodeId")
                                        ?: ""
                                )
                        )
                    }
                    composable("bugReport") {
                        BugReportView(BugReportViewModel(manager.apiClient))
                    }
                    composable("about") {
                        AboutView()
                    }
                    composable("mdmSettings") {
                        MDMSettingsDebugView(manager.mdmSettings)
                    }
                }
            }
        }
        handleIntent()
    }

    init {
        // Watch the model's browseToURL and launch the browser when it changes
        // This will trigger the login flow
        lifecycleScope.launch {
            manager.model.browseToURL.collect { url ->
                url?.let {
                    Dispatchers.Main.run {
                        login(it)
                    }
                }
            }
        }
    }

    private fun login(url: String) {
        // (jonathan) TODO: This is functional, but the navigation doesn't quite work
        // as expected.  There's probably a better built in way to do this.  This will
        // unblock in dev for the time being though.
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(browserIntent)
    }


    override fun onResume() {
        super.onResume()
        val restrictionsManager =
                this.getSystemService(Context.RESTRICTIONS_SERVICE) as RestrictionsManager
        manager.mdmSettings = MDMSettings(restrictionsManager)
    }


    override fun onNewIntent(i: Intent?) {
        super.onNewIntent(i)
        intent = i
        handleIntent()
    }


    // (jonathan) TODO: Copied from the original IPNActivity, presumably to support Taildrop.
    // This should be refactored and cleaned up to remove the deprecations.

    private fun handleIntent() {
        val it = intent
        val act = it.action
        val texts: Array<String?>
        val uris: Array<Uri?>
        if (Intent.ACTION_SEND == act) {
            uris = arrayOf(it.getParcelableExtra(Intent.EXTRA_STREAM))
            texts = arrayOf(it.getStringExtra(Intent.EXTRA_TEXT))
        } else if (Intent.ACTION_SEND_MULTIPLE == act) {
            val extraUris: List<Uri?>? = it.getParcelableArrayListExtra(Intent.EXTRA_STREAM)
            uris = extraUris!!.toTypedArray<Uri?>()
            texts = arrayOfNulls(uris.size)
        } else {
            return
        }
        val mime = it.type
        val nitems = uris.size
        val items = arrayOfNulls<String>(nitems)
        val mimes = arrayOfNulls<String>(nitems)
        val types = IntArray(nitems)
        val names = arrayOfNulls<String>(nitems)
        val sizes = LongArray(nitems)
        var nfiles = 0
        for (i in uris.indices) {
            val text = texts[i]
            val uri = uris[i]
            if (text != null) {
                types[nfiles] = 1 // FileTypeText
                names[nfiles] = "file.txt"
                mimes[nfiles] = mime
                items[nfiles] = text
                // Determined by len(text) in Go to eliminate UTF-8 encoding differences.
                sizes[nfiles] = 0
                nfiles++
            } else if (uri != null) {
                val c = contentResolver.query(uri, null, null, null, null)
                        ?: // Ignore files we have no permission to access.
                        continue
                val nameCol = c.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeCol = c.getColumnIndex(OpenableColumns.SIZE)
                c.moveToFirst()
                val name = c.getString(nameCol)
                val size = c.getLong(sizeCol)
                types[nfiles] = 2 // FileTypeURI
                mimes[nfiles] = mime
                items[nfiles] = uri.toString()
                names[nfiles] = name
                sizes[nfiles] = size
                nfiles++
            }
        }
        App.onShareIntent(nfiles, types, mimes, items, names, sizes)
    }
}

