package com.kash4me.ui.activity.customer.customer_dashboard

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.kash4me.merchant.R
import com.kash4me.merchant.databinding.ActivityCustomerDashboardBinding
import com.kash4me.network.ApiServices
import com.kash4me.network.NetworkConnectionInterceptor
import com.kash4me.network.NotFoundInterceptor
import com.kash4me.repository.UserDetailsRepository
import com.kash4me.ui.activity.splash.SplashViewModel
import com.kash4me.ui.activity.splash.SplashViewModelFactory
import com.kash4me.ui.common.RequestLocationPermissionActivity
import com.kash4me.ui.fragments.customer.home.CustomerHomeFragment
import com.kash4me.ui.fragments.customer.profile.CustomerProfileFragment
import com.kash4me.ui.fragments.customer.search.CustomerSearchFragment
import com.kash4me.ui.fragments.customer.total_transaction.CustomerTotalTransactions2Fragment
import com.kash4me.utils.LocationUtils
import com.kash4me.utils.SessionManager
import com.kash4me.utils.custom_views.CustomProgressDialog
import dagger.hilt.android.AndroidEntryPoint
import pub.devrel.easypermissions.EasyPermissions
import timber.log.Timber

@AndroidEntryPoint
class CustomerDashboardActivity : AppCompatActivity() {

    companion object {

        private const val FRAGMENT_TAG = "FRAGMENT_TAG"
        const val IS_FRESH_LOGIN = "is_fresh_login"

        fun getNewIntent(
            packageContext: Context,
            fragment: NavigationViewFragment? = null,
            isFreshLogin: Boolean
        ): Intent {
            val intent = Intent(packageContext, CustomerDashboardActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            intent.putExtra(FRAGMENT_TAG, fragment?.tag)
            intent.putExtra(IS_FRESH_LOGIN, isFreshLogin)
            return intent
        }

    }

    enum class NavigationViewFragment(
        val fragment: Fragment, val tag: String, val position: Int
    ) {
        HOME(fragment = CustomerHomeFragment(), tag = "1", position = 0),
        SEARCH(fragment = CustomerSearchFragment(), tag = "2", position = 1),
        CASHBACK(fragment = CustomerTotalTransactions2Fragment(), tag = "3", position = 2),
        PERSON(fragment = CustomerProfileFragment(), tag = "4", position = 3)
    }

    val customDialogClass: CustomProgressDialog by lazy { CustomProgressDialog(this) }

    private val navController by lazy { findNavController(R.id.customer_dashboard) }

    private val sessionManager by lazy { SessionManager(context = this) }

    private val permissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)

    private var binding: ActivityCustomerDashboardBinding? = null
    private val mBinding get() = binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCustomerDashboardBinding.inflate(layoutInflater)
        val view = binding!!.root
        setContentView(view)

        val hasPermissions = EasyPermissions.hasPermissions(this, *permissions)
        if (hasPermissions) {

            Timber.d("We have location permissions, no need to request permissions")

        } else {

            Timber.d("We don't have location permissions, we must request it")
            goToRequestLocationPermissionActivity()

        }

        LocationUtils().updateCurrentLocation(context = this)

        mBinding.bottomNav.setupWithNavController(navController)
        setupOnItemSelectedListener()

        if (sessionManager.fetchCustomerDetails() == null) {
            fetchCustomerDetails()
        }

        setupToolbar()
        setCurrentFragment()

    }

    private fun setupOnItemSelectedListener() {
        mBinding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.customerHomeFragment -> {
                    Timber.d("Selected bottom nav -> Home")
                    navController.navigate(R.id.customerHomeFragment)
                    true
                }

                R.id.customerSearchFragment -> {
                    Timber.d("Selected bottom nav -> Search")
                    navController.navigate(R.id.customerSearchFragment)
                    true
                }

                R.id.customerTotalTransactionFragment -> {
                    Timber.d("Selected bottom nav -> Total Transaction")
                    navController.navigate(R.id.customerTotalTransactionFragment)
                    true
                }

                R.id.announcementsFragment -> {
                    Timber.d("Selected bottom nav -> Announcements Fragment")
                    navController.navigate(R.id.announcementsFragment)
                    true
                }

                R.id.customerProfileFragment -> {
                    Timber.d("Selected bottom nav -> Profile")
                    navController.navigate(R.id.customerProfileFragment)
                    true
                }

                else -> {
                    false
                }
            }
        }
    }

    private fun goToRequestLocationPermissionActivity() {
        val intent = RequestLocationPermissionActivity.getNewIntent(activity = this)
        startActivity(intent)
    }

    private fun setCurrentFragment() {
        val fragmentTag = intent.getStringExtra(FRAGMENT_TAG)
        val fragment = NavigationViewFragment.values().find { it.tag == fragmentTag }
            ?: NavigationViewFragment.HOME
        Timber.d("Selected fragment -> $fragment")

        val fragmentId = when (fragment) {
            NavigationViewFragment.HOME -> R.id.customerHomeFragment
            NavigationViewFragment.SEARCH -> R.id.customerSearchFragment
            NavigationViewFragment.CASHBACK -> R.id.customerTotalTransactionFragment
            NavigationViewFragment.PERSON -> R.id.customerProfileFragment
        }
        navController.navigate(fragmentId)
    }

    fun setupToolbar() {
        setSupportActionBar(mBinding.customAppBar.toolbar)
    }

    override fun onBackPressed() {

        if (navController.currentDestination?.id == R.id.customerHomeFragment) {
            finish()
        } else {
            navController.navigate(R.id.customerHomeFragment)
        }

    }

    private fun fetchCustomerDetails() {

        val apiInterface =
            ApiServices.invoke(
                NetworkConnectionInterceptor(applicationContext),
                NotFoundInterceptor()
            )

        val userDetailsRepository = UserDetailsRepository(apiInterface)

        val viewModel = ViewModelProvider(
            this, SplashViewModelFactory(userDetailsRepository),
        )[SplashViewModel::class.java]

        viewModel.fetchCustomerDetails()

    }

}