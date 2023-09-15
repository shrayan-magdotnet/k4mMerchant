package com.kash4me.ui.common

import android.Manifest
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.kash4me.merchant.R
import com.kash4me.merchant.databinding.ActivityRequestLocationPermissionBinding
import com.kash4me.ui.activity.customer.customer_dashboard.CustomerDashboardActivity
import com.kash4me.utils.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import timber.log.Timber

class RequestLocationPermissionActivity

    : AppCompatActivity(), EasyPermissions.PermissionCallbacks, EasyPermissions.RationaleCallbacks {

    private var binding: ActivityRequestLocationPermissionBinding? = null
    private val mBinding get() = binding!!

    private val mPermissions by lazy { arrayOf(Manifest.permission.ACCESS_FINE_LOCATION) }

    companion object {

        private const val LOCATION_REQ = 1000

        fun getNewIntent(activity: AppCompatActivity): Intent {

            val intent = Intent(activity, RequestLocationPermissionActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            return intent

        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRequestLocationPermissionBinding.inflate(layoutInflater)
        val view = binding!!.root
        setContentView(view)

        initIvBack()
        btnTurnOnLocationListener()

    }

    private fun btnTurnOnLocationListener() {
        mBinding.btnTurnOnLocation.setOnClickListener {

            requestPhoneStatePermission()

        }
    }

    private fun initIvBack() {
        mBinding.customAppBar.backIV.isVisible = true
        mBinding.customAppBar.backIV.setOnClickListener {

            AlertDialog.Builder(this)
                .setTitle(R.string.log_out)
                .setMessage(R.string.are_you_sure_you_want_to_log_out)
                .setPositiveButton(R.string.yes) { dialog, _ ->
                    dialog.dismiss()
                    logOut()
                }
                .setNegativeButton(R.string.no) { dialog, _ ->
                    dialog.dismiss()
                }
                .show()

        }
    }

    private fun logOut() {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                SessionManager(this@RequestLocationPermissionActivity).logoutUser(packageContext = this@RequestLocationPermissionActivity)
            }
        }
    }

    override fun onResume() {
        super.onResume()

        val hasPermissions = EasyPermissions.hasPermissions(this, *mPermissions)
        if (hasPermissions) {

            Timber.d("We have location permissions, no need to request permissions")
            navigateToCustomerDashboard()

        } else {

            Timber.d("We don't have location permissions, we must request it")

        }


    }

    private fun navigateToCustomerDashboard() {
        val intent =
            CustomerDashboardActivity.getNewIntent(packageContext = this, isFreshLogin = true)
        startActivity(intent)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {

        if (requestCode == LOCATION_REQ) {

            val arePermissionsGranted = EasyPermissions.hasPermissions(this, *mPermissions)
            if (arePermissionsGranted) {

                Timber.d("All permissions have been granted for using Customer Dashboard")
                navigateToCustomerDashboard()

            } else {

                Timber.d("Permissions are missing for using Customer Dashboard")
                proceedToRequestingPhoneStatePermission()

            }

        }


    }

    private fun proceedToRequestingPhoneStatePermission() {
        AlertDialog.Builder(this)
            .setTitle(R.string.turn_on_location)
            .setMessage(R.string.location_permission_rationale)
            .setPositiveButton(R.string.ok, positiveButtonListener)
            .setNegativeButton(R.string.cancel, negativeButtonListener)
            .show()
    }

    private val positiveButtonListener = DialogInterface.OnClickListener { _, _ ->
        requestPhoneStatePermission()
    }

    private val negativeButtonListener = DialogInterface.OnClickListener { dialog, _ ->
        dialog.dismiss()
    }

    private fun requestPhoneStatePermission() {

        val rationale = getString(R.string.location_permission_rationale)
        EasyPermissions.requestPermissions(this, rationale, LOCATION_REQ, *mPermissions)

    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {

        val isAnyPermissionPermanentlyDenied =
            EasyPermissions.somePermissionPermanentlyDenied(this, perms)
        if (isAnyPermissionPermanentlyDenied) {
            Timber.d("onPermissionsDenied: Some permissions permanently denied")
            AppSettingsDialog.Builder(this).build().show()
        }
    }

    override fun onRationaleAccepted(requestCode: Int) {

        if (requestCode == LOCATION_REQ)
            Timber.d("onRationaleAccepted: User has accepted the request dialog")

    }

    override fun onRationaleDenied(requestCode: Int) {

        if (requestCode == LOCATION_REQ)
            Timber.d("onRationaleDenied: User has cancelled the request dialog")

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == AppSettingsDialog.DEFAULT_SETTINGS_REQ_CODE)
            Timber.d("onActivityResult: Dialog requesting permission has been shown to the user")

    }

}