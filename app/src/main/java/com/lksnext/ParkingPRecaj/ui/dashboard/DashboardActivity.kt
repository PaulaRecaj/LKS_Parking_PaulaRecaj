package com.lksnext.ParkingPRecaj.ui.dashboard

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.lksnext.ParkingPRecaj.ParkingApplication
import com.lksnext.ParkingPRecaj.databinding.ActivityDashboardBinding
import com.lksnext.ParkingPRecaj.notification.NotificationScheduler
import com.lksnext.ParkingPRecaj.ui.ViewModelFactory
import com.lksnext.ParkingPRecaj.ui.adapter.ReservationAdapter
import com.lksnext.ParkingPRecaj.ui.auth.LoginActivity
import com.lksnext.ParkingPRecaj.ui.reservation.MyReservationsActivity
import com.lksnext.ParkingPRecaj.ui.reservation.NewReservationActivity
import kotlin.collections.forEach

class DashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardBinding
    private val viewModel: DashboardViewModel by viewModels {
        ViewModelFactory(reservationRepository = (application as ParkingApplication).reservationRepository)
    }
    private lateinit var adapter: ReservationAdapter

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            viewModel.loadUpcomingReservations()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkNotificationsPermission()
        setupUI()
        setupRecyclerView()
        observeViewModel()

        viewModel.loadUpcomingReservations()
    }

    override fun onResume() {
        super.onResume()
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

    private fun checkNotificationsPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
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
