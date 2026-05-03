package com.example.lks_parking_paularecaj.ui.reservation

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.lks_parking_paularecaj.ParkingApplication
import com.example.lks_parking_paularecaj.databinding.ActivityNewReservationBinding
import com.example.lks_parking_paularecaj.ui.ViewModelFactory

class NewReservationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNewReservationBinding
    private val viewModel: ReservationViewModel by viewModels {
        ViewModelFactory(reservationRepository = (application as ParkingApplication).reservationRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewReservationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        observeViewModel()
    }

    private fun setupUI() {
        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnConfirm.setOnClickListener {
            // Lógica para confirmar reserva
            // viewModel.createReservation(...)
        }
    }

    private fun observeViewModel() {
        viewModel.reservationCreated.observe(this) { success ->
            if (success) {
                Toast.makeText(this, "Reserva creada", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

        viewModel.error.observe(this) { error ->
            error?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
            }
        }
    }
}
