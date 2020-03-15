package com.example.amorient.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize


@Parcelize
class CheckPoint(
        val lat: Double,
        val lng: Double,
        val description: String = "",
        val title: String = "",
        val image: Int,
        val key: Int
): Parcelable