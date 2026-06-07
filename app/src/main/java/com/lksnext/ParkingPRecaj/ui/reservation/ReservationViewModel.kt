package com.lksnext.ParkingPRecaj.ui.reservation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lksnext.ParkingPRecaj.data.model.ParkingType
import com.lksnext.ParkingPRecaj.data.model.Reservation
import com.lksnext.ParkingPRecaj.data.repository.ReservationRepository
import kotlinx.coroutines.launch
import java.util.Calendar

class ReservationViewModel(
    private val reservationRepository: ReservationRepository
) : ViewModel() {

    private val _availableSpots = MutableLiveData<List<String>>()
    val availableSpots: LiveData<List<String>> = _availableSpots

    private val _reservationCreated = MutableLiveData<Reservation?>()
    val reservationCreated: LiveData<Reservation?> = _reservationCreated

    private val _reservationUpdated = MutableLiveData<Reservation?>()
    val reservationUpdated: LiveData<Reservation?> = _reservationUpdated

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
        if (startTime.isEmpty() || endTime.isEmpty()) return
        
        viewModelScope.launch {
            _loading.value = true
            try {
                if (!validateDate(date)) {
                    _error.value = "La reserva debe ser dentro de los próximos 7 días"
                    _availableSpots.value = emptyList()
                    return@launch
                }

                if (!validateStartTime(date, startTime)) {
                    _error.value = "La hora de inicio debe ser posterior a la actual"
                    _availableSpots.value = emptyList()
                    return@launch
                }

                if (!validateDuration(startTime, endTime)) {
                    _error.value = "La reserva no puede ser mayor a 9 horas"
                    _availableSpots.value = emptyList()
                    return@launch
                }
                
                // Consultar spots disponibles filtrando por fecha y hora para evitar solapamientos
                val spots = reservationRepository.getAvailableSpots(type, date, startTime, endTime)
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
        if (!performValidations(date, startTime, endTime)) return

        viewModelScope.launch {
            _loading.value = true
            try {
                if (spotNumber.isEmpty()) {
                    _error.value = "Debes seleccionar una plaza"
                    return@launch
                }

                // Crear reserva
                val reservation = reservationRepository.createReservation(
                    date, startTime, endTime, parkingType, spotNumber
                )
                _reservationCreated.value = reservation
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
        if (!performValidations(date, startTime, endTime)) return

        viewModelScope.launch {
            _loading.value = true
            try {
                val updated = reservationRepository.updateReservation(id, date, startTime, endTime, parkingType, spotNumber)
                _reservationUpdated.value = updated
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }

    private fun performValidations(date: Long, startTime: String, endTime: String): Boolean {
        if (!validateDate(date)) {
            _error.value = "La reserva debe ser dentro de los próximos 7 días"
            return false
        }

        if (!validateStartTime(date, startTime)) {
            _error.value = "La hora de inicio debe ser posterior a la actual"
            return false
        }

        if (!validateDuration(startTime, endTime)) {
            _error.value = "La reserva no puede ser mayor a 9 horas"
            return false
        }
        return true
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

        var durationMinutes = endMinutes - startMinutes
        
        // Si la hora de fin es menor que la de inicio, asumimos que es el día siguiente
        if (durationMinutes <= 0) {
            durationMinutes += 24 * 60
        }

        val maxMinutes = 9 * 60 // 9 horas

        return durationMinutes > 0 && durationMinutes <= maxMinutes
    }

    private fun validateStartTime(date: Long, startTime: String): Boolean {
        val now = Calendar.getInstance()
        
        // Convertir el timestamp UTC a medianoche local para comparar días
        val selectedDate = Calendar.getInstance().apply {
            timeInMillis = date
            // Ajustamos a medianoche local para la comparación de días
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

        // Si es hoy, verificar que la hora de inicio no haya pasado
        if (selectedDate.timeInMillis == today.timeInMillis) {
            val (startHour, startMinute) = startTime.split(":").map { it.toInt() }
            val currentHour = now.get(Calendar.HOUR_OF_DAY)
            val currentMinute = now.get(Calendar.MINUTE)

            if (startHour < currentHour || (startHour == currentHour && startMinute <= currentMinute)) {
                return false
            }
        }
        
        return true
    }
}
