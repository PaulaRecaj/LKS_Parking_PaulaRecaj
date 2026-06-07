package com.lksnext.ParkingPRecaj.data.repository

import com.lksnext.ParkingPRecaj.data.api.ParkingApiService
import com.lksnext.ParkingPRecaj.data.model.ParkingType
import com.lksnext.ParkingPRecaj.data.model.Reservation
import com.lksnext.ParkingPRecaj.data.model.ReservationStatus
import java.util.UUID

class ReservationRepository(
    private val apiService: ParkingApiService
) {
    // Lista en memoria para simular persistencia en el prototipo si falla el backend
    private val mockReservations = mutableListOf<Reservation>()

    suspend fun getUpcomingReservations(): List<Reservation> {
        return try {
            val apiResults = apiService.getUpcomingReservations()
            if (apiResults.isEmpty()) mockReservations else apiResults
        } catch (e: Exception) {
            mockReservations.filter { it.status == ReservationStatus.CONFIRMED }
                .sortedBy { it.date }
        }
    }

    suspend fun getPastReservations(): List<Reservation> {
        return try {
            apiService.getPastReservations()
        } catch (e: Exception) {
            mockReservations.filter { it.status == ReservationStatus.COMPLETED }
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
            val reservation = apiService.createReservation(
                date, startTime, endTime, parkingType, spotNumber
            )
            mockReservations.add(reservation)
            reservation
        } catch (e: Exception) {
            // Simulación para prototipo: guardamos localmente si falla la red
            val newReservation = Reservation(
                id = UUID.randomUUID().toString(),
                userId = "user_test",
                date = date,
                startTime = startTime,
                endTime = endTime,
                parkingType = parkingType,
                spotNumber = spotNumber,
                status = ReservationStatus.CONFIRMED
            )
            mockReservations.add(newReservation)
            newReservation
        }
    }

    suspend fun getAvailableSpots(
        type: ParkingType,
        date: Long,
        startTime: String,
        endTime: String
    ): List<String> {
        return try {
            val apiSpots = apiService.getAvailableSpots(type, date, startTime, endTime)
            if (apiSpots.isEmpty()) filterMockAvailableSpots(type, date, startTime, endTime) else apiSpots
        } catch (e: Exception) {
            filterMockAvailableSpots(type, date, startTime, endTime)
        }
    }

    private fun filterMockAvailableSpots(
        type: ParkingType,
        date: Long,
        startTime: String,
        endTime: String
    ): List<String> {
        val allSpots = com.lksnext.ParkingPRecaj.data.model.ParkingSpots.getSpotsByType(type)
        
        // Convertir horas a minutos para comparar
        val (newStartH, newStartM) = startTime.split(":").map { it.toInt() }
        val (newEndH, newEndM) = endTime.split(":").map { it.toInt() }
        var newStartMinutes = newStartH * 60 + newStartM
        var newEndMinutes = newEndH * 60 + newEndM
        if (newEndMinutes <= newStartMinutes) newEndMinutes += 24 * 60

        // Filtrar las plazas que ya tienen una reserva que se solapa
        val reservedSpotNumbers = mockReservations.filter { res ->
            res.status == ReservationStatus.CONFIRMED && 
            res.date == date && 
            res.parkingType == type
        }.filter { res ->
            val (resStartH, resStartM) = res.startTime.split(":").map { it.toInt() }
            val (resEndH, resEndM) = res.endTime.split(":").map { it.toInt() }
            var resStartMin = resStartH * 60 + resStartM
            var resEndMin = resEndH * 60 + resEndM
            if (resEndMin <= resStartMin) resEndMin += 24 * 60

            // Hay solapamiento si (Inicio1 < Fin2) Y (Fin1 > Inicio2)
            (newStartMinutes < resEndMin) && (newEndMinutes > resStartMin)
        }.map { it.spotNumber }

        return allSpots.filter { it !in reservedSpotNumbers }
    }

    suspend fun updateReservation(
        id: String,
        date: Long,
        startTime: String,
        endTime: String,
        parkingType: ParkingType,
        spotNumber: String
    ): Reservation {
        return try {
            val updated = apiService.updateReservation(id, date, startTime, endTime, parkingType, spotNumber)
            val index = mockReservations.indexOfFirst { it.id == id }
            if (index != -1) mockReservations[index] = updated
            updated
        } catch (e: Exception) {
            val index = mockReservations.indexOfFirst { it.id == id }
            val updated = Reservation(
                id = id,
                userId = "user_test",
                date = date,
                startTime = startTime,
                endTime = endTime,
                parkingType = parkingType,
                spotNumber = spotNumber,
                status = ReservationStatus.CONFIRMED
            )
            if (index != -1) {
                mockReservations[index] = updated
            } else {
                mockReservations.add(updated)
            }
            updated
        }
    }

    suspend fun cancelReservation(id: String) {
        try {
            apiService.cancelReservation(id)
            mockReservations.removeAll { it.id == id }
        } catch (e: Exception) {
            mockReservations.removeAll { it.id == id }
        }
    }
}
