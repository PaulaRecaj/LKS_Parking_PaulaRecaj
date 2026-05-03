package com.example.lks_parking_paularecaj.ui.dashboard

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.lks_parking_paularecaj.ParkingApplication
import com.example.lks_parking_paularecaj.databinding.ActivityDashboardBinding
import com.example.lks_parking_paularecaj.notification.NotificationScheduler
import com.example.lks_parking_paularecaj.ui.ViewModelFactory
import com.example.lks_parking_paularecaj.ui.adapter.ReservationAdapter
import com.example.lks_parking_paularecaj.ui.auth.LoginActivity
import com.example.lks_parking_paularecaj.ui.reservation.MyReservationsActivity
import com.example.lks_parking_paularecaj.ui.reservation.NewReservationActivity

class DashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardBinding
    private val viewModel: DashboardViewModel by viewModels {
        ViewModelFactory(reservationRepository = (application as ParkingApplication).reservationRepository)
    }
    private lateinit var adapter: ReservationAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        setupRecyclerView()
        observeViewModel()

        viewModel.loadUpcomingReservations()
    }

    private fun setupUI() {
        binding.btnNewReservation.setOnClickListener {
            startActivity(Intent(this, NewReservationActivity::class.java))
        }

        binding.btnMyReservations.setOnClickListener {
            startActivity(Intent(this, MyReservationsActivity::class.java))
        }

        binding.btnLogout.setOnClickListener {
            logout()
        }
    }

    private fun setupRecyclerView() {
        adapter = ReservationAdapter(emptyList())
        binding.rvUpcomingReservations.apply {
            layoutManager = LinearLayoutManager(this@DashboardActivity)
            this.adapter = this@DashboardActivity.adapter
        }
    }

    private fun observeViewModel() {
        viewModel.upcomingReservations.observe(this) { reservations ->
            adapter.updateReservations(reservations)

            // Programar notificaciones para las reservas
            reservations.forEach { reservation ->
                NotificationScheduler.scheduleReservationNotifications(this, reservation)
            }
        }
    }

    private fun logout() {
        // Limpiar sesión
        val prefs = getSharedPreferences("parking_prefs", MODE_PRIVATE)
        prefs.edit().clear().apply()

        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}
