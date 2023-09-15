package com.kash4me.ui.activity.customer.merchant_details.transactions

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.journeyapps.barcodescanner.BarcodeEncoder
import com.kash4me.databinding.ActivityTransactionDetainForReturnPurchaseBinding
import com.kash4me.security.AES
import com.kash4me.ui.dialog.ErrorDialog
import com.kash4me.utils.AppConstants
import com.kash4me.utils.custom_views.CustomProgressDialog
import com.kash4me.utils.extensions.formatAsCurrency
import com.kash4me.utils.extensions.getEmptyIfNull
import com.kash4me.utils.network.Resource
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class TransactionDetainForReturnPurchaseActivity : AppCompatActivity() {

    private var binding: ActivityTransactionDetainForReturnPurchaseBinding? = null
    private val mBinding get() = binding!!

    companion object {

        private const val TRANSACTION_ID = "transaction_id"

        fun getNewIntent(activity: AppCompatActivity, transactionId: Int): Intent {
            val intent = Intent(activity, TransactionDetainForReturnPurchaseActivity::class.java)
            intent.putExtra(TRANSACTION_ID, transactionId)
            return intent
        }

    }

    private val mProgressDialog by lazy { CustomProgressDialog(context = this) }

    private val mViewModel: TransactionsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTransactionDetainForReturnPurchaseBinding.inflate(layoutInflater)
        val view = binding!!.root
        setContentView(view)

        val transactionId: Int = intent.getIntExtra(TRANSACTION_ID, AppConstants.MINUS_ONE)
        setupToolbar()

        mBinding.btnDone.setOnClickListener { onBackPressed() }

        mViewModel.getTransactionDetails(transactionId = transactionId).observe(this) {

            when (it) {
                is Resource.Failure -> {
                    mProgressDialog.hide()
                    val errorDialog = ErrorDialog.getInstance(message = it.errorMsg)
                    errorDialog.show(supportFragmentManager, errorDialog.tag)
                }

                Resource.Loading -> {
                    mProgressDialog.show()
                }

                is Resource.Success -> {
                    mProgressDialog.hide()
                    Timber.d("Success -> ${it.value}")

                    mBinding.apply {

                        tvDateTime.text = it.value.created_at
                        tvAmount.text = "$" + it.value.amount_spent?.formatAsCurrency()
                        tvCashBack.text = "$" + it.value.cashback_amount?.formatAsCurrency()
//                        val qrCode = ImageUtils().decodeImageFromBase64(
//                            context = this@TransactionDetainForReturnPurchaseActivity,
//                            base64String = it.value.qr_image
//                        )
//                        mBinding.ivQrCode.setImageBitmap(qrCode)

                        val mWriter = MultiFormatWriter()
                        try {
                            val mMatrix =
                                mWriter.encode(it.value.qr_image, BarcodeFormat.QR_CODE, 1000, 1000)
                            val mEncoder = BarcodeEncoder()
                            val mBitmap = mEncoder.createBitmap(mMatrix)
                            mBinding.ivQrCode.setImageBitmap(mBitmap)
                        } catch (e: WriterException) {
                            e.printStackTrace()
                        }

                        Timber.d("Encoded data -> ${it.value.qr_image}")

                        val decryptedData = try {
                            AES().decodeQrContents(qrContents = it.value.qr_image.getEmptyIfNull())
                        } catch (ex: Exception) {
                            ""
                        }
                        Timber.d("Decoded data -> $decryptedData")

                        mProgressDialog.hide()

                        supportActionBar?.title = it.value.merchant_name

                    }
                }
            }

        }

    }

    fun setupToolbar() {
        setSupportActionBar(mBinding.toolbar.root)
        supportActionBar?.title = "Transaction Details"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        mBinding.toolbar.root.setNavigationOnClickListener { onBackPressed() }
    }

}