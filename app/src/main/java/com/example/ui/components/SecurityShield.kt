package com.example.ui.components

import android.view.WindowManager
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogWindowProvider
import com.example.ui.theme.DeepCharcoal
import com.example.ui.theme.SageContainer
import com.example.ui.theme.SageEmerald

/**
 * Ensures any Compose Dialog / AlertDialog window inherits FLAG_SECURE so screenshots,
 * screen recording, and screen casting remain strictly blocked across modal overlays too.
 */
@Composable
fun SecureDialogWindowEffect() {
    val view = LocalView.current
    SideEffect {
        val window = (view.parent as? DialogWindowProvider)?.window
        window?.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )
    }
}

@Composable
fun PrivacyShieldBanner(modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = SageContainer,
        border = BorderStroke(1.dp, SageEmerald.copy(alpha = 0.35f)),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.PrivacyTip,
                contentDescription = "Screen privacy shield active",
                tint = SageEmerald,
                modifier = Modifier.size(22.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Screen Privacy Shield Active",
                    style = MaterialTheme.typography.titleSmall,
                    color = DeepCharcoal
                )
                Text(
                    text = "Screenshots, screen recording, and app-switcher previews are blocked across all screens and receipts.",
                    style = MaterialTheme.typography.bodySmall,
                    color = DeepCharcoal.copy(alpha = 0.85f)
                )
            }
        }
    }
}
