package com.example.amorient.map

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.PorterDuff
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.os.SystemClock
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.View.MeasureSpec.UNSPECIFIED
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.example.amorient.R
import com.example.amorient.detail.PointDetailActivity
import com.example.amorient.menu.MenuActivity
import com.example.amorient.model.CheckPoint
import com.example.amorient.util.Utils
import com.example.amorient.util.extensions.formatDistance
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import kotlinx.android.synthetic.main.activity_maps.*
import kotlinx.android.synthetic.main.finish_game_dialog.view.*


class MapsActivity : FragmentActivity(), OnMapReadyCallback, SensorEventListener {

    var mLastLocation: Location? = null
    private var mGoogleMap: GoogleMap? = null
    private var sensorManager: SensorManager? = null
    private var accelerometer: Sensor? = null
    private var magnetometer: Sensor? = null
    private var rotationvector: Sensor? = null

    private var mapFrag: SupportMapFragment? = null
    private var mLocationRequest: LocationRequest? = null
    private var mFusedLocationClient: FusedLocationProviderClient? = null

    private var checkPoints : MutableList<CheckPoint> = mutableListOf()
    private var totalPoints = 0
    private val hashMapMarker = HashMap<Int, Marker>()

    private var rotationMatrix = FloatArray(9)
    private var orientation = FloatArray(3)
    private var azimuth: Int = 0

    private var lastAccelerometer = FloatArray(3)
    private var lastAccelerometerSet = false

    private var lastMagnetometer = FloatArray(3)
    private var lastMagnetometerSet = false

    var haveSensorAccelerometer = false
    var haveSensorMagnetometer = false
    var haveSensorRotationVector = false

    companion object {
        const val REQUEST_IMAGE_CAPTURE = 1
        const val MY_PERMISSIONS_REQUEST_LOCATION = 99
        const val INTERVAL_CHECK_LOCATION = 500L //  120000 two minute interval

        fun launchIntent(context: Context) = Intent(context, MapsActivity::class.java)

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        startCompass()

        checkPoints = Utils.addPoints()
        totalPoints = checkPoints.size
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        mapFrag = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFrag?.getMapAsync(this)

        bindListener()
    }

    private fun bindListener() {
        btnPhoto.setOnClickListener {
            dispatchTakePictureIntent()
        }

        btnPhotoFirstStep.setOnClickListener {
            dispatchTakePictureIntent()
        }

        chronometer.setOnChronometerTickListener {
            val time = SystemClock.elapsedRealtime() - chronometer.base
            val h = (time /3600000).toInt()
            val m = ((time - h*3600000)/60000).toInt()
            val s = ((time - h*3600000 - m*60000)/1000).toInt()

            val hour = if (h < 10) "0$h" else "$h"
            val minute = if (m < 10) "0$m" else "$m"
            val second = if (s < 10) "0$s" else "$s"


            val t = "$hour : $minute : $second"
            chronometer.text = t
        }

        chronometer.text = "00:00"
    }

    private var mLocationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val locationList = locationResult.locations
            if (locationList.size > 0) {
                //The last location in the list is the newest
                val location = locationList[locationList.size - 1]

                if(mLastLocation == null)
                    zoomCurrentPosition(location)

                mLastLocation = location

            }
        }
    }

    public override fun onPause() {
        super.onPause()
        stopCompass()
        //stop location updates when Activity is no longer active
        mFusedLocationClient?.let { mFusedLocationClient!!.removeLocationUpdates(mLocationCallback) }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mGoogleMap = googleMap.apply {
            uiSettings.isMapToolbarEnabled = false
            uiSettings.isMyLocationButtonEnabled = false
            uiSettings.isCompassEnabled = false
            uiSettings.isRotateGesturesEnabled = false
            uiSettings.isTiltGesturesEnabled = false

            setMapStyle(MapStyleOptions.loadRawResourceStyle(this@MapsActivity, R.raw.style_json))

            setOnMarkerClickListener {
                val tag = it.tag as Int?

                tag?.let {
                    checkPoints.forEach { checkPoint ->
                        if (checkPoint.key == tag) {
                            val pointDistance = getDistanceAzimute(checkPoint)

                            val intent = PointDetailActivity.launchIntent(
                                    this@MapsActivity, pointDistance
                            )
                            startActivity(intent)
                        }
                    }
                }

                false
            }
        }

        mLocationRequest = LocationRequest().apply {
            interval = INTERVAL_CHECK_LOCATION
            fastestInterval = INTERVAL_CHECK_LOCATION
            priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
        }

        checkPoints.forEachIndexed { index, checkPoint ->
            val image = if(checkPoint.image == null)
                getMarkerBitmapFromUri(checkPoint.imagePath)
            else
                getMarkerBitmapFromView(checkPoint.image)

            val markerOptions = MarkerOptions()
                    .position(LatLng(checkPoint.lat, checkPoint.lng))
                    .icon(BitmapDescriptorFactory.fromBitmap(image))

            val marker =  mGoogleMap!!.addMarker(markerOptions)
            marker.tag = checkPoint.key

            hashMapMarker[checkPoint.key] = marker
        }


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                //Location Permission already granted
                mFusedLocationClient!!.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper())
                mGoogleMap?.isMyLocationEnabled = true

            } else {
                //Request Location Permission
                checkLocationPermission()
            }
        } else {
            mFusedLocationClient!!.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper())
            mGoogleMap?.isMyLocationEnabled = true
        }
    }

    private fun getDistanceAzimute(point: CheckPoint): CheckPoint {
        mLastLocation?.let { myLocation ->
            val distance = Utils.distance(
                    myLocation.latitude, point.lat,
                    myLocation.longitude, point.lng
            ).formatDistance()

            val azimute = Utils.getAzimuteCoordenate(
                    myLocation.latitude, myLocation.longitude, point.lat, point.lng
            )

            return point.copy(distance = distance + azimute)
        }

        return point
    }

    private fun zoomCurrentPosition(location: Location) {
        val target = LatLng(location.latitude, location.longitude)

        val cameraPosition = CameraPosition.Builder()
                .target(target)
                .zoom(16f)
                .bearing(10f)
                .build()


        addPolilyne()
        mGoogleMap?.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))

    }

    private fun addPolilyne() {
        val colorRed = 0xFFF44336.toInt()

        val polyline = PolylineOptions()
                .add(
                        LatLng(-32.079711, -52.173849),
                        LatLng(-32.069907, -52.171849)
                )

        val polyline2 = PolylineOptions()
                .add(
                        LatLng(-32.079711, -52.170349),
                        LatLng(-32.069907, -52.168349)
                )

        val polyline3 = PolylineOptions()
                .add(
                        LatLng(-32.080711, -52.167849),
                        LatLng(-32.070907, -52.165849)
                )

        val pol = mGoogleMap?.addPolyline(polyline)
        val pol2 = mGoogleMap?.addPolyline(polyline2)
        val pol3 = mGoogleMap?.addPolyline(polyline3)

        pol?.apply {
            tag = "A"
            width = 5f
            color = colorRed
            endCap = CustomCap(BitmapDescriptorFactory.fromResource(R.drawable.ic_triangle), 6f)
        }

        pol2?.apply {
            tag = "B"
            width = 5f
            color = colorRed
            endCap = CustomCap(BitmapDescriptorFactory.fromResource(R.drawable.ic_triangle), 6f)
        }

        pol3?.apply {
            tag = "C"
            width = 5f
            color = colorRed
            endCap = CustomCap(BitmapDescriptorFactory.fromResource(R.drawable.ic_triangle), 6f)
        }
    }

    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(packageManager)?.also {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
            }
        }
    }

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)) {

                AlertDialog.Builder(this)
                        .setTitle("Location Permission Needed")
                        .setMessage("This app needs the Location permission, please accept to use location functionality")
                        .setPositiveButton("OK") { _, i ->
                            //Prompt the user once explanation has been shown
                            ActivityCompat.requestPermissions(this@MapsActivity,
                                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                                    MY_PERMISSIONS_REQUEST_LOCATION)
                        }
                        .create()
                        .show()


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        MY_PERMISSIONS_REQUEST_LOCATION)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == MY_PERMISSIONS_REQUEST_LOCATION) {
            // If request is cancelled, the result arrays are empty.
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                // permission was granted, yay! Do the
                // location-related task you need to do.
                if (ContextCompat.checkSelfPermission(this,
                                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                    mFusedLocationClient!!.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper())
                    mGoogleMap?.isMyLocationEnabled = true
                }
            } else {
                // permission denied, boo! Disable the
                // functionality that depends on this permission.
                Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show()
            }
            return
        }
    }

    private fun getMarkerBitmapFromUri(imageUri: Uri?): Bitmap {
        return  MediaStore.Images.Media.getBitmap(this.contentResolver, imageUri)
    }

    private fun getMarkerBitmapFromView(@DrawableRes resId: Int): Bitmap {

        val customMarkerView = (getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater)
                .inflate(R.layout.view_custom_marker, null)

        val markerImageView = customMarkerView.findViewById(R.id.profile_image) as ImageView
        markerImageView.setImageResource(resId)
        customMarkerView.measure(UNSPECIFIED, UNSPECIFIED)
        customMarkerView.layout(0, 0, customMarkerView.measuredWidth, customMarkerView.measuredHeight)

        val returnedBitmap = Bitmap.createBitmap(customMarkerView.measuredWidth,
                customMarkerView.measuredHeight,
                Bitmap.Config.ARGB_8888
        )

        val canvas = Canvas(returnedBitmap).apply {
            drawColor(Color.WHITE, PorterDuff.Mode.SRC_IN)
        }

        customMarkerView.background?.draw(canvas)
        customMarkerView.draw(canvas)

        return returnedBitmap
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            checkPointIsValidate()
        }
    }

    private fun checkPointIsValidate() {
        mLastLocation?.let {
            checkPoints.forEachIndexed { index, checkPoint ->
                val distance = Utils.distance(
                        it.latitude, checkPoint.lat,
                        it.longitude, checkPoint.lng
                )

                if(distance <= 25) {
                    if (checkPoints[index].key == 1) {
                        layoutFirstStep.visibility = View.GONE
                        openDialog()
                    }

                    val key = checkPoints[index].key
                    val marker = hashMapMarker[key]

                    marker?.remove()

                    hashMapMarker.remove(key)
                    checkPoints.removeAt(index)

                    val timeText = "${totalPoints-checkPoints.size} / $totalPoints"
                    txtTime.text = timeText

                    if (checkPoints.isEmpty())
                        finishGameDialog()

                    return
                }
            }
        }
    }

    private fun openDialog() {
        AlertDialog.Builder(this).apply {
            setMessage("Seus desafio agora é decidir a melhor ordem de visitação. \nVamos Nessa!")

            setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }

            setOnDismissListener {
                chronometer.base = SystemClock.elapsedRealtime()
                chronometer.start()
            }

            create().show()
        }
    }

    private fun finishGameDialog() {
        chronometer.stop()
        val timeElapsed =  SystemClock.elapsedRealtime() - chronometer.base
        val duration = Utils.parseTimeElapsed(timeElapsed)

        val dialogView = LayoutInflater.from(this).inflate(R.layout.finish_game_dialog, null)

        val mBuilder = AlertDialog.Builder(this)
                .setView(dialogView)

        val mAlertDialog = mBuilder.show()
        mAlertDialog.setCanceledOnTouchOutside(false)

        dialogView.txtTime.text = "Tempo: $duration"

        dialogView.btnOk.setOnClickListener {
            mAlertDialog.dismiss()

            val intent = MenuActivity.launchIntent(this)
            startActivity(intent)

            finish()
        }
    }



    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) { }
    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ROTATION_VECTOR) {
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
            azimuth = (Math.toDegrees(SensorManager.getOrientation(rotationMatrix , orientation)[0].toDouble())+360).toInt()%360
        }

        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, lastAccelerometer, 0, event.values.size)
            lastAccelerometerSet = true
        } else if (event?.sensor?.type == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, lastMagnetometer, 0, event.values.size)
            lastMagnetometerSet = true
        }

        if (lastAccelerometerSet && lastMagnetometerSet) {
            SensorManager.getRotationMatrix(rotationMatrix, null, lastAccelerometer, lastAccelerometer)
            SensorManager.getOrientation(rotationMatrix, orientation)
            azimuth = (Math.toDegrees(SensorManager.getOrientation(rotationMatrix, orientation)[0].toDouble())+360).toInt()%360
        }

        azimuth = Math.round(azimuth.toFloat())
        imgCompass.rotation = (-azimuth).toFloat()
    }



    private fun startCompass() {
        if (sensorManager!!.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR) == null) {
            if (sensorManager!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) == null ||
                    sensorManager!!.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) == null  ) {
                noSensorAlert()
            } else {
                accelerometer = sensorManager!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
                magnetometer = sensorManager!!.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

                haveSensorAccelerometer = sensorManager!!.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI)
                haveSensorMagnetometer = sensorManager!!.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI)
            }
        } else {
            rotationvector = sensorManager!!.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
            haveSensorRotationVector = sensorManager!!.registerListener(this, rotationvector, SensorManager.SENSOR_DELAY_UI)
        }
    }

    fun stopCompass() {
        if(haveSensorRotationVector) sensorManager!!.unregisterListener(this, rotationvector)
        if(haveSensorMagnetometer) sensorManager!!.unregisterListener(this, magnetometer)
        if(haveSensorAccelerometer) sensorManager!!.unregisterListener(this, accelerometer)
    }

    fun noSensorAlert() {

    }

    override fun onResume() {
        super.onResume()
        startCompass()
    }

}
