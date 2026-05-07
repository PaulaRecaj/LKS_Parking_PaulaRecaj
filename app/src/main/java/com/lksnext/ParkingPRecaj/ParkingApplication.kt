package com.lksnext.ParkingPRecaj

import android.app.Application
import com.lksnext.ParkingPRecaj.data.api.RetrofitClient
import com.lksnext.ParkingPRecaj.data.repository.AuthRepository
import com.lksnext.ParkingPRecaj.data.repository.ReservationRepository
import com.lksnext.ParkingPRecaj.notification.NotificationScheduler

class ParkingApplication : Application() {

    // Repositorios globales
    val authRepository: AuthRepository by lazy {
        AuthRepository(RetrofitClient.apiService, this)
    }

    val reservationRepository: ReservationRepository by lazy {
        ReservationRepository(RetrofitClient.apiService)
    }

    override fun onCreate() {
        super.onCreate()

        // Inicializar canal de notificaciones
        NotificationScheduler.createNotificationChannel(this)
    }
}