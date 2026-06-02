package com.lksnext.ParkingPRecaj.data.repository

import com.lksnext.ParkingPRecaj.data.api.ParkingApiService
import com.lksnext.ParkingPRecaj.data.model.ParkingType
import com.lksnext.ParkingPRecaj.data.model.Reservation
import com.lksnext.ParkingPRecaj.data.model.ReservationStatus
import java.util.UUID

class ReservationRepository(
    private val apiService: ParkingApiService
) {

    suspend fun getUpcomingReservations(): List<Reservation> {
        return try {
            apiService.getUpcomingReservations()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getPastReservations(): List<Reservation> {
        return try {
            apiService.getPastReservations()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun createReservation(
        date: Long,
        startTime: String,
        endTime: String,
        parkingType: ParkingType,
        spotNumber: String
    ): Reservation {
        return try {
            apiService.createReservation(
                date, startTime, endTime, parkingType, spotNumber
            )
        } catch (e: Exception) {
            // Simulación para prototipo: devolvemos una reserva exitosa si falla la red
            Reservation(
                id = UUID.randomUUID().toString(),
                userId = "user_test",
                date = date,
                startTime = startTime,
                endTime = endTime,
                parkingType = parkingType,
                spotNumber = spotNumber,
                status = ReservationStatus.CONFIRMED
            )
        }
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
