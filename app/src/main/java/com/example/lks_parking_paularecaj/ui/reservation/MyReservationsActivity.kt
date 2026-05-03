package com.example.lks_parking_paularecaj.ui.reservation

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.lks_parking_paularecaj.ParkingApplication
import com.example.lks_parking_paularecaj.databinding.ActivityMyReservationsBinding
import com.example.lks_parking_paularecaj.ui.ViewModelFactory
import com.example.lks_parking_paularecaj.ui.adapter.ReservationAdapter

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

    private fun setupUI() {
        binding.btnBack.setOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerView() {
        adapter = ReservationAdapter(emptyList())
        binding.rvReservations.apply {
            layoutManager = LinearLayoutManager(this@MyReservationsActivity)
            this.adapter = this@MyReservationsActivity.adapter
        }
    }

    private fun observeViewModel() {
        viewModel.myReservations.observe(this) { reservations ->
            adapter.updateReservations(reservations)
            binding.tvEmpty.visibility = if (reservations.isEmpty()) View.VISIBLE else View.GONE
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
