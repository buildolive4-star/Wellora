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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.model.Booking
import com.example.model.BookingStatus
import com.example.model.PaymentTransaction
import com.example.ui.components.StatusBadge
import com.example.ui.theme.AlabasterSurface
import com.example.ui.theme.ChampagneGold
import com.example.ui.theme.CrimsonAlert
import com.example.ui.theme.DeepBronze
import com.example.ui.theme.DeepCharcoal
import com.example.ui.theme.IvoryBackground
import com.example.ui.theme.IvorySurface
import com.example.ui.theme.MutedTaupe
import com.example.ui.theme.SageEmerald
import com.example.ui.theme.WarmStoneOutline
import com.example.util.formatInr
import com.example.util.formatTimestamp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingsPaymentsScreen(
    bookings: List<Booking>,
    payments: List<PaymentTransaction>,
    reviewedBookingIds: Set<String>,
    onConfirmPendingOrder: (booking: Booking, payment: PaymentTransaction) -> Unit,
    onMarkCompleted: (Booking) -> Unit,
    onReschedule: (Booking, newDate: String, newSlot: String, newAddress: String) -> Unit,
    onCancelBooking: (Booking, reason: String, linkedPayment: PaymentTransaction?) -> Unit,
    onSubmitReview: (Booking, rating: Int, comment: String) -> Unit,
    onDiscoverServices: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Upcoming", "Completed", "Cancelled", "Payments")

    var reviewTarget by remember { mutableStateOf<Booking?>(null) }
    var cancelTarget by remember { mutableStateOf<Booking?>(null) }
    var rescheduleTarget by remember { mutableStateOf<Booking?>(null) }
    var receiptTarget by remember { mutableStateOf<PaymentTransaction?>(null) }

    val upcoming = remember(bookings) {
        bookings.filter {
            it.status == BookingStatus.CONFIRMED.code || it.status == BookingStatus.PENDING_PAYMENT.code
        }
    }
    val completed = remember(bookings) {
        bookings.filter { it.status == BookingStatus.COMPLETED.code }
    }
    val cancelled = remember(bookings) {
        bookings.filter { it.status == BookingStatus.CANCELLED.code }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(IvoryBackground)
    ) {
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp)) {
            Text("RESERVATIONS & LEDGER", style = MaterialTheme.typography.labelSmall, color = DeepBronze)
            Text("Bookings & Payments", style = MaterialTheme.typography.headlineMedium, color = DeepCharcoal)
        }

        PrimaryTabRow(
            selectedTabIndex = selectedTab,
            containerColor = IvorySurface,
            contentColor = DeepCharcoal
        ) {
            tabs.forEachIndexed { idx, title ->
                Tab(
                    selected = selectedTab == idx,
                    onClick = { selectedTab = idx },
                    text = { Text(title, style = MaterialTheme.typography.labelMedium) }
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            when (selectedTab) {
                0 -> {
                    if (upcoming.isEmpty()) {
                        item { EmptyBookingsCard("No upcoming reservations", onDiscoverServices) }
                    } else {
                        items(upcoming, key = { it.id }) { booking ->
                            val linkedPayment = payments.firstOrNull { it.bookingId == booking.id }
                            BookingCard(
                                booking = booking,
                                onConfirmOrder = if (booking.status == BookingStatus.PENDING_PAYMENT.code && linkedPayment != null) {
                                    { onConfirmPendingOrder(booking, linkedPayment) }
                                } else null,
                                onReschedule = { rescheduleTarget = booking },
                                onMarkCompleted = if (booking.status == BookingStatus.CONFIRMED.code) {
                                    { onMarkCompleted(booking) }
                                } else null,
                                onCancel = { cancelTarget = booking },
                                onWriteReview = null
                            )
                        }
                    }
                }
                1 -> {
                    if (completed.isEmpty()) {
                        item { EmptyBookingsCard("No completed experiences yet", onDiscoverServices) }
                    } else {
                        items(completed, key = { it.id }) { booking ->
                            val alreadyReviewed = booking.id in reviewedBookingIds
                            BookingCard(
                                booking = booking,
                                onConfirmOrder = null,
                                onReschedule = null,
                                onMarkCompleted = null,
                                onCancel = null,
                                onWriteReview = if (!alreadyReviewed) {
                                    { reviewTarget = booking }
                                } else null,
                                alreadyReviewed = alreadyReviewed
                            )
                        }
                    }
                }
                2 -> {
                    if (cancelled.isEmpty()) {
                        item { EmptyBookingsCard("No cancelled bookings", onDiscoverServices) }
                    } else {
                        items(cancelled, key = { it.id }) { booking ->
                            BookingCard(
                                booking = booking,
                                onConfirmOrder = null,
                                onReschedule = null,
                                onMarkCompleted = null,
                                onCancel = null,
                                onWriteReview = null
                            )
                        }
                    }
                }
                else -> {
                    if (payments.isEmpty()) {
                        item { EmptyBookingsCard("No transactions recorded yet", onDiscoverServices) }
                    } else {
                        items(payments, key = { it.id }) { tx ->
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = IvorySurface,
                                border = BorderStroke(1.dp, WarmStoneOutline),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(tx.receiptNumber, style = MaterialTheme.typography.titleSmall, color = DeepCharcoal)
                                        StatusBadge(label = tx.status, tone = tx.status)
                                    }
                                    Text("Booking ${tx.bookingCode} • ${tx.maskedInstrument}", style = MaterialTheme.typography.bodySmall, color = MutedTaupe)
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(formatInr(tx.amountInr), style = MaterialTheme.typography.titleMedium, color = DeepBronze)
                                        TextButton(onClick = { receiptTarget = tx }) {
                                            Icon(Icons.Filled.ReceiptLong, contentDescription = null)
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("View Receipt")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Verified Review Dialog
    reviewTarget?.let { target ->
        var rating by remember { mutableIntStateOf(5) }
        var comment by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { reviewTarget = null },
            containerColor = IvorySurface,
            title = { Text("Verified Review — ${target.serviceTitle}", style = MaterialTheme.typography.titleLarge) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Your public review displays only your first name and last initial for privacy.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MutedTaupe
                    )
                    Row {
                        (1..5).forEach { star ->
                            IconButton(onClick = { rating = star }) {
                                Icon(
                                    imageVector = Icons.Filled.Star,
                                    contentDescription = "$star stars",
                                    tint = if (star <= rating) ChampagneGold else WarmStoneOutline
                                )
                            }
                        }
                    }
                    OutlinedTextField(
                        value = comment,
                        onValueChange = { comment = it },
                        label = { Text("Share your experience") },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onSubmitReview(target, rating, comment)
                        reviewTarget = null
                    },
                    enabled = comment.trim().length >= 3,
                    colors = ButtonDefaults.buttonColors(containerColor = DeepCharcoal)
                ) { Text("Publish Verified Review") }
            },
            dismissButton = {
                TextButton(onClick = { reviewTarget = null }) { Text("Cancel") }
            }
        )
    }

    // Cancellation & Refund Dialog
    cancelTarget?.let { target ->
        var reason by remember { mutableStateOf("") }
        val linkedPayment = payments.firstOrNull { it.bookingId == target.id }
        AlertDialog(
            onDismissRequest = { cancelTarget = null },
            containerColor = IvorySurface,
            title = { Text("Cancel Booking ${target.bookingCode}", style = MaterialTheme.typography.titleLarge) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Paid reservations automatically initiate a tracked refund request and support ticket.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MutedTaupe
                    )
                    OutlinedTextField(
                        value = reason,
                        onValueChange = { reason = it },
                        label = { Text("Reason for cancellation") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onCancelBooking(target, reason, linkedPayment)
                        cancelTarget = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonAlert)
                ) { Text("Confirm Cancellation") }
            },
            dismissButton = {
                TextButton(onClick = { cancelTarget = null }) { Text("Keep Booking") }
            }
        )
    }

    // Reschedule Dialog
    rescheduleTarget?.let { target ->
        var newDate by remember { mutableStateOf(target.scheduledDate) }
        var newSlot by remember { mutableStateOf(target.scheduledTimeSlot) }
        var newAddress by remember { mutableStateOf(target.addressSummary) }
        AlertDialog(
            onDismissRequest = { rescheduleTarget = null },
            containerColor = IvorySurface,
            title = { Text("Reschedule ${target.bookingCode}", style = MaterialTheme.typography.titleLarge) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(value = newDate, onValueChange = { newDate = it }, label = { Text("Date (YYYY-MM-DD)") })
                    OutlinedTextField(value = newSlot, onValueChange = { newSlot = it }, label = { Text("Time Slot") })
                    OutlinedTextField(value = newAddress, onValueChange = { newAddress = it }, label = { Text("Address / Venue") })
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onReschedule(target, newDate, newSlot, newAddress)
                        rescheduleTarget = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DeepCharcoal)
                ) { Text("Save Changes") }
            },
            dismissButton = {
                TextButton(onClick = { rescheduleTarget = null }) { Text("Cancel") }
            }
        )
    }

    // Digital Receipt Modal
    receiptTarget?.let { tx ->
        AlertDialog(
            onDismissRequest = { receiptTarget = null },
            containerColor = IvorySurface,
            title = { Text("Official Payment Receipt", style = MaterialTheme.typography.titleLarge) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Receipt No: ${tx.receiptNumber}", style = MaterialTheme.typography.titleSmall)
                    Text("Booking Code: ${tx.bookingCode}", style = MaterialTheme.typography.bodySmall)
                    Text("Payment Method: ${tx.method}", style = MaterialTheme.typography.bodySmall)
                    Text("Instrument (Tokenized): ${tx.maskedInstrument}", style = MaterialTheme.typography.bodySmall)
                    Text("Gateway Ref: ${tx.gatewayRef.ifBlank { "Pending settlement" }}", style = MaterialTheme.typography.bodySmall)
                    Text("Status: ${tx.status}", style = MaterialTheme.typography.bodySmall, color = DeepBronze)
                    Text("Date: ${formatTimestamp(tx.createdAt)}", style = MaterialTheme.typography.bodySmall, color = MutedTaupe)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    Text("Amount: ${formatInr(tx.amountInr)}", style = MaterialTheme.typography.titleMedium, color = DeepCharcoal)
                }
            },
            confirmButton = {
                TextButton(onClick = { receiptTarget = null }) { Text("Done") }
            }
        )
    }
}

@Composable
private fun BookingCard(
    booking: Booking,
    onConfirmOrder: (() -> Unit)?,
    onReschedule: (() -> Unit)?,
    onMarkCompleted: (() -> Unit)?,
    onCancel: (() -> Unit)?,
    onWriteReview: (() -> Unit)?,
    alreadyReviewed: Boolean = false
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = IvorySurface,
        border = BorderStroke(1.dp, WarmStoneOutline),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("booking_card_${booking.bookingCode}")
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(booking.bookingCode, style = MaterialTheme.typography.labelMedium, color = DeepBronze)
                StatusBadge(label = BookingStatus.fromCode(booking.status).label, tone = booking.status)
            }
            Text(booking.serviceTitle, style = MaterialTheme.typography.titleLarge, color = DeepCharcoal)
            Text(
                text = "${booking.packageName} • ${booking.providerName}",
                style = MaterialTheme.typography.bodySmall,
                color = MutedTaupe
            )
            Text(
                text = "${booking.scheduledDate} at ${booking.scheduledTimeSlot} • ${booking.city}",
                style = MaterialTheme.typography.bodySmall,
                color = DeepCharcoal
            )
            Text(
                text = "Venue: ${booking.addressSummary}",
                style = MaterialTheme.typography.bodySmall,
                color = MutedTaupe
            )
            HorizontalDivider(color = WarmStoneOutline)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(formatInr(booking.totalInr), style = MaterialTheme.typography.titleMedium, color = DeepCharcoal)
                StatusBadge(label = "Pay: ${booking.paymentStatus}", tone = booking.paymentStatus)
            }

            if (onConfirmOrder != null) {
                Button(
                    onClick = onConfirmOrder,
                    colors = ButtonDefaults.buttonColors(containerColor = SageEmerald),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) { Text("Verify & Settle Pending Order") }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                if (onReschedule != null) {
                    OutlinedButton(onClick = onReschedule, modifier = Modifier.weight(1f), shape = RoundedCornerShape(10.dp)) {
                        Text("Reschedule")
                    }
                }
                if (onCancel != null) {
                    OutlinedButton(
                        onClick = onCancel,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, CrimsonAlert.copy(alpha = 0.4f))
                    ) { Text("Cancel", color = CrimsonAlert) }
                }
            }

            if (onMarkCompleted != null) {
                Button(
                    onClick = onMarkCompleted,
                    colors = ButtonDefaults.buttonColors(containerColor = DeepCharcoal),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) { Text("Mark Experience Completed") }
            }

            if (onWriteReview != null) {
                Button(
                    onClick = onWriteReview,
                    colors = ButtonDefaults.buttonColors(containerColor = ChampagneGold, contentColor = DeepCharcoal),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) { Text("Write Verified Review") }
            } else if (alreadyReviewed) {
                Text("Verified review submitted", style = MaterialTheme.typography.labelSmall, color = SageEmerald)
            }
        }
    }
}

@Composable
private fun EmptyBookingsCard(message: String, onExplore: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = AlabasterSurface,
        border = BorderStroke(1.dp, WarmStoneOutline),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(message, style = MaterialTheme.typography.titleMedium, color = DeepCharcoal)
            Button(
                onClick = onExplore,
                colors = ButtonDefaults.buttonColors(containerColor = DeepCharcoal),
                shape = RoundedCornerShape(10.dp)
            ) { Text("Discover Experiences") }
        }
    }
}
