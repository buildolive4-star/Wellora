package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        UserProfileEntity::class,
        SavedAddressEntity::class,
        FavoriteServiceEntity::class,
        BookingEntity::class,
        PaymentTransactionEntity::class,
        ServiceReviewEntity::class,
        SupportTicketEntity::class,
        SupportMessageEntity::class,
        SafetyReportEntity::class,
        AppNotificationEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(AureviaTypeConverters::class)
abstract class AureviaDatabase : RoomDatabase() {
    abstract fun aureviaDao(): AureviaDao

    companion object {
        @Volatile
        private var instance: AureviaDatabase? = null

        fun getInstance(context: Context): AureviaDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AureviaDatabase::class.java,
                    "aurevia_local.db"
                ).build().also { instance = it }
            }
        }
    }
}
