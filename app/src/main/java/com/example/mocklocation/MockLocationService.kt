package com.example.mocklocation

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.location.Location
import android.location.LocationManager
import android.location.provider.ProviderProperties
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.SystemClock

/**
 * Service ini yang membuat mock location tetap "hidup" walau MainActivity
 * diminimize atau layar dimatikan. Berjalan sebagai Foreground Service
 * dengan notifikasi persisten, sehingga Android memperlakukannya sebagai
 * proses prioritas tinggi yang jarang dimatikan sistem.
 */
class MockLocationService : Service() {

    private lateinit var locationManager: LocationManager
    private val provider = LocationManager.GPS_PROVIDER
    private val refreshHandler = Handler(Looper.getMainLooper())
    private var lat = 0.0
    private var lng = 0.0

    private val refreshRunnable = object : Runnable {
        override fun run() {
            pushMockLocation(lat, lng)
            refreshHandler.postDelayed(this, 1000L)
        }
    }

    companion object {
        const val CHANNEL_ID = "mock_location_channel"
        const val NOTIFICATION_ID = 1001
        const val EXTRA_LAT = "extra_lat"
        const val EXTRA_LNG = "extra_lng"
        const val ACTION_STOP = "com.example.mocklocation.ACTION_STOP"
    }

    override fun onCreate() {
        super.onCreate()
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopMocking()
            stopSelf()
            return START_NOT_STICKY
        }

        lat = intent?.getDoubleExtra(EXTRA_LAT, 0.0) ?: 0.0
        lng = intent?.getDoubleExtra(EXTRA_LNG, 0.0) ?: 0.0

        startForeground(NOTIFICATION_ID, buildNotification(lat, lng))
        setupTestProvider()

        refreshHandler.removeCallbacks(refreshRunnable)
        refreshHandler.post(refreshRunnable)

        return START_STICKY // minta Android restart service ini kalau sempat dimatikan
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        stopMocking()
    }

    private fun stopMocking() {
        refreshHandler.removeCallbacks(refreshRunnable)
        try {
            locationManager.removeTestProvider(provider)
        } catch (e: Exception) { /* aman diabaikan */ }
        try {
            locationManager.removeTestProvider(LocationManager.NETWORK_PROVIDER)
        } catch (e: Exception) { /* aman diabaikan */ }
    }

    private fun setupTestProvider() {
        try {
            locationManager.removeTestProvider(provider)
        } catch (e: Exception) { /* belum ada, aman */ }

        locationManager.addTestProvider(
            provider,
            false, false, false, false,
            true, true, true,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) ProviderProperties.POWER_USAGE_LOW else 1,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) ProviderProperties.ACCURACY_FINE else 1
        )
        locationManager.setTestProviderEnabled(provider, true)

        try {
            locationManager.removeTestProvider(LocationManager.NETWORK_PROVIDER)
        } catch (e: Exception) { /* aman diabaikan */ }
        locationManager.addTestProvider(
            LocationManager.NETWORK_PROVIDER,
            false, false, false, false,
            true, true, true,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) ProviderProperties.POWER_USAGE_LOW else 1,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) ProviderProperties.ACCURACY_COARSE else 2
        )
        locationManager.setTestProviderEnabled(LocationManager.NETWORK_PROVIDER, true)
    }

    private fun pushMockLocation(lat: Double, lng: Double) {
        val now = System.currentTimeMillis()
        val elapsed = SystemClock.elapsedRealtimeNanos()

        val mockGps = Location(provider).apply {
            latitude = lat
            longitude = lng
            accuracy = 5f
            altitude = 0.0
            speed = 0f
            bearing = 0f
            time = now
            elapsedRealtimeNanos = elapsed
        }
        try {
            locationManager.setTestProviderLocation(provider, mockGps)
        } catch (e: Exception) { /* aman diabaikan kalau provider sempat dicabut */ }

        try {
            val mockNetwork = Location(LocationManager.NETWORK_PROVIDER).apply {
                latitude = lat
                longitude = lng
                accuracy = 10f
                altitude = 0.0
                time = now
                elapsedRealtimeNanos = elapsed
            }
            locationManager.setTestProviderLocation(LocationManager.NETWORK_PROVIDER, mockNetwork)
        } catch (e: Exception) { /* aman diabaikan */ }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Afwan Fake GPS",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Notifikasi status mock location aktif"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(lat: Double, lng: Double): Notification {
        val stopIntent = Intent(this, MockLocationService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this, 0, stopIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val openAppIntent = Intent(this, MainActivity::class.java)
        val openAppPendingIntent = PendingIntent.getActivity(
            this, 0, openAppIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return Notification.Builder(this, CHANNEL_ID)
            .setContentTitle("Afwan Fake GPS aktif")
            .setContentText("Lokasi palsu: $lat, $lng")
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setOngoing(true)
            .setContentIntent(openAppPendingIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Stop", stopPendingIntent)
            .build()
    }
}
