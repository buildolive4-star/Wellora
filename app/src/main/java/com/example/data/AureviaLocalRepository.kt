package com.example.data

import com.example.data.local.AppNotificationEntity
import com.example.data.local.AureviaDao
import com.example.data.local.BookingEntity
import com.example.data.local.FavoriteServiceEntity
import com.example.data.local.PaymentTransactionEntity
import com.example.data.local.SafetyReportEntity
import com.example.data.local.SavedAddressEntity
import com.example.data.local.ServiceReviewEntity
import com.example.data.local.SupportMessageEntity
import com.example.data.local.SupportTicketEntity
import com.example.data.local.UserProfileEntity
import com.example.data.local.toDomain
import com.example.model.AppNotification
import com.example.model.Booking
import com.example.model.BookingStatus
import com.example.model.FavoriteService
import com.example.model.NotificationType
import com.example.model.PaymentMethod
import com.example.model.PaymentStatus
import com.example.model.PaymentTransaction
import com.example.model.SavedAddress
import com.example.model.ServiceItem
import com.example.model.ServicePackageTier
import com.example.model.ServiceReview
import com.example.model.SupportMessage
import com.example.model.SupportTicket
import com.example.model.TicketCategory
import com.example.model.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class AureviaLocalRepository(
    private val dao: AureviaDao,
    private val userId: String
) {

    fun observeUserProfile(targetUserId: String): Flow<UserProfile?> =
        dao.observeUserProfile(targetUserId).map { it?.toDomain() }

    suspend fun ensureInitialProfile(
        targetUserId: String,
        fullName: String,
        email: String
    ): Result<Unit> = runCatching {
        val existing = dao.getUserProfile(targetUserId)
        val now = System.currentTimeMillis()
        if (existing == null) {
            dao.upsertUserProfile(
                UserProfileEntity(
                    userId = targetUserId,
                    fullName = fullName.trim().ifBlank { "Aurevia Member" }.take(80),
                    email = email.trim().take(120),
                    mobileNumber = "",
                    preferredCity = "Mumbai",
                    emergencyContact = "",
                    isAdultConfirmed = false,
                    blockedProviderIds = emptyList(),
                    notifyEmail = true,
                    notifySms = true,
                    notifyWhatsApp = false,
                    notifyPush = true,
                    sessionEpoch = 1L,
                    createdAt = now,
                    updatedAt = now
                )
            )
        }
    }

    suspend fun saveUserProfile(
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
    ): Result<Unit> = runCatching {
        val now = System.currentTimeMillis()
        val existing = dao.getUserProfile(userId)
        dao.upsertUserProfile(
            UserProfileEntity(
                userId = userId,
                fullName = fullName.trim().ifBlank { "Aurevia Member" }.take(80),
                email = email.trim().take(120),
                mobileNumber = mobileNumber.trim().take(25),
                preferredCity = preferredCity.trim().ifBlank { "Mumbai" }.take(60),
                emergencyContact = emergencyContact.trim().take(100),
                isAdultConfirmed = isAdultConfirmed,
                blockedProviderIds = existing?.blockedProviderIds.orEmpty(),
                notifyEmail = notifyEmail,
                notifySms = notifySms,
                notifyWhatsApp = notifyWhatsApp,
                notifyPush = notifyPush,
                sessionEpoch = existing?.sessionEpoch ?: 1L,
                createdAt = existing?.createdAt ?: now,
                updatedAt = now
            )
        )
    }

    suspend fun bumpSessionEpochForLogoutAllDevices(): Result<Unit> = runCatching {
        val existing = dao.getUserProfile(userId) ?: return@runCatching
        dao.upsertUserProfile(
            existing.copy(
                sessionEpoch = existing.sessionEpoch + 1L,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    fun observeAddresses(targetUserId: String): Flow<List<SavedAddress>> =
        dao.observeAddresses(targetUserId).map { list -> list.map { it.toDomain() } }

    suspend fun addAddress(
        label: String,
        line1: String,
        city: String,
        postalCode: String,
        isDefault: Boolean
    ): Result<String> = runCatching {
        val id = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()
        dao.insertAddress(
            SavedAddressEntity(
                id = id,
                userId = userId,
                label = label.trim().take(40),
                line1 = line1.trim().take(180),
                city = city.trim().take(60),
                postalCode = postalCode.trim().take(20),
                isDefault = isDefault,
                createdAt = now,
                updatedAt = now
            )
        )
        id
    }

    suspend fun deleteAddress(addressId: String): Result<Unit> = runCatching {
        dao.deleteAddress(addressId = addressId, userId = userId)
    }

    fun observeFavorites(targetUserId: String): Flow<List<FavoriteService>> =
        dao.observeFavorites(targetUserId).map { list -> list.map { it.toDomain() } }

    suspend fun toggleFavorite(service: ServiceItem, isCurrentlyFavorite: Boolean): Result<Unit> = runCatching {
        if (isCurrentlyFavorite) {
            dao.deleteFavorite(userId = userId, serviceId = service.id)
        } else {
            dao.insertFavorite(
                FavoriteServiceEntity(
                    id = "${userId}_${service.id}",
                    userId = userId,
                    serviceId = service.id,
                    serviceTitle = service.title.take(120),
                    category = service.category.displayName.take(60),
                    createdAt = System.currentTimeMillis()
                )
            )
        }
    }

    fun observeBookings(targetUserId: String): Flow<List<Booking>> =
        dao.observeBookings(targetUserId).map { list -> list.map { it.toDomain() } }

    suspend fun createPendingBookingOrder(
        service: ServiceItem,
        pkg: ServicePackageTier,
        city: String,
        addressSummary: String,
        scheduledDate: String,
        scheduledTimeSlot: String,
        guestNotes: String,
        paymentMethod: PaymentMethod,
        maskedInstrument: String,
        discountPercent: Int
    ): Result<Pair<String, String>> = runCatching {
        val bookingId = UUID.randomUUID().toString()
        val paymentId = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()

        val datePart = SimpleDateFormat("yyyyMMdd", Locale.US).format(Date(now))
        val suffix = UUID.randomUUID().toString().take(4).uppercase(Locale.US)
        val bookingCode = "AUR-$datePart-$suffix"
        val receiptNumber = "RCP-$datePart-$suffix"

        val discountedSubtotal = ((pkg.priceInr * (100 - discountPercent)) / 100.0).toInt().coerceAtLeast(0)
        val taxInr = (discountedSubtotal * 0.18).toInt()
        val totalInr = discountedSubtotal + taxInr

        dao.upsertBooking(
            BookingEntity(
                id = bookingId,
                userId = userId,
                bookingCode = bookingCode,
                serviceId = service.id,
                serviceTitle = service.title.take(120),
                category = service.category.displayName.take(60),
                providerName = service.providerName.take(80),
                packageName = "${pkg.name} (${pkg.durationMinutes} min)".take(60),
                city = city.trim().take(60),
                addressSummary = addressSummary.trim().take(200),
                scheduledDate = scheduledDate.trim().take(40),
                scheduledTimeSlot = scheduledTimeSlot.trim().take(40),
                durationMinutes = pkg.durationMinutes.toLong(),
                subtotalInr = discountedSubtotal.toLong(),
                taxInr = taxInr.toLong(),
                totalInr = totalInr.toLong(),
                guestNotes = guestNotes.trim().take(400),
                consentAccepted = true,
                status = BookingStatus.PENDING_PAYMENT.code,
                paymentStatus = PaymentStatus.PENDING.code,
                paymentMethod = paymentMethod.code,
                transactionRef = paymentId,
                cancellationReason = "",
                createdAt = now,
                updatedAt = now
            )
        )

        dao.upsertPayment(
            PaymentTransactionEntity(
                id = paymentId,
                userId = userId,
                bookingId = bookingId,
                bookingCode = bookingCode,
                method = paymentMethod.code,
                maskedInstrument = maskedInstrument.trim().ifBlank { paymentMethod.label }.take(60),
                amountInr = totalInr.toLong(),
                status = "INITIATED",
                receiptNumber = receiptNumber,
                gatewayRef = "",
                createdAt = now,
                updatedAt = now
            )
        )

        dao.insertNotification(
            AppNotificationEntity(
                id = UUID.randomUUID().toString(),
                userId = userId,
                title = "Order Initiated ($bookingCode)",
                message = "Awaiting gateway payment confirmation for ${service.title}.",
                type = NotificationType.PAYMENT.code,
                isRead = false,
                relatedId = bookingId,
                createdAt = now
            )
        )

        bookingId to paymentId
    }

    suspend fun confirmGatewayPayment(
        bookingId: String,
        paymentId: String,
        bookingCode: String,
        serviceTitle: String,
        gatewayRef: String
    ): Result<Unit> = runCatching {
        val now = System.currentTimeMillis()
        val cleanRef = gatewayRef.trim().take(80)
        dao.updateBookingPaymentStatus(
            bookingId = bookingId,
            userId = userId,
            status = BookingStatus.CONFIRMED.code,
            paymentStatus = PaymentStatus.PAID.code,
            transactionRef = cleanRef,
            updatedAt = now
        )
        dao.updatePaymentCaptured(
            paymentId = paymentId,
            userId = userId,
            status = "CAPTURED",
            gatewayRef = cleanRef,
            updatedAt = now
        )
        dao.insertNotification(
            AppNotificationEntity(
                id = UUID.randomUUID().toString(),
                userId = userId,
                title = "Booking Confirmed ($bookingCode)",
                message = "Payment captured ($cleanRef). Your $serviceTitle reservation is confirmed.",
                type = NotificationType.BOOKING.code,
                isRead = false,
                relatedId = bookingId,
                createdAt = now
            )
        )
    }

    suspend fun markBookingCompleted(booking: Booking): Result<Unit> = runCatching {
        val now = System.currentTimeMillis()
        dao.updateBookingStatus(
            bookingId = booking.id,
            userId = userId,
            status = BookingStatus.COMPLETED.code,
            updatedAt = now
        )
        dao.insertNotification(
            AppNotificationEntity(
                id = UUID.randomUUID().toString(),
                userId = userId,
                title = "Experience Completed (${booking.bookingCode})",
                message = "Thank you for choosing Aurevia. You may now leave a verified review for ${booking.serviceTitle}.",
                type = NotificationType.BOOKING.code,
                isRead = false,
                relatedId = booking.id,
                createdAt = now
            )
        )
    }

    suspend fun rescheduleBooking(
        booking: Booking,
        newDate: String,
        newTimeSlot: String,
        newAddress: String
    ): Result<Unit> = runCatching {
        val now = System.currentTimeMillis()
        dao.rescheduleBooking(
            bookingId = booking.id,
            userId = userId,
            newDate = newDate.trim().take(40),
            newSlot = newTimeSlot.trim().take(40),
            newAddress = newAddress.trim().take(200),
            updatedAt = now
        )
        dao.insertNotification(
            AppNotificationEntity(
                id = UUID.randomUUID().toString(),
                userId = userId,
                title = "Booking Rescheduled (${booking.bookingCode})",
                message = "Updated to $newDate at $newTimeSlot.",
                type = NotificationType.BOOKING.code,
                isRead = false,
                relatedId = booking.id,
                createdAt = now
            )
        )
    }

    suspend fun cancelBookingAndRequestRefund(
        booking: Booking,
        reason: String,
        linkedPayment: PaymentTransaction?
    ): Result<Unit> = runCatching {
        val now = System.currentTimeMillis()
        val wasPaid = booking.paymentStatus == PaymentStatus.PAID.code
        val nextPayStatus = if (wasPaid) PaymentStatus.REFUND_REQUESTED.code else PaymentStatus.PENDING.code

        dao.cancelBooking(
            bookingId = booking.id,
            userId = userId,
            status = BookingStatus.CANCELLED.code,
            paymentStatus = nextPayStatus,
            reason = reason.trim().ifBlank { "Customer requested cancellation" }.take(240),
            updatedAt = now
        )
        if (wasPaid && linkedPayment != null && linkedPayment.status == "CAPTURED") {
            dao.updatePaymentStatus(
                paymentId = linkedPayment.id,
                userId = userId,
                status = "REFUND_INITIATED",
                updatedAt = now
            )
        }
        dao.insertSupportTicket(
            SupportTicketEntity(
                id = UUID.randomUUID().toString(),
                userId = userId,
                bookingId = booking.id,
                category = if (wasPaid) TicketCategory.REFUND.code else TicketCategory.CANCELLATION.code,
                subject = "Cancellation & Refund — ${booking.bookingCode}",
                description = "Reason: ${reason.trim().ifBlank { "Requested by member" }}",
                status = "IN_REVIEW",
                createdAt = now,
                updatedAt = now
            )
        )
        dao.insertNotification(
            AppNotificationEntity(
                id = UUID.randomUUID().toString(),
                userId = userId,
                title = "Cancellation Recorded (${booking.bookingCode})",
                message = if (wasPaid) {
                    "Your refund request has been initiated and is being tracked in Payments."
                } else {
                    "Your pending order has been cancelled."
                },
                type = NotificationType.REFUND.code,
                isRead = false,
                relatedId = booking.id,
                createdAt = now
            )
        )
    }

    fun observePayments(targetUserId: String): Flow<List<PaymentTransaction>> =
        dao.observePayments(targetUserId).map { list -> list.map { it.toDomain() } }

    fun observeAllVerifiedReviews(): Flow<List<ServiceReview>> =
        dao.observeAllVerifiedReviews().map { list -> list.map { it.toDomain() } }

    suspend fun submitVerifiedReview(
        booking: Booking,
        reviewerFullName: String,
        rating: Int,
        comment: String
    ): Result<String> = runCatching {
        require(booking.status == BookingStatus.COMPLETED.code) {
            "Only completed bookings may submit a public verified review."
        }
        val id = UUID.randomUUID().toString()
        dao.insertReview(
            ServiceReviewEntity(
                id = id,
                userId = userId,
                reviewerDisplayName = formatReviewerDisplayName(reviewerFullName),
                serviceId = booking.serviceId,
                bookingId = booking.id,
                rating = rating.coerceIn(1, 5).toLong(),
                comment = comment.trim().take(600),
                isVerifiedBooking = true,
                createdAt = System.currentTimeMillis()
            )
        )
        id
    }

    private fun formatReviewerDisplayName(fullName: String): String {
        val parts = fullName.trim().split(" ").filter { it.isNotBlank() }
        return when {
            parts.isEmpty() -> "Verified Member"
            parts.size == 1 -> parts[0].take(30)
            else -> "${parts[0]} ${parts[1].first().uppercaseChar()}.".take(40)
        }
    }

    fun observeSupportTickets(targetUserId: String): Flow<List<SupportTicket>> =
        dao.observeSupportTickets(targetUserId).map { list -> list.map { it.toDomain() } }

    fun observeTicketMessages(targetUserId: String, ticketId: String): Flow<List<SupportMessage>> =
        dao.observeTicketMessages(targetUserId, ticketId).map { list -> list.map { it.toDomain() } }

    suspend fun createSupportTicket(
        bookingId: String,
        category: TicketCategory,
        subject: String,
        description: String
    ): Result<String> = runCatching {
        val ticketId = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()
        dao.insertSupportTicket(
            SupportTicketEntity(
                id = ticketId,
                userId = userId,
                bookingId = bookingId.trim().take(128),
                category = category.code,
                subject = subject.trim().take(140),
                description = description.trim().take(1000),
                status = if (category == TicketCategory.SAFETY_ESCALATION) "IN_REVIEW" else "OPEN",
                createdAt = now,
                updatedAt = now
            )
        )
        dao.insertSupportMessage(
            SupportMessageEntity(
                id = UUID.randomUUID().toString(),
                ticketId = ticketId,
                userId = userId,
                senderRole = "CUSTOMER",
                body = description.trim().take(1000),
                createdAt = now
            )
        )
        dao.insertNotification(
            AppNotificationEntity(
                id = UUID.randomUUID().toString(),
                userId = userId,
                title = "Support Ticket Opened",
                message = "Concierge Desk logged: ${subject.trim().take(80)}",
                type = if (category == TicketCategory.SAFETY_ESCALATION) NotificationType.SAFETY.code else NotificationType.SUPPORT.code,
                isRead = false,
                relatedId = ticketId,
                createdAt = now
            )
        )
        ticketId
    }

    suspend fun sendSupportMessage(ticketId: String, body: String): Result<Unit> = runCatching {
        val now = System.currentTimeMillis()
        dao.insertSupportMessage(
            SupportMessageEntity(
                id = UUID.randomUUID().toString(),
                ticketId = ticketId,
                userId = userId,
                senderRole = "CUSTOMER",
                body = body.trim().take(1000),
                createdAt = now
            )
        )
        dao.insertSupportMessage(
            SupportMessageEntity(
                id = UUID.randomUUID().toString(),
                ticketId = ticketId,
                userId = userId,
                senderRole = "CONCIERGE",
                body = "Aurevia Member Concierge has received your message and logged it on case #${ticketId.take(6).uppercase(Locale.US)}. A dedicated specialist responds within 15 minutes.",
                createdAt = now + 1L
            )
        )
        dao.insertNotification(
            AppNotificationEntity(
                id = UUID.randomUUID().toString(),
                userId = userId,
                title = "Concierge Reply (#${ticketId.take(6).uppercase(Locale.US)})",
                message = "Aurevia Member Concierge replied to your support conversation.",
                type = NotificationType.SUPPORT.code,
                isRead = false,
                relatedId = ticketId,
                createdAt = now + 2L
            )
        )
    }

    suspend fun submitSafetyReport(
        serviceId: String,
        providerId: String,
        providerName: String,
        bookingId: String,
        reason: String,
        details: String,
        blockProvider: Boolean,
        currentBlockedIds: List<String>
    ): Result<Unit> = runCatching {
        val now = System.currentTimeMillis()
        val reportId = UUID.randomUUID().toString()
        dao.insertSafetyReport(
            SafetyReportEntity(
                id = reportId,
                userId = userId,
                serviceId = serviceId.take(64),
                providerName = providerName.take(80),
                bookingId = bookingId.take(128),
                reason = reason.trim().take(120),
                details = details.trim().take(1000),
                blockProvider = blockProvider,
                status = "ESCALATED",
                createdAt = now
            )
        )
        dao.insertNotification(
            AppNotificationEntity(
                id = UUID.randomUUID().toString(),
                userId = userId,
                title = "Safety Report Escalated",
                message = "Trust & Safety team received your report regarding $providerName.",
                type = NotificationType.SAFETY.code,
                isRead = false,
                relatedId = reportId,
                createdAt = now
            )
        )
        if (blockProvider && providerId.isNotBlank()) {
            val profile = dao.getUserProfile(userId)
            if (profile != null) {
                val updatedBlocked = (currentBlockedIds + providerId).distinct().take(50)
                dao.upsertUserProfile(
                    profile.copy(
                        blockedProviderIds = updatedBlocked,
                        updatedAt = now
                    )
                )
            }
        }
    }

    fun observeNotifications(targetUserId: String): Flow<List<AppNotification>> =
        dao.observeNotifications(targetUserId).map { list -> list.map { it.toDomain() } }

    suspend fun markNotificationRead(notificationId: String): Result<Unit> = runCatching {
        dao.markNotificationRead(notificationId = notificationId, userId = userId)
    }

    suspend fun markAllNotificationsRead(notificationIds: List<String>): Result<Unit> = runCatching {
        if (notificationIds.isEmpty()) return@runCatching
        dao.markAllNotificationsRead(userId = userId, notificationIds = notificationIds.take(400))
    }

    suspend fun sendBookingReminderNotification(booking: Booking): Result<Unit> = runCatching {
        dao.insertNotification(
            AppNotificationEntity(
                id = UUID.randomUUID().toString(),
                userId = userId,
                title = "Upcoming Reservation Reminder (${booking.bookingCode})",
                message = "${booking.serviceTitle} is scheduled on ${booking.scheduledDate} at ${booking.scheduledTimeSlot} (${booking.city}).",
                type = NotificationType.BOOKING.code,
                isRead = false,
                relatedId = booking.id,
                createdAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun createTestNotification(): Result<Unit> = runCatching {
        dao.insertNotification(
            AppNotificationEntity(
                id = UUID.randomUUID().toString(),
                userId = userId,
                title = "Aurevia Concierge Alerts Active",
                message = "Booking confirmations, payment receipts, refund updates, and concierge replies will notify you here.",
                type = NotificationType.BOOKING.code,
                isRead = false,
                relatedId = "test_alert",
                createdAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun finalizeRefund(
        booking: Booking,
        linkedPayment: PaymentTransaction?
    ): Result<Unit> = runCatching {
        val now = System.currentTimeMillis()
        dao.updateBookingRefundStatus(
            bookingId = booking.id,
            userId = userId,
            paymentStatus = PaymentStatus.REFUNDED.code,
            updatedAt = now
        )
        if (linkedPayment != null && linkedPayment.status == "REFUND_INITIATED") {
            dao.updatePaymentStatus(
                paymentId = linkedPayment.id,
                userId = userId,
                status = "REFUNDED",
                updatedAt = now
            )
        }
        dao.insertNotification(
            AppNotificationEntity(
                id = UUID.randomUUID().toString(),
                userId = userId,
                title = "Refund Credited (${booking.bookingCode})",
                message = "Your refund for ${booking.serviceTitle} has settled cleanly to your original payment method.",
                type = NotificationType.REFUND.code,
                isRead = false,
                relatedId = booking.id,
                createdAt = now
            )
        )
    }
}
