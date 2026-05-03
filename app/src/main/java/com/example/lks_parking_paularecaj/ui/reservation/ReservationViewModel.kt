package com.example.lks_parking_paularecaj.ui.reservation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lks_parking_paularecaj.data.model.ParkingSpot
import com.example.lks_parking_paularecaj.data.model.ParkingSpots
import com.example.lks_parking_paularecaj.data.model.ParkingType
import com.example.lks_parking_paularecaj.data.model.Reservation
import com.example.lks_parking_paularecaj.data.repository.ReservationRepository
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date

class ReservationViewModel(
    private val reservationRepository: ReservationRepository
) : ViewModel() {

    private val _availableSpots = MutableLiveData<List<String>>()
    val availableSpots: LiveData<List<String>> = _availableSpots

    private val _reservationCreated = MutableLiveData<Boolean>()
    val reservationCreated: LiveData<Boolean> = _reservationCreated

    private val _reservationUpdated = MutableLiveData<Boolean>()
    val reservationUpdated: LiveData<Boolean> = _reservationUpdated

    private val _reservationCancelled = MutableLiveData<Boolean>()
    val reservationCancelled: LiveData<Boolean> = _reservationCancelled

    private val _myReservations = MutableLiveData<List<Reservation>>()
    val myReservations: LiveData<List<Reservation>> = _myReservations

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun loadMyReservations() {
        viewModelScope.launch {
            _loading.value = true
            try {
                // En un caso real, obtendríamos las próximas y las pasadas
                val reservations = reservationRepository.getUpcomingReservations()
                _myReservations.value = reservations
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }

    fun loadAvailableSpots(type: ParkingType, date: Long, startTime: String, endTime: String) {
        viewModelScope.launch {
            _loading.value = true
            try {
                // En producción, consultar backend para spots disponibles
                val spots = ParkingSpots.getSpotsByType(type)
                _availableSpots.value = spots
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }

    fun createReservation(
        date: Long,
        startTime: String,
        endTime: String,
        parkingType: ParkingType,
        spotNumber: String
    ) {
        viewModelScope.launch {
            _loading.value = true
            try {
                // Validaciones
                if (!validateDate(date)) {
                    _error.value = "La reserva debe ser dentro de los próximos 7 días"
                    _loading.value = false
                    return@launch
                }

                if (!validateDuration(startTime, endTime)) {
                    _error.value = "La reserva no puede ser mayor a 9 horas"
                    _loading.value = false
                    return@launch
                }

                // Crear reserva
                reservationRepository.createReservation(
                    date, startTime, endTime, parkingType, spotNumber
                )
                _reservationCreated.value = true
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }

    fun updateReservation(
        id: String,
        date: Long,
        startTime: String,
        endTime: String,
        parkingType: ParkingType,
        spotNumber: String
    ) {
        viewModelScope.launch {
            _loading.value = true
            try {
                reservationRepository.updateReservation(id, date, startTime, endTime, parkingType, spotNumber)
                _reservationUpdated.value = true
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }

    fun cancelReservation(id: String) {
        viewModelScope.launch {
            _loading.value = true
            try {
                reservationRepository.cancelReservation(id)
                _reservationCancelled.value = true
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }

    private fun validateDate(timestamp: Long): Boolean {
        val selectedDate = Calendar.getInstance().apply {
            timeInMillis = timestamp
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val maxDate = Calendar.getInstance().apply {
            timeInMillis = today.timeInMillis
            add(Calendar.DAY_OF_YEAR, 7)
        }

        return selectedDate.timeInMillis >= today.timeInMillis &&
                selectedDate.timeInMillis <= maxDate.timeInMillis
    }

    private fun validateDuration(startTime: String, endTime: String): Boolean {
        val (startHour, startMinute) = startTime.split(":").map { it.toInt() }
        val (endHour, endMinute) = endTime.split(":").map { it.toInt() }

        val startMinutes = startHour * 60 + startMinute
        val endMinutes = endHour * 60 + endMinute

        val durationMinutes = endMinutes - startMinutes
        val maxMinutes = 9 * 60 // 9 horas

        return durationMinutes > 0 && durationMinutes <= maxMinutes
    }
}
