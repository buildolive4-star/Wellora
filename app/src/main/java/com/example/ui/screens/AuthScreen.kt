package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.theme.AlabasterSurface
import com.example.ui.theme.DeepBronze
import com.example.ui.theme.DeepCharcoal
import com.example.ui.theme.IvoryBackground
import com.example.ui.theme.IvorySurface
import com.example.ui.theme.MutedTaupe
import com.example.ui.theme.WarmStoneOutline

@Composable
fun AuthScreen(
    onLocalSignIn: (displayName: String, email: String) -> Unit
) {
    var displayName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(IvoryBackground)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(270.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_hero_sanctuary),
                    contentDescription = "Aurevia Sanctuary",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.matchParentSize()
                )
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color.Black.copy(alpha = 0.25f),
                                    IvoryBackground.copy(alpha = 0.96f)
                                )
                            )
                        )
                )
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 24.dp, vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_aurevia_emblem),
                        contentDescription = "Aurevia Group Emblem",
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "AUREVIA GROUP",
                        style = MaterialTheme.typography.labelMedium,
                        color = DeepBronze
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Private Lifestyle & Wellness Portal",
                        style = MaterialTheme.typography.displaySmall,
                        color = DeepCharcoal,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Column(
                modifier = Modifier
                    .widthIn(max = 560.dp)
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Curated spa rituals, private training, hospitality hosting, and strictly non-sexual 18+ cultural companionship.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MutedTaupe,
                    textAlign = TextAlign.Center
                )

                TrustHighlightRow(
                    icon = Icons.Filled.VerifiedUser,
                    title = "Verified Specialists & Clear Boundaries",
                    subtitle = "Background-checked providers with strict lawful & non-sexual charters."
                )
                TrustHighlightRow(
                    icon = Icons.Filled.Lock,
                    title = "On-Device Encrypted Private Vault",
                    subtitle = "All bookings, receipts, and concierge tickets stay privately stored on your phone."
                )
                TrustHighlightRow(
                    icon = Icons.Filled.Shield,
                    title = "24/7 Concierge & Safety Escalation",
                    subtitle = "Instant support tickets, transparent refunds, and one-tap specialist blocking."
                )

                OutlinedTextField(
                    value = displayName,
                    onValueChange = { displayName = it },
                    label = { Text("Your Name (Optional)") },
                    placeholder = { Text("Aurevia Member") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("member_name_input")
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email (Optional)") },
                    placeholder = { Text("member@aurevia.local") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("member_email_input")
                )

                Spacer(modifier = Modifier.height(4.dp))

                Button(
                    onClick = { onLocalSignIn(displayName, email) },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DeepCharcoal,
                        contentColor = IvorySurface
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("local_sign_in_button")
                ) {
                    Text(
                        text = "Enter Aurevia Portal",
                        style = MaterialTheme.typography.labelLarge
                    )
                }

                Text(
                    text = "Instant offline access active — customize your mobile contact, preferred city, and safety preferences inside Account.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MutedTaupe,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun TrustHighlightRow(
    icon: ImageVector,
    title: String,
    subtitle: String
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = IvorySurface,
        border = BorderStroke(1.dp, WarmStoneOutline),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = AlabasterSurface,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = DeepBronze,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = DeepCharcoal
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MutedTaupe
                )
            }
        }
    }
}
