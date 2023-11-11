package com.udacity.project4.locationreminders.savereminder

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.PendingIntent
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSaveReminderBinding
import com.udacity.project4.locationreminders.geofence.GeofenceBroadcastReceiver
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.utils.Constance
import com.udacity.project4.utils.Logger
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject

class SaveReminderFragment : BaseFragment() {

    // Get the view model this time as a single to be shared with the another fragment
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSaveReminderBinding
    private lateinit var reminderDataItem: ReminderDataItem
    private val isAndroidQOrLater = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q

    private lateinit var geofencingClient: GeofencingClient


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val layoutId = R.layout.fragment_save_reminder
        binding = DataBindingUtil.inflate(inflater, layoutId, container, false)

        setDisplayHomeAsUpEnabled(true)
        binding.viewModel = _viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this
        binding.selectLocation.setOnClickListener {
            // Navigate to another fragment to get the user location
            val directions = SaveReminderFragmentDirections
                .actionSaveReminderFragmentToSelectLocationFragment()
            _viewModel.navigationCommand.value = NavigationCommand.To(directions)
        }

        geofencingClient = LocationServices.getGeofencingClient(requireContext())

        binding.saveReminder.setOnClickListener {
            val title = _viewModel.reminderTitle.value
            val description = _viewModel.reminderDescription.value
            val location = _viewModel.reminderSelectedLocationStr.value
            val latitude = _viewModel.latitude.value
            val longitude = _viewModel.longitude.value
            reminderDataItem =ReminderDataItem(title, description, location, latitude, longitude)


            // TODO: use the user entered reminder details to:
            //  1) add a geofencing request
            //  2) save the reminder to the local db

            if (_viewModel.validateAndSaveReminder(reminderDataItem)) {
                setupGeofencing()
            }
        }
    }

    private fun setupGeofencing() {
        if (hasLocationPermissionForForegroundAndBackground()) {
            verifyLocationSettingsAndActivateGeofence()
        } else {
            requestLocationPermissionForForegroundAndBackground()
        }
    }

    @TargetApi (29)
    private fun requestLocationPermissionForForegroundAndBackground() {
        if (hasLocationPermissionForForegroundAndBackground()) {
            return
        }
        var permissionsArray = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        val requestCode = when {
            isAndroidQOrLater -> {
                permissionsArray += Manifest.permission.ACCESS_BACKGROUND_LOCATION
                Constance.REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE
            }
            else ->Constance.REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE
        }
        Logger.lod("Request permission to access location in the foreground")
        requestPermissions(permissionsArray, requestCode)
    }

    @TargetApi (29)
    private fun verifyLocationSettingsAndActivateGeofence(resolve:Boolean = true) {
        val locationRequest = LocationRequest.create()
            .setPriority(LocationRequest.PRIORITY_LOW_POWER)

        val settingsClient = LocationServices.getSettingsClient(requireContext())
        val settingsRequest = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
            .build()

        settingsClient.checkLocationSettings(settingsRequest)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    createGeofenceForReminder()
                } else {
                    val exception = task.exception!!

                    if (exception is ResolvableApiException && resolve) {
                        try {
                            exception.startResolutionForResult(
                                requireActivity(),
                                Constance.REQUEST_TURN_DEVICE_LOCATION_ON
                            )
                        } catch (sendEx: IntentSender.SendIntentException) {
                            Logger.lod("Error getting location settings resolution: " + sendEx.message)
                        }
                    } else {
                        // Motivate user to grant permission
                    }
                }
            }
    }

    @SuppressLint("MissingPermission")
    private fun createGeofenceForReminder() {
        val geofence = createGeofence(reminderDataItem)
        val geofencingRequest = buildGeofencingRequest(geofence)
        val geofencePendingIntent = createGeofencePendingIntent()

        geofencingClient.removeGeofences(geofencePendingIntent)?.run {
            addOnCompleteListener {
                geofencingClient.addGeofences(geofencingRequest, geofencePendingIntent)?.run {
                    addOnSuccessListener {
                        Logger.lod("Create Geofence : ${geofence.requestId}")

                    }
                    addOnFailureListener {

                        if ((it.message != null)) {
                            Logger.lod("${it.message}")
                        }
                    }
                }
            }
        }
    }
    private fun buildGeofencingRequest(geofence: Geofence): GeofencingRequest {
        return GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(geofence)
            .build()
    }

    private fun createGeofencePendingIntent(): PendingIntent {
        val intent = Intent(activity, GeofenceBroadcastReceiver::class.java)
        intent.action = Constance.ACTION_GEOFENCE_EVENT

        return PendingIntent.getBroadcast(
            requireContext(),
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun createGeofence(reminderData: ReminderDataItem): Geofence {
        return Geofence.Builder()
            .setRequestId(reminderData.id)
            .setCircularRegion(
                reminderData.latitude!!,
                reminderData.longitude!!,
                Constance.GEOFENCE_RADIUS_IN_METERS
            )
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
            .build()
    }


    private fun hasLocationPermissionForForegroundAndBackground(): Boolean {
        return isForegroundLocationPermissionApproved() && isBackgroundLocationPermissionApproved()

    }

    private fun isBackgroundLocationPermissionApproved(): Boolean {
        return if (isAndroidQOrLater) {
            PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )
        } else {
            true
        }
    }

    private fun isForegroundLocationPermissionApproved(): Boolean {
        return PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        Logger.lod("onRequestPermissionResult")

        if (grantResults.isEmpty()) {
            handleDeniedPermissions(grantResults)
            return
        }

        if (requestCode == Constance.REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE) {
            if (grantResults[Constance.LOCATION_PERMISSION_INDEX] == PackageManager.PERMISSION_DENIED ||
                grantResults[Constance.BACKGROUND_LOCATION_PERMISSION_INDEX] ==
                PackageManager.PERMISSION_DENIED) {
                handleDeniedPermissions(grantResults)
                return
            }
        }

        handleGrantedPermissions()
    }

    private fun handleDeniedPermissions(grantResults: IntArray) {
        if (grantResults[Constance.LOCATION_PERMISSION_INDEX] == PackageManager.PERMISSION_DENIED) {
            // Explain user why app needs this permission
        } else if (grantResults[Constance.BACKGROUND_LOCATION_PERMISSION_INDEX] ==
            PackageManager.PERMISSION_DENIED) {
            // Explain user why app needs this permission
        }
    }

    private fun handleGrantedPermissions() {
        verifyLocationSettingsAndActivateGeofence()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Make sure to clear the view model after destroy, as it's a single view model.
        _viewModel.onClear()
    }
}