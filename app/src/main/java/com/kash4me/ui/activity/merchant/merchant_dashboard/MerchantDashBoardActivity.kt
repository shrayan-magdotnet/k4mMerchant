package com.kash4me.ui.activity.merchant.merchant_dashboard

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.gson.Gson
import com.google.zxing.client.android.Intents
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanIntentResult
import com.journeyapps.barcodescanner.ScanOptions
import com.kash4me.R
import com.kash4me.data.models.QRResponse
import com.kash4me.data.models.customer.pay_by_kash4me.PayByKash4meQr
import com.kash4me.data.models.merchant.purchase_return.ReturnPurchaseQr
import com.kash4me.network.ApiServices
import com.kash4me.network.NetworkConnectionInterceptor
import com.kash4me.network.NotFoundInterceptor
import com.kash4me.repository.UserDetailsRepository
import com.kash4me.security.AES
import com.kash4me.ui.activity.calculate_cashback.CalculateCashBackActivity
import com.kash4me.ui.activity.merchant.accept_kash4me_payment.AcceptKash4mePaymentActivity
import com.kash4me.ui.activity.merchant.merchant_dashboard.qr_scan.ScanQrActivity
import com.kash4me.ui.activity.merchant.return_purchase.PurchaseReturnActivity
import com.kash4me.ui.activity.splash.SplashViewModel
import com.kash4me.ui.activity.splash.SplashViewModelFactory
import com.kash4me.ui.activity.splash.merchantDetailsResponse
import com.kash4me.ui.dialog.ErrorDialog
import com.kash4me.ui.fragments.merchant.home.MerchantHomeFragment
import com.kash4me.ui.fragments.merchant.home.MerchantHomeViewModel
import com.kash4me.ui.fragments.merchant.profile.MerchantProfileFragment
import com.kash4me.ui.fragments.merchant.search.MerchantSearchFragment
import com.kash4me.utils.QrCodeType
import com.kash4me.utils.SessionManager
import com.kash4me.utils.toast
import dagger.hilt.android.AndroidEntryPoint
import org.json.JSONObject
import timber.log.Timber

@AndroidEntryPoint
class MerchantDashBoardActivity : AppCompatActivity() {

    enum class NavigationViewFragment(
        val fragment: Fragment?, val tag: String, val position: Int
    ) {
        HOME(fragment = MerchantHomeFragment(), tag = "1", position = 0),
        SEARCH(fragment = MerchantSearchFragment(), tag = "2", position = 1),
        QR_SCAN(fragment = null, tag = "3", position = 2), // Not used since it is not a fragment
        PERSON(fragment = MerchantProfileFragment(), tag = "4", position = 3)
    }

    private lateinit var activeFragment: NavigationViewFragment
    private lateinit var bottomNavigationView: BottomNavigationView

    val merchantHomeViewModel: MerchantHomeViewModel by viewModels()

    private val viewModel by lazy { ViewModelProvider(this)[MerchantDashboardViewModel::class.java] }

    companion object {

        private const val TAG = "MerchantDashBoardActivity"
        private const val ACTIVE_FRAGMENT_TAG = "active_fragment_tag"
        const val IS_FRESH_LOGIN = "is_fresh_login"

        fun getNewIntent(packageContext: Context, isFreshLogin: Boolean): Intent {
            val intent = Intent(packageContext, MerchantDashBoardActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            intent.putExtra(IS_FRESH_LOGIN, isFreshLogin)
            return intent
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        setContentView(R.layout.activity_merchant_dash_board)
        bottomNavigationView = findViewById(R.id.merchantBottomNavigation)

        activeFragment = getActiveFragment(savedInstanceState)
        setFragment(activeFragment)

        viewModel.saveMerchantProfileInSession()

        bottomNavigationView.setOnItemSelectedListener { item ->

            when (item.itemId) {
                R.id.homePage -> {
                    setFragment(NavigationViewFragment.HOME)
                    true
                }

                R.id.searchPage -> {
                    setFragment(NavigationViewFragment.SEARCH)
                    true
                }

                R.id.qRPage -> {
                    // We are not setting fragment here because we are launching QR code scanner
                    // This way previously opened fragment will be the active fragment
                    // Returning from QR code scanner, we will navigate to the active fragment
                    openScanQrActivity()
                    true
                }

                R.id.accountPage -> {
                    setFragment(NavigationViewFragment.PERSON)
                    true
                }

                else -> false
            }

        }

        if (merchantDetailsResponse == null) {
            fetchMerchantDetails()
        }

    }


    private fun openScanQrActivity() {
        val options = ScanOptions().setCaptureActivity(ScanQrActivity::class.java)
        barcodeLauncher.launch(options)
    }

    private fun getActiveFragment(savedInstanceState: Bundle?) =
        if (savedInstanceState != null) {
            val tag = savedInstanceState.getString(ACTIVE_FRAGMENT_TAG)
            getSelectedFragment(tag = tag)
        } else {
            NavigationViewFragment.HOME
        }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(ACTIVE_FRAGMENT_TAG, activeFragment.tag)
        super.onSaveInstanceState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        val tag = savedInstanceState.getString(ACTIVE_FRAGMENT_TAG)
        when (tag) {
            NavigationViewFragment.HOME.tag -> {
                Timber.d("Let's show home")
                activeFragment = NavigationViewFragment.HOME
            }

            NavigationViewFragment.SEARCH.tag -> {
                Timber.d("Let's show search")
                activeFragment = NavigationViewFragment.SEARCH
            }

            NavigationViewFragment.QR_SCAN.tag -> {
                Timber.d("Let's show qr scan")
                activeFragment = NavigationViewFragment.QR_SCAN
            }

            NavigationViewFragment.PERSON.tag -> {
                Timber.d("Let's show person")
                activeFragment = NavigationViewFragment.PERSON
            }
        }
    }

    private fun getSelectedFragment(tag: String?): NavigationViewFragment {
        return when (tag) {
            NavigationViewFragment.HOME.tag -> {
                NavigationViewFragment.HOME
            }

            NavigationViewFragment.SEARCH.tag -> {
                NavigationViewFragment.SEARCH
            }

            NavigationViewFragment.QR_SCAN.tag -> {
                NavigationViewFragment.QR_SCAN
            }

            NavigationViewFragment.PERSON.tag -> {
                NavigationViewFragment.PERSON
            }

            else -> {
                NavigationViewFragment.HOME
            }
        }
    }

    private fun setFragment(navigationViewFragment: NavigationViewFragment) {

        if (navigationViewFragment.fragment?.isAdded == true) {
            supportFragmentManager.beginTransaction()
                .hide(activeFragment.fragment!!)
                .show(navigationViewFragment.fragment)
                .commit()
        } else {
            supportFragmentManager.beginTransaction()
                .replace(
                    R.id.fragment_container_view,
                    navigationViewFragment.fragment!!,
                    navigationViewFragment.tag
                )
                .commit()
        }
        bottomNavigationView.menu.getItem(navigationViewFragment.position).isChecked = true
        activeFragment = navigationViewFragment
        Log.d(TAG, "setFragment: active fragment -> $activeFragment")
    }

    override fun onBackPressed() {
        if (activeFragment == NavigationViewFragment.HOME) {
            finish()
        } else {
            setFragment(NavigationViewFragment.HOME)
        }
    }

    private fun fetchMerchantDetails() {

        val token = SessionManager(applicationContext).fetchAuthToken() ?: ""
        val apiInterface =
            ApiServices.invoke(
                NetworkConnectionInterceptor(applicationContext),
                NotFoundInterceptor()
            )

        val userDetailsRepository = UserDetailsRepository(apiInterface)

        val splashViewModel = ViewModelProvider(
            this, SplashViewModelFactory(userDetailsRepository)
        )[SplashViewModel::class.java]

        splashViewModel.fetchMerchantDetails(token)

    }


    private val barcodeLauncher =
        registerForActivityResult(ScanContract()) { result: ScanIntentResult ->
            if (result.contents == null) {
                val originalIntent = result.originalIntent
                if (originalIntent == null) {
                    Log.d("ScanQRFragment", "Cancelled scan")
                    toast("Cancelled")
                    updateSelectedNavigationItem()
                } else if (originalIntent.hasExtra(Intents.Scan.MISSING_CAMERA_PERMISSION)) {
                    Log.d("ScanQRFragment", "Cancelled scan due to missing camera permission")
                    toast("Please provide camera permission")
                    updateSelectedNavigationItem()
                }
            } else {
                setData(result.contents)
                updateSelectedNavigationItem()
            }
        }


    private fun setData(qr: String) {

        Timber.d("QR contents -> $qr")

        val decodedQrCode = try {
            AES().decodeQrContents(qrContents = qr)
        } catch (ex: Exception) {
            ""
        }

        if (decodedQrCode.isBlank()) {
            val errorDialog = ErrorDialog.getInstance(message = "Invalid QR")
            errorDialog.show(supportFragmentManager, errorDialog.tag)
            return
        }

        Timber.d("Decoded QR code data -> $decodedQrCode")

        val type = try {
            val qrInJson = JSONObject(decodedQrCode)
            qrInJson.getString("type")
        } catch (ex: Exception) {
            Timber.d(ex.message)
            ""
        }
        when (type) {

            QrCodeType.PAY_BY_KASH4ME.value -> acceptKash4mePayment(decodedQrCode)

            QrCodeType.PURCHASE_RETURN.value -> returnPurchase(decodedQrCode)

            QrCodeType.ASSIGN_CASHBACK.value -> assignCashback(decodedQrCode)

            else -> {

                val errorDialog = ErrorDialog.getInstance(message = "Invalid QR")
                errorDialog.show(supportFragmentManager, errorDialog.tag)

            }

        }
    }

    private fun acceptKash4mePayment(decryptedQrCode: String) {
        val gson = Gson()
        val qrResponse = gson.fromJson(decryptedQrCode, PayByKash4meQr::class.java)
        val intent = AcceptKash4mePaymentActivity.getNewIntent(
            activity = this, qrResponse = qrResponse
        )
        startActivity(intent)
    }

    private fun returnPurchase(decryptedQrCode: String) {
        val gson = Gson()
        val returnPurchaseQr = gson.fromJson(decryptedQrCode, ReturnPurchaseQr::class.java)
        val intent = PurchaseReturnActivity.getNewIntent(
            activity = this,
            response = returnPurchaseQr
        )
        startActivity(intent)
    }

    private fun assignCashback(decryptedQrCode: String) {
        val gson = Gson()
        val qrResponse = gson.fromJson(decryptedQrCode, QRResponse::class.java)
        val intent = CalculateCashBackActivity.newIntent(
            activity = this, qrResponse = qrResponse
        )
        intent.putExtra("qrResponse", qrResponse)
        startActivity(intent)
    }

    private fun updateSelectedNavigationItem() {
        setFragment(activeFragment)
    }

}