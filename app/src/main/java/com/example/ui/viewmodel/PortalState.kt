package com.example.ui.viewmodel

import com.example.model.AppNotification
import com.example.model.Booking
import com.example.model.FavoriteService
import com.example.model.PaymentMethod
import com.example.model.PaymentTransaction
import com.example.model.SavedAddress
import com.example.model.ServiceCategory
import com.example.model.ServiceItem
import com.example.model.ServicePackageTier
import com.example.model.ServiceReview
import com.example.model.SupportTicket
import com.example.model.UserProfile

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

data class DiscoveryFilter(
    val searchQuery: String = "",
    val category: ServiceCategory? = null,
    val city: String? = null,
    val date: String? = null,
    val maxPriceInr: Int = 35000,
    val minRating: Double = 0.0
)

data class BookingDraft(
    val step: Int = 1, // 1..9
    val service: ServiceItem? = null,
    val city: String = "Mumbai",
    val addressSummary: String = "",
    val scheduledDate: String = "",
    val scheduledTimeSlot: String = "",
    val selectedPackage: ServicePackageTier? = null,
    val guestNotes: String = "",
    val promoCode: String = "",
    val discountPercent: Int = 0,
    val consentAccepted: Boolean = false,
    val paymentMethod: PaymentMethod = PaymentMethod.UPI,
    val maskedInstrumentInput: String = "",
    val isSubmitting: Boolean = false,
    val awaitingBookingId: String? = null,
    val awaitingPaymentId: String? = null,
    val awaitingBookingCode: String? = null,
    val confirmedBookingCode: String? = null,
    val confirmedGatewayRef: String? = null,
    val errorMessage: String? = null
)

data class PortalDashboardData(
    val profile: UserProfile?,
    val addresses: List<SavedAddress>,
    val favorites: List<FavoriteService>,
    val bookings: List<Booking>,
    val payments: List<PaymentTransaction>,
    val reviews: List<ServiceReview>,
    val tickets: List<SupportTicket>,
    val notifications: List<AppNotification>
) {
    val unreadNotificationCount: Int
        get() = notifications.count { !it.isRead }
}
