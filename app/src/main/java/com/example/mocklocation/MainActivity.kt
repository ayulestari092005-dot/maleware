package com.example.mocklocation

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private var isMockActive = false

    private val requestPermissionLauncher =
        registerForActivityResult(androidx.activity.result.contract.ActivityResultContracts.RequestPermission()) { granted ->
            if (!granted) {
                Toast.makeText(this, "Izin lokasi dibutuhkan", Toast.LENGTH_SHORT).show()
            }
        }

    private val requestNotifPermissionLauncher =
        registerForActivityResult(androidx.activity.result.contract.ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val etLat = findViewById<EditText>(R.id.etLat)
        val etLng = findViewById<EditText>(R.id.etLng)
        val tvStatus = findViewById<TextView>(R.id.tvStatus)
        val btnSet = findViewById<Button>(R.id.btnSetLocation)
        val btnStop = findViewById<Button>(R.id.btnStop)
        val btnDevSettings = findViewById<Button>(R.id.btnOpenDevSettings)

        checkLocationPermission()
        checkNotificationPermission()

        btnSet.setOnClickListener {
            val lat = etLat.text.toString().toDoubleOrNull()
            val lng = etLng.text.toString().toDoubleOrNull()

            if (lat == null || lng == null) {
                Toast.makeText(this, "Isi latitude & longitude dengan benar", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            try {
                val serviceIntent = Intent(this, MockLocationService::class.java).apply {
                    putExtra(MockLocationService.EXTRA_LAT, lat)
                    putExtra(MockLocationService.EXTRA_LNG, lng)
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(serviceIntent)
                } else {
                    startService(serviceIntent)
                }
                isMockActive = true
                tvStatus.text = "Status: aktif ($lat, $lng) — jalan di background"
                tvStatus.setTextColor(0xFF4CAF50.toInt())
            } catch (e: SecurityException) {
                Toast.makeText(
                    this,
                    "Aktifkan dulu app ini sebagai 'mock location app' di Developer Options",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        btnStop.setOnClickListener {
            val stopIntent = Intent(this, MockLocationService::class.java).apply {
                action = MockLocationService.ACTION_STOP
            }
            startService(stopIntent)
            isMockActive = false
            tvStatus.text = "Status: nonaktif"
            tvStatus.setTextColor(0xFFFF5722.toInt())
        }

        btnDevSettings.setOnClickListener {
            startActivity(Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS))
        }
    }

    private fun checkLocationPermission() {
        val fine = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        if (fine != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            if (granted != PackageManager.PERMISSION_GRANTED) {
                requestNotifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}
