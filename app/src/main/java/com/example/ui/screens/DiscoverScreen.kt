package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.CatalogRepository
import com.example.model.ServiceCategory
import com.example.model.ServiceItem
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
import com.example.ui.viewmodel.DiscoveryFilter
import com.example.util.formatInr

@Composable
fun DiscoverScreen(
    catalog: CatalogRepository,
    filter: DiscoveryFilter,
    services: List<ServiceItem>,
    favoriteIds: Set<String>,
    onSearchChange: (String) -> Unit,
    onCategorySelect: (ServiceCategory?) -> Unit,
    onCitySelect: (String?) -> Unit,
    onDateSelect: (String?) -> Unit,
    onMaxPriceChange: (Int) -> Unit,
    onMinRatingChange: (Double) -> Unit,
    onClearFilters: () -> Unit,
    onToggleFavorite: (ServiceItem, Boolean) -> Unit,
    onOpenDetails: (ServiceItem) -> Unit,
    onBookNow: (ServiceItem) -> Unit,
    onComparePackages: (ServiceItem) -> Unit
) {
    var showAdvancedFilters by remember { mutableStateOf(false) }
    val allDates = remember(catalog) {
        catalog.services.flatMap { it.availableDates }.distinct().sorted()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(IvoryBackground),
        contentPadding = PaddingValues(bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "CURATED CATALOG",
                            style = MaterialTheme.typography.labelSmall,
                            color = DeepBronze
                        )
                        Text(
                            text = "Discover Experiences",
                            style = MaterialTheme.typography.headlineMedium,
                            color = DeepCharcoal
                        )
                    }
                    OutlinedButton(
                        onClick = { showAdvancedFilters = !showAdvancedFilters },
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, WarmStoneOutline),
                        modifier = Modifier.testTag("discover_filter_toggle")
                    ) {
                        Icon(Icons.Filled.Tune, contentDescription = "Filters", tint = DeepCharcoal)
                        Spacer(modifier = Modifier.padding(2.dp))
                        Text("Filters")
                    }
                }

                OutlinedTextField(
                    value = filter.searchQuery,
                    onValueChange = onSearchChange,
                    placeholder = { Text("Search services, specialists, or categories") },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    trailingIcon = {
                        if (filter.searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchChange("") }) {
                                Icon(Icons.Filled.Clear, contentDescription = "Clear search")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("discover_search_input")
                )
            }
        }

        // Category Filter Row
        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = filter.category == null,
                        onClick = { onCategorySelect(null) },
                        label = { Text("All Categories") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = DeepCharcoal,
                            selectedLabelColor = IvorySurface
                        )
                    )
                }
                items(ServiceCategory.entries) { category ->
                    FilterChip(
                        selected = filter.category == category,
                        onClick = {
                            onCategorySelect(if (filter.category == category) null else category)
                        },
                        label = { Text(category.displayName) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = DeepCharcoal,
                            selectedLabelColor = IvorySurface
                        )
                    )
                }
            }
        }

        // City Filter Row
        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = filter.city == null,
                        onClick = { onCitySelect(null) },
                        label = { Text("All Cities") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ChampagneGoldLight,
                            selectedLabelColor = DeepBronze
                        )
                    )
                }
                items(catalog.cities) { city ->
                    FilterChip(
                        selected = filter.city == city.name,
                        onClick = {
                            onCitySelect(if (filter.city == city.name) null else city.name)
                        },
                        label = { Text(city.name) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ChampagneGoldLight,
                            selectedLabelColor = DeepBronze
                        )
                    )
                }
            }
        }

        // Expandable Price, Date, and Rating Filters
        if (showAdvancedFilters) {
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = IvorySurface,
                    border = BorderStroke(1.dp, WarmStoneOutline),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Date Availability",
                            style = MaterialTheme.typography.titleSmall,
                            color = DeepCharcoal
                        )
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            item {
                                FilterChip(
                                    selected = filter.date == null,
                                    onClick = { onDateSelect(null) },
                                    label = { Text("Any Date") }
                                )
                            }
                            items(allDates) { dateStr ->
                                FilterChip(
                                    selected = filter.date == dateStr,
                                    onClick = { onDateSelect(if (filter.date == dateStr) null else dateStr) },
                                    label = { Text(dateStr) }
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Starting Price Up To",
                                style = MaterialTheme.typography.titleSmall,
                                color = DeepCharcoal
                            )
                            Text(
                                text = formatInr(filter.maxPriceInr),
                                style = MaterialTheme.typography.titleSmall,
                                color = DeepBronze
                            )
                        }
                        Slider(
                            value = filter.maxPriceInr.toFloat(),
                            onValueChange = { onMaxPriceChange(it.toInt()) },
                            valueRange = 4500f..35000f,
                            colors = SliderDefaults.colors(
                                thumbColor = ChampagneGold,
                                activeTrackColor = ChampagneGold
                            )
                        )

                        Text(
                            text = "Minimum Rating",
                            style = MaterialTheme.typography.titleSmall,
                            color = DeepCharcoal
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(0.0 to "Any", 4.5 to "4.5+", 4.9 to "4.9+").forEach { (rating, label) ->
                                FilterChip(
                                    selected = filter.minRating == rating,
                                    onClick = { onMinRatingChange(rating) },
                                    label = { Text(label) }
                                )
                            }
                        }

                        TextButton(
                            onClick = onClearFilters,
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("Reset All Filters", color = DeepBronze)
                        }
                    }
                }
            }
        }

        // Results Summary
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${services.size} curated service(s) available",
                    style = MaterialTheme.typography.labelMedium,
                    color = MutedTaupe
                )
            }
        }

        if (services.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = AlabasterSurface,
                    border = BorderStroke(1.dp, WarmStoneOutline),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "No matching experiences",
                            style = MaterialTheme.typography.titleMedium,
                            color = DeepCharcoal
                        )
                        Text(
                            text = "Try broadening your city, date, or price criteria.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MutedTaupe
                        )
                        OutlinedButton(onClick = onClearFilters) {
                            Text("Clear Filters")
                        }
                    }
                }
            }
        } else {
            items(services, key = { it.id }) { service ->
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ServiceEditorialCard(
                        service = service,
                        isFavorite = service.id in favoriteIds,
                        onToggleFavorite = { onToggleFavorite(service, service.id in favoriteIds) },
                        onOpenDetails = { onOpenDetails(service) },
                        onBookNow = { onBookNow(service) }
                    )
                    TextButton(
                        onClick = { onComparePackages(service) },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CompareArrows,
                            contentDescription = null,
                            tint = DeepBronze
                        )
                        Spacer(modifier = Modifier.padding(2.dp))
                        Text("Compare ${service.packages.size} Packages", color = DeepBronze)
                    }
                }
            }
        }
    }
}
