package com.example.lks_parking_paularecaj.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class User(
    val id: String,
    val email: String,
    val name: String,
    val token: String? = null
) : Parcelable