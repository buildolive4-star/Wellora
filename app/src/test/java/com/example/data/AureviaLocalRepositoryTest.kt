package com.example.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.auth.LocalSessionManager
import com.example.data.local.AureviaDatabase
import com.example.model.BookingStatus
import com.example.model.PaymentMethod
import com.example.model.PaymentStatus
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class AureviaLocalRepositoryTest {

    private lateinit var db: AureviaDatabase
    private lateinit var repository: AureviaLocalRepository
    private lateinit var sessionManager: LocalSessionManager
    private val catalog = CatalogRepository()

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AureviaDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = AureviaLocalRepository(db.aureviaDao(), "local_member")
        sessionManager = LocalSessionManager(context)
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `local session sign in and sign out persists cleanly`() {
        assertNull(sessionManager.currentUser.value)
        val user = sessionManager.signInLocalMember("Aarav Mehta", "aarav@aurevia.local")
        assertEquals("Aarav Mehta", user.displayName)
        assertNotNull(sessionManager.currentUser.value)

        sessionManager.signOut()
        assertNull(sessionManager.currentUser.value)
    }

    @Test
    fun `booking creation payment confirmation and completion persist in Room`() = runTest {
        repository.ensureInitialProfile("local_member", "Aarav Mehta", "aarav@aurevia.local").getOrThrow()
        val service = catalog.services.first()
        val pkg = service.packages.first()

        val (bookingId, paymentId) = repository.createPendingBookingOrder(
            service = service,
            pkg = pkg,
            city = service.cities.first(),
            addressSummary = "Residence, Worli",
            scheduledDate = service.availableDates.first(),
            scheduledTimeSlot = service.availableTimeSlots.first(),
            guestNotes = "Quiet room",
            paymentMethod = PaymentMethod.UPI,
            maskedInstrument = "aarav@upi",
            discountPercent = 0
        ).getOrThrow()

        var bookings = repository.observeBookings("local_member").first()
        assertEquals(1, bookings.size)
        assertEquals(BookingStatus.PENDING_PAYMENT.code, bookings.first().status)

        repository.confirmGatewayPayment(
            bookingId = bookingId,
            paymentId = paymentId,
            bookingCode = bookings.first().bookingCode,
            serviceTitle = service.title,
            gatewayRef = "TXN-12345"
        ).getOrThrow()

        bookings = repository.observeBookings("local_member").first()
        assertEquals(BookingStatus.CONFIRMED.code, bookings.first().status)
        assertEquals(PaymentStatus.PAID.code, bookings.first().paymentStatus)

        val notifications = repository.observeNotifications("local_member").first()
        assertTrue(notifications.isNotEmpty())
    }
}
