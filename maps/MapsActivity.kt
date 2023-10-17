package com.app.dicodingstoryapp.maps

import android.content.Context
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.app.dicodingstoryapp.ApiConfig
import com.app.dicodingstoryapp.R
import com.app.dicodingstoryapp.Story
import com.app.dicodingstoryapp.ViewModelFactory
import com.app.dicodingstoryapp.auth.AuthRepository
import com.app.dicodingstoryapp.databinding.ActivityMapsBinding
import com.app.dicodingstoryapp.model.UserPreferences
import com.app.dicodingstoryapp.story.StoryRepository
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.launch

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var mapsViewModel: MapsViewModel
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val userPreferences = UserPreferences.getInstance(dataStore)
        val apiService = ApiConfig.getApiService()
        val authRepository = AuthRepository(userPreferences, apiService)
        val storyRepository = StoryRepository(userPreferences, apiService)

        val viewModelFactory = ViewModelFactory(authRepository, storyRepository)

        mapsViewModel = ViewModelProvider(this, viewModelFactory)[MapsViewModel::class.java]

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        mapsViewModel.getStoryResponse.observe(this, { locS ->
            locStories(locS)

        })

        lifecycleScope.launch {
            mapsViewModel.getAllLocation(100, 1)
        }

        getStoryLocation()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.uiSettings.isIndoorLevelPickerEnabled = true
        mMap.uiSettings.isCompassEnabled = true
        mMap.uiSettings.isMapToolbarEnabled = true

    }

    private fun getStoryLocation() {
        if (ContextCompat.checkSelfPermission(
                this.applicationContext,
                android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        ) {
            mMap.isMyLocationEnabled = true
            fusedLocationClient.lastLocation
                .addOnSuccessListener { loc ->
                    if (loc != null) {
                        val locStory = LatLng(loc.latitude, loc.longitude)
                        mMap.animateCamera(CameraUpdateFactory.newLatLng(locStory))
                    } else {
                        Toast.makeText(this, "Mohon Aktifkan Lokasi", Toast.LENGTH_SHORT).show()
                    }
                }
        } else {
            requestPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
    }
}

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                getStoryLocation()

            }
        }

    private fun locStories(loc: List<Story>) {
        loc.map {
            story ->
            val lat = story.lat
            val lon = story.lon
            val name = story.name
            val create = story.createdAt

            if (lat != null && lon != null) {
                val location = LatLng(lat, lon)

                mMap.addMarker(
                    MarkerOptions().position(location)
                        .title(name)
                        .snippet(create)
                )
            }
        }


    }
}