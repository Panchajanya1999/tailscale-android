// Copyright (c) Tailscale Inc & AUTHORS
// SPDX-License-Identifier: BSD-3-Clause


package com.tailscale.ipn.ui.view

import android.graphics.drawable.Icon
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.tailscale.ipn.R
import com.tailscale.ipn.ui.viewModel.ExitNodePickerViewModel


@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ExitNodePicker(
    viewModel: ExitNodePickerViewModel, onNavigateToMullvadCountry: (String) -> Unit
) {
    Surface(color = MaterialTheme.colorScheme.surface) {
        Column {
            Text(
                stringResource(R.string.choose_exit_node),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleMedium
            )

            val tailnetExitNodes = viewModel.tailnetExitNodes.collectAsState()
            val mullvadExitNodes = viewModel.mullvadExitNodesByCountryCode.collectAsState()

            LazyColumn(
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                stickyHeader {
                    Text(stringResource(R.string.tailnet_exit_nodes))
                }

                items(tailnetExitNodes.value, key = { it.id }) { node ->
                    ListItem(headlineContent = {
                        Text(node.label)
                    }, trailingContent = {
                        if (node.selected) {
                            Icon(
                                Icons.Outlined.Check,
                                contentDescription = stringResource(R.string.more)
                            )
                        }
                    })
                }

                stickyHeader {
                    Text(stringResource(R.string.mullvad_exit_nodes))
                }

                items(mullvadExitNodes.value.entries.toList()) { (countryCode, nodes) ->
                    // TODO(oxtoacart): the modifier on the ListItem occasionally causes a crash
                    // with java.lang.ClassCastException: androidx.compose.ui.ComposedModifier cannot be cast to androidx.compose.runtime.RecomposeScopeImpl
                    // Wrapping it in a Box eliminates this. It appears to be some kind of
                    // interaction between the LazyList and the modifier.
                    Box {
                        ListItem(modifier = Modifier.clickable {
                            onNavigateToMullvadCountry(
                                countryCode
                            )
                        }, headlineContent = {
                            Text("${nodes.first().country}")
                        }, trailingContent = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("${nodes.size}")
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                                    contentDescription = stringResource(R.string.more)
                                )
                            }
                        })
                    }
                }
            }
        }
    }
}