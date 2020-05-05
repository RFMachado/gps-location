package com.example.amorient.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class TeamResult(
    val teamName: String,
    val mode: String,
    var duration: String
): Parcelable