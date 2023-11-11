package com.udacity.project4.locationreminders.savereminder.selectreminderlocation

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PointOfInterest
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.Constance
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject

class SelectLocationFragment : BaseFragment(), OnMapReadyCallback {

    // Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSelectLocationBinding
    private lateinit var googleMap: GoogleMap
    private lateinit var pointOfInterest: PointOfInterest


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val layoutId = R.layout.fragment_select_location
        binding = DataBindingUtil.inflate(inflater, layoutId, container, false)

        binding.viewModel = _viewModel
        binding.lifecycleOwner = this

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)

        // TODO: add the map setup implementation
        // TODO: zoom to the user location after taking his permission
        // TODO: add style to the map
        // TODO: put a marker to location that the user selected

        // TODO: call this function after the user confirms on the selected location

        val mapFragment = childFragmentManager
            .findFragmentById(R.id.google_map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        return binding.root
    }

    private fun onLocationSelected() {
        // TODO: When the user confirms on the selected location,
        //  send back the selected location details to the view model
        //  and navigate back to the previous fragment to save the reminder and add the geofence
        binding.btnSave.setOnClickListener {

            if (::pointOfInterest.isInitialized) {
                updateLocationData()
                navigateBackToPreviousScreenForSavingAndAddingGeofence()
            } else {
                showToast("Please select a location that is open now")
            }

        }
    }

    private fun navigateBackToPreviousScreenForSavingAndAddingGeofence() {
        _viewModel.navigationCommand.value =
            NavigationCommand.Back
    }

    private fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }

    private fun updateLocationData() {
        with(_viewModel) {
            latitude.value = pointOfInterest.latLng.latitude
            longitude.value = pointOfInterest.latLng.longitude
            reminderSelectedLocationStr.value = pointOfInterest.name
            selectedPOI.value = pointOfInterest
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        // TODO: Change the map type based on the user's selection.
        R.id.normal_map -> {
            googleMap.mapType = GoogleMap.MAP_TYPE_NORMAL
            true
        }
        R.id.hybrid_map -> {
            googleMap.mapType = GoogleMap.MAP_TYPE_HYBRID
            true
        }
        R.id.satellite_map -> {
            googleMap.mapType = GoogleMap.MAP_TYPE_SATELLITE
            true
        }
        R.id.terrain_map -> {
            googleMap.mapType = GoogleMap.MAP_TYPE_TERRAIN
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        // Vị trí Hà Nội, Việt Nam
        val hanoi = LatLng(21.0285, 105.8542)
        val zoomLevel = 15f
        googleMap.addMarker(MarkerOptions().position(hanoi).title("Marked in Hanoi"))
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(hanoi, zoomLevel))
        googleMap.uiSettings.isZoomControlsEnabled = true

        requestLocationPermission()
        registerPoiClickListener(map)
        onLocationSelected()
    }

    private fun registerPoiClickListener(map: GoogleMap) {
        map.setOnPoiClickListener { poi ->
            clearAndDisplayPoi(map, poi)
        }

    }

    private fun clearAndDisplayPoi(map: GoogleMap, poi: PointOfInterest) {
        map.clear()
        pointOfInterest = poi
        val poiMarker = map.addMarker(
            MarkerOptions()
                .position(poi.latLng)
                .title(poi.name)
        )

        val circleOptions = CircleOptions()
            .center(poi.latLng)
            .radius(200.0)
            .strokeColor(Color.argb(255, 200, 0, 0))
            .fillColor(Color.argb(64, 255, 0, 0))
            .strokeWidth(4F)

        map.addCircle(circleOptions)
        poiMarker?.showInfoWindow()
    }

    private fun requestLocationPermission() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            googleMap.isMyLocationEnabled = true
            Toast.makeText(context, "The user gave permission for the app to access their location", Toast.LENGTH_LONG).show()
        } else if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION)) {
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                Constance.FINE_LOCATION_ACCESS_REQUEST_CODE
            )
        } else {
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                Constance.FINE_LOCATION_ACCESS_REQUEST_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            Constance.FINE_LOCATION_ACCESS_REQUEST_CODE -> handleFineLocationPermissionResult(grantResults)
        }
    }
    private fun handleFineLocationPermissionResult(grantResults: IntArray) {
        if (isFineLocationPermissionGranted(grantResults)) {
            requestLocationPermission()
        } else {
            showLocationPermissionDeniedMessage()
        }
    }

    private fun isFineLocationPermissionGranted(grantResults: IntArray): Boolean {
        return grantResults.isNotEmpty() && (grantResults[0] == PackageManager.PERMISSION_GRANTED)
    }
    private fun showLocationPermissionDeniedMessage() {
        Toast.makeText(context, "Location permission was not granted.", Toast.LENGTH_LONG).show()
    }

}