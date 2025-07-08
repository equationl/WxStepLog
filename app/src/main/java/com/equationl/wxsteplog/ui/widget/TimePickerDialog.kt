package com.equationl.wxsteplog.ui.widget

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerState
import androidx.compose.runtime.Composable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(
    timePickerState: TimePickerState,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    onClear: (() -> Unit)? = null
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        dismissButton = {
            TextButton(onClick = { if (onClear == null) onDismiss() else onClear() }) {
                Text(if (onClear == null) "取消" else "清除")
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm() }) {
                Text("确定")
            }
        },
        text = {
            TimePicker(
                state = timePickerState,
            )
        }
    )
}
