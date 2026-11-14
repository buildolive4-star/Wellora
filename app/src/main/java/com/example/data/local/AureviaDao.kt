package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AureviaDao {

    @Query("SELECT * FROM user_profiles WHERE userId = :userId LIMIT 1")
    fun observeUserProfile(userId: String): Flow<UserProfileEntity?>

    @Query("SELECT * FROM user_profiles WHERE userId = :userId LIMIT 1")
    suspend fun getUserProfile(userId: String): UserProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertUserProfile(profile: UserProfileEntity)

    @Query("SELECT * FROM saved_addresses WHERE userId = :userId ORDER BY isDefault DESC, createdAt DESC")
    fun observeAddresses(userId: String): Flow<List<SavedAddressEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAddress(address: SavedAddressEntity)

    @Query("DELETE FROM saved_addresses WHERE id = :addressId AND userId = :userId")
    suspend fun deleteAddress(addressId: String, userId: String)

    @Query("SELECT * FROM favorite_services WHERE userId = :userId ORDER BY createdAt DESC")
    fun observeFavorites(userId: String): Flow<List<FavoriteServiceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: FavoriteServiceEntity)

    @Query("DELETE FROM favorite_services WHERE userId = :userId AND serviceId = :serviceId")
    suspend fun deleteFavorite(userId: String, serviceId: String)

    @Query("SELECT * FROM bookings WHERE userId = :userId ORDER BY createdAt DESC")
    fun observeBookings(userId: String): Flow<List<BookingEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertBooking(booking: BookingEntity)

    @Query(
        "UPDATE bookings SET status = :status, paymentStatus = :paymentStatus, transactionRef = :transactionRef, updatedAt = :updatedAt WHERE id = :bookingId AND userId = :userId"
    )
    suspend fun updateBookingPaymentStatus(
        bookingId: String,
        userId: String,
        status: String,
        paymentStatus: String,
        transactionRef: String,
        updatedAt: Long
    )

    @Query("UPDATE bookings SET status = :status, updatedAt = :updatedAt WHERE id = :bookingId AND userId = :userId")
    suspend fun updateBookingStatus(bookingId: String, userId: String, status: String, updatedAt: Long)

    @Query(
        "UPDATE bookings SET scheduledDate = :newDate, scheduledTimeSlot = :newSlot, addressSummary = :newAddress, updatedAt = :updatedAt WHERE id = :bookingId AND userId = :userId"
    )
    suspend fun rescheduleBooking(
        bookingId: String,
        userId: String,
        newDate: String,
        newSlot: String,
        newAddress: String,
        updatedAt: Long
    )

    @Query(
        "UPDATE bookings SET status = :status, paymentStatus = :paymentStatus, cancellationReason = :reason, updatedAt = :updatedAt WHERE id = :bookingId AND userId = :userId"
    )
    suspend fun cancelBooking(
        bookingId: String,
        userId: String,
        status: String,
        paymentStatus: String,
        reason: String,
        updatedAt: Long
    )

    @Query("UPDATE bookings SET paymentStatus = :paymentStatus, updatedAt = :updatedAt WHERE id = :bookingId AND userId = :userId")
    suspend fun updateBookingRefundStatus(
        bookingId: String,
        userId: String,
        paymentStatus: String,
        updatedAt: Long
    )

    @Query("SELECT * FROM payment_transactions WHERE userId = :userId ORDER BY createdAt DESC")
    fun observePayments(userId: String): Flow<List<PaymentTransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPayment(payment: PaymentTransactionEntity)

    @Query("UPDATE payment_transactions SET status = :status, gatewayRef = :gatewayRef, updatedAt = :updatedAt WHERE id = :paymentId AND userId = :userId")
    suspend fun updatePaymentCaptured(
        paymentId: String,
        userId: String,
        status: String,
        gatewayRef: String,
        updatedAt: Long
    )

    @Query("UPDATE payment_transactions SET status = :status, updatedAt = :updatedAt WHERE id = :paymentId AND userId = :userId")
    suspend fun updatePaymentStatus(paymentId: String, userId: String, status: String, updatedAt: Long)

    @Query("SELECT * FROM service_reviews ORDER BY createdAt DESC")
    fun observeAllVerifiedReviews(): Flow<List<ServiceReviewEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReview(review: ServiceReviewEntity)

    @Query("SELECT * FROM support_tickets WHERE userId = :userId ORDER BY createdAt DESC")
    fun observeSupportTickets(userId: String): Flow<List<SupportTicketEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSupportTicket(ticket: SupportTicketEntity)

    @Query("SELECT * FROM support_messages WHERE userId = :userId AND ticketId = :ticketId ORDER BY createdAt ASC")
    fun observeTicketMessages(userId: String, ticketId: String): Flow<List<SupportMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSupportMessage(message: SupportMessageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSafetyReport(report: SafetyReportEntity)

    @Query("SELECT * FROM app_notifications WHERE userId = :userId ORDER BY createdAt DESC")
    fun observeNotifications(userId: String): Flow<List<AppNotificationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: AppNotificationEntity)

    @Query("UPDATE app_notifications SET isRead = 1 WHERE id = :notificationId AND userId = :userId")
    suspend fun markNotificationRead(notificationId: String, userId: String)

    @Query("UPDATE app_notifications SET isRead = 1 WHERE userId = :userId AND id IN (:notificationIds)")
    suspend fun markAllNotificationsRead(userId: String, notificationIds: List<String>)
}
