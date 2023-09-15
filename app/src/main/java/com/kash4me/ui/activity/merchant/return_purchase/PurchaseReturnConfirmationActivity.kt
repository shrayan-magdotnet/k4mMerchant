package com.kash4me.ui.activity.merchant.return_purchase

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.kash4me.data.models.merchant.purchase_return.ReturnPurchaseQr
import com.kash4me.merchant.R
import com.kash4me.merchant.databinding.ActivityPurchaseReturnConfirmationBinding
import com.kash4me.ui.dialog.ErrorDialog
import com.kash4me.utils.custom_views.CustomProgressDialog
import com.kash4me.utils.extensions.formatUsingCurrencySystem
import com.kash4me.utils.extensions.getEmptyIfNull
import com.kash4me.utils.extensions.getMinusOneIfNull
import com.kash4me.utils.extensions.getZeroIfNull
import com.kash4me.utils.extensions.showToast
import com.kash4me.utils.network.Resource
import com.kash4me.utils.parcelable
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class PurchaseReturnConfirmationActivity : AppCompatActivity() {

    private var binding: ActivityPurchaseReturnConfirmationBinding? = null
    private val mBinding get() = binding!!

    private var cashbackAmount: String? = null

    companion object {

        private const val AMOUNT_TO_BE_RETURNED = "amount_to_be_returned"
        private const val QR_CODE = "qr_code"

        fun getNewIntent(
            activity: AppCompatActivity,
            amountToBeReturned: Double,
            returnPurchaseQr: ReturnPurchaseQr?
        ): Intent {
            val intent = Intent(activity, PurchaseReturnConfirmationActivity::class.java)
            intent.putExtra(AMOUNT_TO_BE_RETURNED, amountToBeReturned)
            intent.putExtra(QR_CODE, returnPurchaseQr)
            return intent
        }

    }

    private val mProgressDialog by lazy { CustomProgressDialog(this) }

    private val amountToBeReturned by lazy { intent.getDoubleExtra(AMOUNT_TO_BE_RETURNED, 0.0) }
    private val qrCode by lazy { intent.parcelable<ReturnPurchaseQr>(QR_CODE) }

    private val mViewModel: PurchaseReturnViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPurchaseReturnConfirmationBinding.inflate(layoutInflater)
        val view = binding!!.root
        setContentView(view)
        setupToolbar()

        Timber.d("Amount to be returned -> $amountToBeReturned")
        Timber.d("QR code -> $qrCode")

        supportActionBar?.subtitle = qrCode?.txnId.toString()

        mBinding.apply {
            setData(qrCode)
            btnConfirmListener(qrCode)
        }

    }

    fun setupToolbar() {
        setSupportActionBar(mBinding.toolbar.root)
        supportActionBar?.setTitle(R.string.purchase_return)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        mBinding.toolbar.root.setNavigationOnClickListener { onBackPressed() }
    }

    private fun ActivityPurchaseReturnConfirmationBinding.setData(returnPurchaseQr: ReturnPurchaseQr?) {
        tvCustomerName.text = returnPurchaseQr?.customerName
        tvCustomerCode.text = returnPurchaseQr?.customerUniqueId

        val formattedPurchaseAmount = "$$amountToBeReturned"
        tvPurchaseAmount.text = formattedPurchaseAmount

        calculateCashbackAmount(returnPurchaseQr)

        tvTransactionDate.text = returnPurchaseQr?.txnDate

    }

    private fun calculateCashbackAmount(qrContents: ReturnPurchaseQr?) {
        mViewModel.calculateCashbackAmount(
            purchaseAmount = intent.getDoubleExtra(AMOUNT_TO_BE_RETURNED, 0.0).toString(),
            cashbackSettingsId = qrContents?.activeCashbackSettingsId.getZeroIfNull(),
            customerId = qrContents?.customerId.getZeroIfNull(),
            merchantShopId = qrContents?.merchantId.getZeroIfNull()
        ).observe(this) { response ->

            when (response) {
                is Resource.Failure -> {
                    mProgressDialog.hide()
                    val errorDialog = ErrorDialog.getInstance(message = response.errorMsg)
                    errorDialog.show(supportFragmentManager, errorDialog.tag)
                }

                Resource.Loading -> {
                    mProgressDialog.show()
                }

                is Resource.Success -> {
                    mProgressDialog.hide()

                    this.cashbackAmount = response.value.total_cashback_amount
                    if (this.cashbackAmount == null) {
                        showToast("An error occurred while trying to calculate cashback amount")
                        mBinding.tvCashbackAmount.setText(R.string.not_available_short_form)
                    } else {
                        val cashbackAmount = "$" + response.value.total_cashback_amount
                            .toDoubleOrNull()?.formatUsingCurrencySystem()
                        mBinding.tvCashbackAmount.text = cashbackAmount
                        this.cashbackAmount = response.value.total_cashback_amount
                    }

                }
            }

        }
    }

    private fun ActivityPurchaseReturnConfirmationBinding.btnConfirmListener(response: ReturnPurchaseQr?) {
        btnConfirm.setOnClickListener {

            val amountToBeReturned = intent.getDoubleExtra(AMOUNT_TO_BE_RETURNED, 0.0)

            AlertDialog.Builder(this@PurchaseReturnConfirmationActivity)
                .setTitle("Confirm Return Purchase?")
                .setMessage("Are you sure you want to accept returning $$amountToBeReturned to ${response?.customerName}")
                .setPositiveButton(R.string.yes) { dialog, _ ->

                    returnPurchase(
                        response = response,
                        amountToReturn = amountToBeReturned.toString()
                    )
                    dialog.dismiss()

                }
                .setNegativeButton(R.string.no) { dialog, _ ->
                    dialog.dismiss()
                }
                .show()

        }
    }

    private fun returnPurchase(response: ReturnPurchaseQr?, amountToReturn: String) {
        mViewModel.returnPurchase(
            transactionId = response?.txnId.getMinusOneIfNull(),
            amountToReturn = amountToReturn,
            qrToken = qrCode?.qrToken.getEmptyIfNull()
        ).observe(this) {

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
//                    val successDialog = SuccessDialog.getInstance(
//                        message = it.value.message.getEmptyIfNull()
//                    )
//                    successDialog.show(supportFragmentManager, successDialog.tag)
                    val intent = PurchaseReturnSuccessActivity.getNewIntent(
                        activity = this,
                        message = it.value.message.getEmptyIfNull(),
                        returnedAmount = amountToBeReturned
                    )
                    startActivity(intent)
                    finish()
                    mBinding.btnConfirm.isEnabled = false
                    mBinding.tvPurchaseAmount.isEnabled = false
                }

                else -> {
                    // Do nothing
                }
            }

        }
    }


}