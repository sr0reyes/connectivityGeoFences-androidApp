package com.example.connectivitygeofence

import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Address
import android.location.Geocoder
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.renderscript.Sampler
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*

import kotlinx.android.synthetic.main.activity_geofence_map.*
import java.io.IOException
import java.util.jar.Manifest

class GeofenceMap : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMapLongClickListener {

    companion object{
        const val TAG = "GeofenceMap"
        const val DEFAULT_ZOOM = 15f
        const val DEFAULT_RADIUS = 200.00
    }

    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var geofencingClient: GeofencingClient
    private lateinit var circle: Circle
    private var currentRadius: Double = DEFAULT_RADIUS



    //Widgets
    private lateinit var editText: EditText
    private lateinit var clearButton: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_geofence_map)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        geofencingClient = LocationServices.getGeofencingClient(this)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        editText = findViewById(R.id.editText)
        editText.setOnEditorActionListener { v, actionId, event ->
            if(actionId == EditorInfo.IME_ACTION_DONE){
                //execute method for searching
                geoLocate()
            }
            false
        }

        clearButton = findViewById(R.id.ic_clear)
        clearButton.setOnClickListener { v: View ->
            editText.setText("")
        }



        seekBar?.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                return
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                return
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                Log.d(TAG, "SeekBar progress: ${seekBar?.progress}")

                when(seekBar?.progress){
                    0 -> changeRadius(150.00)
                    1 -> changeRadius(DEFAULT_RADIUS)
                    2 -> changeRadius(250.00)
                    3 -> changeRadius(300.00)
                    else -> Log.d(TAG, "Error en seekBar")
                }
            }
        })
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

        mMap.setOnMapLongClickListener(this)
        getDevicePosition()
        zoomAtDevicePosition()


        // Add a marker in Sydney and move the camera
        val sydney = LatLng(-34.0, 151.0)
        mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
    }

    private fun getDevicePosition(){
        if (ContextCompat.checkSelfPermission(applicationContext,
                android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            mMap.isMyLocationEnabled = true
            changeButtonPosition()
        }
        return
    }

    private fun zoomAtDevicePosition(){
        fusedLocationProviderClient.lastLocation.addOnSuccessListener { location: Location? ->
            if(location != null){
                val latLng = LatLng(location.latitude, location.longitude)
                moveCamera(latLng, DEFAULT_ZOOM)
            }
        }
    }

    private fun moveCamera(latLng: LatLng, zoom: Float){
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom))
    }

    private fun geoLocate(){
        val searchString = editText.text.toString()
        Log.d(TAG, searchString)
        val geoCoder = Geocoder(this)
        var list = listOf<Address>()

        try {
            list = geoCoder.getFromLocationName(searchString,1)
        }catch(e: IOException){
            Log.e(TAG,"geolocate IOException" + e.message)
        }

        if(list.isNotEmpty()){
            val address = list.get(0)
            Log.d(TAG, "geoLocate found location: $address")
            val latLng = LatLng(address.latitude, address.longitude)
            moveCamera(latLng, DEFAULT_ZOOM)
        }
    }



    override fun onMapLongClick(p0: LatLng?) {
        mMap.clear()
        Log.d(TAG, "Posicion presionada: $p0")
        if(p0 != null){
            addMarker(p0)
            addCircle(p0)
        }

    }

    private fun addMarker(latLng: LatLng){
        val markerOptions = MarkerOptions()
        markerOptions.position(latLng)

        mMap.addMarker(markerOptions)
    }

    private fun addCircle(latLng: LatLng){
        val circleOptions = CircleOptions()
        circleOptions.center(latLng)
        circleOptions.radius(currentRadius)
        circleOptions.strokeColor(Color.argb(255, 255, 0, 0))
        circleOptions.fillColor(Color.argb(64, 255, 0, 0))
        circleOptions.strokeWidth(4f)
        circle = mMap.addCircle(circleOptions)

    }

    private fun changeButtonPosition(){
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map)
        val locationButton= (mapFragment?.view?.findViewById<View>(Integer.parseInt("1"))?.parent as View).findViewById<View>(Integer.parseInt("2"))
        val rlp=locationButton.layoutParams as (RelativeLayout.LayoutParams)
        // position on right bottom
        rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP,RelativeLayout.TRUE)
        rlp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, 0)
        rlp.setMargins(0,160,60,0);
    }

    private fun changeRadius(radius: Double){
        currentRadius = radius
        if(this::circle.isInitialized){
            circle.radius = currentRadius
        }
    }


}

