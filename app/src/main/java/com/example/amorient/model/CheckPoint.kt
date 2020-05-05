package com.example.amorient.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class CheckPoint(
    val lat: Double,
    val lng: Double,
    val description: String = "",
    val distance: String = "",
    val image: Int? = null,
    val imagePath: String? = null,
    val key: Int,
    var isFirst: Boolean = false
): Parcelable