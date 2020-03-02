package com.example.amorient

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

    fun addPoints() = mutableListOf(
            CheckPoint(lat = -32.075731, lng = -52.171524, image = R.drawable.img_ponto_um, key = 1),
            CheckPoint(lat = -32.076270,  lng = -52.170581, image = R.drawable.img_ponto_dois, key = 2),
            CheckPoint(lat = -32.075284, lng = -52.173304, image = R.drawable.img_ponto_quatro, key = 3),
            CheckPoint(lat = -32.075800, lng = -52.172000, image = R.drawable.img_ponto_cinco, key = 4),
            CheckPoint(lat = -32.074263, lng = -52.172776, image = R.drawable.img_ponto_sete, key = 5),
            CheckPoint(lat = -32.076489, lng = -52.170248, image = R.drawable.img_ponto_oito, key = 6),
            CheckPoint(lat = -32.076250, lng = -52.169062, image = R.drawable.img_ponto_nove, key = 7),
            CheckPoint(lat = -32.077472, lng = -52.168094, image = R.drawable.img_ponto_onze, key = 8),
            CheckPoint(lat= -32.076847, lng = -52.167289, image = R.drawable.img_ponto_doze, key = 9),
            CheckPoint(lat = -32.077458, lng = -52.166176, image = R.drawable.img_ponto_treze, key = 10),
            CheckPoint(lat = -32.075472, lng = -52.167952, image = R.drawable.img_ponto_quinze, key = 11),
            CheckPoint(lat = -32.073725, lng = -52.168713, image = R.drawable.img_ponto_dezessseis, key = 12),
            CheckPoint(lat = -32.074682, lng = -52.169858, image = R.drawable.img_ponto_dezessete, key = 13),
            CheckPoint(lat = -32.075521, lng =  -52.169220, image = R.drawable.img_ponto_dezoito, key = 14),
            CheckPoint(lat = -32.077628, lng = -52.171355, image = R.drawable.img_ponto_dezenove, key = 15)
    )
}