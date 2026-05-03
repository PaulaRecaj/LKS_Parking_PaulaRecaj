package com.example.lks_parking_paularecaj.ui.reservation

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.lks_parking_paularecaj.ParkingApplication
import com.example.lks_parking_paularecaj.data.model.Reservation
import com.example.lks_parking_paularecaj.databinding.ActivityEditReservationBinding
import com.example.lks_parking_paularecaj.ui.ViewModelFactory

class EditReservationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditReservationBinding
    private val viewModel: ReservationViewModel by viewModels {
        ViewModelFactory(reservationRepository = (application as ParkingApplication).reservationRepository)
    }

    private var reservation: Reservation? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditReservationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtener la reserva a editar
        reservation = intent.getParcelableExtra("reservation")

        setupUI()
        observeViewModel()
        loadReservationData()
    }

    private fun setupUI() {
        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnSave.setOnClickListener {
            saveChanges()
        }

        binding.btnCancel.setOnClickListener {
            cancelReservation()
        }
    }

    private fun loadReservationData() {
        reservation?.let {
            // binding.tvDate.text = it.date.toString() // Formatear adecuadamente
            // Cargar resto de datos...
        }
    }

    private fun saveChanges() {
        reservation?.let {
            // viewModel.updateReservation(...)
        }
    }

    private fun cancelReservation() {
        reservation?.let {
            viewModel.cancelReservation(it.id)
        }
    }

    private fun observeViewModel() {
        viewModel.reservationUpdated.observe(this) { success ->
            if (success) {
                Toast.makeText(this, "Reserva actualizada", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

        viewModel.reservationCancelled.observe(this) { success ->
            if (success) {
                Toast.makeText(this, "Reserva cancelada", Toast.LENGTH_SHORT).show()
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
