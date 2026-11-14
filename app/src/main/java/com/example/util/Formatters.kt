package com.example.util

import com.google.firebase.Timestamp
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

fun formatInr(amount: Long): String {
    val formatter = NumberFormat.getNumberInstance(Locale("en", "IN"))
    return "₹${formatter.format(amount)}"
}

fun formatInr(amount: Int): String = formatInr(amount.toLong())

fun formatTimestamp(ts: Timestamp?): String {
    val date = (ts ?: Timestamp.now()).toDate()
    val fmt = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.US)
    return fmt.format(date)
}
