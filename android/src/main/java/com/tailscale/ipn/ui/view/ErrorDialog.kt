package com.tailscale.ipn.ui.view

import androidx.annotation.StringRes
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.tailscale.ipn.R

enum class ErrorDialogType {
    LOGOUT_FAILED, SWITCH_USER_FAILED;

    val message: Int
        get() {
            return when (this) {
                LOGOUT_FAILED -> R.string.logout_failed
                SWITCH_USER_FAILED -> R.string.switch_user_failed
                else -> R.string.error
            }
        }

    val title: Int
        get() {
            return when (this) {
                LOGOUT_FAILED -> R.string.error
                SWITCH_USER_FAILED -> R.string.error
                else -> R.string.error
            }
        }

    val buttonText: Int
        get() {
            return R.string.ok
        }
}

@Composable
fun ErrorDialog(type: ErrorDialogType, action: () -> Unit = {}) {
    ErrorDialog(title = type.title,
            message = type.message,
            buttonText = type.buttonText,
            onDismiss = action)
}

@Composable
fun ErrorDialog(
        @StringRes title: Int,
        @StringRes message: Int,
        @StringRes buttonText: Int = R.string.ok,
        onDismiss: () -> Unit = {}
) {
    AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(text = stringResource(id = title))
            },
            text = {
                Text(text = stringResource(id = message))
            },
            confirmButton = {
                PrimaryActionButton(onClick = onDismiss) {
                    Text(text = stringResource(id = buttonText))
                }
            }
    )
}
