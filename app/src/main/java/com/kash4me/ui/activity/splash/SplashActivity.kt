package com.kash4me.ui.activity.splash

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.location.LocationServices
import com.google.android.libraries.places.api.Places
import com.kash4me.R
import com.kash4me.data.models.user.UserType
import com.kash4me.databinding.ActivitySplashBinding
import com.kash4me.network.ApiServices
import com.kash4me.network.NetworkConnectionInterceptor
import com.kash4me.network.NotFoundInterceptor
import com.kash4me.repository.UserDetailsRepository
import com.kash4me.ui.activity.customer.customer_dashboard.CustomerDashboardActivity
import com.kash4me.ui.activity.login.LoginActivity
import com.kash4me.ui.activity.merchant.merchant_dashboard.MerchantDashBoardActivity
import com.kash4me.ui.activity.staff.StaffDashboardActivity
import com.kash4me.utils.CurrentLocation
import com.kash4me.utils.SessionManager
import com.kash4me.utils.toast
import java.io.IOException
import java.util.Locale


class SplashActivity : AppCompatActivity() {


    private val splashTime = 1000L

    private lateinit var viewModel: SplashViewModel


    private lateinit var blackWingIV: ImageView
    private lateinit var orangeWingIV: ImageView
    private lateinit var greenWingIV: ImageView
    private lateinit var dollarIV: ImageView

    private lateinit var sessionManager: SessionManager
    private var userProfile: Boolean = false
    private var cbSettings: Boolean = false
    private lateinit var binding: ActivitySplashBinding

    companion object {

        private const val TAG = "SplashActivity"

        fun getNewIntent(packageContext: Context): Intent {
            val intent = Intent(packageContext, SplashActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            return intent
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        sessionManager = SessionManager(this)
        userProfile = sessionManager.fetchUserProfile()
        cbSettings = sessionManager.fetchCBSettings()
        blackWingIV = binding.blackWingIV
        orangeWingIV = binding.orangeWingIV
        greenWingIV = binding.greenWingIV
        dollarIV = binding.dollarIV

        initVM()

        checkLocationPermission()

        delayCode(animateBlackWing(), splashTime)
//        initPlacesAPI()
    }

    private fun initVM() {

        val apiInterface =
            ApiServices.invoke(
                NetworkConnectionInterceptor(applicationContext),
                NotFoundInterceptor()
            )

        val userDetailsRepository = UserDetailsRepository(apiInterface)

        viewModel = ViewModelProvider(
            this, SplashViewModelFactory(userDetailsRepository)
        )[SplashViewModel::class.java]

    }


    // takes function and time and executes after given time
    private fun delayCode(function: Unit, time: Long) {
        Handler(Looper.getMainLooper()).postDelayed({ function }, time)
    }

    private fun animateBlackWing() {

        val anim = AnimationUtils.loadAnimation(
            this,
            R.anim.zoom_out
        )
        //Listener to get callback when animation of tv1 ends
        val animListener1 = object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {}

            override fun onAnimationEnd(animation: Animation) {
                // when animation of first TextView ends we start
                animateOrangeWing()
                orangeWingIV.visibility = View.VISIBLE
            }

            override fun onAnimationRepeat(animation: Animation) {}
        }

        anim.setAnimationListener(animListener1)
        blackWingIV.visibility = View.VISIBLE
        blackWingIV.startAnimation(anim)

    }

    private fun animateOrangeWing() {

        val anim = AnimationUtils.loadAnimation(
            this,
            R.anim.zoom_out
        )
        //Listener to get callback when animation of tv1 ends
        val animListener1 = object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {}

            override fun onAnimationEnd(animation: Animation) {
                // when animation of first TextView ends we start
                animateGreenWing()
                greenWingIV.visibility = View.VISIBLE
            }

            override fun onAnimationRepeat(animation: Animation) {}
        }

        anim.setAnimationListener(animListener1)
        orangeWingIV.startAnimation(anim)

    }

    private fun animateGreenWing() {

        val anim = AnimationUtils.loadAnimation(
            this,
            R.anim.zoom_out
        )
        //Listener to get callback when animation of tv1 ends
        val animListener1 = object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {}

            override fun onAnimationEnd(animation: Animation) {
                // when animation of first TextView ends we start
                animateDollar()
                dollarIV.visibility = View.VISIBLE
            }

            override fun onAnimationRepeat(animation: Animation) {}
        }

        anim.setAnimationListener(animListener1)
        greenWingIV.startAnimation(anim)

    }

    private fun animateDollar() {

        val anim = AnimationUtils.loadAnimation(
            this,
            R.anim.zoom_out
        )
        //Listener to get callback when animation of tv1 ends
        val animListener1 = object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {}

            override fun onAnimationEnd(animation: Animation) {
                // when animation of first TextView ends we start
                animateDollarZoomIn()
            }

            override fun onAnimationRepeat(animation: Animation) {}
        }

        anim.setAnimationListener(animListener1)
        dollarIV.startAnimation(anim)
    }

    private fun animateDollarZoomIn() {

        val anim = AnimationUtils.loadAnimation(
            this,
            R.anim.zoom_in
        )
        //Listener to get callback when animation of tv1 ends
        val animListener1 = object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {}

            override fun onAnimationEnd(animation: Animation) {
                // when animation of first TextView ends we start
//                initPlacesAPI()

                val userType = sessionManager.fetchUserType()

                if (userType == UserType.MERCHANT && userProfile && cbSettings) {
                    delayCode(goToMerchantDashBoardActivity(), splashTime)
                    return
                }

                if (userType == UserType.CUSTOMER && userProfile) {
                    delayCode(goToCustomerDashBoardActivity(), splashTime)
                    return
                }

                if (userType == UserType.STAFF) {
                    delayCode(goToStaffDashBoardActivity(), splashTime)
                    return
                }

                delayCode(goToLoginActivity(), splashTime)


            }

            override fun onAnimationRepeat(animation: Animation) {}
        }

        anim.setAnimationListener(animListener1)
        dollarIV.startAnimation(anim)


    }

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1
                )
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1
                )
            }

        } else {
            getLastKnownLocation()
        }
    }

    private fun getLastKnownLocation() {

        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        if (ActivityCompat
                .checkSelfPermission(
                    this, Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            checkLocationPermission()
            return
        }
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            Log.d(TAG, "Location detected -> $location")
            CurrentLocation.lat = location?.latitude ?: 0.0
            CurrentLocation.lng = location?.longitude ?: 0.0
            Log.d(TAG, "Lat: ${CurrentLocation.lat} Long: ${CurrentLocation.lng}")
            getCountryName()
        }

    }

    private fun getCountryName() {
        val geocoder = Geocoder(applicationContext, Locale.US)
        val addresses: List<Address>?
        try {
            addresses = geocoder.getFromLocation(CurrentLocation.lat, CurrentLocation.lng, 1)
            Log.d(TAG, "Latitude -> ${CurrentLocation.lat}")
            Log.d(TAG, "Longitude -> ${CurrentLocation.lng}")

            Log.d(TAG, "getCountryName: $addresses")

            if (!addresses.isNullOrEmpty()) {
                addresses.getOrNull(0)?.countryName?.let { CurrentLocation.country = it }
                addresses.getOrNull(0)?.postalCode?.let { CurrentLocation.postalCode = it }
                addresses.getOrNull(0)?.countryCode?.let { CurrentLocation.countryCode = it }
            }

        } catch (ignored: IOException) {
            Log.d("SplashActivity", "getCountryName: Error: ${ignored.localizedMessage}")
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            1 -> {
                if (grantResults.isNotEmpty() && grantResults[0] ==
                    PackageManager.PERMISSION_GRANTED
                ) {
                    if ((ContextCompat.checkSelfPermission(
                            this,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) ==
                                PackageManager.PERMISSION_GRANTED)
                    ) {
                        getLastKnownLocation()
                    }
                } else {
                    toast("Please provide location permission")
                    checkLocationPermission()
                }
                return
            }
        }
    }


    private fun goToLoginActivity() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
        finish()
    }

    private fun goToCustomerDashBoardActivity() {
        val intent =
            CustomerDashboardActivity.getNewIntent(packageContext = this, isFreshLogin = false)
        startActivity(intent)
    }

    private fun goToStaffDashBoardActivity() {
        val intent = StaffDashboardActivity.getNewIntent(packageContext = this)
        startActivity(intent)
    }

    private fun goToMerchantDashBoardActivity() {
        val intent = MerchantDashBoardActivity.getNewIntent(
            packageContext = this, isFreshLogin = false
        )
        startActivity(intent)
    }

    private fun initPlacesAPI() {

        val apiKey = getString(R.string.places_api_key)


        // Initialize the SDK
        Places.initialize(applicationContext, apiKey)
    }


}
