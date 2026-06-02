package com.lksnext.ParkingPRecaj.ui.reservation

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.lksnext.ParkingPRecaj.ParkingApplication
import com.lksnext.ParkingPRecaj.data.model.ParkingType
import com.lksnext.ParkingPRecaj.databinding.ActivityNewReservationBinding
import com.lksnext.ParkingPRecaj.ui.ViewModelFactory
import com.lksnext.ParkingPRecaj.ui.adapter.SpotAdapter
import java.text.SimpleDateFormat
import java.util.*

class NewReservationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNewReservationBinding
    private val viewModel: ReservationViewModel by viewModels {
        ViewModelFactory(reservationRepository = (application as ParkingApplication).reservationRepository)
    }

    private lateinit var spotAdapter: SpotAdapter
    private var selectedDate: Long = System.currentTimeMillis()
    
    private val dateFormatDisplay = SimpleDateFormat("EEEE, d 'de' MMMM", Locale("es", "ES"))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewReservationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        observeViewModel()
    }

    private fun setupUI() {
        binding.btnBack.setOnClickListener { finish() }

        // Grid de plazas
        spotAdapter = SpotAdapter(emptyList()) { _ -> }
        binding.rvParkingSpots.layoutManager = GridLayoutManager(this, 3)
        binding.rvParkingSpots.adapter = spotAdapter

        // Inicializar fecha
        updateDateDisplay()
        
        binding.etDate.setOnClickListener { showMaterialDatePicker() }
        binding.etStartTime.setOnClickListener { showMaterialTimePicker(true) }
        binding.etEndTime.setOnClickListener { showMaterialTimePicker(false) }

        binding.chipGroupParkingType.setOnCheckedStateChangeListener { _, _ -> updateAvailableSpots() }

        binding.btnConfirm.setOnClickListener { confirmReservation() }
        
        // Carga inicial
        updateAvailableSpots()
    }

    private fun updateDateDisplay() {
        val dateStr = dateFormatDisplay.format(Date(selectedDate)).replaceFirstChar { it.uppercase() }
        binding.etDate.setText(dateStr)
    }

    private fun showMaterialDatePicker() {
        val today = MaterialDatePicker.todayInUtcMilliseconds()
        
        // Límite de 7 días
        val constraints = CalendarConstraints.Builder()
            .setValidator(DateValidatorPointForward.now())
            .setStart(today)
            .setEnd(today + (7 * 24 * 60 * 60 * 1000L)) 
            .build()

        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Selecciona el día")
            .setSelection(selectedDate)
            .setCalendarConstraints(constraints)
            .build()

        datePicker.addOnPositiveButtonClickListener { selection ->
            selectedDate = selection ?: selectedDate
            updateDateDisplay()
            updateAvailableSpots()
        }
        datePicker.show(supportFragmentManager, "DATE_PICKER")
    }

    private fun showMaterialTimePicker(isStartTime: Boolean) {
        val picker = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_24H)
            .setHour(if (isStartTime) 8 else 14)
            .setMinute(0)
            .setTitleText(if (isStartTime) "Hora de inicio" else "Hora de fin")
            .build()

        picker.addOnPositiveButtonClickListener {
            val timeStr = String.format(Locale.getDefault(), "%02d:%02d", picker.hour, picker.minute)
            if (isStartTime) binding.etStartTime.setText(timeStr) 
            else binding.etEndTime.setText(timeStr)
            updateAvailableSpots()
        }
        picker.show(supportFragmentManager, "TIME_PICKER")
    }

    private fun updateAvailableSpots() {
        val startTime = binding.etStartTime.text.toString()
        val endTime = binding.etEndTime.text.toString()
        if (startTime.isNotEmpty() && endTime.isNotEmpty()) {
            viewModel.loadAvailableSpots(getSelectedParkingType(), selectedDate, startTime, endTime)
        }
    }

    private fun getSelectedParkingType(): ParkingType {
        return when (binding.chipGroupParkingType.checkedChipId) {
            binding.chipElectric.id -> ParkingType.ELECTRIC
            binding.chipMotorcycle.id -> ParkingType.MOTORCYCLE
            else -> ParkingType.NORMAL
        }
    }

    private fun confirmReservation() {
        val startTime = binding.etStartTime.text.toString()
        val endTime = binding.etEndTime.text.toString()
        val spot = spotAdapter.getSelectedSpot()

        if (startTime.isEmpty() || endTime.isEmpty()) {
            Toast.makeText(this, "Selecciona el horario", Toast.LENGTH_SHORT).show()
            return
        }

        if (spot == null) {
            Toast.makeText(this, "Selecciona una plaza", Toast.LENGTH_SHORT).show()
            return
        }

        viewModel.createReservation(selectedDate, startTime, endTime, getSelectedParkingType(), spot)
    }

    private fun observeViewModel() {
        viewModel.availableSpots.observe(this) { spots -> spotAdapter.updateSpots(spots) }

        viewModel.reservationCreated.observe(this) { success ->
            if (success) {
                Toast.makeText(this, "Reserva confirmada con éxito", Toast.LENGTH_LONG).show()
                finish()
            }
        }

        viewModel.error.observe(this) { error ->
            error?.let { Toast.makeText(this, it, Toast.LENGTH_SHORT).show() }
        }

        viewModel.loading.observe(this) { isLoading ->
            binding.btnConfirm.isEnabled = !isLoading
            binding.btnConfirm.text = if (isLoading) "Procesando..." else "Confirmar Reserva"
        }
    }
}
