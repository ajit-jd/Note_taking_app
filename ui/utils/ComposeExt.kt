package com.example.project7.ui.utils

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.StateFlow

@Composable
fun <T> StateFlow<T>.collectAsStateWithLifecycleManual(
    initialValueOverride: T? = null,
    lifecycleState: Lifecycle.State = Lifecycle.State.CREATED
): State<T> {
    val lifecycleOwner = LocalLifecycleOwner.current
    val state = remember(this, initialValueOverride) {
        mutableStateOf(initialValueOverride ?: this.value)
    }
    LaunchedEffect(this, lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(lifecycleState) {
            this@collectAsStateWithLifecycleManual.collect { value ->
                state.value = value
            }
        }
    }
    return state
}

@Composable
fun FormattingButton(
    icon: ImageVector,
    desc: String,
    isSelected: Boolean,
    onClick: () -> Unit, // Changed to non-composable
    enabled: () -> Boolean = { true },
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onClick, // No cast needed
        enabled = enabled(),
        modifier = modifier.size(40.dp),
        colors = IconButtonDefaults.iconButtonColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else Color.Transparent,
            contentColor = if (isSelected) MaterialTheme.colorScheme.primary else LocalContentColor.current,
            disabledContentColor = LocalContentColor.current.copy(alpha = 0.38f)
        )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = desc,
            modifier = Modifier.size(20.dp)
        )
    }
}