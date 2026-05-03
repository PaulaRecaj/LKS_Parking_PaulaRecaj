package com.example.lks_parking_paularecaj.data.api

import com.example.lks_parking_paularecaj.data.model.ParkingType
import com.example.lks_parking_paularecaj.data.model.Reservation
import com.example.lks_parking_paularecaj.data.model.User
import retrofit2.http.*

interface ParkingApiService {

    @POST("auth/login")
    suspend fun login(
        @Body credentials: Map<String, String>
    ): User

    @POST("auth/register")
    suspend fun register(
        @Body userData: Map<String, String>
    ): User

    @POST("auth/forgot-password")
    suspend fun forgotPassword(
        @Body email: Map<String, String>
    )

    @GET("reservations/upcoming")
    suspend fun getUpcomingReservations(): List<Reservation>

    @GET("reservations/past")
    suspend fun getPastReservations(): List<Reservation>

    @POST("reservations")
    suspend fun createReservation(
        @Query("date") date: Long,
        @Query("startTime") startTime: String,
        @Query("endTime") endTime: String,
        @Query("parkingType") parkingType: ParkingType,
        @Query("spotNumber") spotNumber: String
    ): Reservation

    @PUT("reservations/{id}")
    suspend fun updateReservation(
        @Path("id") id: String,
        @Query("date") date: Long,
        @Query("startTime") startTime: String,
        @Query("endTime") endTime: String,
        @Query("parkingType") parkingType: ParkingType,
        @Query("spotNumber") spotNumber: String
    ): Reservation

    @DELETE("reservations/{id}")
    suspend fun cancelReservation(@Path("id") id: String)

    @GET("parking-spots/available")
    suspend fun getAvailableSpots(
        @Query("type") type: ParkingType,
        @Query("date") date: Long,
        @Query("startTime") startTime: String,
        @Query("endTime") endTime: String
    ): List<String>
}
