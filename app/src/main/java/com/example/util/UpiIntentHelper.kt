package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri

object UpiIntentHelper {
    fun launchUpiPayment(
        context: Context,
        amountInr: Int,
        bookingCode: String,
        serviceTitle: String
    ): Boolean {
        val uri = Uri.Builder()
            .scheme("upi")
            .authority("pay")
            .appendQueryParameter("pa", "aurevia.escrow@icici")
            .appendQueryParameter("pn", "Aurevia Group Hospitality")
            .appendQueryParameter("tr", bookingCode)
            .appendQueryParameter("tn", "Aurevia $serviceTitle")
            .appendQueryParameter("am", "$amountInr.00")
            .appendQueryParameter("cu", "INR")
            .build()

        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return try {
            val resolved = intent.resolveActivity(context.packageManager)
            if (resolved != null) {
                context.startActivity(Intent.createChooser(intent, "Pay with UPI"))
                true
            } else {
                false
            }
        } catch (_: Exception) {
            false
        }
    }
}
