package com.kash4me.ui.activity.customer.request_cashback

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.kash4me.data.models.QRResponse
import com.kash4me.merchant.R
import com.kash4me.merchant.databinding.ActivityRequestCashbackBinding
import com.kash4me.ui.dialog.ErrorDialog
import com.kash4me.ui.fragments.customer.home.CustomerHomeViewModel
import com.kash4me.utils.AppConstants
import com.kash4me.utils.ImageUtils
import com.kash4me.utils.SessionManager
import com.kash4me.utils.custom_views.CustomProgressDialog
import com.kash4me.utils.extensions.getAmount
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class RequestCashbackActivity : AppCompatActivity() {

    private var amount: Double = 0.0

    private val navController: NavController by lazy { findNavController(R.id.nav_host_fragment) }

    val viewModel: CustomerHomeViewModel by viewModels()

    private val progressDialog by lazy { CustomProgressDialog(this) }

    private var binding: ActivityRequestCashbackBinding? = null
    private val mBinding get() = binding!!

    companion object {

        const val QR_RESPONSE = "qr_response"
        const val MERCHANT_ID = "merchant_id"
        const val MERCHANT_NAME = "merchant_name"
        const val MERCHANT_UNIQUE_ID = "merchant_unique_id"

        fun getNewIntent(
            packageContext: Context,
            qrResponse: QRResponse,
            merchantId: Int,
            merchantName: String,
            merchantUniqueId: String
        ): Intent {
            val intent = Intent(packageContext, RequestCashbackActivity::class.java)
            intent.putExtra(QR_RESPONSE, qrResponse)
            intent.putExtra(MERCHANT_ID, merchantId)
            intent.putExtra(MERCHANT_NAME, merchantName)
            intent.putExtra(MERCHANT_UNIQUE_ID, merchantUniqueId)
            return intent
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRequestCashbackBinding.inflate(layoutInflater)
        val view = binding!!.root
        setContentView(view)

        setupToolbar(title = "Request Cashback")

        etCashbackAmountListener()
//        btnGenerateQrCodeListener()

    }

    fun setupToolbar(title: String) {
        setSupportActionBar(mBinding.toolbarLayout.toolbar)
        supportActionBar?.title = title
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        mBinding.toolbarLayout.toolbar.setNavigationOnClickListener {
            val wasNavigationSuccessful = navController.navigateUp()
            Timber.d("Navigate up result -> $wasNavigationSuccessful")
            if (!wasNavigationSuccessful) {
                finish()
            }
        }
    }

    private fun etCashbackAmountListener() {
        mBinding.etCashbackAmount.doAfterTextChanged {
            Timber.d("String -> " + it.toString())
            amount = mBinding.etCashbackAmount.getAmount()
        }
    }

    private fun btnGenerateQrCodeListener() {
        mBinding.btnGenerateQrCode.setOnClickListener {

            Log.d("RequestCashback", "Amount -> $amount")

            val token = SessionManager(this).fetchAuthToken() ?: ""
            val qrResponse = intent.getParcelableExtra<QRResponse>(QR_RESPONSE)
            Log.d("RequestCashback", "qr response -> $qrResponse")
            val shopId = intent.getIntExtra(MERCHANT_ID, AppConstants.MINUS_ONE)
            if (shopId == AppConstants.MINUS_ONE) {
                val errorDialog = ErrorDialog.getInstance(message = "Something went wrong")
                errorDialog.show(supportFragmentManager, errorDialog.tag)
                return@setOnClickListener
            }

            progressDialog.show()
            requestQrCode(token, shopId)

        }
    }

    private fun requestQrCode(token: String, shopId: Int) {

        viewModel.requestQrCode(token = token, amount = amount, shopId = shopId).observe(this) {

            lifecycleScope.launch {

                Timber.d("QR Image -> ${it.qrImage}")
                val qrCode = ImageUtils().decodeImageFromBase64(
                    context = this@RequestCashbackActivity, base64String = it.qrImage
                )
                mBinding.ivQrCode.setImageBitmap(qrCode)
                progressDialog.hide()

            }

        }

        viewModel.errorMessage.observe(this) {
            val errorDialog = ErrorDialog.getInstance(message = it)
            errorDialog.show(supportFragmentManager, errorDialog.tag)
            progressDialog.hide()
        }

    }

}