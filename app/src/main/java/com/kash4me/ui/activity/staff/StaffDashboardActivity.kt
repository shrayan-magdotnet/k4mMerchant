package com.kash4me.ui.activity.staff

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.gson.Gson
import com.google.zxing.client.android.Intents
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanIntentResult
import com.journeyapps.barcodescanner.ScanOptions
import com.kash4me.data.models.QRResponse
import com.kash4me.data.models.customer.pay_by_kash4me.PayByKash4meQr
import com.kash4me.data.models.merchant.purchase_return.ReturnPurchaseQr
import com.kash4me.merchant.R
import com.kash4me.merchant.databinding.ActivityStaffDashboardBinding
import com.kash4me.security.AES
import com.kash4me.ui.activity.calculate_cashback.CalculateCashBackActivity
import com.kash4me.ui.activity.merchant.accept_kash4me_payment.AcceptKash4mePaymentActivity
import com.kash4me.ui.activity.merchant.return_purchase.PurchaseReturnActivity
import com.kash4me.ui.dialog.ErrorDialog
import com.kash4me.ui.fragments.staff.my_transactions.MyTransactionsFragment
import com.kash4me.ui.fragments.staff.profile.StaffProfileFragment
import com.kash4me.utils.QrCodeType
import com.kash4me.utils.toast
import org.json.JSONObject
import timber.log.Timber

class StaffDashboardActivity : AppCompatActivity() {

    enum class NavigationViewFragment(
        val fragment: Fragment?, val tag: String, val position: Int
    ) {
        TAB_1(fragment = MyTransactionsFragment(), tag = "1", position = 0),
        QR_SCAN(fragment = null, tag = "2", position = 1), // Not used since it is not a fragment,
        PROFILE(fragment = StaffProfileFragment(), tag = "3", position = 2)
    }

    private lateinit var activeFragment: NavigationViewFragment

    companion object {
        private const val ACTIVE_FRAGMENT_TAG = "active_fragment_tag"

        fun getNewIntent(packageContext: Context): Intent {
            val intent = Intent(packageContext, StaffDashboardActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            return intent
        }
    }

    private var binding: ActivityStaffDashboardBinding? = null
    private val mBinding get() = binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStaffDashboardBinding.inflate(layoutInflater)
        val view = binding!!.root
        setContentView(view)

        activeFragment = getActiveFragment(savedInstanceState)
        setFragment(activeFragment)

        mBinding.bottomNav.setOnItemSelectedListener { item ->

            when (item.itemId) {
                R.id.tab_1 -> {
                    setFragment(NavigationViewFragment.TAB_1)
                    true
                }

                R.id.scan_qr -> {
                    // We are not setting fragment here because we are launching QR code scanner
                    // This way previously opened fragment will be the active fragment
                    // Returning from QR code scanner, we will navigate to the active fragment
                    openScanner()
                    true
                }

                R.id.profile -> {
                    setFragment(NavigationViewFragment.PROFILE)
                    true
                }

                else -> false
            }

        }

    }

    private fun getSelectedFragment(tag: String?): NavigationViewFragment {
        return when (tag) {
            NavigationViewFragment.TAB_1.tag -> {
                NavigationViewFragment.TAB_1
            }

            NavigationViewFragment.QR_SCAN.tag -> {
                NavigationViewFragment.QR_SCAN
            }

            NavigationViewFragment.PROFILE.tag -> {
                NavigationViewFragment.PROFILE
            }

            else -> {
                NavigationViewFragment.TAB_1
            }
        }
    }

    private fun getActiveFragment(savedInstanceState: Bundle?) =
        if (savedInstanceState != null) {
            val tag = savedInstanceState.getString(ACTIVE_FRAGMENT_TAG)
            getSelectedFragment(tag = tag)
        } else {
            NavigationViewFragment.TAB_1
        }

    private fun setFragment(navigationViewFragment: NavigationViewFragment) {

        if (navigationViewFragment.fragment == null) return

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
        mBinding.bottomNav.menu.getItem(navigationViewFragment.position).isChecked = true
        activeFragment = navigationViewFragment
        Timber.d("setFragment: active fragment -> $activeFragment")
    }

    // Launch
    private fun openScanner() {
        barcodeLauncher.launch(ScanOptions())
    }

    private val barcodeLauncher = registerForActivityResult(
        ScanContract()
    ) { result: ScanIntentResult ->
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