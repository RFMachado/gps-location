package com.example.amorient.util.extensions

import java.text.DecimalFormat

fun Double.formatDistance(): String = DecimalFormat("#.##m").format(this)

fun Double.formatAzimute(): String = "(" +DecimalFormat("#.##").format(this) + "°)"
