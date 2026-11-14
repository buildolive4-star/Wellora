package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.example.model.AppNotification
import com.example.model.Booking
import com.example.model.FavoriteService
import com.example.model.PaymentTransaction
import com.example.model.SafetyReport
import com.example.model.SavedAddress
import com.example.model.ServiceReview
import com.example.model.SupportMessage
import com.example.model.SupportTicket
import com.example.model.UserProfile
import org.json.JSONArray

class AureviaTypeConverters {
    @TypeConverter
    fun fromStringList(list: List<String>): String = JSONArray(list).toString()

    @TypeConverter
    fun toStringList(raw: String): List<String> {
        if (raw.isBlank()) return emptyList()
        return runCatching {
            val array = JSONArray(raw)
            List(array.length()) { index -> array.optString(index) }.filter { it.isNotBlank() }
        }.getOrDefault(emptyList())
    }
}

@Entity(tableName = "user_profiles")
data class UserProfileEntity(
    @PrimaryKey val userId: String,
    val fullName: String,
    val email: String,
    val mobileNumber: String,
    val preferredCity: String,
    val emergencyContact: String,
    val isAdultConfirmed: Boolean,
    val blockedProviderIds: List<String>,
    val notifyEmail: Boolean,
    val notifySms: Boolean,
    val notifyWhatsApp: Boolean,
    val notifyPush: Boolean,
    val sessionEpoch: Long,
    val createdAt: Long,
    val updatedAt: Long
)

@Entity(tableName = "saved_addresses")
data class SavedAddressEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val label: String,
    val line1: String,
    val city: String,
    val postalCode: String,
    val isDefault: Boolean,
    val createdAt: Long,
    val updatedAt: Long
)

@Entity(tableName = "favorite_services")
data class FavoriteServiceEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val serviceId: String,
    val serviceTitle: String,
    val category: String,
    val createdAt: Long
)

@Entity(tableName = "bookings")
data class BookingEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val bookingCode: String,
    val serviceId: String,
    val serviceTitle: String,
    val category: String,
    val providerName: String,
    val packageName: String,
    val city: String,
    val addressSummary: String,
    val scheduledDate: String,
    val scheduledTimeSlot: String,
    val durationMinutes: Long,
    val subtotalInr: Long,
    val taxInr: Long,
    val totalInr: Long,
    val guestNotes: String,
    val consentAccepted: Boolean,
    val status: String,
    val paymentStatus: String,
    val paymentMethod: String,
    val transactionRef: String,
    val cancellationReason: String,
    val createdAt: Long,
    val updatedAt: Long
)

@Entity(tableName = "payment_transactions")
data class PaymentTransactionEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val bookingId: String,
    val bookingCode: String,
    val method: String,
    val maskedInstrument: String,
    val amountInr: Long,
    val status: String,
    val receiptNumber: String,
    val gatewayRef: String,
    val createdAt: Long,
    val updatedAt: Long
)

@Entity(tableName = "service_reviews")
data class ServiceReviewEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val reviewerDisplayName: String,
    val serviceId: String,
    val bookingId: String,
    val rating: Long,
    val comment: String,
    val isVerifiedBooking: Boolean,
    val createdAt: Long
)

@Entity(tableName = "support_tickets")
data class SupportTicketEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val bookingId: String,
    val category: String,
    val subject: String,
    val description: String,
    val status: String,
    val createdAt: Long,
    val updatedAt: Long
)

@Entity(tableName = "support_messages")
data class SupportMessageEntity(
    @PrimaryKey val id: String,
    val ticketId: String,
    val userId: String,
    val senderRole: String,
    val body: String,
    val createdAt: Long
)

@Entity(tableName = "safety_reports")
data class SafetyReportEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val serviceId: String,
    val providerName: String,
    val bookingId: String,
    val reason: String,
    val details: String,
    val blockProvider: Boolean,
    val status: String,
    val createdAt: Long
)

@Entity(tableName = "app_notifications")
data class AppNotificationEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val title: String,
    val message: String,
    val type: String,
    val isRead: Boolean,
    val relatedId: String,
    val createdAt: Long
)

fun UserProfileEntity.toDomain() = UserProfile(
    id = userId,
    userId = userId,
    fullName = fullName,
    email = email,
    mobileNumber = mobileNumber,
    preferredCity = preferredCity,
    emergencyContact = emergencyContact,
    isAdultConfirmed = isAdultConfirmed,
    blockedProviderIds = blockedProviderIds,
    notifyEmail = notifyEmail,
    notifySms = notifySms,
    notifyWhatsApp = notifyWhatsApp,
    notifyPush = notifyPush,
    sessionEpoch = sessionEpoch,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun SavedAddressEntity.toDomain() = SavedAddress(
    id = id,
    userId = userId,
    label = label,
    line1 = line1,
    city = city,
    postalCode = postalCode,
    isDefault = isDefault,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun FavoriteServiceEntity.toDomain() = FavoriteService(
    id = id,
    userId = userId,
    serviceId = serviceId,
    serviceTitle = serviceTitle,
    category = category,
    createdAt = createdAt
)

fun BookingEntity.toDomain() = Booking(
    id = id,
    userId = userId,
    bookingCode = bookingCode,
    serviceId = serviceId,
    serviceTitle = serviceTitle,
    category = category,
    providerName = providerName,
    packageName = packageName,
    city = city,
    addressSummary = addressSummary,
    scheduledDate = scheduledDate,
    scheduledTimeSlot = scheduledTimeSlot,
    durationMinutes = durationMinutes,
    subtotalInr = subtotalInr,
    taxInr = taxInr,
    totalInr = totalInr,
    guestNotes = guestNotes,
    consentAccepted = consentAccepted,
    status = status,
    paymentStatus = paymentStatus,
    paymentMethod = paymentMethod,
    transactionRef = transactionRef,
    cancellationReason = cancellationReason,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun PaymentTransactionEntity.toDomain() = PaymentTransaction(
    id = id,
    userId = userId,
    bookingId = bookingId,
    bookingCode = bookingCode,
    method = method,
    maskedInstrument = maskedInstrument,
    amountInr = amountInr,
    status = status,
    receiptNumber = receiptNumber,
    gatewayRef = gatewayRef,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun ServiceReviewEntity.toDomain() = ServiceReview(
    id = id,
    userId = userId,
    reviewerDisplayName = reviewerDisplayName,
    serviceId = serviceId,
    bookingId = bookingId,
    rating = rating,
    comment = comment,
    isVerifiedBooking = isVerifiedBooking,
    createdAt = createdAt
)

fun SupportTicketEntity.toDomain() = SupportTicket(
    id = id,
    userId = userId,
    bookingId = bookingId,
    category = category,
    subject = subject,
    description = description,
    status = status,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun SupportMessageEntity.toDomain() = SupportMessage(
    id = id,
    userId = userId,
    senderRole = senderRole,
    body = body,
    createdAt = createdAt
)

fun SafetyReportEntity.toDomain() = SafetyReport(
    id = id,
    userId = userId,
    serviceId = serviceId,
    providerName = providerName,
    bookingId = bookingId,
    reason = reason,
    details = details,
    blockProvider = blockProvider,
    status = status,
    createdAt = createdAt
)

fun AppNotificationEntity.toDomain() = AppNotification(
    id = id,
    userId = userId,
    title = title,
    message = message,
    type = type,
    isRead = isRead,
    relatedId = relatedId,
    createdAt = createdAt
)
