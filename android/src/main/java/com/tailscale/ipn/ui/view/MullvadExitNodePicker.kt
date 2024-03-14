// Copyright (c) Tailscale Inc & AUTHORS
// SPDX-License-Identifier: BSD-3-Clause


package com.tailscale.ipn.ui.view

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.tailscale.ipn.R
import com.tailscale.ipn.ui.viewModel.ExitNodePickerViewModel


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MullvadExitNodePicker(viewModel: ExitNodePickerViewModel, countryCode: String) {
    Surface(color = MaterialTheme.colorScheme.surface) {
        Column {
            Text(
                countryCode,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleMedium
            )

            val mullvadExitNodes = viewModel.mullvadExitNodesByCountryCode.collectAsState()
            mullvadExitNodes.value.get(countryCode)?.toList()?.let { nodes ->
                Text("${nodes.first().country} ${stringResource(R.string.best_available)}")
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(nodes) { node ->
                        ListItem(headlineContent = {
                            Text("${node.city}")
                        })
                    }
                }
            }
        }
    }
}