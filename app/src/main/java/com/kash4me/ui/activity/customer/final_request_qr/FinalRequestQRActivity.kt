package com.kash4me.ui.activity.customer.final_request_qr

import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.journeyapps.barcodescanner.BarcodeEncoder
import com.kash4me.data.models.QRResponse
import com.kash4me.databinding.ActivityFinalRequestQractivityBinding
import com.kash4me.ui.activity.customer.customer_dashboard.CustomerDashboardActivity
import com.kash4me.ui.dialog.ErrorDialog
import com.kash4me.ui.fragments.customer.home.CustomerHomeViewModel
import com.kash4me.utils.ImageUtils
import com.kash4me.utils.SessionManager
import com.kash4me.utils.extensions.getZeroIfNull
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class FinalRequestQRActivity : AppCompatActivity() {

    private lateinit var qrResponse: QRResponse

    private val viewModel: CustomerHomeViewModel by viewModels()

    private var binding: ActivityFinalRequestQractivityBinding? = null
    private val mBinding get() = binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFinalRequestQractivityBinding.inflate(layoutInflater)
        val view = binding!!.root
        setContentView(view)

        val bundle = intent?.extras
        if (bundle != null) {
            qrResponse = bundle.getParcelable("qrResponse")!!
            qrResponse.finalQR = true
        }

        initUI()
        setupToolbar()
        getQrCode()

    }

    private fun setupToolbar() {
        setSupportActionBar(mBinding.toolbarLayout.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Request Cash Back"
        mBinding.toolbarLayout.toolbar.setNavigationOnClickListener { onBackPressed() }
    }

    private fun initUI() {
//        generateQR(qrImage = qrResponse.qrCodeInBase64)
        initOnClick()
    }

    private fun getQrCode() {
        val token = SessionManager(context = this).fetchAuthToken()
        if (token != null) {

            Timber.d("Amount -> ${qrResponse.purchaseAmountInt}")

            viewModel.requestQrCode(
                token = token,
                amount = qrResponse.purchaseAmountInt.getZeroIfNull(),
                shopId = qrResponse.merchantId.getZeroIfNull()
            ).observe(this) {

                lifecycleScope.launch {

                    Timber.d("QR Image -> ${it.qrImage}")
                    val qrCode = ImageUtils().decodeImageFromBase64(
                        context = this@FinalRequestQRActivity, base64String = it.qrImage
                    )
                    mBinding.qrIV.setImageBitmap(qrCode)

                }


            }

            viewModel.errorMessage.observe(this) {
                val errorDialog = ErrorDialog.getInstance(message = it)
                errorDialog.show(supportFragmentManager, errorDialog.tag)
            }

        }
    }

    private fun generateQR(qrImage: String?) {
        Log.d("TAG", "generateQR: qrResponse: $qrResponse")
        val gson = Gson()
        val mWriter = MultiFormatWriter()
        try {
            val mMatrix = mWriter.encode(gson.toJson(qrResponse), BarcodeFormat.QR_CODE, 1000, 1000)
            val mEncoder = BarcodeEncoder()
            val mBitmap = mEncoder.createBitmap(mMatrix)
//            qrIV.setImageBitmap(mBitmap)
        } catch (e: WriterException) {
            e.printStackTrace()
        }

        lifecycleScope.launch {

            val qrCode = ImageUtils().decodeImageFromBase64(
                context = this@FinalRequestQRActivity,
                base64String = qrImage
            )
            mBinding.qrIV.setImageBitmap(qrCode)

        }

    }

    private fun initOnClick() {
        mBinding.goBackIV.setOnClickListener { onBackPressed() }
        mBinding.doneBtn.setOnClickListener { navigateBackToCustomerHome() }
    }

    private fun navigateBackToCustomerHome() {
        val intent =
            CustomerDashboardActivity.getNewIntent(packageContext = this, isFreshLogin = false)
        startActivity(intent)
    }
}