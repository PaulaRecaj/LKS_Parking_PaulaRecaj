package com.example.lks_parking_paularecaj.ui.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lks_parking_paularecaj.data.model.Reservation
import com.example.lks_parking_paularecaj.data.repository.ReservationRepository
import kotlinx.coroutines.launch

class DashboardViewModel(
    private val reservationRepository: ReservationRepository
) : ViewModel() {

    private val _upcomingReservations = MutableLiveData<List<Reservation>>()
    val upcomingReservations: LiveData<List<Reservation>> = _upcomingReservations

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun loadUpcomingReservations() {
        viewModelScope.launch {
            _loading.value = true
            try {
                val reservations = reservationRepository.getUpcomingReservations()
                _upcomingReservations.value = reservations
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }
}
