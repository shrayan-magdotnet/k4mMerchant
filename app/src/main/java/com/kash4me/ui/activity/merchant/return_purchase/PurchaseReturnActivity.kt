package com.kash4me.ui.activity.merchant.return_purchase

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.kash4me.R
import com.kash4me.data.models.merchant.purchase_return.ReturnPurchaseQr
import com.kash4me.databinding.ActivityPurchaseReturnBinding
import com.kash4me.ui.dialog.ErrorDialog
import com.kash4me.utils.AppConstants
import com.kash4me.utils.custom_views.CustomProgressDialog
import com.kash4me.utils.extensions.formatAsCurrency
import com.kash4me.utils.extensions.getAmount
import com.kash4me.utils.extensions.getZeroIfNull
import com.kash4me.utils.extensions.toLong
import com.kash4me.utils.parcelable
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class PurchaseReturnActivity : AppCompatActivity() {

    private var binding: ActivityPurchaseReturnBinding? = null
    private val mBinding get() = binding!!

    companion object {

        private const val RETURN_PURCHASE_RESPONSE = "return_purchase_response"

        fun getNewIntent(activity: AppCompatActivity, response: ReturnPurchaseQr): Intent {

            val intent = Intent(activity, PurchaseReturnActivity::class.java)
            intent.putExtra(RETURN_PURCHASE_RESPONSE, response)
            return intent

        }

    }

    private val mProgressDialog by lazy { CustomProgressDialog(this) }

    val response: ReturnPurchaseQr? by lazy { intent.parcelable(RETURN_PURCHASE_RESPONSE) }

    private val mViewModel: PurchaseReturnViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPurchaseReturnBinding.inflate(layoutInflater)
        val view = binding!!.root
        setContentView(view)
        setupToolbar()

        Timber.d("Return purchase response -> $response")

        supportActionBar?.subtitle = response?.txnId.toString()

        mBinding.apply {
            setData(response)
            btnReturnListener(response)
        }

    }

    private fun ActivityPurchaseReturnBinding.setData(response: ReturnPurchaseQr?) {
        tvCustomerName.text = response?.customerName
        tvCustomerCode.text = response?.customerUniqueId

        val purchaseAmount = response?.purchaseAmount?.toDoubleOrNull().toLong()
            .getZeroIfNull()
        etPurchaseAmount.setValue(purchaseAmount)
        val cashbackAmount = "$" + response?.cashbackAmount?.formatAsCurrency()
        tvCashbackAmount.text = cashbackAmount

        tvTransactionDate.text = response?.txnDate
    }

    private fun ActivityPurchaseReturnBinding.btnReturnListener(response: ReturnPurchaseQr?) {
        btnNext.setOnClickListener {

            val amountToBeReturned = mBinding.etPurchaseAmount.getAmount()
            Timber.d("Purchase amount from input field -> $amountToBeReturned")

            val amountFromQr = response?.purchaseAmount?.toDoubleOrNull().getZeroIfNull()
            Timber.d("Purchase amount from QR -> $amountFromQr")

            if (amountToBeReturned < AppConstants.MIN_TRANSACTION_AMOUNT) {
                val errorDialog = ErrorDialog.getInstance(
                    message = "Please enter amount greater than ${AppConstants.MIN_TRANSACTION_AMOUNT}"
                )
                errorDialog.show(supportFragmentManager, errorDialog.tag)
                return@setOnClickListener
            }

            if (amountToBeReturned > amountFromQr) {
                val errorDialog = ErrorDialog.getInstance(
                    message = "Please enter amount less than the purchase amount"
                )
                errorDialog.show(supportFragmentManager, errorDialog.tag)
                return@setOnClickListener
            }

            val intent = PurchaseReturnConfirmationActivity.getNewIntent(
                activity = this@PurchaseReturnActivity,
                amountToBeReturned = amountToBeReturned,
                returnPurchaseQr = response
            )
            startActivity(intent)
//            showConfirmationDialog(amountToBeReturned, response)

        }
    }

    private fun showConfirmationDialog(
        amountToBeReturned: Double,
        response: ReturnPurchaseQr?
    ) {
        AlertDialog.Builder(this@PurchaseReturnActivity)
            .setTitle("Return Purchase?")
            .setMessage("Are you sure you are going to Accept return $$amountToBeReturned from ${response?.customerName}")
            .setPositiveButton(R.string.yes) { dialog, _ ->

                //                    returnPurchase(
                //                        response = response,
                //                        amountToReturn = amountToBeReturned.toString()
                //                    )
                // TODO: Calculate cashback value here


                dialog.dismiss()

            }
            .setNegativeButton(R.string.no) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    fun setupToolbar() {
        setSupportActionBar(mBinding.toolbar.root)
        supportActionBar?.setTitle(R.string.purchase_return)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        mBinding.toolbar.root.setNavigationOnClickListener { onBackPressed() }
    }

}