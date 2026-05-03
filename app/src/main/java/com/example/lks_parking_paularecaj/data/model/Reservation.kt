package com.example.lks_parking_paularecaj.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
data class Reservation(
    val id: String,
    val userId: String,
    val date: Long, // Timestamp
    val startTime: String, // "HH:mm"
    val endTime: String, // "HH:mm"
    val parkingType: ParkingType,
    val spotNumber: String,
    val status: ReservationStatus
) : Parcelable

enum class ParkingType {
    NORMAL,
    ELECTRIC,
    MOTORCYCLE;

    fun getDisplayName(): String = when (this) {
        NORMAL -> "Normal"
        ELECTRIC -> "Cargador Eléctrico"
        MOTORCYCLE -> "Moto"
    }
}

enum class ReservationStatus {
    CONFIRMED,
    COMPLETED,
    CANCELLED
}
