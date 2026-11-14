package com.example.ui.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.model.ServiceItem
import com.example.ui.components.StatusBadge
import com.example.ui.theme.AlabasterSurface
import com.example.ui.theme.CrimsonAlert
import com.example.ui.theme.DeepBronze
import com.example.ui.theme.DeepCharcoal
import com.example.ui.theme.IvoryBackground
import com.example.ui.theme.IvorySurface
import com.example.ui.theme.MutedTaupe
import com.example.ui.theme.WarmStoneOutline
import com.example.ui.viewmodel.PortalDashboardData
import com.example.util.NotificationHelper

@Composable
fun AccountScreen(
    dashboardData: PortalDashboardData?,
    allServices: List<ServiceItem>,
    isBusy: Boolean,
    onSaveProfile: (
        fullName: String,
        email: String,
        mobileNumber: String,
        preferredCity: String,
        emergencyContact: String,
        isAdultConfirmed: Boolean,
        notifyEmail: Boolean,
        notifySms: Boolean,
        notifyWhatsApp: Boolean,
        notifyPush: Boolean
    ) -> Unit,
    onAddAddress: (label: String, line1: String, city: String, postalCode: String) -> Unit,
    onDeleteAddress: (addressId: String) -> Unit,
    onOpenService: (ServiceItem) -> Unit,
    onMarkNotificationRead: (String) -> Unit,
    onSignOutSingleDevice: () -> Unit,
    onSignOutAllDevices: () -> Unit
) {
    val profile = dashboardData?.profile
    val context = LocalContext.current

    var fullName by remember(profile?.fullName) { mutableStateOf(profile?.fullName ?: "") }
    var email by remember(profile?.email) { mutableStateOf(profile?.email ?: "") }
    var mobileNumber by remember(profile?.mobileNumber) { mutableStateOf(profile?.mobileNumber ?: "") }
    var preferredCity by remember(profile?.preferredCity) { mutableStateOf(profile?.preferredCity ?: "Mumbai") }
    var emergencyContact by remember(profile?.emergencyContact) { mutableStateOf(profile?.emergencyContact ?: "") }
    var isAdult by remember(profile?.isAdultConfirmed) { mutableStateOf(profile?.isAdultConfirmed ?: false) }

    var notifyEmail by remember(profile?.notifyEmail) { mutableStateOf(profile?.notifyEmail ?: true) }
    var notifySms by remember(profile?.notifySms) { mutableStateOf(profile?.notifySms ?: true) }
    var notifyWhatsApp by remember(profile?.notifyWhatsApp) { mutableStateOf(profile?.notifyWhatsApp ?: false) }
    var notifyPush by remember(profile?.notifyPush) { mutableStateOf(profile?.notifyPush ?: true) }

    var showAddAddressDialog by remember { mutableStateOf(false) }

    val notifPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        notifyPush = granted
    }

    val favoriteServices = remember(dashboardData, allServices) {
        val favIds = dashboardData?.favorites?.map { it.serviceId }?.toSet() ?: emptySet()
        allServices.filter { it.id in favIds }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(IvoryBackground),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            Column {
                Text("MEMBER PORTAL", style = MaterialTheme.typography.labelSmall, color = DeepBronze)
                Text("My Account & Preferences", style = MaterialTheme.typography.headlineMedium, color = DeepCharcoal)
            }
        }

        // Profile Completion & Verification Card
        item {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = IvorySurface,
                border = BorderStroke(1.dp, WarmStoneOutline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Member Profile", style = MaterialTheme.typography.titleMedium)
                        StatusBadge(
                            label = if (isAdult && mobileNumber.isNotBlank()) "VERIFIED" else "COMPLETE PROFILE",
                            tone = if (isAdult && mobileNumber.isNotBlank()) "VERIFIED" else "PENDING"
                        )
                    }
                    OutlinedTextField(value = fullName, onValueChange = { fullName = it }, label = { Text("Full Name") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Verified Email") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = mobileNumber, onValueChange = { mobileNumber = it }, label = { Text("Mobile Number") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = preferredCity, onValueChange = { preferredCity = it }, label = { Text("Preferred City") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = emergencyContact, onValueChange = { emergencyContact = it }, label = { Text("Emergency Contact Name & Phone") }, modifier = Modifier.fillMaxWidth())

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = isAdult, onCheckedChange = { isAdult = it })
                        Text(
                            text = "I attest that I am 18+ years old and agree to Aurevia's lawful & non-sexual boundaries.",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    HorizontalDivider(color = WarmStoneOutline)
                    Text("Notification Channels (Lawful Opt-In)", style = MaterialTheme.typography.titleSmall)
                    ChannelToggleRow("Push Alerts (Booking & Refund Updates)", notifyPush) { enabled ->
                        if (enabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                            !NotificationHelper.hasNotificationPermission(context)
                        ) {
                            notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            notifyPush = enabled
                        }
                    }
                    ChannelToggleRow("Email Receipts & Confirmations", notifyEmail) { notifyEmail = it }
                    ChannelToggleRow("SMS Reminders", notifySms) { notifySms = it }
                    ChannelToggleRow("WhatsApp Concierge Updates", notifyWhatsApp) { notifyWhatsApp = it }

                    Button(
                        onClick = {
                            onSaveProfile(
                                fullName, email, mobileNumber, preferredCity, emergencyContact,
                                isAdult, notifyEmail, notifySms, notifyWhatsApp, notifyPush
                            )
                        },
                        enabled = !isBusy,
                        colors = ButtonDefaults.buttonColors(containerColor = DeepCharcoal),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("save_profile_button")
                    ) { Text("Save Profile & Preferences") }
                }
            }
        }

        // Notifications Inbox
        item {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = IvorySurface,
                border = BorderStroke(1.dp, WarmStoneOutline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Filled.Notifications, contentDescription = null, tint = DeepBronze)
                        Text("Notifications Inbox (${dashboardData?.notifications?.size ?: 0})", style = MaterialTheme.typography.titleMedium)
                    }
                    val notifications = dashboardData?.notifications.orEmpty()
                    if (notifications.isEmpty()) {
                        Text("No notifications yet. Booking, payment, and refund alerts appear here.", style = MaterialTheme.typography.bodySmall, color = MutedTaupe)
                    } else {
                        notifications.take(6).forEach { notif ->
                            Surface(
                                onClick = { if (!notif.isRead) onMarkNotificationRead(notif.id) },
                                shape = RoundedCornerShape(12.dp),
                                color = if (notif.isRead) AlabasterSurface else IvoryBackground,
                                border = BorderStroke(1.dp, WarmStoneOutline),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(notif.title, style = MaterialTheme.typography.titleSmall)
                                        if (!notif.isRead) StatusBadge("NEW", "PENDING")
                                    }
                                    Text(notif.message, style = MaterialTheme.typography.bodySmall, color = MutedTaupe)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Saved Addresses
        item {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = IvorySurface,
                border = BorderStroke(1.dp, WarmStoneOutline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Saved Addresses", style = MaterialTheme.typography.titleMedium)
                        TextButton(onClick = { showAddAddressDialog = true }) {
                            Icon(Icons.Filled.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add")
                        }
                    }
                    val addresses = dashboardData?.addresses.orEmpty()
                    if (addresses.isEmpty()) {
                        Text("No saved residences or hotels yet.", style = MaterialTheme.typography.bodySmall, color = MutedTaupe)
                    } else {
                        addresses.forEach { addr ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(addr.label, style = MaterialTheme.typography.titleSmall)
                                    Text("${addr.line1}, ${addr.city} ${addr.postalCode}", style = MaterialTheme.typography.bodySmall, color = MutedTaupe)
                                }
                                IconButton(onClick = { onDeleteAddress(addr.id) }) {
                                    Icon(Icons.Filled.DeleteOutline, contentDescription = "Remove address", tint = CrimsonAlert)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Favourite Services
        item {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = IvorySurface,
                border = BorderStroke(1.dp, WarmStoneOutline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Favourite Services", style = MaterialTheme.typography.titleMedium)
                    if (favoriteServices.isEmpty()) {
                        Text("Tap the heart icon on any service to save it here.", style = MaterialTheme.typography.bodySmall, color = MutedTaupe)
                    } else {
                        favoriteServices.forEach { svc ->
                            Surface(
                                onClick = { onOpenService(svc) },
                                shape = RoundedCornerShape(10.dp),
                                color = AlabasterSurface,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(svc.title, style = MaterialTheme.typography.titleSmall)
                                        Text(svc.category.displayName, style = MaterialTheme.typography.bodySmall, color = DeepBronze)
                                    }
                                    Text("View →", style = MaterialTheme.typography.labelSmall, color = DeepCharcoal)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Session Security & Logout From All Devices
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                    onClick = onSignOutSingleDevice,
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Filled.Logout, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Sign Out")
                }
                OutlinedButton(
                    onClick = onSignOutAllDevices,
                    border = BorderStroke(1.dp, CrimsonAlert.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("logout_all_devices_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Logout From All Devices", color = CrimsonAlert)
                }
            }
        }
    }

    if (showAddAddressDialog) {
        var label by remember { mutableStateOf("Residence") }
        var line1 by remember { mutableStateOf("") }
        var city by remember { mutableStateOf("Mumbai") }
        var postal by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddAddressDialog = false },
            containerColor = IvorySurface,
            title = { Text("Add Saved Address", style = MaterialTheme.typography.titleLarge) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = label, onValueChange = { label = it }, label = { Text("Label (Residence, Hotel, Office)") })
                    OutlinedTextField(value = line1, onValueChange = { line1 = it }, label = { Text("Street / Building / Suite") })
                    OutlinedTextField(value = city, onValueChange = { city = it }, label = { Text("City") })
                    OutlinedTextField(value = postal, onValueChange = { postal = it }, label = { Text("Postal / PIN Code") })
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onAddAddress(label, line1, city, postal)
                        showAddAddressDialog = false
                    },
                    enabled = line1.trim().length >= 3 && postal.trim().length >= 3,
                    colors = ButtonDefaults.buttonColors(containerColor = DeepCharcoal)
                ) { Text("Save Address") }
            },
            dismissButton = {
                TextButton(onClick = { showAddAddressDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun ChannelToggleRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = DeepCharcoal, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
