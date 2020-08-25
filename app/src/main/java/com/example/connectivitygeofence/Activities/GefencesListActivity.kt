package com.example.connectivitygeofence.Activities

import android.Manifest
import android.app.PendingIntent
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.*
import com.example.connectivitygeofence.GeofenceHelper
import com.example.connectivitygeofence.MyGeoFence
import com.example.connectivitygeofence.R
import com.example.connectivitygeofence.RecyclerViewAdapter
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.LocationServices
import kotlinx.android.synthetic.main.activity_geofences_list.*


class GefencesListActivity : AppCompatActivity() {
    companion object{
        const val TAG = "MainActivity"
        const val FINE_LOCATION_REQUEST_CODE = 1001
    }

    private lateinit var geofencingClient: GeofencingClient
    private lateinit var rvAdapter: RecyclerViewAdapter
    private lateinit var itemTouchHelper: ItemTouchHelper

    // Geogences List
    private lateinit var currentGeofenceList: MutableList<MyGeoFence>




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_geofences_list)
        setSupportActionBar(toolbar)

        geofencingClient = LocationServices.getGeofencingClient(this)

        currentGeofenceList =  MyGeoFence.bluetoothGeoFences
        getPermissions()
        buildRecyclerView()

        itemTouchHelper = ItemTouchHelper(simpleCallback)
        itemTouchHelper.attachToRecyclerView(recycler_view)


        floating_add.setOnClickListener { view ->
            val intent = Intent(this, GeofenceMapActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onStart() {
        super.onStart()
        rvAdapter.notifyDataSetChanged()
    }

    private val simpleCallback = object: ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT){
        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            return false
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            cancelGeoFence(viewHolder.adapterPosition)
            removeGeoFence(viewHolder.adapterPosition)
        }

    }

    private fun buildRecyclerView(){

        recycler_view.apply {
            layoutManager = LinearLayoutManager(this@GefencesListActivity)
            rvAdapter = RecyclerViewAdapter(currentGeofenceList)
            adapter = rvAdapter
        }

        val dividerItemDecoration = DividerItemDecoration(applicationContext, DividerItemDecoration.VERTICAL)
        recycler_view.addItemDecoration(dividerItemDecoration)

    }


    private fun getPermissions(){
        when{
            ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED -> {
                // Permissions are granted
            }
            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> {
                //Show the dialog explaining why you need the permissions
                showDialog()
            }
            else -> {
                // Just Ask for the permissions
                requestPermissions(this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    FINE_LOCATION_REQUEST_CODE
                )
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when(requestCode){
            FINE_LOCATION_REQUEST_CODE ->{
                if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    //Permission is granted.

                }else{
                    // User did´t grant the permissions so close the app
                    finish()
                }
            }
        }

    }

    private fun showDialog(){
        val builder: AlertDialog.Builder? = this.let{
            AlertDialog.Builder(it)
        }

        builder?.setMessage(R.string.permissions_message)
        builder?.setTitle(R.string.app_name)

        builder?.setPositiveButton(R.string.ok, DialogInterface.OnClickListener { dialog, id ->
            //ask for permissions
            requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                FINE_LOCATION_REQUEST_CODE
            )
        })

        val dialog: AlertDialog? = builder?.create()

        dialog?.show()
    }

    private fun cancelGeoFence(position: Int){
        val geofencePendingIntent = GeofenceHelper(this).getPendingIntent(currentGeofenceList[position].actionCode)
        geofencingClient.removeGeofences(geofencePendingIntent)
    }

    private fun removeGeoFence(position: Int){
        currentGeofenceList.removeAt(position)
        rvAdapter.notifyItemRemoved(position)
        Log.d(TAG, "Geofence removed")
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }



}
