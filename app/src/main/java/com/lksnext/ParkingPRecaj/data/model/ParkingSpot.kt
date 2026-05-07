package com.lksnext.ParkingPRecaj.data.model

data class ParkingSpot(
    val number: String,
    val type: ParkingType,
    val isAvailable: Boolean = true
)

object ParkingSpots {
    val NORMAL = listOf("A-10", "A-15", "A-22", "B-05", "B-12", "B-18", "C-03", "C-09")
    val ELECTRIC = listOf("E-01", "E-02", "E-03", "E-04", "E-05")
    val MOTORCYCLE = listOf("M-01", "M-02", "M-03", "M-04", "M-05", "M-06")

    fun getSpotsByType(type: ParkingType): List<String> = when (type) {
        ParkingType.NORMAL -> NORMAL
        ParkingType.ELECTRIC -> ELECTRIC
        ParkingType.MOTORCYCLE -> MOTORCYCLE
    }
}
