package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.model.FaqItem
import com.example.model.SupportMessage
import com.example.model.SupportTicket
import com.example.model.TicketCategory
import com.example.ui.components.SecureDialogWindowEffect
import com.example.ui.components.StatusBadge
import com.example.ui.theme.AlabasterSurface
import com.example.ui.theme.ChampagneGold
import com.example.ui.theme.ChampagneGoldLight
import com.example.ui.theme.CrimsonAlert
import com.example.ui.theme.CrimsonContainer
import com.example.ui.theme.DeepBronze
import com.example.ui.theme.DeepCharcoal
import com.example.ui.theme.IvoryBackground
import com.example.ui.theme.IvorySurface
import com.example.ui.theme.MutedTaupe
import com.example.ui.theme.WarmStoneOutline

@Composable
fun SupportScreen(
    faqs: List<FaqItem>,
    tickets: List<SupportTicket>,
    activeTicketId: String?,
    activeMessages: List<SupportMessage>,
    onSelectTicket: (String?) -> Unit,
    onCreateTicket: (bookingId: String, category: TicketCategory, subject: String, description: String) -> Unit,
    onSendChatMessage: (ticketId: String, body: String) -> Unit
) {
    var showCreateDialog by remember { mutableStateOf(false) }
    var preselectedCategory by remember { mutableStateOf(TicketCategory.GENERAL) }
    var expandedFaqId by remember { mutableStateOf<String?>(null) }
    var faqQuery by remember { mutableStateOf("") }

    val filteredFaqs = remember(faqs, faqQuery) {
        if (faqQuery.isBlank()) faqs
        else faqs.filter {
            it.question.contains(faqQuery, ignoreCase = true) ||
                it.answer.contains(faqQuery, ignoreCase = true) ||
                it.category.contains(faqQuery, ignoreCase = true)
        }
    }

    val activeTicket = tickets.firstOrNull { it.id == activeTicketId }

    if (activeTicket != null) {
        BackHandler { onSelectTicket(null) }
        var draftMsg by remember { mutableStateOf("") }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(IvoryBackground)
        ) {
            Surface(color = IvorySurface, border = BorderStroke(1.dp, WarmStoneOutline)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { onSelectTicket(null) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back to tickets")
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(activeTicket.subject, style = MaterialTheme.typography.titleMedium, color = DeepCharcoal)
                        Text("Live Aurevia Concierge • ${activeTicket.category}", style = MaterialTheme.typography.bodySmall, color = DeepBronze)
                    }
                    StatusBadge(label = activeTicket.status, tone = activeTicket.status)
                }
            }

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(activeMessages, key = { it.id }) { msg ->
                    val isCustomer = msg.senderRole == "CUSTOMER"
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (isCustomer) Arrangement.End else Arrangement.Start
                    ) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (isCustomer) DeepCharcoal else IvorySurface,
                            border = if (isCustomer) null else BorderStroke(1.dp, WarmStoneOutline),
                            modifier = Modifier.fillMaxWidth(0.84f)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = if (isCustomer) "You" else "Aurevia Concierge",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isCustomer) ChampagneGold else DeepBronze
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = msg.body,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (isCustomer) IvorySurface else DeepCharcoal
                                )
                            }
                        }
                    }
                }
            }

            Surface(color = IvorySurface, border = BorderStroke(1.dp, WarmStoneOutline)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = draftMsg,
                        onValueChange = { draftMsg = it },
                        placeholder = { Text("Message Aurevia Concierge...") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    IconButton(
                        onClick = {
                            if (draftMsg.isNotBlank()) {
                                onSendChatMessage(activeTicket.id, draftMsg.trim())
                                draftMsg = ""
                            }
                        },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = DeepCharcoal)
                    }
                }
            }
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(IvoryBackground),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text("24/7 CONCIERGE & TRUST DESK", style = MaterialTheme.typography.labelSmall, color = DeepBronze)
                Text("Customer Support Centre", style = MaterialTheme.typography.headlineMedium, color = DeepCharcoal)
            }
        }

        // Emergency Trust & Safety Escalation Banner
        item {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = CrimsonContainer,
                border = BorderStroke(1.dp, CrimsonAlert.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Filled.Shield, contentDescription = null, tint = CrimsonAlert)
                        Text("Emergency Safety Escalation", style = MaterialTheme.typography.titleMedium, color = CrimsonAlert)
                    }
                    Text(
                        text = "Experiencing an urgent boundary or safety concern during a service? Escalate directly to our live Trust & Safety duty officer.",
                        style = MaterialTheme.typography.bodySmall,
                        color = DeepCharcoal
                    )
                    Button(
                        onClick = {
                            preselectedCategory = TicketCategory.SAFETY_ESCALATION
                            showCreateDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CrimsonAlert),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("emergency_safety_escalation_button")
                    ) { Text("Immediate Safety Escalation") }
                }
            }
        }

        // Quick Support Actions
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Your Concierge Cases", style = MaterialTheme.typography.headlineSmall, color = DeepCharcoal)
                Button(
                    onClick = {
                        preselectedCategory = TicketCategory.GENERAL
                        showCreateDialog = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DeepCharcoal),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("new_support_ticket_button")
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("New Ticket")
                }
            }
        }

        if (tickets.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = AlabasterSurface,
                    border = BorderStroke(1.dp, WarmStoneOutline),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Filled.SupportAgent, contentDescription = null, tint = DeepBronze)
                        Text(
                            text = "Start a live concierge chat or open a ticket for booking help, refunds, or escalations.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MutedTaupe
                        )
                    }
                }
            }
        } else {
            items(tickets, key = { it.id }) { ticket ->
                Surface(
                    onClick = { onSelectTicket(ticket.id) },
                    shape = RoundedCornerShape(14.dp),
                    color = IvorySurface,
                    border = BorderStroke(1.dp, WarmStoneOutline),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(ticket.subject, style = MaterialTheme.typography.titleSmall, color = DeepCharcoal)
                            StatusBadge(label = ticket.status, tone = ticket.status)
                        }
                        Text(ticket.description, style = MaterialTheme.typography.bodySmall, color = MutedTaupe, maxLines = 2)
                        Text("Tap to open Live Concierge Chat →", style = MaterialTheme.typography.labelSmall, color = DeepBronze)
                    }
                }
            }
        }

        // FAQ Search & Accordion
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.padding(top = 6.dp)) {
                Text(
                    text = "Frequently Asked Questions",
                    style = MaterialTheme.typography.headlineSmall,
                    color = DeepCharcoal
                )
                OutlinedTextField(
                    value = faqQuery,
                    onValueChange = { faqQuery = it },
                    placeholder = { Text("Search FAQs (refunds, boundaries, UPI, privacy)...") },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        items(filteredFaqs, key = { it.id }) { faq ->
            val expanded = expandedFaqId == faq.id
            Surface(
                onClick = { expandedFaqId = if (expanded) null else faq.id },
                shape = RoundedCornerShape(14.dp),
                color = IvorySurface,
                border = BorderStroke(1.dp, WarmStoneOutline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(faq.category.uppercase(), style = MaterialTheme.typography.labelSmall, color = DeepBronze)
                            Text(faq.question, style = MaterialTheme.typography.titleSmall, color = DeepCharcoal)
                        }
                        Icon(
                            imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                            contentDescription = "Toggle answer"
                        )
                    }
                    AnimatedVisibility(visible = expanded) {
                        Text(
                            text = faq.answer,
                            style = MaterialTheme.typography.bodySmall,
                            color = MutedTaupe,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        var category by remember { mutableStateOf(preselectedCategory) }
        var subject by remember { mutableStateOf("") }
        var description by remember { mutableStateOf("") }
        var bookingId by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            containerColor = IvorySurface,
            title = { Text("Open Concierge Support Ticket", style = MaterialTheme.typography.titleLarge) },
            text = {
                SecureDialogWindowEffect()
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    TicketCategory.entries.forEach { cat ->
                        Surface(
                            onClick = { category = cat },
                            shape = RoundedCornerShape(8.dp),
                            color = if (category == cat) ChampagneGoldLight else AlabasterSurface,
                            border = BorderStroke(1.dp, if (category == cat) ChampagneGold else WarmStoneOutline),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(cat.label, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(8.dp))
                        }
                    }
                    OutlinedTextField(value = bookingId, onValueChange = { bookingId = it }, label = { Text("Booking ID (Optional)") })
                    OutlinedTextField(value = subject, onValueChange = { subject = it }, label = { Text("Subject") })
                    OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Details") }, minLines = 3)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onCreateTicket(bookingId, category, subject, description)
                        showCreateDialog = false
                    },
                    enabled = subject.trim().length >= 3 && description.trim().length >= 3,
                    colors = ButtonDefaults.buttonColors(containerColor = DeepCharcoal)
                ) { Text("Submit & Start Chat") }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) { Text("Cancel") }
            }
        )
    }
}
