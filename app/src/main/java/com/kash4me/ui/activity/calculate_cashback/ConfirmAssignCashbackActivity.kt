package com.kash4me.ui.activity.calculate_cashback

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.kash4me.R
import com.kash4me.data.models.CashBackSuccessResponse
import com.kash4me.data.models.QRResponse
import com.kash4me.databinding.ActivityConfirmAssignCashbackBinding
import com.kash4me.network.ApiServices
import com.kash4me.network.NetworkConnectionInterceptor
import com.kash4me.network.NotFoundInterceptor
import com.kash4me.repository.CashBackRepository
import com.kash4me.ui.activity.merchant.cashback_success.MerchantCashBackSuccessActivity
import com.kash4me.ui.dialog.ErrorDialog
import com.kash4me.utils.AppConstants
import com.kash4me.utils.SessionManager
import com.kash4me.utils.custom_views.CustomProgressDialog
import com.kash4me.utils.extensions.formatUsingCurrencySystem
import com.kash4me.utils.extensions.getEmptyIfNull
import com.kash4me.utils.extensions.getZeroIfNull
import com.kash4me.utils.extensions.showToast
import com.kash4me.utils.parcelable
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class ConfirmAssignCashbackActivity : AppCompatActivity() {

    private var binding: ActivityConfirmAssignCashbackBinding? = null
    private val mBinding get() = binding!!

    private val qrResponse: QRResponse? by lazy { intent.parcelable(QR_RESPONSE) }

    private val mProgressDialog by lazy { CustomProgressDialog(context = this) }

    @Inject
    lateinit var mSessionManager: SessionManager

    companion object {

        private const val PURCHASE_AMOUNT = "purchase_amount"
        private const val CASHBACK_AMOUNT = "cashback_amount"

        private const val QR_RESPONSE = "qr_response"

        private var mRequest: HashMap<String, Any>? = null

        fun getNewIntent(
            activity: AppCompatActivity,
            purchaseAmount: Double,
            cashbackAmount: Double,
            qrResponse: QRResponse?,
            request: HashMap<String, Any>
        ): Intent {

            val intent = Intent(activity, ConfirmAssignCashbackActivity::class.java)
            intent.putExtra(PURCHASE_AMOUNT, purchaseAmount)
            intent.putExtra(CASHBACK_AMOUNT, cashbackAmount)
            intent.putExtra(QR_RESPONSE, qrResponse)
            mRequest = request
            return intent

        }

    }

    private val viewModel: CalculateCashBackViewModel by lazy {
        val apiInterface =
            ApiServices.invoke(
                NetworkConnectionInterceptor(applicationContext),
                NotFoundInterceptor()
            )

        val sessionManager = SessionManager(this)
        val cashBackRepository = CashBackRepository(apiInterface, sessionManager)

        ViewModelProvider(
            this,
            CalculateCashBackViewModelFactory(cashBackRepository)
        )[CalculateCashBackViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConfirmAssignCashbackBinding.inflate(layoutInflater)
        val view = binding!!.root
        setContentView(view)

        mBinding.apply {

            setData()
            btnConfirmListener()

        }

        setupToolbar()

        calculateCashbackAmount()

        viewModel.cashBackResponse.observe(this) {
            mProgressDialog.hide()
            mBinding.tvCashbackAmount.text = "$" + it.total_cashback_amount
        }

        viewModel.cashBackSuccessResponse.observe(this) {
            mProgressDialog.hide()
            goToSuccessActivity(it)
        }

        viewModel.errorMessage.observe(this) {
            val errorDialog = ErrorDialog.getInstance(message = it)
            errorDialog.show(supportFragmentManager, errorDialog.tag)
            mProgressDialog.hide()
        }

    }

    fun setupToolbar() {
        setSupportActionBar(mBinding.toolbar.root)
        supportActionBar?.setTitle(R.string.confirm_assign_cashback)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        mBinding.toolbar.root.setNavigationOnClickListener { onBackPressed() }
    }

    private fun goToSuccessActivity(cashBackSuccessResponse: CashBackSuccessResponse) {
        val intent = MerchantCashBackSuccessActivity.getNewIntent(
            activity = this,
            successResponse = cashBackSuccessResponse
        )
        startActivity(intent)
    }

    private fun ActivityConfirmAssignCashbackBinding.btnConfirmListener() {
        btnConfirm.setOnClickListener {

            val request = HashMap<String, Any>()

            request["shop_id"] = qrResponse?.merchantId.getZeroIfNull()
            request["customer"] = qrResponse?.customerId.getZeroIfNull()
            request["cashback_settings"] = qrResponse?.activeCashbackSettingsId.getZeroIfNull()
            request["amount_spent"] = intent.getDoubleExtra(PURCHASE_AMOUNT, 0.0).toString()
            request["cashback_amount"] = intent.getDoubleExtra(CASHBACK_AMOUNT, 0.0).toString()
            request["qr_token"] = qrResponse?.qrToken.getEmptyIfNull()

            mProgressDialog.show()

            viewModel.createCashBackTransaction(
                merchantShopID = qrResponse?.merchantId.getZeroIfNull(),
                token = mSessionManager.fetchAuthToken().getEmptyIfNull(),
                userParams = request
            )

        }
    }

    private fun ActivityConfirmAssignCashbackBinding.setData() {

        tvDescription.text =
            "Please confirm the below information are correct for ${qrResponse?.customerName} (${qrResponse?.customerUniqueId})"

        tvPurchaseAmount.text = "$" + intent.getDoubleExtra(PURCHASE_AMOUNT, 0.0)
        tvCashbackAmount.text = "$" + intent.getDoubleExtra(CASHBACK_AMOUNT, 0.0)
    }

    private fun calculateCashbackAmount() {

        val purchaseAmount = intent.getDoubleExtra(PURCHASE_AMOUNT, 0.0)
        if (purchaseAmount <= AppConstants.MIN_TRANSACTION_AMOUNT) {
            showToast("Couldn't calculate cashback amount for the provided purchase amount $${purchaseAmount.formatUsingCurrencySystem()}")
            return
        }

        val userParams = HashMap<String, Any>()
        userParams["shop_id"] = qrResponse?.merchantId.getZeroIfNull()
        userParams["customer"] = qrResponse?.customerId.getZeroIfNull()
        userParams["cashback_settings"] = qrResponse?.activeCashbackSettingsId.getZeroIfNull()
        userParams["amount_spent"] = purchaseAmount

        val token = mSessionManager.fetchAuthToken().toString()

        Timber.d("Request -> $userParams")

        mProgressDialog.show()
        viewModel.getCashBackInfo(token, userParams)

    }


}