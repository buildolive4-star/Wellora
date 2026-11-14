package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.model.ServiceItem
import com.example.model.ServicePackageTier
import com.example.model.ServiceReview
import com.example.ui.components.SafetyReportDialog
import com.example.ui.theme.AlabasterSurface
import com.example.ui.theme.ChampagneGold
import com.example.ui.theme.ChampagneGoldLight
import com.example.ui.theme.CrimsonAlert
import com.example.ui.theme.DeepBronze
import com.example.ui.theme.DeepCharcoal
import com.example.ui.theme.IvoryBackground
import com.example.ui.theme.IvorySurface
import com.example.ui.theme.MutedTaupe
import com.example.ui.theme.SageEmerald
import com.example.ui.theme.WarmStoneOutline
import com.example.util.formatInr

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ServiceDetailScreen(
    service: ServiceItem,
    isFavorite: Boolean,
    reviews: List<ServiceReview>,
    onBack: () -> Unit,
    onToggleFavorite: () -> Unit,
    onComparePackages: () -> Unit,
    onBookPackage: (ServicePackageTier) -> Unit,
    onSubmitSafetyReport: (reason: String, details: String, blockProvider: Boolean) -> Unit
) {
    BackHandler(onBack = onBack)

    var selectedPkg by remember(service.id) {
        mutableStateOf(service.packages.getOrNull(1) ?: service.packages.first())
    }
    var showSafetyDialog by remember { mutableStateOf(false) }

    val serviceReviews = remember(reviews, service.id) {
        reviews.filter { it.serviceId == service.id }
    }

    Scaffold(
        containerColor = IvoryBackground,
        bottomBar = {
            Surface(
                color = IvorySurface,
                shadowElevation = 8.dp,
                border = BorderStroke(1.dp, WarmStoneOutline)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = selectedPkg.name,
                            style = MaterialTheme.typography.labelSmall,
                            color = DeepBronze
                        )
                        Text(
                            text = formatInr(selectedPkg.priceInr),
                            style = MaterialTheme.typography.titleLarge,
                            color = DeepCharcoal
                        )
                    }
                    Button(
                        onClick = { onBookPackage(selectedPkg) },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DeepCharcoal,
                            contentColor = IvorySurface
                        ),
                        modifier = Modifier
                            .height(50.dp)
                            .testTag("detail_book_now_button")
                    ) {
                        Text("Book Now")
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
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
                                    listOf(
                                        Color.Black.copy(alpha = 0.45f),
                                        Color.Transparent,
                                        Color.Black.copy(alpha = 0.65f)
                                    )
                                )
                            )
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = IvorySurface.copy(alpha = 0.92f)
                        ) {
                            IconButton(
                                onClick = onBack,
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = DeepCharcoal
                                )
                            }
                        }
                        Surface(
                            shape = CircleShape,
                            color = IvorySurface.copy(alpha = 0.92f)
                        ) {
                            IconButton(
                                onClick = onToggleFavorite,
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(
                                    imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                    contentDescription = "Favourite",
                                    tint = if (isFavorite) CrimsonAlert else DeepCharcoal
                                )
                            }
                        }
                    }

                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(20.dp)
                    ) {
                        Text(
                            text = service.category.displayName.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = ChampagneGoldLight
                        )
                        Text(
                            text = service.title,
                            style = MaterialTheme.typography.headlineMedium,
                            color = IvorySurface
                        )
                    }
                }
            }

            item {
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Verified,
                                contentDescription = null,
                                tint = ChampagneGold,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "${service.providerName} • ${service.providerTitle}",
                                style = MaterialTheme.typography.labelMedium,
                                color = DeepBronze
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Star, contentDescription = null, tint = ChampagneGold, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("${service.rating}", style = MaterialTheme.typography.labelLarge)
                        }
                    }

                    Text(
                        text = service.description,
                        style = MaterialTheme.typography.bodyLarge,
                        color = DeepCharcoal
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Filled.Schedule, contentDescription = null, tint = MutedTaupe, modifier = Modifier.size(16.dp))
                        Text(
                            text = "Duration: ${service.durationRange}  •  Available Slots: ${service.availableTimeSlots.joinToString(", ")}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MutedTaupe
                        )
                    }

                    Text(
                        text = "Location Coverage",
                        style = MaterialTheme.typography.titleSmall,
                        color = DeepCharcoal
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        service.cities.forEach { city ->
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = AlabasterSurface,
                                border = BorderStroke(1.dp, WarmStoneOutline)
                            ) {
                                Text(
                                    text = city,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = DeepCharcoal,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Package Tiers
            item {
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Select Package",
                            style = MaterialTheme.typography.headlineSmall,
                            color = DeepCharcoal
                        )
                        TextButton(onClick = onComparePackages) {
                            Icon(Icons.Filled.CompareArrows, contentDescription = null, tint = DeepBronze)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Compare", color = DeepBronze)
                        }
                    }

                    service.packages.forEach { pkg ->
                        val isSelected = selectedPkg.id == pkg.id
                        Surface(
                            onClick = { selectedPkg = pkg },
                            shape = RoundedCornerShape(14.dp),
                            color = if (isSelected) ChampagneGoldLight else IvorySurface,
                            border = BorderStroke(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) ChampagneGold else WarmStoneOutline
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "${pkg.name} (${pkg.durationMinutes} min)",
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
                                    text = pkg.summary,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MutedTaupe
                                )
                            }
                        }
                    }
                }
            }

            // What's Included & What's Not Included / Boundaries
            item {
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = IvorySurface,
                        border = BorderStroke(1.dp, WarmStoneOutline),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("What's Included", style = MaterialTheme.typography.titleMedium, color = DeepCharcoal)
                            service.includedItems.forEach { item ->
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Top) {
                                    Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = SageEmerald, modifier = Modifier.size(16.dp))
                                    Text(item, style = MaterialTheme.typography.bodySmall, color = DeepCharcoal)
                                }
                            }
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = AlabasterSurface,
                        border = BorderStroke(1.dp, WarmStoneOutline),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("What's Not Included & Service Boundaries", style = MaterialTheme.typography.titleMedium, color = DeepCharcoal)
                            service.excludedAndBoundaries.forEach { item ->
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Top) {
                                    Icon(Icons.Filled.Block, contentDescription = null, tint = CrimsonAlert, modifier = Modifier.size(16.dp))
                                    Text(item, style = MaterialTheme.typography.bodySmall, color = DeepCharcoal)
                                }
                            }
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = IvorySurface,
                        border = BorderStroke(1.dp, WarmStoneOutline),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Filled.Shield, contentDescription = null, tint = ChampagneGold)
                                Text("Safety & Cancellation Policy", style = MaterialTheme.typography.titleMedium, color = DeepCharcoal)
                            }
                            Text(service.safetyInformation, style = MaterialTheme.typography.bodySmall, color = DeepCharcoal)
                            HorizontalDivider(color = WarmStoneOutline)
                            Text(service.cancellationPolicy, style = MaterialTheme.typography.bodySmall, color = MutedTaupe)
                            OutlinedButton(
                                onClick = { showSafetyDialog = true },
                                border = BorderStroke(1.dp, CrimsonAlert.copy(alpha = 0.5f)),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Filled.Flag, contentDescription = null, tint = CrimsonAlert, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Report or Block Specialist", color = CrimsonAlert)
                            }
                        }
                    }
                }
            }

            // Customer Reviews
            item {
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Verified Customer Reviews",
                        style = MaterialTheme.typography.headlineSmall,
                        color = DeepCharcoal
                    )
                    if (serviceReviews.isEmpty()) {
                        Text(
                            text = "Public reviews are unlocked exclusively for customers with completed bookings for this experience.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MutedTaupe
                        )
                    } else {
                        serviceReviews.forEach { rev ->
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = IvorySurface,
                                border = BorderStroke(1.dp, WarmStoneOutline),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("${rev.reviewerDisplayName} • Verified", style = MaterialTheme.typography.labelMedium, color = DeepBronze)
                                        Text("${rev.rating} ★", style = MaterialTheme.typography.labelMedium, color = DeepCharcoal)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(rev.comment, style = MaterialTheme.typography.bodySmall, color = DeepCharcoal)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showSafetyDialog) {
        SafetyReportDialog(
            providerName = service.providerName,
            onDismiss = { showSafetyDialog = false },
            onSubmit = { reason, details, block ->
                onSubmitSafetyReport(reason, details, block)
                showSafetyDialog = false
            }
        )
    }
}
