package com.example.util

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun formatInr(amount: Long): String {
    val formatter = NumberFormat.getNumberInstance(Locale.Builder().setLanguage("en").setRegion("IN").build())
    return "₹${formatter.format(amount)}"
}

fun formatInr(amount: Int): String = formatInr(amount.toLong())

fun formatTimestamp(tsMillis: Long?): String {
    val date = Date(tsMillis ?: System.currentTimeMillis())
    val fmt = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.US)
    return fmt.format(date)
}
