package com.example.mounttrack.utils

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.google.firebase.Timestamp

object DateUtils {

    private val displayDateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    private val inputDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    fun formatDate(timestamp: Timestamp): String {
        return displayDateFormat.format(timestamp.toDate())
    }

    fun formatDate(date: Date): String {
        return displayDateFormat.format(date)
    }

    fun formatDateRange(startDate: Date, endDate: Date): String {
        val startFormat = SimpleDateFormat("dd", Locale.getDefault())
        val endFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        return "${startFormat.format(startDate)}-${endFormat.format(endDate)}"
    }

    fun parseDate(dateString: String): Date? {
        return try {
            inputDateFormat.parse(dateString)
        } catch (e: Exception) {
            null
        }
    }

    fun formatHeight(height: Int): String {
        val numberFormat = NumberFormat.getNumberInstance(Locale.getDefault())
        return "${numberFormat.format(height)} mdpl"
    }
}

