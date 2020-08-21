package com.example.connectivitygeofence.Activities

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.connectivitygeofence.GeofenceHelper
import com.example.connectivitygeofence.MyGeoFence
import com.example.connectivitygeofence.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.Circle
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.activity_geofence_map.*
import java.io.IOException
import java.util.*

class GeofenceMapActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMapLongClickListener {

    companion object{
        const val TAG = "GeofenceMap"
        const val DEFAULT_ZOOM = 15f
        const val DEFAULT_RADIUS = 200.00
        const val DEFAULT_ACTION = 0
    }

    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var geofencingClient: GeofencingClient
    private lateinit var geofenceHelper: GeofenceHelper

    //Geofence attributes
    private var geofenceId: Int = 0
    private lateinit var geofenceCircle: Circle
    private lateinit var geofenceAddress: String
    private var geofenceAction: Int = DEFAULT_ACTION


    private var currentRadius: Double = DEFAULT_RADIUS




    //Widgets
    private lateinit var editText: EditText
    private lateinit var clearButton: ImageView
    private lateinit var seekBar: SeekBar


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_geofence_map)
        supportActionBar?.setDisplayHomeAsUpEnabled(true);
        supportActionBar?.setDisplayShowHomeEnabled(true);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        geofencingClient = LocationServices.getGeofencingClient(this)
        geofenceHelper = GeofenceHelper(this)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // search bar
        editText = findViewById(R.id.editText)
        editText.setOnEditorActionListener { _, actionId, _ ->
            if(actionId == EditorInfo.IME_ACTION_DONE){
                //execute method for searching
                geoLocate()
            }
            false
        }

        // clear button
        clearButton = findViewById(R.id.clear_button)
        clearButton.setOnClickListener {
            editText.setText("")
        }


        // radius seekBar
        seekBar = findViewById(R.id.seekBar)
        seekBar.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                when(progress){
                    0 -> currentRadius = 150.00
                    1 -> currentRadius =
                        DEFAULT_RADIUS
                    2 -> currentRadius = 250.00
                    3 -> currentRadius = 300.00
                    else -> Log.d(TAG, "Error en seekBar")
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                return
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                changeRadius(currentRadius)
                Log.d(TAG, "SeekBar progress: ${seekBar?.progress}, radius: $currentRadius")

            }
        })


        floating_create.setOnClickListener {
            if(this::geofenceCircle.isInitialized){
                showActionDialog()

            }else {
                Log.d(TAG, "No hay geofence creada en el mapa")
                showAlertDialog()
            }
        }
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
                moveCamera(latLng,
                    DEFAULT_ZOOM
                )
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
            val address = list[0]
            Log.d(TAG, "geoLocate found location: $address")
            val latLng = LatLng(address.latitude, address.longitude)
            moveCamera(latLng,
                DEFAULT_ZOOM
            )
        }
    }



    override fun onMapLongClick(latLng: LatLng?) {
        mMap.clear()
        if(latLng != null){
            showLocation(latLng)
            addMarker(latLng)
            addCircle(latLng)
            Log.d(TAG, "onMapLongClick: Geofence dibujado en la posicon: $latLng, radio: $currentRadius, address: ${this.geofenceAddress}")
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
        geofenceCircle = mMap.addCircle(circleOptions)

    }

    private fun createGeoFence(latLng: LatLng, radius: Double, action: Int){
        geofenceId = System.currentTimeMillis().toInt()
        val geofence = geofenceHelper.getGeofence(geofenceId.toString(), latLng, radius.toFloat(), Geofence.GEOFENCE_TRANSITION_DWELL)
        val pendingIntent = geofenceHelper.getPendingIntent(action)
        val geofencingRequest = geofenceHelper.getGeoFencingRequest(geofence)

        geofencingClient.addGeofences(geofencingRequest, pendingIntent)?.run {
            addOnSuccessListener {
                showToast("Geovallado creado")
                val createdGeofence = MyGeoFence(geofenceId, geofenceCircle, geofenceAction, geofenceAddress)
                MyGeoFence.bluetoothGeoFences.add(createdGeofence)
                Log.d(TAG,"createGeoFence onSuccess, geofence added")
                finish()
            }

            addOnFailureListener{ e->
                val errorMessage = geofenceHelper.getErrors(e)
                Log.d(TAG, "createGeoFence onFailure: $errorMessage")
            }
        }


        Log.d(TAG, "createGeofence: se creo la geofence: id: $geofenceId, position: $latLng, radius: $radius")
    }

    private fun changeButtonPosition(){
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map)
        val locationButton= (mapFragment?.view?.findViewById<View>(Integer.parseInt("1"))?.parent as View).findViewById<View>(Integer.parseInt("2"))
        val rlp=locationButton.layoutParams as (RelativeLayout.LayoutParams)
        // position on right bottom
        rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP,RelativeLayout.TRUE)
        rlp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, 0)
        rlp.setMargins(0,160,60,0)
    }

    private fun changeRadius(radius: Double){
        currentRadius = radius
        if(this::geofenceCircle.isInitialized){
            geofenceCircle.radius = currentRadius
        }
    }

    private fun showAlertDialog(){
        val builder: AlertDialog.Builder? = this.let{
            AlertDialog.Builder(it)
        }

        builder?.setMessage(R.string.no_geofence_message)
        builder?.setTitle(R.string.app_name)

        builder?.setPositiveButton(R.string.ok, DialogInterface.OnClickListener { dialog, id ->
        })

        val dialog: AlertDialog? = builder?.create()

        dialog?.show()
    }

    private fun showActionDialog(){
        val builder: AlertDialog.Builder? = this.let{
            AlertDialog.Builder(it)
        }

        builder?.setTitle("Selecciono una acción")
        builder?.setSingleChoiceItems(
            R.array.action_options, 0,
                        DialogInterface.OnClickListener { dialog, item ->
                            geofenceAction = item
                        })
        builder?.setPositiveButton(R.string.ok, DialogInterface.OnClickListener { dialog, id ->
            createGeoFence(geofenceCircle.center, geofenceCircle.radius, geofenceAction)
        })

        builder?.setNegativeButton(R.string.cancel, DialogInterface.OnClickListener{ dialog, id ->

        })

        val dialog: AlertDialog? = builder?.create()

        dialog?.show()
    }

    private fun showLocation(latLng: LatLng){

        val addresses: List<Address>
        val geocoder: Geocoder = Geocoder(this, Locale.getDefault())

        addresses = geocoder.getFromLocation(
            latLng.latitude,
            latLng.longitude,
            1
        ) // Here 1 represent max location result to returned, by documents it recommended 1 to 5


        val address = addresses[0]
            .getAddressLine(0) // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()

        this.geofenceAddress = address
        editText.setText(this.geofenceAddress)

        Log.d(TAG, "Location clicked: $address")

    }

    private fun showToast(message: String){

        val duration = Toast.LENGTH_SHORT

        val toast = Toast.makeText(applicationContext, message, duration)
        toast.show()
    }

}

