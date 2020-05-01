package com.example.amorient.util

import com.example.amorient.R
import com.example.amorient.model.CheckPoint
import java.util.concurrent.TimeUnit
import kotlin.math.*


object Utils {

    fun distance(lat1: Double, lat2: Double, lon1: Double, lon2: Double): Double {
        val radiosEarth = 6371.0 // Radius of the earth

        val latDistance = deg2rad(lat2 - lat1)
        val lonDistance = deg2rad(lon2 - lon1)

        val a = sin(latDistance / 2) * sin(latDistance / 2) + (cos(deg2rad(lat1)) * cos(deg2rad(lat2))
                * sin(lonDistance / 2) * sin(lonDistance / 2))

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        var distance = radiosEarth * c * 1000.0 // convert to meters

        val height = 0.0
        distance = distance.pow(2.0) + height.pow(2.0)

        return sqrt(distance)
    }

    private fun deg2rad(deg: Double): Double {
        return deg * Math.PI / 180.0
    }

    fun getAzimuteCoordenate(lat1: Double, lon1: Double, lat2: Double, lon2: Double): String {
        val latitude1 = Math.toRadians(lat1)
        val latitude2 = Math.toRadians(lat2)

        val longDiff = Math.toRadians(lon2 - lon1)

        val y = sin(longDiff) * cos(latitude2)
        val x = cos(latitude1) * sin(latitude2) - sin(latitude1) * cos(latitude2) * cos(longDiff)

        val resultDegree = (Math.toDegrees(atan2(y = y, x = x)) + 360) % 360
        val coordNames = arrayOf("N", "NNE", "NE", "ENE", "L", "ESE", "SE", "SSE", "S", "SSO", "SO", "OSO", "O", "ONO", "NO", "NNO", "N")
        var directionid = Math.round(resultDegree / 22.5).toDouble()
        // no of array contain 360/16=22.5
        if (directionid < 0) {
            directionid += 16
        }

        val compasLoc = coordNames[directionid.toInt()]

        return "   $compasLoc ${resultDegree.formatAzimute()}"
    }

    fun parseTimeElapsed(timeElapsed: Long): String {
        val time = StringBuilder()
        var millis = timeElapsed

        require(millis >= 0) { "Duração deve ser maior do que zero!" }

        val hours = TimeUnit.MILLISECONDS.toHours(millis)
        millis -= TimeUnit.HOURS.toMillis(hours)

        val minutes = TimeUnit.MILLISECONDS.toMinutes(millis)
        millis -= TimeUnit.MINUTES.toMillis(minutes)

        val seconds = TimeUnit.MILLISECONDS.toSeconds(millis)

        if (hours > 0) {
            time.append(hours)

            if(hours <= 1L)
                time.append(" hora ")
            else
                time.append(" horas ")
        }

        if (minutes > 0) {
            time.append(minutes)

            if(minutes <= 1L)
                time.append(" minuto ")
            else
                time.append(" minutos ")
        }

        if (seconds > 0) {
            time.append(seconds)

            if(seconds <= 1L)
                time.append(" segundo")
            else
                time.append(" segundos")
        }

        return time.toString()
    }

    fun addPoints() = mutableListOf(
            CheckPoint(lat = -32.075706, lng = -52.171241, image = R.drawable.img_ponto_um,
                     description = "Mural CAIC - FURG", key = 1),
            CheckPoint(lat = -32.076275, lng = -52.170570, image = R.drawable.img_ponto_dois,
                    description = "Sistema de caixas d'água", key = 2),
            CheckPoint(lat = -32.075283, lng = -52.173188, image = R.drawable.img_ponto_quatro,
                    description = "Quadra Poliesportiva", key = 3),
            CheckPoint(lat = -32.075847, lng = -52.172147, image = R.drawable.img_ponto_cinco,
                    description = "Pracinha CAIC-FURG", key = 4),
            CheckPoint(lat = -32.076485, lng = -52.170247, image = R.drawable.img_ponto_oito,
                    description = "Mesa de piquenique", key = 5),
            CheckPoint(lat = -32.077460, lng = -52.168101, image = R.drawable.img_ponto_onze,
                    description = "Árvore com balanço", key = 6),
            CheckPoint(lat = -32.074682, lng = -52.169831, image = R.drawable.img_ponto_dezessete,
                    description = "Faixada ICHI - Psicologia", key = 7),
            CheckPoint(lat = -32.077540, lng = -52.171314, image = R.drawable.img_ponto_dezenove,
                    description = "Nova Construção", key = 8)
    )
}