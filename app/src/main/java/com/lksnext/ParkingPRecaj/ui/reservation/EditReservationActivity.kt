package com.lksnext.ParkingPRecaj.ui.reservation

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.lksnext.ParkingPRecaj.ParkingApplication
import com.lksnext.ParkingPRecaj.data.model.Reservation
import com.lksnext.ParkingPRecaj.databinding.ActivityEditReservationBinding
import com.lksnext.ParkingPRecaj.ui.ViewModelFactory
import java.text.SimpleDateFormat
import java.util.*

class EditReservationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditReservationBinding
    private val viewModel: ReservationViewModel by viewModels {
        ViewModelFactory(reservationRepository = (application as ParkingApplication).reservationRepository)
    }

    private var reservation: Reservation? = null
    private var selectedDate: Long = 0
    private val dateFormatDisplay = SimpleDateFormat("EEEE, d 'de' MMMM", Locale("es", "ES"))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditReservationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        reservation = intent.getParcelableExtra("reservation")
        if (reservation == null) {
            finish()
            return
        }

        setupUI()
        loadReservationData()
        observeViewModel()
    }

    private fun setupUI() {
        binding.btnBack.setOnClickListener { finish() }

        binding.etDate.setOnClickListener { showMaterialDatePicker() }
        binding.etStartTime.setOnClickListener { showMaterialTimePicker(true) }
        binding.etEndTime.setOnClickListener { showMaterialTimePicker(false) }

        binding.btnSave.setOnClickListener { saveChanges() }
        binding.btnCancel.setOnClickListener { cancelReservation() }
    }

    private fun loadReservationData() {
        reservation?.let {
            selectedDate = it.date
            updateDateDisplay()
            binding.etStartTime.setText(it.startTime)
            binding.etEndTime.setText(it.endTime)
            binding.tvSpotInfo.text = "Plaza: ${it.spotNumber} (${it.parkingType.getDisplayName()})"
        }
    }

    private fun updateDateDisplay() {
        val dateStr = dateFormatDisplay.format(Date(selectedDate)).replaceFirstChar { it.uppercase() }
        binding.etDate.setText(dateStr)
    }

    private fun showMaterialDatePicker() {
        val today = MaterialDatePicker.todayInUtcMilliseconds()
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
        }
        datePicker.show(supportFragmentManager, "DATE_PICKER")
    }

    private fun showMaterialTimePicker(isStartTime: Boolean) {
        val currentTime = if (isStartTime) binding.etStartTime.text.toString() else binding.etEndTime.text.toString()
        val (h, m) = if (currentTime.isNotEmpty()) {
            currentTime.split(":").map { it.toInt() }
        } else {
            listOf(if (isStartTime) 8 else 14, 0)
        }

        val picker = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_24H)
            .setHour(h)
            .setMinute(m)
            .setTitleText(if (isStartTime) "Hora de inicio" else "Hora de fin")
            .build()

        picker.addOnPositiveButtonClickListener {
            val timeStr = String.format(Locale.getDefault(), "%02d:%02d", picker.hour, picker.minute)
            if (isStartTime) binding.etStartTime.setText(timeStr)
            else binding.etEndTime.setText(timeStr)
        }
        picker.show(supportFragmentManager, "TIME_PICKER")
    }

    private fun saveChanges() {
        val res = reservation ?: return
        val startTime = binding.etStartTime.text.toString()
        val endTime = binding.etEndTime.text.toString()

        viewModel.updateReservation(
            res.id,
            selectedDate,
            startTime,
            endTime,
            res.parkingType,
            res.spotNumber
        )
    }

    private fun cancelReservation() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Cancelar Reserva")
            .setMessage("¿Estás seguro de que deseas cancelar esta reserva?")
            .setPositiveButton("Sí, cancelar") { _, _ ->
                reservation?.let { viewModel.cancelReservation(it.id) }
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun observeViewModel() {
        viewModel.reservationUpdated.observe(this) { updatedReservation ->
            if (updatedReservation != null) {
                // Cancelar notificaciones antiguas y programar las nuevas
                com.lksnext.ParkingPRecaj.notification.NotificationScheduler.cancelReservationNotifications(this, updatedReservation.id)
                com.lksnext.ParkingPRecaj.notification.NotificationScheduler.scheduleReservationNotifications(this, updatedReservation)
                
                Toast.makeText(this, "Reserva actualizada con éxito", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

        viewModel.reservationCancelled.observe(this) { success ->
            if (success) {
                reservation?.let { 
                    com.lksnext.ParkingPRecaj.notification.NotificationScheduler.cancelReservationNotifications(this, it.id)
                }
                Toast.makeText(this, "Reserva cancelada con éxito", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

        viewModel.error.observe(this) { error ->
            error?.let { Toast.makeText(this, it, Toast.LENGTH_SHORT).show() }
        }
    }
}
