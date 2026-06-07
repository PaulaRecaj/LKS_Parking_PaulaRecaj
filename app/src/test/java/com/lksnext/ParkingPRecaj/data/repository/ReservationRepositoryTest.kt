package com.lksnext.ParkingPRecaj.data.repository

import com.lksnext.ParkingPRecaj.data.api.ParkingApiService
import com.lksnext.ParkingPRecaj.data.model.ParkingType
import com.lksnext.ParkingPRecaj.data.model.ReservationStatus
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.Calendar

@ExperimentalCoroutinesApi
class ReservationRepositoryTest {

    private lateinit var apiService: ParkingApiService
    private lateinit var repository: ReservationRepository

    @Before
    fun setup() {
        apiService = mockk()
        repository = ReservationRepository(apiService)
    }

    @Test
    fun `getAvailableSpots should filter out overlapping reservations`() = runTest {
        // Given: An existing reservation for today from 10:00 to 12:00 at A-10
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val today = calendar.timeInMillis
        
        coEvery { apiService.getUpcomingReservations() } returns emptyList()
        coEvery { apiService.createReservation(any(), any(), any(), any(), any()) } throws Exception("API Offline")
        
        // Use createReservation to fill the mock list (since we are testing the mock behavior)
        repository.createReservation(today, "10:00", "12:00", ParkingType.NORMAL, "A-10")

        // When: Checking availability for same day, 11:00 to 13:00 (overlaps)
        val availableSpots = repository.getAvailableSpots(ParkingType.NORMAL, today, "11:00", "13:00")

        // Then: A-10 should NOT be in the available list
        assertFalse(availableSpots.contains("A-10"))
        assertTrue(availableSpots.contains("A-15")) // Another normal spot
    }

    @Test
    fun `getAvailableSpots should allow non-overlapping reservations`() = runTest {
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
        }.timeInMillis

        coEvery { apiService.createReservation(any(), any(), any(), any(), any()) } throws Exception("API Offline")
        
        // Existing: 10:00 - 12:00
        repository.createReservation(today, "10:00", "12:00", ParkingType.NORMAL, "A-10")

        // When: Checking availability for 12:00 to 14:00 (does not overlap according to our logic)
        // Our logic says: (newStart < resEnd) AND (newEnd > resStart)
        // (12:00 < 12:00) is FALSE -> No overlap.
        val availableSpots = repository.getAvailableSpots(ParkingType.NORMAL, today, "12:00", "14:00")

        // Then: A-10 SHOULD be available
        assertTrue(availableSpots.contains("A-10"))
    }
}
