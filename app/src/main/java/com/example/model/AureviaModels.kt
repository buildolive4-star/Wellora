package com.example.model

import androidx.annotation.DrawableRes

enum class ServiceCategory(val displayName: String, val tagline: String) {
    SPA_WELLNESS("Spa & Wellness", "Private thermal rituals & holistic bodywork"),
    FITNESS_TRAINING("Fitness & Training", "Bespoke coaching, Pilates & mobility"),
    LIFESTYLE_ASSISTANCE("Lifestyle Assistance", "Discreet errands, wardrobe & concierge"),
    HOSPITALITY("Hospitality Experiences", "Private dining, sommelier & residence hosting"),
    SOCIAL_COMPANIONSHIP("Social Companionship", "Strictly non-sexual 18+ cultural & dining company"),
    EVENT_TRAVEL("Event & Travel", "Gala, exhibition & destination itinerary companionship")
}

enum class BookingStatus(val code: String, val label: String) {
    PENDING_PAYMENT("PENDING_PAYMENT", "Awaiting Payment"),
    CONFIRMED("CONFIRMED", "Confirmed"),
    COMPLETED("COMPLETED", "Completed"),
    CANCELLED("CANCELLED", "Cancelled");

    companion object {
        fun fromCode(code: String): BookingStatus =
            entries.firstOrNull { it.code == code } ?: CONFIRMED
    }
}

enum class PaymentStatus(val code: String, val label: String) {
    PENDING("PENDING", "Pending"),
    PAID("PAID", "Paid"),
    REFUND_REQUESTED("REFUND_REQUESTED", "Refund In Review"),
    REFUNDED("REFUNDED", "Refunded");

    companion object {
        fun fromCode(code: String): PaymentStatus =
            entries.firstOrNull { it.code == code } ?: PENDING
    }
}

enum class PaymentMethod(val code: String, val label: String) {
    UPI("UPI", "UPI Instant Pay"),
    DEBIT_CARD("DEBIT_CARD", "Debit Card (Tokenized)"),
    CREDIT_CARD("CREDIT_CARD", "Credit Card (Tokenized)"),
    NET_BANKING("NET_BANKING", "Net Banking");

    companion object {
        fun fromCode(code: String): PaymentMethod =
            entries.firstOrNull { it.code == code } ?: UPI
    }
}

enum class TicketCategory(val code: String, val label: String) {
    COMPLAINT("COMPLAINT", "Booking Complaint"),
    REFUND("REFUND", "Refund Request"),
    CANCELLATION("CANCELLATION", "Cancellation Help"),
    SAFETY_ESCALATION("SAFETY_ESCALATION", "Safety Escalation"),
    GENERAL("GENERAL", "Concierge Inquiry");

    companion object {
        fun fromCode(code: String): TicketCategory =
            entries.firstOrNull { it.code == code } ?: GENERAL
    }
}

enum class NotificationType(val code: String) {
    BOOKING("BOOKING"),
    PAYMENT("PAYMENT"),
    REFUND("REFUND"),
    SUPPORT("SUPPORT"),
    SAFETY("SAFETY");

    companion object {
        fun fromCode(code: String): NotificationType =
            entries.firstOrNull { it.code == code } ?: BOOKING
    }
}

data class ServicePackageTier(
    val id: String,
    val name: String,
    val durationMinutes: Int,
    val priceInr: Int,
    val summary: String,
    val highlights: List<String>
)

data class ServiceItem(
    val id: String,
    val title: String,
    val category: ServiceCategory,
    val providerId: String,
    val providerName: String,
    val providerTitle: String,
    val shortSummary: String,
    val description: String,
    @DrawableRes val imageRes: Int,
    val startingPriceInr: Int,
    val durationRange: String,
    val rating: Double,
    val reviewCount: Int,
    val cities: List<String>,
    val availableDates: List<String>,
    val availableTimeSlots: List<String>,
    val includedItems: List<String>,
    val excludedAndBoundaries: List<String>,
    val cancellationPolicy: String,
    val safetyInformation: String,
    val isFeatured: Boolean,
    val isAdultVerifiedOnly: Boolean,
    val packages: List<ServicePackageTier>
)

data class CitySpot(
    val name: String,
    val regionLabel: String,
    val activeSpecialists: Int
)

data class SpecialOffer(
    val id: String,
    val badge: String,
    val title: String,
    val subtitle: String,
    val code: String,
    val discountPercent: Int
)

data class FaqItem(
    val id: String,
    val category: String,
    val question: String,
    val answer: String
)

// --- LOCAL PERSISTENCE DOMAIN MODELS ---

data class UserProfile(
    val id: String = "",
    val userId: String = "",
    val fullName: String = "",
    val email: String = "",
    val mobileNumber: String = "",
    val preferredCity: String = "Mumbai",
    val emergencyContact: String = "",
    val isAdultConfirmed: Boolean = false,
    val blockedProviderIds: List<String> = emptyList(),
    val notifyEmail: Boolean = true,
    val notifySms: Boolean = true,
    val notifyWhatsApp: Boolean = false,
    val notifyPush: Boolean = true,
    val sessionEpoch: Long = 1L,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class SavedAddress(
    val id: String = "",
    val userId: String = "",
    val label: String = "Residence",
    val line1: String = "",
    val city: String = "Mumbai",
    val postalCode: String = "",
    val isDefault: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class FavoriteService(
    val id: String = "",
    val userId: String = "",
    val serviceId: String = "",
    val serviceTitle: String = "",
    val category: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

data class Booking(
    val id: String = "",
    val userId: String = "",
    val bookingCode: String = "",
    val serviceId: String = "",
    val serviceTitle: String = "",
    val category: String = "",
    val providerName: String = "",
    val packageName: String = "",
    val city: String = "",
    val addressSummary: String = "",
    val scheduledDate: String = "",
    val scheduledTimeSlot: String = "",
    val durationMinutes: Long = 60L,
    val subtotalInr: Long = 0L,
    val taxInr: Long = 0L,
    val totalInr: Long = 0L,
    val guestNotes: String = "",
    val consentAccepted: Boolean = false,
    val status: String = BookingStatus.PENDING_PAYMENT.code,
    val paymentStatus: String = PaymentStatus.PENDING.code,
    val paymentMethod: String = PaymentMethod.UPI.code,
    val transactionRef: String = "",
    val cancellationReason: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class PaymentTransaction(
    val id: String = "",
    val userId: String = "",
    val bookingId: String = "",
    val bookingCode: String = "",
    val method: String = PaymentMethod.UPI.code,
    val maskedInstrument: String = "",
    val amountInr: Long = 0L,
    val status: String = "INITIATED",
    val receiptNumber: String = "",
    val gatewayRef: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class ServiceReview(
    val id: String = "",
    val userId: String = "",
    val reviewerDisplayName: String = "",
    val serviceId: String = "",
    val bookingId: String = "",
    val rating: Long = 5L,
    val comment: String = "",
    val isVerifiedBooking: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

data class SupportTicket(
    val id: String = "",
    val userId: String = "",
    val bookingId: String = "",
    val category: String = TicketCategory.GENERAL.code,
    val subject: String = "",
    val description: String = "",
    val status: String = "OPEN",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class SupportMessage(
    val id: String = "",
    val userId: String = "",
    val senderRole: String = "CUSTOMER",
    val body: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

data class SafetyReport(
    val id: String = "",
    val userId: String = "",
    val serviceId: String = "",
    val providerName: String = "",
    val bookingId: String = "",
    val reason: String = "",
    val details: String = "",
    val blockProvider: Boolean = false,
    val status: String = "SUBMITTED",
    val createdAt: Long = System.currentTimeMillis()
)

data class AppNotification(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val message: String = "",
    val type: String = NotificationType.BOOKING.code,
    val isRead: Boolean = false,
    val relatedId: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
