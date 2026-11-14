package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import com.example.model.PaymentMethod
import com.example.model.SavedAddress
import com.example.model.ServicePackageTier
import com.example.ui.theme.AlabasterSurface
import com.example.ui.theme.AmberContainer
import com.example.ui.theme.AmberOchre
import com.example.ui.theme.ChampagneGold
import com.example.ui.theme.ChampagneGoldLight
import com.example.ui.theme.CrimsonAlert
import com.example.ui.theme.CrimsonContainer
import com.example.ui.theme.DeepBronze
import com.example.ui.theme.DeepCharcoal
import com.example.ui.theme.IvoryBackground
import com.example.ui.theme.IvorySurface
import com.example.ui.theme.MutedTaupe
import com.example.ui.theme.SageContainer
import com.example.ui.theme.SageEmerald
import com.example.ui.theme.WarmStoneOutline
import com.example.ui.viewmodel.BookingDraft
import com.example.util.UpiIntentHelper
import com.example.util.formatInr
import java.util.UUID

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BookingWizardScreen(
    draft: BookingDraft,
    savedAddresses: List<SavedAddress>,
    onClose: () -> Unit,
    onSetStep: (Int) -> Unit,
    onUpdateLocation: (city: String, address: String) -> Unit,
    onUpdateDate: (String) -> Unit,
    onUpdateTimeSlot: (String) -> Unit,
    onUpdatePackage: (ServicePackageTier) -> Unit,
    onUpdateNotesPromo: (notes: String, promo: String) -> Unit,
    onUpdateConsent: (Boolean) -> Unit,
    onUpdatePayment: (PaymentMethod, String) -> Unit,
    onInitiateOrder: ((bookingCode: String, totalInr: Int, serviceTitle: String) -> Unit) -> Unit,
    onConfirmGateway: (gatewayRef: String) -> Unit
) {
    val service = draft.service ?: return
    val pkg = draft.selectedPackage ?: service.packages.first()
    val context = LocalContext.current
    var upiLaunchHint by remember { mutableStateOf<String?>(null) }

    BackHandler {
        if (draft.step in 3..8 && draft.awaitingBookingId == null) {
            onSetStep(draft.step - 1)
        } else {
            onClose()
        }
    }

    val subtotal = ((pkg.priceInr * (100 - draft.discountPercent)) / 100.0).toInt()
    val tax = (subtotal * 0.18).toInt()
    val total = subtotal + tax

    val stepTitle = when (draft.step) {
        1 -> "1. Selected Service"
        2 -> "2. Location & Address"
        3 -> "3. Select Date"
        4 -> "4. Select Time Slot"
        5 -> "5. Select Package"
        6 -> "6. Review Booking Summary"
        7 -> "7. Terms & Safety Consent"
        8 -> "8. Secure Payment"
        else -> "9. Booking Confirmed"
    }

    Scaffold(
        containerColor = IvoryBackground,
        topBar = {
            Surface(color = IvorySurface, border = BorderStroke(1.dp, WarmStoneOutline)) {
                Column(modifier = Modifier.statusBarsPadding().padding(horizontal = 16.dp, vertical = 12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(onClick = onClose) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Close booking")
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("AUREVIA RESERVATION", style = MaterialTheme.typography.labelSmall, color = DeepBronze)
                            Text(stepTitle, style = MaterialTheme.typography.titleMedium, color = DeepCharcoal)
                        }
                        Spacer(modifier = Modifier.width(48.dp))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { draft.step / 9f },
                        color = ChampagneGold,
                        trackColor = AlabasterSurface,
                        modifier = Modifier.fillMaxWidth().height(5.dp)
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (draft.errorMessage != null) {
                Surface(shape = RoundedCornerShape(12.dp), color = CrimsonContainer, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = draft.errorMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = CrimsonAlert,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            AnimatedContent(targetState = draft.step, label = "booking_step") { step ->
                when (step) {
                    1 -> {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = IvorySurface,
                                border = BorderStroke(1.dp, WarmStoneOutline),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(service.category.displayName.uppercase(), style = MaterialTheme.typography.labelSmall, color = DeepBronze)
                                    Text(service.title, style = MaterialTheme.typography.titleLarge, color = DeepCharcoal)
                                    Text(service.shortSummary, style = MaterialTheme.typography.bodySmall, color = MutedTaupe)
                                }
                            }
                            Button(
                                onClick = { onSetStep(2) },
                                modifier = Modifier.fillMaxWidth().height(50.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = DeepCharcoal)
                            ) { Text("Continue to Location") }
                        }
                    }

                    2 -> {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("Choose Service City", style = MaterialTheme.typography.titleMedium)
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                service.cities.forEach { city ->
                                    val selected = draft.city == city
                                    Surface(
                                        onClick = { onUpdateLocation(city, draft.addressSummary) },
                                        shape = RoundedCornerShape(50),
                                        color = if (selected) DeepCharcoal else IvorySurface,
                                        border = BorderStroke(1.dp, WarmStoneOutline)
                                    ) {
                                        Text(
                                            text = city,
                                            color = if (selected) IvorySurface else DeepCharcoal,
                                            style = MaterialTheme.typography.labelMedium,
                                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                        )
                                    }
                                }
                            }
                            if (savedAddresses.isNotEmpty()) {
                                Text("Saved Addresses", style = MaterialTheme.typography.titleSmall)
                                savedAddresses.forEach { addr ->
                                    val summary = "${addr.label}: ${addr.line1}, ${addr.city} ${addr.postalCode}"
                                    Surface(
                                        onClick = { onUpdateLocation(addr.city, summary) },
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (draft.addressSummary == summary) ChampagneGoldLight else IvorySurface,
                                        border = BorderStroke(1.dp, WarmStoneOutline),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(summary, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(12.dp))
                                    }
                                }
                            }
                            OutlinedTextField(
                                value = draft.addressSummary,
                                onValueChange = { onUpdateLocation(draft.city, it) },
                                label = { Text("Venue / Residence / Hotel Address") },
                                placeholder = { Text("e.g., Taj Mahal Palace Suite or Partner Sanctuary") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Button(
                                onClick = { onSetStep(3) },
                                modifier = Modifier.fillMaxWidth().height(50.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = DeepCharcoal)
                            ) { Text("Continue to Date") }
                        }
                    }

                    3 -> {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("Select Preferred Date", style = MaterialTheme.typography.titleMedium)
                            service.availableDates.forEach { dateStr ->
                                val selected = draft.scheduledDate == dateStr
                                Surface(
                                    onClick = { onUpdateDate(dateStr) },
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (selected) ChampagneGoldLight else IvorySurface,
                                    border = BorderStroke(1.dp, if (selected) ChampagneGold else WarmStoneOutline),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = dateStr,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = DeepCharcoal,
                                        modifier = Modifier.padding(14.dp)
                                    )
                                }
                            }
                            StepNavButtons(onBack = { onSetStep(2) }, onNext = { onSetStep(4) }, nextLabel = "Continue to Time")
                        }
                    }

                    4 -> {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("Select Available Time Slot (${draft.scheduledDate})", style = MaterialTheme.typography.titleMedium)
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                service.availableTimeSlots.forEach { slot ->
                                    val selected = draft.scheduledTimeSlot == slot
                                    Surface(
                                        onClick = { onUpdateTimeSlot(slot) },
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (selected) DeepCharcoal else IvorySurface,
                                        border = BorderStroke(1.dp, WarmStoneOutline)
                                    ) {
                                        Text(
                                            text = slot,
                                            color = if (selected) IvorySurface else DeepCharcoal,
                                            style = MaterialTheme.typography.labelLarge,
                                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                                        )
                                    }
                                }
                            }
                            StepNavButtons(onBack = { onSetStep(3) }, onNext = { onSetStep(5) }, nextLabel = "Continue to Package")
                        }
                    }

                    5 -> {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("Confirm Experience Package", style = MaterialTheme.typography.titleMedium)
                            service.packages.forEach { tier ->
                                val selected = pkg.id == tier.id
                                Surface(
                                    onClick = { onUpdatePackage(tier) },
                                    shape = RoundedCornerShape(14.dp),
                                    color = if (selected) ChampagneGoldLight else IvorySurface,
                                    border = BorderStroke(if (selected) 2.dp else 1.dp, if (selected) ChampagneGold else WarmStoneOutline),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text("${tier.name} (${tier.durationMinutes} min)", style = MaterialTheme.typography.titleMedium)
                                            Text(formatInr(tier.priceInr), style = MaterialTheme.typography.titleMedium, color = DeepBronze)
                                        }
                                        Text(tier.summary, style = MaterialTheme.typography.bodySmall, color = MutedTaupe)
                                    }
                                }
                            }
                            StepNavButtons(onBack = { onSetStep(4) }, onNext = { onSetStep(6) }, nextLabel = "Review Booking")
                        }
                    }

                    6 -> {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = IvorySurface,
                                border = BorderStroke(1.dp, WarmStoneOutline),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(service.title, style = MaterialTheme.typography.titleLarge)
                                    Text("${pkg.name} • ${pkg.durationMinutes} min", style = MaterialTheme.typography.labelMedium, color = DeepBronze)
                                    HorizontalDivider(color = WarmStoneOutline)
                                    SummaryRow("City & Venue", "${draft.city} — ${draft.addressSummary.ifBlank { "Partner Sanctuary" }}")
                                    SummaryRow("Schedule", "${draft.scheduledDate} at ${draft.scheduledTimeSlot}")
                                    SummaryRow("Specialist", service.providerName)
                                    HorizontalDivider(color = WarmStoneOutline)
                                    SummaryRow("Package Subtotal", formatInr(pkg.priceInr))
                                    if (draft.discountPercent > 0) {
                                        SummaryRow("Member Privilege (${draft.promoCode})", "-${draft.discountPercent}%")
                                    }
                                    SummaryRow("GST (18%)", formatInr(tax))
                                    SummaryRow("Total Payable", formatInr(total), emphasize = true)
                                }
                            }
                            OutlinedTextField(
                                value = draft.promoCode,
                                onValueChange = { onUpdateNotesPromo(draft.guestNotes, it) },
                                label = { Text("Privilege Code (Optional: AUREVIA15 or PRIVILEGE10)") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = draft.guestNotes,
                                onValueChange = { onUpdateNotesPromo(it, draft.promoCode) },
                                label = { Text("Concierge Notes & Preferences") },
                                minLines = 2,
                                modifier = Modifier.fillMaxWidth()
                            )
                            StepNavButtons(onBack = { onSetStep(5) }, onNext = { onSetStep(7) }, nextLabel = "Continue to Terms")
                        }
                    }

                    7 -> {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = AlabasterSurface,
                                border = BorderStroke(1.dp, WarmStoneOutline),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Icon(Icons.Filled.VerifiedUser, contentDescription = null, tint = DeepBronze)
                                        Text("Service Boundaries & Mutual Consent Charter", style = MaterialTheme.typography.titleMedium)
                                    }
                                    Text(
                                        text = "1. I confirm I am 18 years or older and booking strictly lawful services.\n" +
                                            "2. For any social, event, or travel companionship booking, I acknowledge strict non-sexual boundaries in public/accredited venues.\n" +
                                            "3. Cancellation policy: ${service.cancellationPolicy}\n" +
                                            "4. Raw payment card numbers are never stored by AUREVIA GROUP.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = DeepCharcoal
                                    )
                                }
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Checkbox(
                                    checked = draft.consentAccepted,
                                    onCheckedChange = onUpdateConsent,
                                    modifier = Modifier.testTag("booking_consent_checkbox")
                                )
                                Text(
                                    text = "I accept the Aurevia Service Terms, Cancellation Policy, and Non-Sexual Boundary Charter.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = DeepCharcoal
                                )
                            }
                            StepNavButtons(
                                onBack = { onSetStep(6) },
                                onNext = { onSetStep(8) },
                                nextEnabled = draft.consentAccepted,
                                nextLabel = "Proceed to Payment"
                            )
                        }
                    }

                    8 -> {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = IvorySurface,
                                border = BorderStroke(1.dp, WarmStoneOutline),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Icon(Icons.Filled.Lock, contentDescription = null, tint = SageEmerald)
                                        Text("Tokenized Sandbox Checkout", style = MaterialTheme.typography.titleSmall)
                                    }
                                    Text(formatInr(total), style = MaterialTheme.typography.titleMedium, color = DeepBronze)
                                }
                            }

                            if (draft.awaitingBookingId == null) {
                                PaymentMethod.entries.forEach { method ->
                                    val selected = draft.paymentMethod == method
                                    Surface(
                                        onClick = { onUpdatePayment(method, draft.maskedInstrumentInput) },
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (selected) ChampagneGoldLight else IvorySurface,
                                        border = BorderStroke(1.dp, if (selected) ChampagneGold else WarmStoneOutline),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = method.label,
                                            style = MaterialTheme.typography.titleSmall,
                                            modifier = Modifier.padding(14.dp)
                                        )
                                    }
                                }

                                val fieldLabel = when (draft.paymentMethod) {
                                    PaymentMethod.UPI -> "UPI ID (e.g., member@okicici)"
                                    PaymentMethod.DEBIT_CARD, PaymentMethod.CREDIT_CARD -> "Last 4 Digits Only (Tokenized — Never Enter Full Card/CVV)"
                                    PaymentMethod.NET_BANKING -> "Bank Name (e.g., HDFC Bank, ICICI Bank)"
                                }
                                OutlinedTextField(
                                    value = draft.maskedInstrumentInput,
                                    onValueChange = { onUpdatePayment(draft.paymentMethod, it) },
                                    label = { Text(fieldLabel) },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Button(
                                    onClick = {
                                        onInitiateOrder { code, amount, title ->
                                            if (draft.paymentMethod == PaymentMethod.UPI) {
                                                val launched = UpiIntentHelper.launchUpiPayment(context, amount, code, title)
                                                upiLaunchHint = if (launched) {
                                                    "UPI app chooser opened. After approving in your UPI app, confirm gateway settlement below."
                                                } else {
                                                    "No native UPI app installed on this device. Complete sandbox gateway verification below."
                                                }
                                            }
                                        }
                                    },
                                    enabled = !draft.isSubmitting,
                                    colors = ButtonDefaults.buttonColors(containerColor = DeepCharcoal),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(52.dp)
                                        .testTag("initiate_payment_order_button")
                                ) {
                                    if (draft.isSubmitting) {
                                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = ChampagneGold, strokeWidth = 2.dp)
                                    } else {
                                        Text("Initiate Secure Order (${formatInr(total)})")
                                    }
                                }
                            } else {
                                // Order exists in Firestore as PENDING_PAYMENT; require explicit gateway confirmation
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = AmberContainer,
                                    border = BorderStroke(1.dp, ChampagneGold),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                        Text(
                                            text = "Order Created • Awaiting Gateway Settlement",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = AmberOchre
                                        )
                                        Text(
                                            text = upiLaunchHint
                                                ?: "Your reservation order is recorded in Firestore with PENDING_PAYMENT status. Booking confirmation is issued strictly after gateway settlement.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = DeepCharcoal
                                        )
                                        Button(
                                            onClick = {
                                                val ref = "${draft.paymentMethod.code}-${UUID.randomUUID().toString().take(8).uppercase()}"
                                                onConfirmGateway(ref)
                                            },
                                            enabled = !draft.isSubmitting,
                                            colors = ButtonDefaults.buttonColors(containerColor = SageEmerald),
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(50.dp)
                                                .testTag("confirm_gateway_settlement_button")
                                        ) {
                                            if (draft.isSubmitting) {
                                                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = IvorySurface, strokeWidth = 2.dp)
                                            } else {
                                                Text("Verify & Confirm Payment Settlement")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    else -> {
                        // Step 9: Confirmed Booking + Receipt
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = SageContainer,
                            border = BorderStroke(1.dp, SageEmerald.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(22.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(Icons.Filled.CheckCircle, contentDescription = "Confirmed", tint = SageEmerald, modifier = Modifier.size(48.dp))
                                Text("Reservation Confirmed", style = MaterialTheme.typography.headlineSmall, color = DeepCharcoal)
                                Text(
                                    text = "Booking ID: ${draft.confirmedBookingCode ?: "AUR-CONFIRMED"}",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = SageEmerald
                                )
                                Text(
                                    text = "Gateway Ref: ${draft.confirmedGatewayRef ?: "VERIFIED"}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = DeepCharcoal
                                )
                                Text(
                                    text = "${service.title} (${pkg.name}) on ${draft.scheduledDate} at ${draft.scheduledTimeSlot}. Your itemized receipt and itinerary are saved in Bookings.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MutedTaupe
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Button(
                                    onClick = onClose,
                                    colors = ButtonDefaults.buttonColors(containerColor = DeepCharcoal),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth().height(50.dp)
                                ) {
                                    Text("View My Bookings")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StepNavButtons(
    onBack: () -> Unit,
    onNext: () -> Unit,
    nextLabel: String,
    nextEnabled: Boolean = true
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedButton(
            onClick = onBack,
            modifier = Modifier.weight(1f).height(50.dp),
            shape = RoundedCornerShape(12.dp)
        ) { Text("Back") }
        Button(
            onClick = onNext,
            enabled = nextEnabled,
            modifier = Modifier.weight(2f).height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = DeepCharcoal)
        ) { Text(nextLabel) }
    }
}

@Composable
private fun SummaryRow(label: String, value: String, emphasize: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = if (emphasize) MaterialTheme.typography.titleSmall else MaterialTheme.typography.bodySmall,
            color = if (emphasize) DeepCharcoal else MutedTaupe
        )
        Text(
            text = value,
            style = if (emphasize) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodySmall,
            color = if (emphasize) DeepBronze else DeepCharcoal
        )
    }
}
