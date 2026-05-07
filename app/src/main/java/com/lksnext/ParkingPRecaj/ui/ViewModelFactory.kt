package com.lksnext.ParkingPRecaj.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.lksnext.ParkingPRecaj.data.repository.AuthRepository
import com.lksnext.ParkingPRecaj.data.repository.ReservationRepository
import com.lksnext.ParkingPRecaj.ui.auth.AuthViewModel
import com.lksnext.ParkingPRecaj.ui.dashboard.DashboardViewModel
import com.lksnext.ParkingPRecaj.ui.reservation.ReservationViewModel

class ViewModelFactory(
    private val authRepository: AuthRepository? = null,
    private val reservationRepository: ReservationRepository? = null
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(AuthViewModel::class.java) -> {
                AuthViewModel(authRepository!!) as T
            }
            modelClass.isAssignableFrom(DashboardViewModel::class.java) -> {
                DashboardViewModel(reservationRepository!!) as T
            }
            modelClass.isAssignableFrom(ReservationViewModel::class.java) -> {
                ReservationViewModel(reservationRepository!!) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
