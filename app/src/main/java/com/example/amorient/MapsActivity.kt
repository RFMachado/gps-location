package com.example.amorient

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
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View.MeasureSpec.UNSPECIFIED
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import kotlinx.android.synthetic.main.activity_maps.*
import kotlin.math.*


class MapsActivity : FragmentActivity(), OnMapReadyCallback {

    var mLastLocation: Location? = null
    private var mGoogleMap: GoogleMap? = null
    private var mapFrag: SupportMapFragment? = null
    private var mLocationRequest: LocationRequest? = null
    private var mFusedLocationClient: FusedLocationProviderClient? = null

    private var checkPoints : List<CheckPoint> = listOf()
    private val hashMapMarker = HashMap<Int, Marker>()

    companion object {
        const val REQUEST_IMAGE_CAPTURE = 1
        const val MY_PERMISSIONS_REQUEST_LOCATION = 99
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        checkPoints = addPoints()
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        mapFrag = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFrag?.getMapAsync(this)

        bindListener()
    }

    private fun bindListener() {
        btnPhoto.setOnClickListener {
            dispatchTakePictureIntent()
        }
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


    private fun addPoints() = listOf(
            CheckPoint( lat = -32.075731, lng = -52.171524, image = R.drawable.img_ponto_um),
            CheckPoint(lat = -32.076270,  lng = -52.170581, image = R.drawable.img_ponto_dois),
            CheckPoint(lat = -32.075462, lng = -52.173190, image = R.drawable.img_ponto_tres),
            CheckPoint(lat = -32.075284, lng = -52.173304, image = R.drawable.img_ponto_quatro),
            CheckPoint(lat = -32.075800, lng = -52.172000, image = R.drawable.img_ponto_cinco),
            CheckPoint(lat = -32.075571, lng = -52.170832, image = R.drawable.img_ponto_seis),
            CheckPoint(lat = -32.074263, lng = -52.172776, image = R.drawable.img_ponto_sete),
            CheckPoint(lat = -32.076489, lng = -52.170248, image = R.drawable.img_ponto_oito),
            CheckPoint(lat = -32.076250, lng = -52.169062, image = R.drawable.img_ponto_nove),
            CheckPoint(lat = -32.077008, lng = -52.168453, image = R.drawable.img_ponto_dez),
            CheckPoint(lat = -32.077472, lng = -52.168094, image = R.drawable.img_ponto_onze),
            CheckPoint(lat= -32.076847, lng = -52.167289, image = R.drawable.img_ponto_doze),
            CheckPoint(lat = -32.077458, lng = -52.166176, image = R.drawable.img_ponto_treze),
            CheckPoint(lat = -32.075061, lng = -52.167076, image = R.drawable.img_ponto_quatorze),
            CheckPoint(lat = -32.075472, lng = -52.167952, image = R.drawable.img_ponto_quinze),
            CheckPoint(lat = -32.073725, lng = -52.168713, image = R.drawable.img_ponto_dezessseis),
            CheckPoint(lat = -32.074682, lng = -52.169858, image = R.drawable.img_ponto_dezessete),
            CheckPoint(lat = -32.075521, lng =  -52.169220, image = R.drawable.img_ponto_dezoito),
            CheckPoint(lat = -32.077628, lng = -52.171355, image = R.drawable.img_ponto_dezenove)
    )

    public override fun onPause() {
        super.onPause()

        //stop location updates when Activity is no longer active
        if (mFusedLocationClient != null) {
            mFusedLocationClient!!.removeLocationUpdates(mLocationCallback)
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mGoogleMap = googleMap

        googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.style_json))

        mLocationRequest = LocationRequest().apply {
            interval = 500 //  120000 two minute interval
            fastestInterval = 500
            priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
        }

        checkPoints.forEachIndexed { index, checkPoint ->
            val image = getMarkerBitmapFromView(checkPoint.image)

            val markerOptions = MarkerOptions()
                    .position(LatLng(checkPoint.lat, checkPoint.lng))
                    .title("P$index")
                    .snippet("P$index")
                    .icon(BitmapDescriptorFactory.fromBitmap(image))

            val marker =  mGoogleMap!!.addMarker(markerOptions)
            hashMapMarker[index] = marker
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
        val zoom = 16f

        mGoogleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(cameraPosition, zoom))
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

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
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

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    private fun distance(lat1: Double, lat2: Double, lon1: Double, lon2: Double): Double {

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
                val distance = distance(
                        mLastLocation!!.latitude, checkPoint.lat,
                        mLastLocation!!.longitude, checkPoint.lng
                )

                if(distance <= 25) {
                    checkPoints.drop(index)

                    val marker = hashMapMarker[index]
                    marker?.remove()
                    hashMapMarker.remove(index)

                    return@forEachIndexed
                }
            }
        }
    }

}
