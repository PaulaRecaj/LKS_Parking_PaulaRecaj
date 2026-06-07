package com.lksnext.ParkingPRecaj.ui.reservation

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.lksnext.ParkingPRecaj.data.model.ParkingType
import com.lksnext.ParkingPRecaj.data.model.Reservation
import com.lksnext.ParkingPRecaj.data.model.ReservationStatus
import com.lksnext.ParkingPRecaj.data.repository.ReservationRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.Calendar

@ExperimentalCoroutinesApi
class ReservationViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: ReservationRepository
    private lateinit var viewModel: ReservationViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk()
        viewModel = ReservationViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `createReservation with invalid duration should show error`() {
        // Given: Tomorrow (to avoid past time validation) 
        // Duration > 9 hours (10:00 to 20:00 = 10h)
        val calendar = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, 1)
        }
        val tomorrow = calendar.timeInMillis
        val startTime = "10:00"
        val endTime = "20:01"

        // When
        viewModel.createReservation(tomorrow, startTime, endTime, ParkingType.NORMAL, "A-10")

        // Then
        assertEquals("La reserva no puede ser mayor a 9 horas", viewModel.error.value)
    }

    @Test
    fun `createReservation more than 7 days in future should show error`() {
        // Given: 8 days in future
        val calendar = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, 8)
        }
        val futureDate = calendar.timeInMillis
        val startTime = "10:00"
        val endTime = "12:00"

        // When
        viewModel.createReservation(futureDate, startTime, endTime, ParkingType.NORMAL, "A-10")

        // Then
        assertEquals("La reserva debe ser dentro de los próximos 7 días", viewModel.error.value)
    }

    @Test
    fun `createReservation for past time today should show error`() {
        // Given: Today, but 1 hour ago
        val now = Calendar.getInstance()
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
        }
        val today = calendar.timeInMillis
        
        // Construct a past time string
        val pastHour = if (now.get(Calendar.HOUR_OF_DAY) > 0) now.get(Calendar.HOUR_OF_DAY) - 1 else 0
        val startTime = String.format("%02d:00", pastHour)
        val endTime = String.format("%02d:00", pastHour + 1)

        // When
        viewModel.createReservation(today, startTime, endTime, ParkingType.NORMAL, "A-10")

        // Then
        if (now.get(Calendar.HOUR_OF_DAY) > 0) {
            assertEquals("La hora de inicio debe ser posterior a la actual", viewModel.error.value)
        }
    }

    @Test
    fun `createReservation with valid data should call repository`() {
        // Given
        val calendar = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, 1) // Tomorrow
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
        }
        val date = calendar.timeInMillis
        val startTime = "10:00"
        val endTime = "12:00"
        val spot = "A-10"
        val type = ParkingType.NORMAL

        val mockReservation = Reservation(
            "1", "user1", date, startTime, endTime, type, spot, ReservationStatus.CONFIRMED
        )

        coEvery { 
            repository.createReservation(date, startTime, endTime, type, spot) 
        } returns mockReservation

        // When
        viewModel.createReservation(date, startTime, endTime, type, spot)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertEquals(mockReservation, viewModel.reservationCreated.value)
        assertEquals(null, viewModel.error.value)
    }
}
