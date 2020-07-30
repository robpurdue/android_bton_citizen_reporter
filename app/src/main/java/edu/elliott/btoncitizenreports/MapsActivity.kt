package edu.elliott.btoncitizenreports

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.OnRequestPermissionsResultCallback

// The "location" package has to be added to the module-level build.gradle file
// implementation 'com.google.android.gms:play-services-location:17.0.0' is the magic line
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener
import com.google.android.gms.maps.GoogleMap.OnMyLocationClickListener
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

private const val TAG = "BTONEOC"

class MapsActivity : AppCompatActivity(), OnMapReadyCallback,
    OnMyLocationButtonClickListener,
    OnMyLocationClickListener,
    OnRequestPermissionsResultCallback {

    private var permissionDenied = false
    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // If location permission is not enabled, center on Bloomington and move the camera
                val bton = LatLng(39.13, -86.587)
                mMap.moveCamera(CameraUpdateFactory.newLatLng(bton))
                mMap.moveCamera(CameraUpdateFactory.zoomTo(15.0f))


            // Permission to access the location is missing. Show rationale and request permission
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)

            // TODO: Figure out how to set the map click listener after permission is granted

            return
        }

        mMap.isMyLocationEnabled = true

        getLastLocation()

        googleMap.setOnMyLocationButtonClickListener(this)
        googleMap.setOnMyLocationClickListener(this)

        mMap.setOnMapClickListener(object :GoogleMap.OnMapClickListener {
            override fun onMapClick(latlng :LatLng) {
                // Clears the previously touched position
                mMap.clear();
                // Animating to the touched position
                mMap.animateCamera(CameraUpdateFactory.newLatLng(latlng));

                val location = LatLng(latlng.latitude,latlng.longitude)
                val address = getAddress(location)
                //toast(message = latlng.latitude.toString())
                mMap.addMarker(MarkerOptions().position(location))
                openReport(latlng.latitude.toString(), latlng.longitude.toString(), address)
            }
        })
    }

    /**
     * Enables the My Location layer if the fine location permission has been granted.
     */


    override fun onMyLocationButtonClick(): Boolean {
        Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT).show()
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false
    }

    override fun onMyLocationClick(p0: Location) {
        Toast.makeText(this, "onMyLocationClick", Toast.LENGTH_SHORT).show()
    }


    override fun onResumeFragments() {
        super.onResumeFragments()
        Log.d(TAG, "In onResumeFragments")
        if (permissionDenied) {
            // Permission was not granted, display error dialog.
            permissionDenied = false
        }
    }

    fun Context.toast(context: Context = applicationContext, message: String, duration: Int = Toast.LENGTH_SHORT){
        Toast.makeText(context, message , duration).show()
    }

    fun openReport(lat: String, long: String, addy: String?) {
        //val intent = Intent(this, ReportActivity::class.java)
        val intent = ReportActivity.newIntent(this@MapsActivity,lat, long, addy)
        startActivity(intent)
    }

    @SuppressLint("MissingPermission")
    private fun getLastLocation(){
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                val userLocation = LatLng(location!!.latitude, location!!.longitude)
                mMap.moveCamera(CameraUpdateFactory.newLatLng(userLocation))
                mMap.moveCamera(CameraUpdateFactory.zoomTo(15.0f))
            }
    }

    private fun getAddress(lat: LatLng): String? {
        val geocoder = Geocoder(this)
        val list = geocoder.getFromLocation(lat.latitude, lat.longitude,1)
        return list[0].getAddressLine(0)
    }

    companion object {
        /**
         * Request code for location permission request.
         *
         * @see .onRequestPermissionsResult
         */
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }
}