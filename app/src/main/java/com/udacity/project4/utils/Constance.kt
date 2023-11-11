package com.udacity.project4.utils

import android.content.Context
import com.google.android.gms.location.GeofenceStatusCodes
import com.udacity.project4.R

object Constance {
    const val SIGN_IN_RESULT_CODE = 1000
    const val ACTION_GEOFENCE_EVENT = "ACTION_GEOFENCE_EVENT"
    const val FINE_LOCATION_ACCESS_REQUEST_CODE = 1
    const val REQUEST_TURN_DEVICE_LOCATION_ON = 29
    const val GEOFENCE_RADIUS_IN_METERS = 600f
    const val REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE = 33
    const val REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE = 34
    const val LOCATION_PERMISSION_INDEX = 0
    const val BACKGROUND_LOCATION_PERMISSION_INDEX = 1


}

fun errorMessage(context: Context, errorCode: Int): String {
    val resources = context.resources
    return resources.getString(
        when (errorCode) {
            GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE -> R.string.geofence_not_available
            GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES -> R.string.geofence_too_many_geofences
            GeofenceStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS -> R.string.geofence_too_many_pending_intents
            else -> R.string.geofence_unknown_error
        }
    )
}