package com.lksnext.ParkingPRecaj.util

import java.text.SimpleDateFormat
import java.util.*

object DateTimeUtils {
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    fun formatDateTime(date: Date): String = dateFormat.format(date)
}
