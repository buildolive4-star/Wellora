package com.example.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AureviaFirestoreRepository
import com.example.data.CatalogRepository
import com.example.data.unwrapFirestoreException
import com.example.model.Booking
import com.example.model.PaymentMethod
import com.example.model.PaymentTransaction
import com.example.model.ServiceCategory
import com.example.model.ServiceItem
import com.example.model.ServicePackageTier
import com.example.model.SupportMessage
import com.example.model.SupportTicket
import com.example.model.TicketCategory
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface PortalEvent {
    data class ShowMessage(val text: String) : PortalEvent
    data class TriggerLocalNotification(val title: String, val body: String, val type: String = "BOOKING") : PortalEvent
    data object ForceRemoteSignOut : PortalEvent
}

@OptIn(ExperimentalCoroutinesApi::class)
class AureviaPortalViewModel(
    val catalog: CatalogRepository,
    private val repository: AureviaFirestoreRepository,
    private val currentUserId: String,
    initialDisplayName: String,
    initialEmail: String
) : ViewModel() {

    private var initialSessionEpoch: Long? = null
    private var initialNotificationsSeeded = false
    private val seenNotificationIds = mutableSetOf<String>()

    private val _events = MutableSharedFlow<PortalEvent>(extraBufferCapacity = 16)
    val events = _events.asSharedFlow()

    private val _filter = MutableStateFlow(DiscoveryFilter())
    val filter: StateFlow<DiscoveryFilter> = _filter.asStateFlow()

    private val _bookingDraft = MutableStateFlow(BookingDraft())
    val bookingDraft: StateFlow<BookingDraft> = _bookingDraft.asStateFlow()

    private val _selectedServiceId = MutableStateFlow<String?>(null)
    val selectedServiceId: StateFlow<String?> = _selectedServiceId.asStateFlow()

    private val _compareServiceId = MutableStateFlow<String?>(null)
    val compareServiceId: StateFlow<String?> = _compareServiceId.asStateFlow()

    private val _activeTicketId = MutableStateFlow<String?>(null)
    val activeTicketId: StateFlow<String?> = _activeTicketId.asStateFlow()

    private val _isBusy = MutableStateFlow(false)
    val isBusy: StateFlow<Boolean> = _isBusy.asStateFlow()

    val dashboardState: StateFlow<UiState<PortalDashboardData>> = combine(
        repository.observeUserProfile(currentUserId),
        repository.observeAddresses(currentUserId),
        repository.observeFavorites(currentUserId),
        repository.observeBookings(currentUserId),
        repository.observePayments(currentUserId),
        repository.observeAllVerifiedReviews(),
        repository.observeSupportTickets(currentUserId),
        repository.observeNotifications(currentUserId)
    ) { values ->
        @Suppress("UNCHECKED_CAST")
        val profile = values[0] as com.example.model.UserProfile?
        if (profile != null) {
            if (initialSessionEpoch == null) {
                initialSessionEpoch = profile.sessionEpoch
            } else if (profile.sessionEpoch > (initialSessionEpoch ?: 1L)) {
                _events.tryEmit(PortalEvent.ForceRemoteSignOut)
            }
        }
        @Suppress("UNCHECKED_CAST")
        val notifications = values[7] as List<com.example.model.AppNotification>
        if (!initialNotificationsSeeded) {
            seenNotificationIds.addAll(notifications.map { it.id })
            initialNotificationsSeeded = true
        } else {
            val pushEnabled = profile?.notifyPush != false
            notifications.forEach { notif ->
                if (notif.id.isNotBlank() && seenNotificationIds.add(notif.id) && !notif.isRead && pushEnabled) {
                    _events.tryEmit(
                        PortalEvent.TriggerLocalNotification(
                            title = notif.title,
                            body = notif.message,
                            type = notif.type
                        )
                    )
                }
            }
        }
        @Suppress("UNCHECKED_CAST")
        PortalDashboardData(
            profile = profile,
            addresses = values[1] as List<com.example.model.SavedAddress>,
            favorites = values[2] as List<com.example.model.FavoriteService>,
            bookings = values[3] as List<Booking>,
            payments = values[4] as List<PaymentTransaction>,
            reviews = values[5] as List<com.example.model.ServiceReview>,
            tickets = values[6] as List<SupportTicket>,
            notifications = notifications
        )
    }
        .map<PortalDashboardData, UiState<PortalDashboardData>> { UiState.Success(it) }
        .catch { error ->
            val fsError = unwrapFirestoreException(error)
            if (fsError == null && error is CancellationException) throw error
            val root = fsError ?: error
            Log.w(TAG, "Dashboard stream error", root)
            emit(UiState.Error(root.message ?: "Unable to sync your Aurevia portal."))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = UiState.Loading
        )

    val filteredServices: StateFlow<List<ServiceItem>> = combine(
        _filter,
        dashboardState
    ) { f, dash ->
        val blockedIds = (dash as? UiState.Success)?.data?.profile?.blockedProviderIds.orEmpty()
        catalog.services.filter { service ->
            val notBlocked = service.providerId !in blockedIds
            val matchesQuery = f.searchQuery.isBlank() ||
                service.title.contains(f.searchQuery, ignoreCase = true) ||
                service.shortSummary.contains(f.searchQuery, ignoreCase = true) ||
                service.providerName.contains(f.searchQuery, ignoreCase = true) ||
                service.category.displayName.contains(f.searchQuery, ignoreCase = true)
            val matchesCategory = f.category == null || service.category == f.category
            val matchesCity = f.city.isNullOrBlank() || service.cities.any { it.equals(f.city, ignoreCase = true) }
            val matchesDate = f.date.isNullOrBlank() || service.availableDates.contains(f.date)
            val matchesPrice = service.startingPriceInr <= f.maxPriceInr
            val matchesRating = service.rating >= f.minRating
            notBlocked && matchesQuery && matchesCategory && matchesCity && matchesDate && matchesPrice && matchesRating
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = catalog.services
    )

    val activeTicketMessages: StateFlow<List<SupportMessage>> = _activeTicketId
        .flatMapLatest { ticketId ->
            if (ticketId.isNullOrBlank()) {
                flowOf(emptyList())
            } else {
                repository.observeTicketMessages(currentUserId, ticketId)
                    .catch { error ->
                        val fsError = unwrapFirestoreException(error)
                        if (fsError == null && error is CancellationException) throw error
                        Log.w(TAG, "Ticket messages error", fsError ?: error)
                        emit(emptyList())
                    }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        )

    init {
        viewModelScope.launch {
            repository.ensureInitialProfile(
                userId = currentUserId,
                fullName = initialDisplayName,
                email = initialEmail
            )
        }
    }

    // --- DISCOVERY & SELECTION ---

    fun updateSearchQuery(query: String) = _filter.update { it.copy(searchQuery = query) }
    fun selectCategoryFilter(category: ServiceCategory?) = _filter.update { it.copy(category = category) }
    fun selectCityFilter(city: String?) = _filter.update { it.copy(city = city) }
    fun selectDateFilter(date: String?) = _filter.update { it.copy(date = date) }
    fun updateMaxPrice(price: Int) = _filter.update { it.copy(maxPriceInr = price) }
    fun updateMinRating(rating: Double) = _filter.update { it.copy(minRating = rating) }
    fun clearFilters() { _filter.value = DiscoveryFilter() }

    fun openServiceDetail(serviceId: String?) { _selectedServiceId.value = serviceId }
    fun openPackageCompare(serviceId: String?) { _compareServiceId.value = serviceId }
    fun openTicketChat(ticketId: String?) { _activeTicketId.value = ticketId }

    // --- BOOKING 9-STEP JOURNEY ---

    fun startBooking(service: ServiceItem, preselectedPackage: ServicePackageTier? = null) {
        val dash = (dashboardState.value as? UiState.Success)?.data
        val defaultCity = dash?.profile?.preferredCity?.takeIf { it in service.cities }
            ?: service.cities.first()
        val defaultAddr = dash?.addresses?.firstOrNull { it.city.equals(defaultCity, ignoreCase = true) }
            ?: dash?.addresses?.firstOrNull()
        val initialPkg = preselectedPackage ?: service.packages.getOrNull(1) ?: service.packages.first()

        _bookingDraft.value = BookingDraft(
            step = 2, // Step 1 (Service) is selected!
            service = service,
            city = defaultCity,
            addressSummary = defaultAddr?.let { "${it.label}: ${it.line1}, ${it.city}" } ?: "",
            scheduledDate = service.availableDates.first(),
            scheduledTimeSlot = service.availableTimeSlots.first(),
            selectedPackage = initialPkg,
            paymentMethod = PaymentMethod.UPI,
            maskedInstrumentInput = ""
        )
    }

    fun closeBookingWizard() {
        _bookingDraft.value = BookingDraft()
    }

    fun setBookingStep(step: Int) {
        _bookingDraft.update { it.copy(step = step.coerceIn(1, 9), errorMessage = null) }
    }

    fun updateBookingLocation(city: String, addressSummary: String) {
        _bookingDraft.update { it.copy(city = city, addressSummary = addressSummary, errorMessage = null) }
    }

    fun updateBookingDate(date: String) {
        _bookingDraft.update { it.copy(scheduledDate = date, errorMessage = null) }
    }

    fun updateBookingTimeSlot(slot: String) {
        _bookingDraft.update { it.copy(scheduledTimeSlot = slot, errorMessage = null) }
    }

    fun updateBookingPackage(pkg: ServicePackageTier) {
        _bookingDraft.update { it.copy(selectedPackage = pkg, errorMessage = null) }
    }

    fun updateBookingNotesAndPromo(notes: String, promoCode: String) {
        val normalized = promoCode.trim().uppercase()
        val discount = catalog.specialOffers.firstOrNull { it.code == normalized }?.discountPercent ?: 0
        _bookingDraft.update {
            it.copy(
                guestNotes = notes,
                promoCode = normalized,
                discountPercent = discount,
                errorMessage = null
            )
        }
    }

    fun updateBookingConsent(accepted: Boolean) {
        _bookingDraft.update { it.copy(consentAccepted = accepted, errorMessage = null) }
    }

    fun updatePaymentDetails(method: PaymentMethod, maskedInput: String) {
        _bookingDraft.update {
            it.copy(paymentMethod = method, maskedInstrumentInput = maskedInput, errorMessage = null)
        }
    }

    fun initiatePendingPaymentOrder(onCreated: (bookingCode: String, totalInr: Int, serviceTitle: String) -> Unit) {
        val draft = _bookingDraft.value
        val service = draft.service ?: return
        val pkg = draft.selectedPackage ?: return
        if (!draft.consentAccepted) {
            _bookingDraft.update { it.copy(errorMessage = "Please accept the service terms & safety charter.") }
            return
        }
        val masked = when (draft.paymentMethod) {
            PaymentMethod.UPI -> draft.maskedInstrumentInput.trim().ifBlank { "member@upi" }
            PaymentMethod.DEBIT_CARD -> "Debit •••• ${draft.maskedInstrumentInput.filter(Char::isDigit).takeLast(4).ifBlank { "4242" }}"
            PaymentMethod.CREDIT_CARD -> "Credit •••• ${draft.maskedInstrumentInput.filter(Char::isDigit).takeLast(4).ifBlank { "8810" }}"
            PaymentMethod.NET_BANKING -> draft.maskedInstrumentInput.trim().ifBlank { "HDFC NetBanking" }
        }

        viewModelScope.launch {
            _bookingDraft.update { it.copy(isSubmitting = true, errorMessage = null) }
            val result = repository.createPendingBookingOrder(
                service = service,
                pkg = pkg,
                city = draft.city,
                addressSummary = draft.addressSummary.ifBlank { "${draft.city} Residence / Partner Sanctuary" },
                scheduledDate = draft.scheduledDate,
                scheduledTimeSlot = draft.scheduledTimeSlot,
                guestNotes = draft.guestNotes,
                paymentMethod = draft.paymentMethod,
                maskedInstrument = masked,
                discountPercent = draft.discountPercent
            )
            result.onSuccess { (bookingId, paymentId) ->
                val subtotal = ((pkg.priceInr * (100 - draft.discountPercent)) / 100.0).toInt()
                val total = subtotal + (subtotal * 0.18).toInt()
                val code = (dashboardState.value as? UiState.Success)?.data?.bookings
                    ?.firstOrNull { it.id == bookingId }?.bookingCode ?: "AUR-${bookingId.take(6).uppercase()}"
                _bookingDraft.update {
                    it.copy(
                        isSubmitting = false,
                        awaitingBookingId = bookingId,
                        awaitingPaymentId = paymentId,
                        awaitingBookingCode = code
                    )
                }
                onCreated(code, total, service.title)
            }.onFailure { err ->
                _bookingDraft.update {
                    it.copy(isSubmitting = false, errorMessage = err.localizedMessage ?: "Could not initiate order")
                }
            }
        }
    }

    fun confirmSandboxGatewayPayment(gatewayRef: String) {
        val draft = _bookingDraft.value
        val bookingId = draft.awaitingBookingId ?: return
        val paymentId = draft.awaitingPaymentId ?: return
        val serviceTitle = draft.service?.title ?: "Aurevia Experience"
        val dash = (dashboardState.value as? UiState.Success)?.data
        val bookingCode = dash?.bookings?.firstOrNull { it.id == bookingId }?.bookingCode
            ?: draft.awaitingBookingCode
            ?: "AUR-CONFIRMED"

        viewModelScope.launch {
            _bookingDraft.update { it.copy(isSubmitting = true, errorMessage = null) }
            repository.confirmGatewayPayment(
                bookingId = bookingId,
                paymentId = paymentId,
                bookingCode = bookingCode,
                serviceTitle = serviceTitle,
                gatewayRef = gatewayRef
            ).onSuccess {
                _bookingDraft.update {
                    it.copy(
                        isSubmitting = false,
                        step = 9,
                        confirmedBookingCode = bookingCode,
                        confirmedGatewayRef = gatewayRef
                    )
                }
            }.onFailure { err ->
                _bookingDraft.update {
                    it.copy(isSubmitting = false, errorMessage = err.localizedMessage ?: "Confirmation failed")
                }
            }
        }
    }

    // --- ACCOUNT, BOOKINGS, REVIEWS, SUPPORT, SAFETY ---

    fun saveProfile(
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
    ) {
        viewModelScope.launch {
            _isBusy.value = true
            repository.saveUserProfile(
                fullName, email, mobileNumber, preferredCity, emergencyContact,
                isAdultConfirmed, notifyEmail, notifySms, notifyWhatsApp, notifyPush
            ).onSuccess {
                _events.tryEmit(PortalEvent.ShowMessage("Profile & preferences saved"))
            }.onFailure {
                _events.tryEmit(PortalEvent.ShowMessage(it.localizedMessage ?: "Failed to save profile"))
            }
            _isBusy.value = false
        }
    }

    fun addSavedAddress(label: String, line1: String, city: String, postalCode: String) {
        viewModelScope.launch {
            repository.addAddress(label, line1, city, postalCode, isDefault = false)
                .onSuccess { _events.tryEmit(PortalEvent.ShowMessage("Address saved")) }
                .onFailure { _events.tryEmit(PortalEvent.ShowMessage(it.localizedMessage ?: "Could not save address")) }
        }
    }

    fun deleteSavedAddress(addressId: String) {
        viewModelScope.launch {
            repository.deleteAddress(addressId)
                .onSuccess { _events.tryEmit(PortalEvent.ShowMessage("Address removed")) }
        }
    }

    fun toggleFavorite(service: ServiceItem, isFavorite: Boolean) {
        viewModelScope.launch {
            repository.toggleFavorite(service, isFavorite)
                .onSuccess {
                    _events.tryEmit(
                        PortalEvent.ShowMessage(if (isFavorite) "Removed from Favourites" else "Saved to Favourites")
                    )
                }
        }
    }

    fun markCompleted(booking: Booking) {
        viewModelScope.launch {
            repository.markBookingCompleted(booking)
                .onSuccess {
                    _events.tryEmit(PortalEvent.ShowMessage("Booking marked completed. Verified review unlocked."))
                }
                .onFailure {
                    _events.tryEmit(PortalEvent.ShowMessage(it.localizedMessage ?: "Failed to update booking"))
                }
        }
    }

    fun rescheduleBooking(booking: Booking, newDate: String, newSlot: String, newAddress: String) {
        viewModelScope.launch {
            repository.rescheduleBooking(booking, newDate, newSlot, newAddress)
                .onSuccess { _events.tryEmit(PortalEvent.ShowMessage("Booking rescheduled")) }
                .onFailure { _events.tryEmit(PortalEvent.ShowMessage(it.localizedMessage ?: "Failed to reschedule")) }
        }
    }

    fun cancelBooking(booking: Booking, reason: String, linkedPayment: PaymentTransaction?) {
        viewModelScope.launch {
            repository.cancelBookingAndRequestRefund(booking, reason, linkedPayment)
                .onSuccess {
                    _events.tryEmit(PortalEvent.ShowMessage("Booking cancelled and refund logged"))
                }
                .onFailure {
                    _events.tryEmit(PortalEvent.ShowMessage(it.localizedMessage ?: "Cancellation failed"))
                }
        }
    }

    fun submitReview(booking: Booking, rating: Int, comment: String) {
        val fullName = (dashboardState.value as? UiState.Success)?.data?.profile?.fullName ?: "Member"
        viewModelScope.launch {
            repository.submitVerifiedReview(booking, fullName, rating, comment)
                .onSuccess { _events.tryEmit(PortalEvent.ShowMessage("Verified review published")) }
                .onFailure { _events.tryEmit(PortalEvent.ShowMessage(it.localizedMessage ?: "Review failed")) }
        }
    }

    fun createTicket(bookingId: String, category: TicketCategory, subject: String, description: String) {
        viewModelScope.launch {
            repository.createSupportTicket(bookingId, category, subject, description)
                .onSuccess { ticketId ->
                    _activeTicketId.value = ticketId
                    _events.tryEmit(PortalEvent.ShowMessage("Concierge ticket opened"))
                }
                .onFailure { _events.tryEmit(PortalEvent.ShowMessage(it.localizedMessage ?: "Ticket creation failed")) }
        }
    }

    fun sendChatMessage(ticketId: String, message: String) {
        if (message.isBlank()) return
        viewModelScope.launch {
            repository.sendSupportMessage(ticketId, message)
                .onFailure { _events.tryEmit(PortalEvent.ShowMessage("Could not send message")) }
        }
    }

    fun submitSafetyReport(
        serviceId: String,
        providerId: String,
        providerName: String,
        bookingId: String,
        reason: String,
        details: String,
        blockProvider: Boolean
    ) {
        val currentBlocked = (dashboardState.value as? UiState.Success)?.data?.profile?.blockedProviderIds.orEmpty()
        viewModelScope.launch {
            repository.submitSafetyReport(
                serviceId, providerId, providerName, bookingId, reason, details, blockProvider, currentBlocked
            ).onSuccess {
                _events.tryEmit(
                    PortalEvent.ShowMessage(
                        if (blockProvider) "Report escalated & specialist blocked from your portal."
                        else "Trust & Safety report escalated."
                    )
                )
            }.onFailure {
                _events.tryEmit(PortalEvent.ShowMessage(it.localizedMessage ?: "Could not submit report"))
            }
        }
    }

    fun confirmPendingOrderFromLedger(booking: Booking, payment: PaymentTransaction) {
        val gatewayRef = "SETTLED-${java.util.UUID.randomUUID().toString().take(8).uppercase()}"
        viewModelScope.launch {
            repository.confirmGatewayPayment(
                bookingId = booking.id,
                paymentId = payment.id,
                bookingCode = booking.bookingCode,
                serviceTitle = booking.serviceTitle,
                gatewayRef = gatewayRef
            ).onSuccess {
                _events.tryEmit(PortalEvent.ShowMessage("Payment settled & booking ${booking.bookingCode} confirmed"))
            }.onFailure {
                _events.tryEmit(PortalEvent.ShowMessage(it.localizedMessage ?: "Settlement failed"))
            }
        }
    }

    fun sendBookingReminder(booking: Booking) {
        viewModelScope.launch {
            repository.sendBookingReminderNotification(booking)
                .onSuccess {
                    _events.tryEmit(PortalEvent.ShowMessage("Booking reminder sent for ${booking.bookingCode}"))
                }
                .onFailure {
                    _events.tryEmit(PortalEvent.ShowMessage(it.localizedMessage ?: "Could not send reminder"))
                }
        }
    }

    fun finalizeRefund(booking: Booking, linkedPayment: PaymentTransaction?) {
        viewModelScope.launch {
            repository.finalizeRefund(booking, linkedPayment)
                .onSuccess {
                    _events.tryEmit(PortalEvent.ShowMessage("Refund settled for ${booking.bookingCode}"))
                }
                .onFailure {
                    _events.tryEmit(PortalEvent.ShowMessage(it.localizedMessage ?: "Could not finalize refund"))
                }
        }
    }

    fun markNotificationRead(id: String) {
        viewModelScope.launch { repository.markNotificationRead(id) }
    }

    fun markAllNotificationsRead() {
        val unreadIds = (dashboardState.value as? UiState.Success)?.data?.notifications
            ?.filter { !it.isRead }
            ?.map { it.id }
            .orEmpty()
        if (unreadIds.isEmpty()) return
        viewModelScope.launch {
            repository.markAllNotificationsRead(unreadIds)
                .onSuccess { _events.tryEmit(PortalEvent.ShowMessage("All notifications marked read")) }
        }
    }

    fun sendTestNotification() {
        viewModelScope.launch {
            repository.createTestNotification()
                .onSuccess { _events.tryEmit(PortalEvent.ShowMessage("Test notification dispatched")) }
                .onFailure { _events.tryEmit(PortalEvent.ShowMessage(it.localizedMessage ?: "Notification test failed")) }
        }
    }

    suspend fun prepareLogoutAllDevices() {
        repository.bumpSessionEpochForLogoutAllDevices()
    }

    private companion object {
        const val TAG = "AureviaPortalVM"
    }
}
