package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.model.ServiceItem
import com.example.ui.theme.AlabasterSurface
import com.example.ui.theme.AmberContainer
import com.example.ui.theme.AmberOchre
import com.example.ui.theme.ChampagneGold
import com.example.ui.theme.ChampagneGoldLight
import com.example.ui.theme.CrimsonAlert
import com.example.ui.theme.CrimsonContainer
import com.example.ui.theme.DeepBronze
import com.example.ui.theme.DeepCharcoal
import com.example.ui.theme.IvorySurface
import com.example.ui.theme.MutedTaupe
import com.example.ui.theme.SageContainer
import com.example.ui.theme.SageEmerald
import com.example.ui.theme.WarmStoneOutline
import com.example.util.formatInr

@Composable
fun StatusBadge(
    label: String,
    tone: String,
    modifier: Modifier = Modifier
) {
    val (bg, fg) = when (tone.uppercase()) {
        "CONFIRMED", "PAID", "CAPTURED", "COMPLETED", "RESOLVED", "VERIFIED" ->
            SageContainer to SageEmerald
        "CANCELLED", "SAFETY_ESCALATION", "ESCALATED" ->
            CrimsonContainer to CrimsonAlert
        else -> AmberContainer to AmberOchre
    }
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(50),
        color = bg
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = fg,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun ServiceEditorialCard(
    service: ServiceItem,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onOpenDetails: () -> Unit,
    onBookNow: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onOpenDetails,
        modifier = modifier
            .fillMaxWidth()
            .testTag("service_card_${service.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = IvorySurface),
        border = BorderStroke(1.dp, WarmStoneOutline),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(176.dp)
            ) {
                Image(
                    painter = painterResource(id = service.imageRes),
                    contentDescription = service.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.matchParentSize()
                )
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.08f),
                                    Color.Black.copy(alpha = 0.55f)
                                )
                            )
                        )
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = IvorySurface.copy(alpha = 0.94f)
                    ) {
                        Text(
                            text = service.category.displayName,
                            style = MaterialTheme.typography.labelSmall,
                            color = DeepCharcoal,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                    Surface(
                        shape = CircleShape,
                        color = IvorySurface.copy(alpha = 0.92f)
                    ) {
                        IconButton(
                            onClick = onToggleFavorite,
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("favorite_btn_${service.id}")
                        ) {
                            Icon(
                                imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                contentDescription = if (isFavorite) "Remove from favourites" else "Save to favourites",
                                tint = if (isFavorite) CrimsonAlert else DeepCharcoal
                            )
                        }
                    }
                }
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = DeepCharcoal.copy(alpha = 0.85f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Verified,
                                contentDescription = "Verified specialist",
                                tint = ChampagneGold,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = if (service.isAdultVerifiedOnly) "Verified 18+ • Non-Sexual" else "Aurevia Verified",
                                style = MaterialTheme.typography.labelSmall,
                                color = IvorySurface
                            )
                        }
                    }
                }
            }

            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = service.providerName,
                        style = MaterialTheme.typography.labelMedium,
                        color = DeepBronze
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = "Rating",
                            tint = ChampagneGold,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "${service.rating} (${service.reviewCount})",
                            style = MaterialTheme.typography.labelMedium,
                            color = DeepCharcoal
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = service.title,
                    style = MaterialTheme.typography.titleLarge,
                    color = DeepCharcoal,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = service.shortSummary,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MutedTaupe,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = WarmStoneOutline)
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "From ${formatInr(service.startingPriceInr)}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = DeepCharcoal
                        )
                        Text(
                            text = "${service.durationRange} • ${service.cities.take(2).joinToString(", ")}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MutedTaupe
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = onOpenDetails,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, WarmStoneOutline)
                        ) {
                            Text("Details")
                        }
                        Button(
                            onClick = onBookNow,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = DeepCharcoal,
                                contentColor = IvorySurface
                            ),
                            modifier = Modifier.testTag("book_now_${service.id}")
                        ) {
                            Text("Book Now")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PackageComparisonDialog(
    service: ServiceItem,
    onDismiss: () -> Unit,
    onSelectPackageToBook: (com.example.model.ServicePackageTier) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = IvorySurface,
        title = {
            Column {
                Text(
                    text = "Compare Packages",
                    style = MaterialTheme.typography.headlineSmall,
                    color = DeepCharcoal
                )
                Text(
                    text = service.title,
                    style = MaterialTheme.typography.bodySmall,
                    color = MutedTaupe
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                service.packages.forEach { pkg ->
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = AlabasterSurface,
                        border = BorderStroke(1.dp, WarmStoneOutline),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = pkg.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = DeepCharcoal
                                )
                                Text(
                                    text = formatInr(pkg.priceInr),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = DeepBronze
                                )
                            }
                            Text(
                                text = "${pkg.durationMinutes} minutes • ${pkg.summary}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MutedTaupe
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            pkg.highlights.forEach { item ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.padding(vertical = 2.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.CheckCircle,
                                        contentDescription = null,
                                        tint = ChampagneGold,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = item,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = DeepCharcoal
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = { onSelectPackageToBook(pkg) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = DeepCharcoal,
                                    contentColor = IvorySurface
                                ),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Select ${pkg.name}")
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
fun SafetyReportDialog(
    providerName: String,
    onDismiss: () -> Unit,
    onSubmit: (reason: String, details: String, blockProvider: Boolean) -> Unit
) {
    val reasons = listOf(
        "Boundary or non-sexual charter concern",
        "Unprofessional conduct or tardiness",
        "Pricing or billing discrepancy",
        "Urgent personal safety escalation"
    )
    var selectedReason by remember { mutableStateOf(reasons.first()) }
    var details by remember { mutableStateOf("") }
    var blockProvider by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = IvorySurface,
        icon = {
            Icon(
                imageVector = Icons.Filled.Shield,
                contentDescription = "Trust and safety",
                tint = CrimsonAlert
            )
        },
        title = {
            Text(
                text = "Report & Safety Protection",
                style = MaterialTheme.typography.titleLarge,
                color = DeepCharcoal
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Confidential escalation regarding $providerName. Our 24/7 Trust & Safety desk reviews all reports immediately.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MutedTaupe
                )
                reasons.forEach { reason ->
                    Surface(
                        onClick = { selectedReason = reason },
                        shape = RoundedCornerShape(10.dp),
                        color = if (selectedReason == reason) ChampagneGoldLight else AlabasterSurface,
                        border = BorderStroke(
                            1.dp,
                            if (selectedReason == reason) ChampagneGold else WarmStoneOutline
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = reason,
                            style = MaterialTheme.typography.bodySmall,
                            color = DeepCharcoal,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
                OutlinedTextField(
                    value = details,
                    onValueChange = { details = it },
                    label = { Text("Describe what happened") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = blockProvider,
                        onCheckedChange = { blockProvider = it }
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Block $providerName from appearing in my portal",
                        style = MaterialTheme.typography.bodySmall,
                        color = DeepCharcoal
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (details.trim().length >= 3) {
                        onSubmit(selectedReason, details.trim(), blockProvider)
                    }
                },
                enabled = details.trim().length >= 3,
                colors = ButtonDefaults.buttonColors(containerColor = CrimsonAlert)
            ) {
                Text("Escalate Report")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
