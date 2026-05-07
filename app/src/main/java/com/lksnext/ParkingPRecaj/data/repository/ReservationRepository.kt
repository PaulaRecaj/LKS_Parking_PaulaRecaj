package com.lksnext.ParkingPRecaj.data.repository

import com.lksnext.ParkingPRecaj.data.api.ParkingApiService
import com.lksnext.ParkingPRecaj.data.model.ParkingType
import com.lksnext.ParkingPRecaj.data.model.Reservation

class ReservationRepository(
    private val apiService: ParkingApiService
) {

    suspend fun getUpcomingReservations(): List<Reservation> {
        return apiService.getUpcomingReservations()
    }

    suspend fun getPastReservations(): List<Reservation> {
        return apiService.getPastReservations()
    }

    suspend fun createReservation(
        date: Long,
        startTime: String,
        endTime: String,
        parkingType: ParkingType,
        spotNumber: String
    ): Reservation {
        return apiService.createReservation(
            date, startTime, endTime, parkingType, spotNumber
        )
    }

    suspend fun updateReservation(
        id: String,
        date: Long,
        startTime: String,
        endTime: String,
        parkingType: ParkingType,
        spotNumber: String
    ): Reservation {
        return apiService.updateReservation(
            id, date, startTime, endTime, parkingType, spotNumber
        )
    }

    suspend fun cancelReservation(id: String) {
        apiService.cancelReservation(id)
    }
}
