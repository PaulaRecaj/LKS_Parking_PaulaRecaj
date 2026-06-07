package com.lksnext.ParkingPRecaj.ui.reservation

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.lksnext.ParkingPRecaj.ParkingApplication
import com.lksnext.ParkingPRecaj.data.model.Reservation
import com.lksnext.ParkingPRecaj.databinding.ActivityMyReservationsBinding
import com.lksnext.ParkingPRecaj.ui.ViewModelFactory
import com.lksnext.ParkingPRecaj.ui.adapter.ReservationAdapter

class MyReservationsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMyReservationsBinding
    private val viewModel: ReservationViewModel by viewModels {
        ViewModelFactory(reservationRepository = (application as ParkingApplication).reservationRepository)
    }
    private lateinit var adapter: ReservationAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyReservationsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        setupRecyclerView()
        observeViewModel()

        viewModel.loadMyReservations()
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadMyReservations()
    }

    private fun setupUI() {
        binding.btnBack.setOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerView() {
        adapter = ReservationAdapter(
            reservations = emptyList(),
            onEditClick = { reservation ->
                val intent = Intent(this, EditReservationActivity::class.java).apply {
                    putExtra("reservation", reservation)
                }
                startActivity(intent)
            },
            onCancelClick = { reservation ->
                showCancelConfirmation(reservation)
            },
            showActions = true
        )
        binding.rvReservations.apply {
            layoutManager = LinearLayoutManager(this@MyReservationsActivity)
            this.adapter = this@MyReservationsActivity.adapter
        }
    }

    private fun showCancelConfirmation(reservation: Reservation) {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Cancelar Reserva")
            .setMessage("¿Estás seguro de que deseas cancelar la reserva en la plaza ${reservation.spotNumber}?")
            .setPositiveButton("Sí, cancelar") { _, _ ->
                viewModel.cancelReservation(reservation.id)
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun observeViewModel() {
        viewModel.myReservations.observe(this) { reservations ->
            adapter.updateReservations(reservations)
            binding.tvEmpty.visibility = if (reservations.isEmpty()) View.VISIBLE else View.GONE
        }

        viewModel.reservationCancelled.observe(this) { success ->
            if (success) {
                // Notificar cancelación de alarmas si tenemos acceso al ID (el ViewModel lo hace por ID)
                // En MyReservationsActivity recargamos la lista, las alarmas se deberían gestionar en el ViewModel o Repository si fuera más complejo
                Toast.makeText(this, "Reserva cancelada con éxito", Toast.LENGTH_SHORT).show()
                viewModel.loadMyReservations()
            }
        }

        viewModel.loading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.error.observe(this) { error ->
            error?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
            }
        }
    }
}
