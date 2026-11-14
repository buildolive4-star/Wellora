package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.CatalogRepository
import com.example.model.ServiceCategory
import com.example.model.ServiceItem
import com.example.ui.components.SecureDialogWindowEffect
import com.example.ui.components.ServiceEditorialCard
import com.example.ui.theme.AlabasterSurface
import com.example.ui.theme.ChampagneGold
import com.example.ui.theme.ChampagneGoldLight
import com.example.ui.theme.DeepBronze
import com.example.ui.theme.DeepCharcoal
import com.example.ui.theme.IvoryBackground
import com.example.ui.theme.IvorySurface
import com.example.ui.theme.MutedTaupe
import com.example.ui.theme.WarmStoneOutline
import com.example.ui.viewmodel.PortalDashboardData

@Composable
fun HomeScreen(
    catalog: CatalogRepository,
    dashboardData: PortalDashboardData?,
    onOpenNotifications: () -> Unit,
    onNavigateDiscover: (category: ServiceCategory?, city: String?) -> Unit,
    onOpenServiceDetail: (ServiceItem) -> Unit,
    onBookService: (ServiceItem) -> Unit,
    onToggleFavorite: (ServiceItem, Boolean) -> Unit,
    onOpenSupport: () -> Unit
) {
    var policyModalTitle by remember { mutableStateOf<String?>(null) }
    var policyModalBody by remember { mutableStateOf("") }

    val favoriteIds = remember(dashboardData) {
        dashboardData?.favorites?.map { it.serviceId }?.toSet() ?: emptySet()
    }
    val blockedIds = remember(dashboardData) {
        dashboardData?.profile?.blockedProviderIds?.toSet() ?: emptySet()
    }
    val featuredServices = remember(catalog, blockedIds) {
        catalog.services.filter { it.isFeatured && it.providerId !in blockedIds }
    }
    val memberName = dashboardData?.profile?.fullName?.split(" ")?.firstOrNull()
        ?.takeIf { it.isNotBlank() } ?: "Member"
    val unreadCount = dashboardData?.unreadNotificationCount ?: 0

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(IvoryBackground),
        contentPadding = PaddingValues(bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(22.dp)
    ) {
        // Editorial Top Welcome Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "AUREVIA GROUP",
                        style = MaterialTheme.typography.labelSmall,
                        color = DeepBronze
                    )
                    Text(
                        text = "Welcome back, $memberName",
                        style = MaterialTheme.typography.headlineMedium,
                        color = DeepCharcoal
                    )
                }
                Surface(
                    shape = CircleShape,
                    color = IvorySurface,
                    border = BorderStroke(1.dp, WarmStoneOutline)
                ) {
                    IconButton(
                        onClick = onOpenNotifications,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("home_notifications_button")
                    ) {
                        BadgedBox(
                            badge = {
                                if (unreadCount > 0) {
                                    Badge(containerColor = ChampagneGold, contentColor = IvorySurface) {
                                        Text("$unreadCount")
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Notifications,
                                contentDescription = "Notifications",
                                tint = DeepCharcoal
                            )
                        }
                    }
                }
            }
        }

        // Quick Search Bar
        item {
            Surface(
                onClick = { onNavigateDiscover(null, null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .testTag("home_search_bar"),
                shape = RoundedCornerShape(14.dp),
                color = IvorySurface,
                border = BorderStroke(1.dp, WarmStoneOutline)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = "Search services",
                        tint = DeepBronze
                    )
                    Text(
                        text = "Search spa rituals, training, hospitality, cities...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MutedTaupe
                    )
                }
            }
        }

        // Hero Banner
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                shape = RoundedCornerShape(22.dp),
                border = BorderStroke(1.dp, WarmStoneOutline)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(230.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_hero_sanctuary),
                        contentDescription = "Aurevia luxury sanctuary",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.matchParentSize()
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        DeepCharcoal.copy(alpha = 0.88f),
                                        DeepCharcoal.copy(alpha = 0.45f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )
                    Column(
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = ChampagneGold
                        ) {
                            Text(
                                text = "PRIVATE CONCIERGE",
                                style = MaterialTheme.typography.labelSmall,
                                color = IvorySurface,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                        Text(
                            text = "Curated Wellness & Refined Experiences",
                            style = MaterialTheme.typography.headlineSmall,
                            color = IvorySurface
                        )
                        Text(
                            text = "Verified specialists, transparent boundaries, and seamless bookings.",
                            style = MaterialTheme.typography.bodySmall,
                            color = IvorySurface.copy(alpha = 0.9f)
                        )
                        Button(
                            onClick = { onNavigateDiscover(null, null) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ChampagneGold,
                                contentColor = DeepCharcoal
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("hero_explore_button")
                        ) {
                            Text("Explore Services")
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }

        // Explore Categories
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Explore Categories",
                    style = MaterialTheme.typography.headlineSmall,
                    color = DeepCharcoal,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(ServiceCategory.entries) { category ->
                        Surface(
                            onClick = { onNavigateDiscover(category, null) },
                            shape = RoundedCornerShape(16.dp),
                            color = IvorySurface,
                            border = BorderStroke(1.dp, WarmStoneOutline),
                            modifier = Modifier
                                .width(190.dp)
                                .testTag("category_chip_${category.name}")
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = category.displayName,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = DeepCharcoal
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = category.tagline,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MutedTaupe,
                                    maxLines = 2
                                )
                            }
                        }
                    }
                }
            }
        }

        // Featured Services
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Featured Experiences",
                    style = MaterialTheme.typography.headlineSmall,
                    color = DeepCharcoal
                )
                TextButton(onClick = { onNavigateDiscover(null, null) }) {
                    Text("View All", color = DeepBronze)
                }
            }
        }

        items(featuredServices, key = { it.id }) { service ->
            Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                ServiceEditorialCard(
                    service = service,
                    isFavorite = service.id in favoriteIds,
                    onToggleFavorite = { onToggleFavorite(service, service.id in favoriteIds) },
                    onOpenDetails = { onOpenServiceDetail(service) },
                    onBookNow = { onBookService(service) }
                )
            }
        }

        // Popular Locations
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Popular Locations",
                    style = MaterialTheme.typography.headlineSmall,
                    color = DeepCharcoal,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(catalog.cities) { city ->
                        Surface(
                            onClick = { onNavigateDiscover(null, city.name) },
                            shape = RoundedCornerShape(14.dp),
                            color = AlabasterSurface,
                            border = BorderStroke(1.dp, WarmStoneOutline),
                            modifier = Modifier.width(168.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Filled.LocationOn,
                                        contentDescription = null,
                                        tint = ChampagneGold,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = city.name,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = DeepCharcoal
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = city.regionLabel,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MutedTaupe
                                )
                                Text(
                                    text = "${city.activeSpecialists} vetted specialists",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = DeepBronze,
                                    modifier = Modifier.padding(top = 6.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Special Offers
        item {
            Column(
                modifier = Modifier.padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Special Member Privileges",
                    style = MaterialTheme.typography.headlineSmall,
                    color = DeepCharcoal
                )
                catalog.specialOffers.forEach { offer ->
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = ChampagneGoldLight,
                        border = BorderStroke(1.dp, ChampagneGold.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = offer.badge,
                                style = MaterialTheme.typography.labelSmall,
                                color = DeepBronze
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = offer.title,
                                style = MaterialTheme.typography.titleMedium,
                                color = DeepCharcoal
                            )
                            Text(
                                text = offer.subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = MutedTaupe
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = DeepCharcoal
                            ) {
                                Text(
                                    text = "CODE: ${offer.code} (${offer.discountPercent}% OFF)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = IvorySurface,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Customer Reviews & Why Choose Aurevia + Trust & Safety
        item {
            Column(
                modifier = Modifier.padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Verified Member Voices",
                    style = MaterialTheme.typography.headlineSmall,
                    color = DeepCharcoal
                )
                val reviews = dashboardData?.reviews.orEmpty()
                if (reviews.isNotEmpty()) {
                    reviews.take(2).forEach { rev ->
                        ReviewHighlightCard(
                            author = "${rev.reviewerDisplayName} • Verified Booking",
                            quote = rev.comment,
                            rating = rev.rating.toInt()
                        )
                    }
                } else {
                    ReviewHighlightCard(
                        author = "Ananya R. • Mumbai Verified Booking",
                        quote = "Discreet arrival, clear pricing, and an extraordinary Ayurvedic thermal ritual.",
                        rating = 5
                    )
                    ReviewHighlightCard(
                        author = "Vikram K. • London Verified Booking",
                        quote = "The cultural gallery companionship exceeded expectations—articulate, punctual, and deeply respectful.",
                        rating = 5
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Why Choose AUREVIA?",
                    style = MaterialTheme.typography.headlineSmall,
                    color = DeepCharcoal
                )
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = DeepCharcoal,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Shield,
                                contentDescription = "Trust and safety",
                                tint = ChampagneGold
                            )
                            Text(
                                text = "Trust, Consent & Safety Charter",
                                style = MaterialTheme.typography.titleMedium,
                                color = IvorySurface
                            )
                        }
                        Text(
                            text = "• Verified 18+ background-screened specialists\n" +
                                "• Strictly non-sexual boundaries on all companionship services\n" +
                                "• Screenshot & screen-recording shield active across all views\n" +
                                "• Private customer records & zero raw card storage\n" +
                                "• 24/7 emergency escalation & transparent refund tracking",
                            style = MaterialTheme.typography.bodySmall,
                            color = IvorySurface.copy(alpha = 0.88f)
                        )
                        Button(
                            onClick = onOpenSupport,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ChampagneGold,
                                contentColor = DeepCharcoal
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.VerifiedUser,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Concierge & Safety Desk")
                        }
                    }
                }

                // Editorial Footer with Company Policies
                HorizontalDivider(color = WarmStoneOutline, modifier = Modifier.padding(top = 8.dp))
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "AUREVIA GROUP • Global Lifestyle & Hospitality Portal",
                        style = MaterialTheme.typography.labelMedium,
                        color = DeepCharcoal
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "Privacy Charter",
                            style = MaterialTheme.typography.labelSmall,
                            color = DeepBronze,
                            modifier = Modifier.clickable {
                                policyModalTitle = "AUREVIA Privacy & Data Protection"
                                policyModalBody = "Customer phone numbers, addresses, and booking history are strictly isolated to your authenticated account. Public reviews show only first name and last initial."
                            }
                        )
                        Text(
                            text = "Boundaries Policy",
                            style = MaterialTheme.typography.labelSmall,
                            color = DeepBronze,
                            modifier = Modifier.clickable {
                                policyModalTitle = "Non-Sexual Companionship & Safety Charter"
                                policyModalBody = "All social, event, and travel companionship bookings are strictly non-sexual, lawful, adult-only (18+) engagements conducted in public or accredited venues with mutual respect."
                            }
                        )
                        Text(
                            text = "Refund Terms",
                            style = MaterialTheme.typography.labelSmall,
                            color = DeepBronze,
                            modifier = Modifier.clickable {
                                policyModalTitle = "Cancellation & Refund Policy"
                                policyModalBody = "Full refunds apply for cancellations 24–48 hours prior (service-dependent). Refund statuses are tracked transparently inside your Payments & Support tabs."
                            }
                        )
                    }
                }
            }
        }
    }

    if (policyModalTitle != null) {
        AlertDialog(
            onDismissRequest = { policyModalTitle = null },
            containerColor = IvorySurface,
            title = { Text(policyModalTitle ?: "", style = MaterialTheme.typography.titleLarge) },
            text = {
                SecureDialogWindowEffect()
                Text(policyModalBody, style = MaterialTheme.typography.bodyMedium, color = MutedTaupe)
            },
            confirmButton = {
                TextButton(onClick = { policyModalTitle = null }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
private fun ReviewHighlightCard(
    author: String,
    quote: String,
    rating: Int
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = IvorySurface,
        border = BorderStroke(1.dp, WarmStoneOutline),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                repeat(rating) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = null,
                        tint = ChampagneGold,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "“$quote”",
                style = MaterialTheme.typography.bodyMedium,
                color = DeepCharcoal
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = author,
                style = MaterialTheme.typography.labelSmall,
                color = MutedTaupe
            )
        }
    }
}
