package com.example.lks_parking_paularecaj.util

import java.text.SimpleDateFormat
import java.util.*

object DateTimeUtils {
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    fun formatDateTime(date: Date): String = dateFormat.format(date)
}
