// Copyright (c) Tailscale Inc & AUTHORS
// SPDX-License-Identifier: BSD-3-Clause


package com.tailscale.ipn.ui.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.tailscale.ipn.R
import com.tailscale.ipn.ui.Links
import com.tailscale.ipn.ui.theme.ts_color_dark_desctrutive_text
import com.tailscale.ipn.ui.util.ChevronRight
import com.tailscale.ipn.ui.util.Header
import com.tailscale.ipn.ui.util.defaultPaddingModifier
import com.tailscale.ipn.ui.util.settingsRowModifier
import com.tailscale.ipn.ui.viewModel.Setting
import com.tailscale.ipn.ui.viewModel.SettingType
import com.tailscale.ipn.ui.viewModel.SettingsViewModel


data class SettingsNav(
        val onNavigateToBugReport: () -> Unit,
        val onNavigateToAbout: () -> Unit,
        val onNavigateToMDMSettings: () -> Unit,
        val onNavigateToManagedBy: () -> Unit,
        val onNavigateToUserSwitcher: () -> Unit,
)

@Composable
fun Settings(viewModel: SettingsViewModel) {
    val handler = LocalUriHandler.current

    Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxHeight()) {

        Column(modifier = defaultPaddingModifier().fillMaxHeight()) {

            Header(title = R.string.settings_title)

            Spacer(modifier = Modifier.height(8.dp))

            // The login/logout button here is probably in the wrong location, but we need something
            // somewhere for the time being.  FUS should probably be implemented for V0 given that
            // it's relatively simple to do so with localAPI.  On iOS, the UI for user switching is
            // all in the FUS screen.

            val user = viewModel.user.collectAsState().value
            val isAdmin = viewModel.isAdmin.collectAsState().value

            UserView(profile = user,
                    actionState = UserActionState.NAV,
                    onClick = viewModel.navigation.onNavigateToUserSwitcher)

            if (isAdmin) {
                Spacer(modifier = Modifier.height(4.dp))
                AdminText(adminText(), { handler.openUri(Links.ADMIN_URL) })
            }

            Spacer(modifier = Modifier.height(8.dp))


            viewModel.settings.forEach { settingBundle ->
                Column(modifier = settingsRowModifier()) {
                    settingBundle.title?.let {
                        Text(
                                text = it,
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(8.dp)
                        )
                    }
                    settingBundle.settings.forEach { setting ->
                        when (setting.type) {
                            SettingType.NAV -> SettingsNavRow(setting)
                            SettingType.SWITCH -> SettingsSwitchRow(setting)
                            SettingType.NAV_WITH_TEXT -> SettingsNavRow(setting)
                            SettingType.TEXT -> SettingsNavRow(setting)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun SettingsTextRow(setting: Setting) {
    val enabled = setting.enabled.collectAsState().value

    Row(modifier = defaultPaddingModifier().clickable { if (enabled) setting.onClick() }) {
        Text(setting.title.getString(),
                style = MaterialTheme.typography.bodyMedium,
                color = if (setting.destructive) ts_color_dark_desctrutive_text else MaterialTheme.colorScheme.primary)
    }
}

@Composable
fun SettingsNavRow(setting: Setting) {
    val txtVal = setting.value?.collectAsState()?.value ?: ""
    val enabled = setting.enabled.collectAsState().value

    Row(modifier = defaultPaddingModifier().clickable { if (enabled) setting.onClick() }) {
        Text(setting.title.getString(),
                style = MaterialTheme.typography.bodyMedium,
                color = if (setting.destructive) ts_color_dark_desctrutive_text else MaterialTheme.colorScheme.primary)
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterEnd) {
            Text(text = txtVal, style = MaterialTheme.typography.bodyMedium)
        }
        ChevronRight()
    }
}

@Composable
fun SettingsSwitchRow(setting: Setting) {
    val swVal = setting.isOn?.collectAsState()?.value ?: false
    val enabled = setting.enabled.collectAsState().value

    Row(modifier = defaultPaddingModifier().clickable { if (enabled) setting.onClick() }, verticalAlignment = Alignment.CenterVertically) {
        Text(setting.title.getString())
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterEnd) {
            Switch(checked = swVal, onCheckedChange = setting.onToggle, enabled = enabled)
        }
    }
}

@Composable
fun adminText(): AnnotatedString {
    val annotatedString = buildAnnotatedString {
        withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary)) {
            append(stringResource(id = R.string.settings_admin_prefix))
        }

        pushStringAnnotation(tag = "link", annotation = Links.ADMIN_URL)
        withStyle(style = SpanStyle(color = Color.Blue)) {
            append(stringResource(id = R.string.settings_admin_link))
        }
        pop()
    }
    return annotatedString
}


@Composable
fun AdminText(adminText: AnnotatedString, onNavigateToAdminConsole: () -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 12.dp)) {
        ClickableText(
                text = adminText,
                style = MaterialTheme.typography.bodySmall,
                onClick = {
                    onNavigateToAdminConsole()
                })
    }
}
