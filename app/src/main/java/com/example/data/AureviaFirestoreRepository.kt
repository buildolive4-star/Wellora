package com.example.data

import android.content.Context
import com.example.R
import com.example.model.AppNotification
import com.example.model.Booking
import com.example.model.BookingStatus
import com.example.model.FavoriteService
import com.example.model.NotificationType
import com.example.model.PaymentMethod
import com.example.model.PaymentStatus
import com.example.model.PaymentTransaction
import com.example.model.SafetyReport
import com.example.model.SavedAddress
import com.example.model.ServiceItem
import com.example.model.ServicePackageTier
import com.example.model.ServiceReview
import com.example.model.SupportMessage
import com.example.model.SupportTicket
import com.example.model.TicketCategory
import com.example.model.UserProfile
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class AureviaFirestoreRepository(private val db: FirebaseFirestore) {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val auth = Firebase.auth

    private fun requireUserId(): String =
        auth.currentUser?.uid
            ?: throw IllegalStateException("Customer must be authenticated with Google Sign-In.")

    // --- USER PROFILE ---

    fun observeUserProfile(userId: String): Flow<UserProfile?> =
        db.collection("users").document(userId)
            .snapshots()
            .map { snapshot ->
                if (snapshot.exists()) {
                    snapshot.toObject(UserProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                } else {
                    null
                }
            }
            .catch { error ->
                val fsError = unwrapFirestoreException(error)
                if (fsError == null && error is CancellationException) throw error
                if (auth.currentUser?.uid == userId) {
                    handleFirestoreError(fsError ?: error, OperationType.GET, "users/$userId")
                }
                throw error
            }

    suspend fun ensureInitialProfile(
        userId: String,
        fullName: String,
        email: String
    ): Result<Unit> = runCatching {
        val docRef = db.collection("users").document(userId)
        val snap = try {
            docRef.get().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, docRef.path)
            throw e
        }
        if (!snap.exists()) {
            val safeName = fullName.trim().ifBlank { "Aurevia Member" }.take(80)
            val payload = mapOf(
                "userId" to userId,
                "fullName" to safeName,
                "email" to email.trim().take(120),
                "mobileNumber" to "",
                "preferredCity" to "Mumbai",
                "emergencyContact" to "",
                "isAdultConfirmed" to false,
                "blockedProviderIds" to emptyList<String>(),
                "notifyEmail" to true,
                "notifySms" to true,
                "notifyWhatsApp" to false,
                "notifyPush" to true,
                "sessionEpoch" to 1L,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            try {
                docRef.set(payload).await()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                handleFirestoreError(e, OperationType.CREATE, docRef.path)
                throw e
            }
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
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid)
        val existing = try {
            docRef.get().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, docRef.path)
            throw e
        }

        if (!existing.exists()) {
            val payload = mapOf(
                "userId" to uid,
                "fullName" to fullName.trim().ifBlank { "Aurevia Member" }.take(80),
                "email" to email.trim().take(120),
                "mobileNumber" to mobileNumber.trim().take(25),
                "preferredCity" to preferredCity.trim().ifBlank { "Mumbai" }.take(60),
                "emergencyContact" to emergencyContact.trim().take(100),
                "isAdultConfirmed" to isAdultConfirmed,
                "blockedProviderIds" to emptyList<String>(),
                "notifyEmail" to notifyEmail,
                "notifySms" to notifySms,
                "notifyWhatsApp" to notifyWhatsApp,
                "notifyPush" to notifyPush,
                "sessionEpoch" to 1L,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            try {
                docRef.set(payload).await()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                handleFirestoreError(e, OperationType.CREATE, docRef.path)
                throw e
            }
        } else {
            val updates = mapOf(
                "fullName" to fullName.trim().ifBlank { "Aurevia Member" }.take(80),
                "email" to email.trim().take(120),
                "mobileNumber" to mobileNumber.trim().take(25),
                "preferredCity" to preferredCity.trim().ifBlank { "Mumbai" }.take(60),
                "emergencyContact" to emergencyContact.trim().take(100),
                "isAdultConfirmed" to isAdultConfirmed,
                "notifyEmail" to notifyEmail,
                "notifySms" to notifySms,
                "notifyWhatsApp" to notifyWhatsApp,
                "notifyPush" to notifyPush,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            try {
                docRef.update(updates).await()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                handleFirestoreError(e, OperationType.UPDATE, docRef.path)
                throw e
            }
        }
    }

    suspend fun bumpSessionEpochForLogoutAllDevices(): Result<Unit> = runCatching {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid)
        try {
            val snap = docRef.get().await()
            if (snap.exists()) {
                val currentEpoch = snap.getLong("sessionEpoch") ?: 1L
                docRef.update(
                    mapOf(
                        "sessionEpoch" to (currentEpoch + 1L),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    // --- SAVED ADDRESSES ---

    fun observeAddresses(userId: String): Flow<List<SavedAddress>> =
        db.collection("users").document(userId).collection("addresses")
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(SavedAddress::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    .sortedByDescending { it.isDefault }
            }
            .catch { error ->
                val fsError = unwrapFirestoreException(error)
                if (fsError == null && error is CancellationException) throw error
                if (auth.currentUser?.uid == userId) {
                    handleFirestoreError(fsError ?: error, OperationType.LIST, "users/$userId/addresses")
                }
                throw error
            }

    suspend fun addAddress(
        label: String,
        line1: String,
        city: String,
        postalCode: String,
        isDefault: Boolean
    ): Result<String> = runCatching {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("addresses").document()
        val payload = mapOf(
            "userId" to uid,
            "label" to label.trim().take(40),
            "line1" to line1.trim().take(180),
            "city" to city.trim().take(60),
            "postalCode" to postalCode.trim().take(20),
            "isDefault" to isDefault,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload).await()
            docRef.id
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    suspend fun deleteAddress(addressId: String): Result<Unit> = runCatching {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("addresses").document(addressId)
        try {
            docRef.delete().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }

    // --- FAVORITE SERVICES ---

    fun observeFavorites(userId: String): Flow<List<FavoriteService>> =
        db.collection("users").document(userId).collection("favorites")
            .snapshots()
            .map { it.toObjects(FavoriteService::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) }
            .catch { error ->
                val fsError = unwrapFirestoreException(error)
                if (fsError == null && error is CancellationException) throw error
                if (auth.currentUser?.uid == userId) {
                    handleFirestoreError(fsError ?: error, OperationType.LIST, "users/$userId/favorites")
                }
                throw error
            }

    suspend fun toggleFavorite(service: ServiceItem, isCurrentlyFavorite: Boolean): Result<Unit> = runCatching {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("favorites").document(service.id)
        try {
            if (isCurrentlyFavorite) {
                docRef.delete().await()
            } else {
                val payload = mapOf(
                    "userId" to uid,
                    "serviceId" to service.id,
                    "serviceTitle" to service.title.take(120),
                    "category" to service.category.displayName.take(60),
                    "createdAt" to FieldValue.serverTimestamp()
                )
                docRef.set(payload).await()
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(
                e,
                if (isCurrentlyFavorite) OperationType.DELETE else OperationType.CREATE,
                docRef.path
            )
            throw e
        }
    }

    // --- BOOKINGS & SANDBOX PAYMENT ORDER FLOW ---

    fun observeBookings(userId: String): Flow<List<Booking>> =
        db.collection("users").document(userId).collection("bookings")
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(Booking::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    .sortedByDescending { it.createdAt ?: com.google.firebase.Timestamp(0, 0) }
            }
            .catch { error ->
                val fsError = unwrapFirestoreException(error)
                if (fsError == null && error is CancellationException) throw error
                if (auth.currentUser?.uid == userId) {
                    handleFirestoreError(fsError ?: error, OperationType.LIST, "users/$userId/bookings")
                }
                throw error
            }

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
        val uid = requireUserId()
        val bookingDocRef = db.collection("users").document(uid).collection("bookings").document()
        val paymentDocRef = db.collection("users").document(uid).collection("payments").document()
        val notifDocRef = db.collection("users").document(uid).collection("notifications").document()

        val datePart = SimpleDateFormat("yyyyMMdd", Locale.US).format(Date())
        val suffix = UUID.randomUUID().toString().take(4).uppercase(Locale.US)
        val bookingCode = "AUR-$datePart-$suffix"
        val receiptNumber = "RCP-$datePart-$suffix"

        val discountedSubtotal = ((pkg.priceInr * (100 - discountPercent)) / 100.0).toInt().coerceAtLeast(0)
        val taxInr = (discountedSubtotal * 0.18).toInt()
        val totalInr = discountedSubtotal + taxInr

        val bookingPayload = mapOf(
            "userId" to uid,
            "bookingCode" to bookingCode,
            "serviceId" to service.id,
            "serviceTitle" to service.title.take(120),
            "category" to service.category.displayName.take(60),
            "providerName" to service.providerName.take(80),
            "packageName" to "${pkg.name} (${pkg.durationMinutes} min)".take(60),
            "city" to city.trim().take(60),
            "addressSummary" to addressSummary.trim().take(200),
            "scheduledDate" to scheduledDate.trim().take(40),
            "scheduledTimeSlot" to scheduledTimeSlot.trim().take(40),
            "durationMinutes" to pkg.durationMinutes,
            "subtotalInr" to discountedSubtotal,
            "taxInr" to taxInr,
            "totalInr" to totalInr,
            "guestNotes" to guestNotes.trim().take(400),
            "consentAccepted" to true,
            "status" to BookingStatus.PENDING_PAYMENT.code,
            "paymentStatus" to PaymentStatus.PENDING.code,
            "paymentMethod" to paymentMethod.code,
            "transactionRef" to paymentDocRef.id,
            "cancellationReason" to "",
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )

        val paymentPayload = mapOf(
            "userId" to uid,
            "bookingId" to bookingDocRef.id,
            "bookingCode" to bookingCode,
            "method" to paymentMethod.code,
            "maskedInstrument" to maskedInstrument.trim().ifBlank { paymentMethod.label }.take(60),
            "amountInr" to totalInr,
            "status" to "INITIATED",
            "receiptNumber" to receiptNumber,
            "gatewayRef" to "",
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )

        val notifPayload = mapOf(
            "userId" to uid,
            "title" to "Order Initiated ($bookingCode)",
            "message" to "Awaiting gateway payment confirmation for ${service.title}.",
            "type" to NotificationType.PAYMENT.code,
            "isRead" to false,
            "relatedId" to bookingDocRef.id,
            "createdAt" to FieldValue.serverTimestamp()
        )

        val batch = db.batch()
        batch.set(bookingDocRef, bookingPayload)
        batch.set(paymentDocRef, paymentPayload)
        batch.set(notifDocRef, notifPayload)

        try {
            batch.commit().await()
            bookingDocRef.id to paymentDocRef.id
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, bookingDocRef.path)
            throw e
        }
    }

    suspend fun confirmGatewayPayment(
        bookingId: String,
        paymentId: String,
        bookingCode: String,
        serviceTitle: String,
        gatewayRef: String
    ): Result<Unit> = runCatching {
        val uid = requireUserId()
        val bookingDocRef = db.collection("users").document(uid).collection("bookings").document(bookingId)
        val paymentDocRef = db.collection("users").document(uid).collection("payments").document(paymentId)
        val notifDocRef = db.collection("users").document(uid).collection("notifications").document()

        val batch = db.batch()
        batch.update(
            bookingDocRef,
            mapOf(
                "status" to BookingStatus.CONFIRMED.code,
                "paymentStatus" to PaymentStatus.PAID.code,
                "transactionRef" to gatewayRef.trim().take(80),
                "updatedAt" to FieldValue.serverTimestamp()
            )
        )
        batch.update(
            paymentDocRef,
            mapOf(
                "status" to "CAPTURED",
                "gatewayRef" to gatewayRef.trim().take(80),
                "updatedAt" to FieldValue.serverTimestamp()
            )
        )
        batch.set(
            notifDocRef,
            mapOf(
                "userId" to uid,
                "title" to "Booking Confirmed ($bookingCode)",
                "message" to "Payment captured ($gatewayRef). Your $serviceTitle reservation is confirmed.",
                "type" to NotificationType.BOOKING.code,
                "isRead" to false,
                "relatedId" to bookingId,
                "createdAt" to FieldValue.serverTimestamp()
            )
        )

        try {
            batch.commit().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, bookingDocRef.path)
            throw e
        }
    }

    suspend fun markBookingCompleted(booking: Booking): Result<Unit> = runCatching {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("bookings").document(booking.id)
        val notifDocRef = db.collection("users").document(uid).collection("notifications").document()
        val batch = db.batch()
        batch.update(
            docRef,
            mapOf(
                "status" to BookingStatus.COMPLETED.code,
                "updatedAt" to FieldValue.serverTimestamp()
            )
        )
        batch.set(
            notifDocRef,
            mapOf(
                "userId" to uid,
                "title" to "Experience Completed (${booking.bookingCode})",
                "message" to "Thank you for choosing Aurevia. You may now leave a verified review for ${booking.serviceTitle}.",
                "type" to NotificationType.BOOKING.code,
                "isRead" to false,
                "relatedId" to booking.id,
                "createdAt" to FieldValue.serverTimestamp()
            )
        )
        try {
            batch.commit().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    suspend fun rescheduleBooking(
        booking: Booking,
        newDate: String,
        newTimeSlot: String,
        newAddress: String
    ): Result<Unit> = runCatching {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("bookings").document(booking.id)
        val notifDocRef = db.collection("users").document(uid).collection("notifications").document()
        val batch = db.batch()
        batch.update(
            docRef,
            mapOf(
                "scheduledDate" to newDate.trim().take(40),
                "scheduledTimeSlot" to newTimeSlot.trim().take(40),
                "addressSummary" to newAddress.trim().take(200),
                "updatedAt" to FieldValue.serverTimestamp()
            )
        )
        batch.set(
            notifDocRef,
            mapOf(
                "userId" to uid,
                "title" to "Booking Rescheduled (${booking.bookingCode})",
                "message" to "Updated to $newDate at $newTimeSlot.",
                "type" to NotificationType.BOOKING.code,
                "isRead" to false,
                "relatedId" to booking.id,
                "createdAt" to FieldValue.serverTimestamp()
            )
        )
        try {
            batch.commit().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    suspend fun cancelBookingAndRequestRefund(
        booking: Booking,
        reason: String,
        linkedPayment: PaymentTransaction?
    ): Result<Unit> = runCatching {
        val uid = requireUserId()
        val bookingDocRef = db.collection("users").document(uid).collection("bookings").document(booking.id)
        val notifDocRef = db.collection("users").document(uid).collection("notifications").document()
        val ticketDocRef = db.collection("users").document(uid).collection("tickets").document()

        val wasPaid = booking.paymentStatus == PaymentStatus.PAID.code
        val nextPayStatus = if (wasPaid) PaymentStatus.REFUND_REQUESTED.code else PaymentStatus.PENDING.code

        val batch = db.batch()
        batch.update(
            bookingDocRef,
            mapOf(
                "status" to BookingStatus.CANCELLED.code,
                "paymentStatus" to nextPayStatus,
                "cancellationReason" to reason.trim().ifBlank { "Customer requested cancellation" }.take(240),
                "updatedAt" to FieldValue.serverTimestamp()
            )
        )
        if (wasPaid && linkedPayment != null && linkedPayment.status == "CAPTURED") {
            val payDocRef = db.collection("users").document(uid).collection("payments").document(linkedPayment.id)
            batch.update(
                payDocRef,
                mapOf(
                    "status" to "REFUND_INITIATED",
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
        }
        batch.set(
            ticketDocRef,
            mapOf(
                "userId" to uid,
                "bookingId" to booking.id,
                "category" to if (wasPaid) TicketCategory.REFUND.code else TicketCategory.CANCELLATION.code,
                "subject" to "Cancellation & Refund — ${booking.bookingCode}",
                "description" to "Reason: ${reason.trim().ifBlank { "Requested by member" }}",
                "status" to "IN_REVIEW",
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
        )
        batch.set(
            notifDocRef,
            mapOf(
                "userId" to uid,
                "title" to "Cancellation Recorded (${booking.bookingCode})",
                "message" to if (wasPaid) {
                    "Your refund request has been initiated and is being tracked in Payments."
                } else {
                    "Your pending order has been cancelled."
                },
                "type" to NotificationType.REFUND.code,
                "isRead" to false,
                "relatedId" to booking.id,
                "createdAt" to FieldValue.serverTimestamp()
            )
        )
        try {
            batch.commit().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, bookingDocRef.path)
            throw e
        }
    }

    // --- PAYMENTS ---

    fun observePayments(userId: String): Flow<List<PaymentTransaction>> =
        db.collection("users").document(userId).collection("payments")
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(PaymentTransaction::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    .sortedByDescending { it.createdAt ?: com.google.firebase.Timestamp(0, 0) }
            }
            .catch { error ->
                val fsError = unwrapFirestoreException(error)
                if (fsError == null && error is CancellationException) throw error
                if (auth.currentUser?.uid == userId) {
                    handleFirestoreError(fsError ?: error, OperationType.LIST, "users/$userId/payments")
                }
                throw error
            }

    // --- VERIFIED PUBLIC REVIEWS ---

    fun observeAllVerifiedReviews(): Flow<List<ServiceReview>> =
        db.collection("service_reviews")
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(ServiceReview::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    .sortedByDescending { it.createdAt ?: com.google.firebase.Timestamp(0, 0) }
            }
            .catch { error ->
                val fsError = unwrapFirestoreException(error)
                if (fsError == null && error is CancellationException) throw error
                if (auth.currentUser != null) {
                    handleFirestoreError(fsError ?: error, OperationType.LIST, "service_reviews")
                }
                throw error
            }

    suspend fun submitVerifiedReview(
        booking: Booking,
        reviewerFullName: String,
        rating: Int,
        comment: String
    ): Result<String> = runCatching {
        val uid = requireUserId()
        require(booking.status == BookingStatus.COMPLETED.code) {
            "Only completed bookings may submit a public verified review."
        }
        val docRef = db.collection("service_reviews").document()
        val maskedDisplay = formatReviewerDisplayName(reviewerFullName)
        val payload = mapOf(
            "userId" to uid,
            "reviewerDisplayName" to maskedDisplay,
            "serviceId" to booking.serviceId,
            "bookingId" to booking.id,
            "rating" to rating.coerceIn(1, 5),
            "comment" to comment.trim().take(600),
            "isVerifiedBooking" to true,
            "createdAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload).await()
            docRef.id
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    private fun formatReviewerDisplayName(fullName: String): String {
        val parts = fullName.trim().split(" ").filter { it.isNotBlank() }
        return when {
            parts.isEmpty() -> "Verified Member"
            parts.size == 1 -> parts[0].take(30)
            else -> "${parts[0]} ${parts[1].first().uppercaseChar()}.".take(40)
        }
    }

    // --- SUPPORT TICKETS & LIVE CONCIERGE CHAT ---

    fun observeSupportTickets(userId: String): Flow<List<SupportTicket>> =
        db.collection("users").document(userId).collection("tickets")
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(SupportTicket::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    .sortedByDescending { it.createdAt ?: com.google.firebase.Timestamp(0, 0) }
            }
            .catch { error ->
                val fsError = unwrapFirestoreException(error)
                if (fsError == null && error is CancellationException) throw error
                if (auth.currentUser?.uid == userId) {
                    handleFirestoreError(fsError ?: error, OperationType.LIST, "users/$userId/tickets")
                }
                throw error
            }

    fun observeTicketMessages(userId: String, ticketId: String): Flow<List<SupportMessage>> =
        db.collection("users").document(userId).collection("tickets")
            .document(ticketId).collection("messages")
            .orderBy("createdAt", Query.Direction.ASCENDING)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(SupportMessage::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            }
            .catch { error ->
                val fsError = unwrapFirestoreException(error)
                if (fsError == null && error is CancellationException) throw error
                if (auth.currentUser?.uid == userId) {
                    handleFirestoreError(
                        fsError ?: error,
                        OperationType.LIST,
                        "users/$userId/tickets/$ticketId/messages"
                    )
                }
                throw error
            }

    suspend fun createSupportTicket(
        bookingId: String,
        category: TicketCategory,
        subject: String,
        description: String
    ): Result<String> = runCatching {
        val uid = requireUserId()
        val ticketRef = db.collection("users").document(uid).collection("tickets").document()
        val firstMsgRef = ticketRef.collection("messages").document()
        val notifRef = db.collection("users").document(uid).collection("notifications").document()

        val batch = db.batch()
        batch.set(
            ticketRef,
            mapOf(
                "userId" to uid,
                "bookingId" to bookingId.trim().take(128),
                "category" to category.code,
                "subject" to subject.trim().take(140),
                "description" to description.trim().take(1000),
                "status" to if (category == TicketCategory.SAFETY_ESCALATION) "IN_REVIEW" else "OPEN",
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
        )
        batch.set(
            firstMsgRef,
            mapOf(
                "userId" to uid,
                "senderRole" to "CUSTOMER",
                "body" to description.trim().take(1000),
                "createdAt" to FieldValue.serverTimestamp()
            )
        )
        batch.set(
            notifRef,
            mapOf(
                "userId" to uid,
                "title" to "Support Ticket Opened",
                "message" to "Concierge Desk logged: ${subject.trim().take(80)}",
                "type" to if (category == TicketCategory.SAFETY_ESCALATION) NotificationType.SAFETY.code else NotificationType.SUPPORT.code,
                "isRead" to false,
                "relatedId" to ticketRef.id,
                "createdAt" to FieldValue.serverTimestamp()
            )
        )
        try {
            batch.commit().await()
            ticketRef.id
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, ticketRef.path)
            throw e
        }
    }

    suspend fun sendSupportMessage(ticketId: String, body: String): Result<Unit> = runCatching {
        val uid = requireUserId()
        val messagesRef = db.collection("users").document(uid)
            .collection("tickets").document(ticketId).collection("messages")
        val msgDoc = messagesRef.document()
        val replyDoc = messagesRef.document()
        val notifDoc = db.collection("users").document(uid).collection("notifications").document()

        try {
            msgDoc.set(
                mapOf(
                    "userId" to uid,
                    "senderRole" to "CUSTOMER",
                    "body" to body.trim().take(1000),
                    "createdAt" to FieldValue.serverTimestamp()
                )
            ).await()

            replyDoc.set(
                mapOf(
                    "userId" to uid,
                    "senderRole" to "CONCIERGE",
                    "body" to "Aurevia Member Concierge has received your message and logged it on case #${ticketId.take(6).uppercase(Locale.US)}. A dedicated specialist responds within 15 minutes.",
                    "createdAt" to FieldValue.serverTimestamp()
                )
            ).await()

            notifDoc.set(
                mapOf(
                    "userId" to uid,
                    "title" to "Concierge Reply (#${ticketId.take(6).uppercase(Locale.US)})",
                    "message" to "Aurevia Member Concierge replied to your support conversation.",
                    "type" to NotificationType.SUPPORT.code,
                    "isRead" to false,
                    "relatedId" to ticketId,
                    "createdAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, msgDoc.path)
            throw e
        }
    }

    // --- TRUST & SAFETY REPORTS + PROVIDER BLOCKING ---

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
        val uid = requireUserId()
        val reportRef = db.collection("users").document(uid).collection("safety_reports").document()
        val notifRef = db.collection("users").document(uid).collection("notifications").document()
        val userRef = db.collection("users").document(uid)

        val batch = db.batch()
        batch.set(
            reportRef,
            mapOf(
                "userId" to uid,
                "serviceId" to serviceId.take(64),
                "providerName" to providerName.take(80),
                "bookingId" to bookingId.take(128),
                "reason" to reason.trim().take(120),
                "details" to details.trim().take(1000),
                "blockProvider" to blockProvider,
                "status" to "ESCALATED",
                "createdAt" to FieldValue.serverTimestamp()
            )
        )
        batch.set(
            notifRef,
            mapOf(
                "userId" to uid,
                "title" to "Safety Report Escalated",
                "message" to "Trust & Safety team received your report regarding $providerName.",
                "type" to NotificationType.SAFETY.code,
                "isRead" to false,
                "relatedId" to reportRef.id,
                "createdAt" to FieldValue.serverTimestamp()
            )
        )
        if (blockProvider && providerId.isNotBlank()) {
            val updatedBlocked = (currentBlockedIds + providerId).distinct().take(50)
            batch.update(
                userRef,
                mapOf(
                    "blockedProviderIds" to updatedBlocked,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
        }
        try {
            batch.commit().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, reportRef.path)
            throw e
        }
    }

    // --- NOTIFICATIONS ---

    fun observeNotifications(userId: String): Flow<List<AppNotification>> =
        db.collection("users").document(userId).collection("notifications")
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(AppNotification::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    .sortedByDescending { it.createdAt ?: com.google.firebase.Timestamp(0, 0) }
            }
            .catch { error ->
                val fsError = unwrapFirestoreException(error)
                if (fsError == null && error is CancellationException) throw error
                if (auth.currentUser?.uid == userId) {
                    handleFirestoreError(fsError ?: error, OperationType.LIST, "users/$userId/notifications")
                }
                throw error
            }

    suspend fun markNotificationRead(notificationId: String): Result<Unit> = runCatching {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("notifications").document(notificationId)
        try {
            docRef.update("isRead", true).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    suspend fun markAllNotificationsRead(notificationIds: List<String>): Result<Unit> = runCatching {
        if (notificationIds.isEmpty()) return@runCatching
        val uid = requireUserId()
        val colRef = db.collection("users").document(uid).collection("notifications")
        val batch = db.batch()
        notificationIds.take(400).forEach { id ->
            batch.update(colRef.document(id), "isRead", true)
        }
        try {
            batch.commit().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, colRef.path)
            throw e
        }
    }

    suspend fun sendBookingReminderNotification(booking: Booking): Result<Unit> = runCatching {
        val uid = requireUserId()
        val notifRef = db.collection("users").document(uid).collection("notifications").document()
        try {
            notifRef.set(
                mapOf(
                    "userId" to uid,
                    "title" to "Upcoming Reservation Reminder (${booking.bookingCode})",
                    "message" to "${booking.serviceTitle} is scheduled on ${booking.scheduledDate} at ${booking.scheduledTimeSlot} (${booking.city}).",
                    "type" to NotificationType.BOOKING.code,
                    "isRead" to false,
                    "relatedId" to booking.id,
                    "createdAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, notifRef.path)
            throw e
        }
    }

    suspend fun createTestNotification(): Result<Unit> = runCatching {
        val uid = requireUserId()
        val notifRef = db.collection("users").document(uid).collection("notifications").document()
        try {
            notifRef.set(
                mapOf(
                    "userId" to uid,
                    "title" to "Aurevia Concierge Alerts Active",
                    "message" to "Booking confirmations, payment receipts, refund updates, and concierge replies will notify you here.",
                    "type" to NotificationType.BOOKING.code,
                    "isRead" to false,
                    "relatedId" to "test_alert",
                    "createdAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, notifRef.path)
            throw e
        }
    }

    suspend fun finalizeRefund(
        booking: Booking,
        linkedPayment: PaymentTransaction?
    ): Result<Unit> = runCatching {
        val uid = requireUserId()
        val bookingDocRef = db.collection("users").document(uid).collection("bookings").document(booking.id)
        val notifDocRef = db.collection("users").document(uid).collection("notifications").document()

        val batch = db.batch()
        batch.update(
            bookingDocRef,
            mapOf(
                "paymentStatus" to PaymentStatus.REFUNDED.code,
                "updatedAt" to FieldValue.serverTimestamp()
            )
        )
        if (linkedPayment != null && linkedPayment.status == "REFUND_INITIATED") {
            val payDocRef = db.collection("users").document(uid).collection("payments").document(linkedPayment.id)
            batch.update(
                payDocRef,
                mapOf(
                    "status" to "REFUNDED",
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
        }
        batch.set(
            notifDocRef,
            mapOf(
                "userId" to uid,
                "title" to "Refund Credited (${booking.bookingCode})",
                "message" to "Your refund for ${booking.serviceTitle} has settled cleanly to your original payment method.",
                "type" to NotificationType.REFUND.code,
                "isRead" to false,
                "relatedId" to booking.id,
                "createdAt" to FieldValue.serverTimestamp()
            )
        )
        try {
            batch.commit().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, bookingDocRef.path)
            throw e
        }
    }
}
