package com.example.lab_test_2

import android.Manifest
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.os.Bundle
import android.os.Environment
import android.widget.Button
import android.widget.TextView
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import java.text.SimpleDateFormat
import java.util.*

// new imports for CSV file
import java.io.File
import java.io.FileWriter
import java.io.IOException

class MainActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null

    private lateinit var locationText: TextView
    private lateinit var outputText: TextView
    private lateinit var Button1: Button

    // takes readings for the sensor
    private var latestReading: FloatArray? = null

    private var Fibonacci1 = 0
    private var Fibonacci2 = 1
    private var FibonacciTemp = 0



    // new variables
    private lateinit var csvFile: File


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        locationText = findViewById(R.id.locationText)
        outputText = findViewById(R.id.outputText)
        Button1 = findViewById(R.id.Button1)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        // Ask for location permission
        checkLocationPermission()

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        // This code creates a CSV file
        // for now, this file is in the files directory of your app.
        // you need to get access to this file and upload a screenshot
        // either finding the location of the file and extracting it to your PC, or
        // creating a new directory by replacing the parent
        // with Environment.getExternalStoragePublicDirectory( some public directory)
        csvFile = File(filesDir, "accelerometer_data.csv")
        // If file doesn't exist, create it and add header row
        if (!csvFile.exists()) {
            csvFile.writeText("timestamp,x,y,z\n")
        }

        if (accelerometer == null) {
            outputText.text = "No accelerometer on this device."
        }

        // Button press action
        Button1.setOnClickListener {

            FibonacciTemp = Fibonacci1
            Fibonacci1 = Fibonacci2
            Fibonacci2 = FibonacciTemp + Fibonacci2

            outputText.text = "The two latest Fibonacci numbers are: %.2f, %.2f".format(
                Fibonacci1.toDouble(),
                Fibonacci2.toDouble())

            // currently, every time you press the button,
            // the latest fibonacci numbers are written to the CSV.
            // you need to alter the program so that,
            // every time the sensor takes an update
            // the CSV is updated with the accelerometer readings instead
            // this may require you to change the location where this segment of code is used
            val timestamp = System.currentTimeMillis()
            val csvLine = "$timestamp,$Fibonacci1,$Fibonacci2,0\n"

            outputText.text = csvLine
            appendToCsv(csvLine)
        }

    }

    // location, don't modify
    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                100
            )
        } else {
            getLocation()
        }
    }

    // location, don't modify
    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    private fun getLocation() {
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                val timestamp = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
                locationText.text = "Lat: %.5f\nLng: %.5f\nTime: %s".format(
                    location.latitude,
                    location.longitude,
                    timestamp
                )
            } else {
                locationText.text = "Unable to fetch location"
            }
        }
    }

    // location, don't modify
    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100 && grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            getLocation()
        } else {
            locationText.text = "Permission denied"
        }
    }


    override fun onResume() {
        super.onResume()
        accelerometer?.also { sensor ->
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }


    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            latestReading = event.values.clone()

            // this function updates every time the sensor changes
            // you may want to use appendToCSV here instead
        }
    }

    private fun appendToCsv(line: String) {
        try {
            FileWriter(csvFile, true).use { writer ->
                writer.append(line)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not needed
    }
}