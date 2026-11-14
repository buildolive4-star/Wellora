package com.example.data

import com.example.base.FirestoreEmulatorTestBase
import com.example.model.BookingStatus
import com.example.model.PaymentMethod
import com.example.model.PaymentStatus
import com.example.model.TicketCategory
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

class AureviaRepositoryRuleTest : FirestoreEmulatorTestBase() {

    private val catalog = CatalogRepository()

    @Test
    fun profileAndBookingLifecycle_ownerFlow_succeedsEndToEnd() = runBlocking {
        val uid = signInTestUser("alice_${UUID.randomUUID()}@aurevia.test")
        val repo = AureviaFirestoreRepository(firestore)

        withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.ensureInitialProfile(uid, "Alice Vance", "alice@aurevia.test").getOrThrow()
            repo.saveUserProfile(
                fullName = "Alice Vance",
                email = "alice@aurevia.test",
                mobileNumber = "+91 98200 11223",
                preferredCity = "Mumbai",
                emergencyContact = "+91 98200 44556",
                isAdultConfirmed = true,
                notifyEmail = true,
                notifySms = true,
                notifyWhatsApp = false,
                notifyPush = true
            ).getOrThrow()
        }

        val profile = withTimeout(FLOW_TIMEOUT_MS) {
            repo.observeUserProfile(uid).first { it != null }
        }
        assertNotNull(profile)
        assertEquals("Alice Vance", profile?.fullName)

        val service = catalog.services.first()
        val pkg = service.packages.first()

        val (bookingId, paymentId) = withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.createPendingBookingOrder(
                service = service,
                pkg = pkg,
                city = "Mumbai",
                addressSummary = "Taj Lands End Residences, Bandra",
                scheduledDate = "2026-11-20",
                scheduledTimeSlot = "04:00 PM",
                guestNotes = "Quiet room preferred",
                paymentMethod = PaymentMethod.UPI,
                maskedInstrument = "alice@okaxis",
                discountPercent = 0
            ).getOrThrow()
        }

        val pendingBooking = withTimeout(FLOW_TIMEOUT_MS) {
            repo.observeBookings(uid).first { list -> list.any { it.id == bookingId } }
                .first { it.id == bookingId }
        }
        assertEquals(BookingStatus.PENDING_PAYMENT.code, pendingBooking.status)

        withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.confirmGatewayPayment(
                bookingId = bookingId,
                paymentId = paymentId,
                bookingCode = pendingBooking.bookingCode,
                serviceTitle = service.title,
                gatewayRef = "UPI-GW-${UUID.randomUUID().toString().take(8)}"
            ).getOrThrow()
        }

        val confirmedBooking = withTimeout(FLOW_TIMEOUT_MS) {
            repo.observeBookings(uid)
                .first { list -> list.firstOrNull { it.id == bookingId }?.status == BookingStatus.CONFIRMED.code }
                .first { it.id == bookingId }
        }
        assertEquals(PaymentStatus.PAID.code, confirmedBooking.paymentStatus)

        // Mark completed and submit verified review
        withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.markBookingCompleted(confirmedBooking).getOrThrow()
        }

        val completedBooking = withTimeout(FLOW_TIMEOUT_MS) {
            repo.observeBookings(uid)
                .first { list -> list.firstOrNull { it.id == bookingId }?.status == BookingStatus.COMPLETED.code }
                .first { it.id == bookingId }
        }

        val reviewId = withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.submitVerifiedReview(
                booking = completedBooking,
                reviewerFullName = "Alice Vance",
                rating = 5,
                comment = "Impeccable wellness sanctuary and verified specialist."
            ).getOrThrow()
        }
        assertTrue(reviewId.isNotBlank())

        // Support ticket & live message
        val ticketId = withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.createSupportTicket(
                bookingId = bookingId,
                category = TicketCategory.GENERAL,
                subject = "Receipt copy request",
                description = "Please confirm GST invoice details."
            ).getOrThrow()
        }
        withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.sendSupportMessage(ticketId, "Thank you, checking now.").getOrThrow()
        }
        val messages = withTimeout(FLOW_TIMEOUT_MS) {
            repo.observeTicketMessages(uid, ticketId).first { it.size >= 2 }
        }
        assertTrue(messages.any { it.senderRole == "CONCIERGE" })
    }

    @Test
    fun observeBookings_crossUserAccess_failsWithPermissionDenied() = runBlocking {
        val aliceUid = signInTestUser("alice_iso_${UUID.randomUUID()}@aurevia.test")
        val aliceRepo = AureviaFirestoreRepository(firestore)
        val service = catalog.services.first()
        val (bookingId, _) = withTimeout(DEFAULT_TIMEOUT_MS) {
            aliceRepo.createPendingBookingOrder(
                service = service,
                pkg = service.packages.first(),
                city = "Mumbai",
                addressSummary = "Worli Sea Face",
                scheduledDate = "2026-11-22",
                scheduledTimeSlot = "10:00 AM",
                guestNotes = "",
                paymentMethod = PaymentMethod.CREDIT_CARD,
                maskedInstrument = "VISA •••• 4242",
                discountPercent = 0
            ).getOrThrow()
        }

        signInTestUser("bob_iso_${UUID.randomUUID()}@aurevia.test")
        val exception = assertThrowsSuspending<FirebaseFirestoreException> {
            withTimeout(DEFAULT_TIMEOUT_MS) {
                firestore.collection("users").document(aliceUid)
                    .collection("bookings").document(bookingId)
                    .get()
                    .await()
            }
        }
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, exception.code)
    }

    @Test
    fun observeBookings_unauthenticatedUser_failsWithPermissionDenied() = runBlocking {
        auth.signOut()
        val repo = AureviaFirestoreRepository(firestore)
        val exception = assertThrowsSuspending<Exception> {
            withTimeout(FLOW_TIMEOUT_MS) {
                repo.observeBookings("some_uid_123").first()
            }
        }
        val fsException = checkNotNull(exception.unwrapFirestoreException()) {
            "Expected FirebaseFirestoreException, got $exception"
        }
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, fsException.code)
    }

    private companion object {
        const val DEFAULT_TIMEOUT_MS = 8000L
        const val FLOW_TIMEOUT_MS = 6000L
    }
}
