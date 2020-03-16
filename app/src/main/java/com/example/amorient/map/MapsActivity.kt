package com.example.amorient.map

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.PorterDuff
import android.location.Location
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
import com.example.amorient.Utils
import com.example.amorient.detail.PointDetailActivity
import com.example.amorient.model.CheckPoint
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import kotlinx.android.synthetic.main.activity_maps.*


class MapsActivity : FragmentActivity(), OnMapReadyCallback {

    var mLastLocation: Location? = null
    private var mGoogleMap: GoogleMap? = null
    private var mapFrag: SupportMapFragment? = null
    private var mLocationRequest: LocationRequest? = null
    private var mFusedLocationClient: FusedLocationProviderClient? = null

    private var checkPoints : MutableList<CheckPoint> = mutableListOf()
    private var totalPoints = 0
    private val hashMapMarker = HashMap<Int, Marker>()

    companion object {
        const val REQUEST_IMAGE_CAPTURE = 1
        const val MY_PERMISSIONS_REQUEST_LOCATION = 99
        const val INTERVAL_CHECK_LOCATION = 500L //  120000 two minute interval

        fun launchIntent(context: Context) = Intent(context, MapsActivity::class.java)

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

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

        //stop location updates when Activity is no longer active
        mFusedLocationClient?.let { mFusedLocationClient!!.removeLocationUpdates(mLocationCallback) }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mGoogleMap = googleMap.apply {
            uiSettings.isMapToolbarEnabled = false
            uiSettings.isMyLocationButtonEnabled = false
            uiSettings.isCompassEnabled = false
            uiSettings.isRotateGesturesEnabled = false

            setMapStyle(MapStyleOptions.loadRawResourceStyle(this@MapsActivity, R.raw.style_json))

            setOnMarkerClickListener {
                val tag = it.tag as Int?

                tag?.let { checkPoints.forEach { checkPoint ->
                        if (checkPoint.key == tag) {
                            val intent = PointDetailActivity.launchIntent(this@MapsActivity, checkPoint)
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
            val image = getMarkerBitmapFromView(checkPoint.image)

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

    private fun zoomCurrentPosition(location: Location) {
        val cameraPosition = LatLng(location.latitude, location.longitude)
        mGoogleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(cameraPosition, 16f))
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

}
