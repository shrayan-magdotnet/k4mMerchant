package com.kash4me.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import androidx.fragment.app.Fragment
import pub.devrel.easypermissions.EasyPermissions

object LocationPermissionHelper {

    private val permissions = arrayOf(
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    fun getPermissions(): Array<String> {
        return permissions
    }

    val REQ_CODE = RequestCodes.LOCATION_PERMISSION.getCode()

    fun arePermissionsGranted(context: Context): Boolean {

        return EasyPermissions.hasPermissions(context, *permissions)

    }

    fun requestPermissions(
        activity: Activity,
        requestCode: RequestCodes = RequestCodes.LOCATION_PERMISSION
    ) {

        val rationale = "Application requires location permissions for better functioning"
        EasyPermissions.requestPermissions(activity, rationale, requestCode.getCode(), *permissions)

    }

    fun requestPermissions(
        fragment: Fragment,
        requestCode: RequestCodes = RequestCodes.LOCATION_PERMISSION
    ) {

        val rationale = "Application requires location permissions for better functioning"
        EasyPermissions.requestPermissions(fragment, rationale, requestCode.getCode(), *permissions)

    }


}