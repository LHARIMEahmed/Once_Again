package com.example.myapplication.fragments.Acceuil.location

import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentLocationPickerBinding
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import java.util.Arrays

class locationPicker : Fragment(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var mPlaceClient: PlacesClient
    private lateinit var mFusedLocationProviderClient: FusedLocationProviderClient

    private var mLastKnownLocation: Location? = null
    private var selectedLatitude: Double? = null
    private var selectedLongitude: Double? = null
    private var selectedAddress: String = ""

    private lateinit var binding: FragmentLocationPickerBinding

    private val DEFAULT_ZOOM = 15f
    private val REQUEST_LOCATION_PERMISSION = 1001

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLocationPickerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mapFragment =
            childFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        Places.initialize(requireContext(), getString(R.string.google_play_services_version))
        mPlaceClient = Places.createClient(requireContext())
        mFusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireActivity())

        binding.donebtn.visibility = View.GONE
        binding.toolbarBackbttn.setOnClickListener { requireActivity().onBackPressed() }
        binding.toolbargpsbtn.setOnClickListener { getCurrentLocation() }
        binding.donebtn.setOnClickListener { sendSelectedLocation() }

        val autocompleteSupportFragment =
            childFragmentManager.findFragmentById(R.id.autocompleteFragment) as AutocompleteSupportFragment
        val placesList = arrayOf(
            Place.Field.ID,
            Place.Field.NAME,
            Place.Field.ADDRESS,
            Place.Field.LAT_LNG
        )
        autocompleteSupportFragment.setPlaceFields(Arrays.asList(*placesList))
        autocompleteSupportFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                val id = place.id
                val title = place.name
                val latLng = place.latLng
                selectedLatitude = latLng?.latitude
                selectedLongitude = latLng?.longitude
                selectedAddress = place.address ?: ""
                addMarker(latLng!!, title!!, selectedAddress)
            }

            override fun onError(status: Status) {
                Log.e(TAG, "onError: ${status.statusMessage}")
            }
        })
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.setOnMapClickListener { latLng ->
            selectedLatitude = latLng.latitude
            Log.d("locationPicker",selectedLatitude.toString())
            selectedLongitude = latLng.longitude
            addressFromLatLng(latLng)
        }
        getCurrentLocation()
    }

    private fun requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // Permission is already granted, proceed with the operation
            getCurrentLocation()
        } else {
            // Permission has not been granted, request it
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_LOCATION_PERMISSION)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, proceed with the operation
                getCurrentLocation()
            } else {
                // Permission denied
                // Handle the denial accordingly, such as displaying a message to the user
                Log.e(TAG, "Location permission denied")
            }
        }
    }
    private fun getCurrentLocation() {
        try {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                val locationResult = mFusedLocationProviderClient.lastLocation
                locationResult.addOnSuccessListener { location ->
                    location?.let {
                        mLastKnownLocation = it
                        val latLng = LatLng(it.latitude, it.longitude)
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM))
                        addressFromLatLng(latLng)
                    } ?: run {
                        Log.e(TAG, "Last known location is null")
                        // Handle the case when the last known location is null
                    }
                }.addOnFailureListener { e ->
                    Log.e(TAG, "onFailure: ", e)
                }
            } else {
                requestLocationPermission() // Request permission if not granted
            }
        } catch (e: Exception) {
            Log.e(TAG, "getCurrentLocation: ", e)
        }
    }
    private fun addressFromLatLng(latLng: LatLng) {
        val geocoder = Geocoder(requireContext())
        try {
            val addressList = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
            val address = addressList?.get(0)
            selectedAddress = address?.getAddressLine(0) ?: ""
            addMarker(latLng, address?.subLocality ?: "", selectedAddress)
        } catch (e: Exception) {
            Log.e(TAG, "addressFromLatLng: ", e)
        }
    }

    private fun addMarker(latLng: LatLng, title: String, address: String) {
        val markerOptions = MarkerOptions()
        markerOptions.position(latLng)
        markerOptions.title(title)
        markerOptions.snippet(address)
        mMap.clear()
        mMap.addMarker(markerOptions)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM))
        binding.donebtn.visibility = View.VISIBLE
        binding.selectedPlace.text = address
        selectedLatitude = latLng.latitude
        selectedLongitude = latLng.longitude
    }

    /*private fun sendSelectedLocation() {
        val intent = Intent()
        intent.putExtra("latitude", selectedLatitude)
        intent.putExtra("longitude", selectedLongitude)
        intent.putExtra("address", selectedAddress)
        requireActivity().setResult(Activity.RESULT_OK, intent)
        requireActivity().finish()
    }*/
    private fun sendSelectedLocation() {
        val result = Bundle().apply {
            putString(BUNDLE_KEY_ADDRESS, selectedAddress)
            putDouble(BUNDLE_KEY_LATITUDE, selectedLatitude ?: 0.0)
            putDouble(BUNDLE_KEY_LONGITUDE, selectedLongitude ?: 0.0)
        }
        parentFragmentManager.setFragmentResult(REQUEST_KEY, result)
        requireActivity().onBackPressed()
    }

    companion object {
        private const val TAG = "LocationPickerFragment"
        const val REQUEST_KEY = "REQUEST_KEY"
        const val BUNDLE_KEY_ADDRESS = "BUNDLE_KEY_ADDRESS"
        const val BUNDLE_KEY_LATITUDE = "BUNDLE_KEY_LATITUDE"
        const val BUNDLE_KEY_LONGITUDE = "BUNDLE_KEY_LONGITUDE"
    }
}