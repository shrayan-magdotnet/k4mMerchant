package com.kash4me.ui.activity.calculate_cashback

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.ViewModelProvider
import com.kash4me.data.models.CashBackSuccessResponse
import com.kash4me.data.models.QRResponse
import com.kash4me.data.models.user.UserType
import com.kash4me.merchant.R
import com.kash4me.merchant.databinding.ActivityCalculateCashbackBinding
import com.kash4me.network.ApiServices
import com.kash4me.network.NetworkConnectionInterceptor
import com.kash4me.network.NotFoundInterceptor
import com.kash4me.repository.CashBackRepository
import com.kash4me.ui.activity.customer.final_request_qr.FinalRequestQRActivity
import com.kash4me.ui.activity.merchant.cashback_success.MerchantCashBackSuccessActivity
import com.kash4me.utils.SessionManager
import com.kash4me.utils.custom_views.CustomProgressDialog
import com.kash4me.utils.extensions.getAmount
import com.kash4me.utils.extensions.getZeroIfNull
import com.kash4me.utils.extensions.showToast
import com.kash4me.utils.extensions.toLong
import com.kash4me.utils.toast
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class CalculateCashBackActivity : AppCompatActivity() {

    private lateinit var viewModel: CalculateCashBackViewModel

    private lateinit var sendBtn: Button
    private lateinit var goBackIV: ImageView

    @Inject
    lateinit var sessionManager: SessionManager

    private val progressDialog by lazy { CustomProgressDialog(this) }

    private var requestFromCustomer = false
    private var finalQR = false

    companion object {

        private var qrCodeResponse: QRResponse? = null

        fun newIntent(activity: AppCompatActivity, qrResponse: QRResponse?): Intent {
            val intent = Intent(activity, CalculateCashBackActivity::class.java)
            Timber.d("QR response -> $qrResponse")
            qrCodeResponse = qrResponse
            return intent
        }

    }

    private var binding: ActivityCalculateCashbackBinding? = null
    private val mBinding get() = binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCalculateCashbackBinding.inflate(layoutInflater)
        val view = binding!!.root
        setContentView(view)

        val bundle = intent?.extras
        setupToolbar()

        requestFromCustomer = intent.getBooleanExtra("requestFromCustomer", false)

//        if (bundle != null) {
//            intent.getStringExtra(QR_RESPONSE)?.let {
//                qrResponse = Gson().fromJson(it, QRResponse::class.java)
//            }
//
//        }

        mBinding.tvCustomerName.text = qrCodeResponse?.customerName
        mBinding.tvCustomerCode.text = qrCodeResponse?.customerUniqueId

        finalQR = qrCodeResponse?.finalQR ?: false
        Timber.d("QR code -> $qrCodeResponse")

        initUI()

        if (!requestFromCustomer) {
            initViewModel()
        }

        val purchaseAmount = qrCodeResponse?.purchaseAmountInt
        val purchaseAmountStr =
            qrCodeResponse?.purchaseAmount?.toDoubleOrNull().toLong().getZeroIfNull()
        Timber.d("Purchase amount -> $purchaseAmount")
        Timber.d("Purchase amount String -> $purchaseAmountStr")

        mBinding.etCashbackAmount.setValue(purchaseAmountStr)

        mBinding.etCashbackAmount.doAfterTextChanged {
            Timber.d("String -> " + it.toString())
            val amount = mBinding.etCashbackAmount.getAmount()
        }

    }

    private fun setupToolbar() {
        setSupportActionBar(mBinding.toolbarLayout.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Assign Cash Back"
        mBinding.toolbarLayout.toolbar.setNavigationOnClickListener { onBackPressed() }
    }

    private fun initViewModel() {

        val apiInterface =
            ApiServices.invoke(
                NetworkConnectionInterceptor(applicationContext),
                NotFoundInterceptor()
            )

        val sessionManager = SessionManager(this)
        val cashBackRepository = CashBackRepository(apiInterface, sessionManager)

        viewModel = ViewModelProvider(
            this,
            CalculateCashBackViewModelFactory(cashBackRepository)
        )[CalculateCashBackViewModel::class.java]

        viewModel.cashBackResponse.observe(this) {
            mBinding.cashBackValueTV.setValue(
                it.total_cashback_amount.toDoubleOrNull().toLong().getZeroIfNull()
            )
        }

        viewModel.cashBackSuccessResponse.observe(this) {
            progressDialog.hide()
            goToSuccessActivity(it)
            finish()
        }
    }

    private fun goToSuccessActivity(cashBackSuccessResponse: CashBackSuccessResponse) {
        val intent = MerchantCashBackSuccessActivity.getNewIntent(
            activity = this,
            successResponse = cashBackSuccessResponse

        )
        startActivity(intent)
    }


    private fun initUI() {
        sendBtn = findViewById(R.id.sendBtn)
        goBackIV = findViewById(R.id.goBackIV)

        if (requestFromCustomer) {
            sendBtn.text = "Request"
        } else {
            if (qrCodeResponse?.purchaseAmount != "0.0") {
//                enterPurchaseAmtTV.editText?.setText(qrResponse.purchaseAmount)
                qrCodeResponse?.purchaseAmount?.toLongOrNull()?.let { purchaseAmount ->
                    mBinding.etCashbackAmount.setValue(purchaseAmount)
                }
                initViewModel()
                getCashBackAmount()
            }
        }


        initOnClick()
    }

    private fun initOnClick() {


        goBackIV.setOnClickListener {
            onBackPressed()
        }

        if (sessionManager.fetchUserType() == UserType.STAFF) {

            mBinding.sendBtn.setText(R.string.next)

        } else if (sessionManager.fetchUserType() == UserType.MERCHANT) {

            mBinding.sendBtn.setText(R.string.send)

        }

        sendBtn.setOnClickListener {

            if (requestFromCustomer) {

                if (mBinding.etCashbackAmount.getAmount() < 0.1) {
                    showToast("Ensure this value is greater than or equal to 0.1")
                    return@setOnClickListener
                }

//                qrResponse.purchaseAmount = enterPurchaseAmtTV.editText?.text.toString()
                qrCodeResponse?.purchaseAmount = mBinding.etCashbackAmount.getAmount().toString()
                qrCodeResponse?.purchaseAmountInt = mBinding.etCashbackAmount.getAmount()
                val intent = Intent(applicationContext, FinalRequestQRActivity::class.java)
                intent.putExtra("qrResponse", qrCodeResponse)
                startActivity(intent)

            } else {

//                if (enterPurchaseAmtTV.editText?.text.isNullOrEmpty()) {
//                    toast("Please enter valid amount")
//                    return@setOnClickListener
//                }

                if (mBinding.etCashbackAmount.getAmount() == 0.0) {
                    toast("Please enter an amount")
                    return@setOnClickListener
                }

//
                if (mBinding.etCashbackAmount.getAmount().isNaN()) {
                    toast("Please enter valid amount")
                    return@setOnClickListener
                }

                val userParams = HashMap<String, Any>()
                userParams["shop_id"] = qrCodeResponse?.merchantId.getZeroIfNull()
                userParams["customer"] = qrCodeResponse?.customerId.getZeroIfNull()
                userParams["cashback_settings"] =
                    qrCodeResponse?.activeCashbackSettingsId.getZeroIfNull()
//                userParams["amount_spent"] =
//                    "%.2f".format(enterPurchaseAmtTV.editText?.text.toString().toDouble())
                userParams["amount_spent"] = mBinding.etCashbackAmount.getAmount()

                userParams["cashback_amount"] = mBinding.cashBackValueTV.getAmount()

                if (sessionManager.fetchUserType() == UserType.STAFF) {
                    val intent = ConfirmAssignCashbackActivity.getNewIntent(
                        activity = this,
                        qrResponse = qrCodeResponse,
                        request = userParams,
                        purchaseAmount = mBinding.etCashbackAmount.getAmount(),
                        cashbackAmount = mBinding.cashBackValueTV.getAmount()
                    )
                    startActivity(intent)
                } else {
                    val intent = ConfirmAssignCashbackActivity.getNewIntent(
                        activity = this,
                        qrResponse = qrCodeResponse,
                        request = userParams,
                        purchaseAmount = mBinding.etCashbackAmount.getAmount(),
                        cashbackAmount = mBinding.cashBackValueTV.getAmount()
                    )
                    startActivity(intent)
//                    val token = sessionManager.fetchAuthToken().toString()
//                    progressDialog.show()
//                    viewModel.createCashBackTransaction(
//                        qrCodeResponse?.merchantId.getZeroIfNull(),
//                        token,
//                        userParams
//                    )
                }


            }

        }

//        Timber.d("requestFromCustomer -> $requestFromCustomer")
//        if (!requestFromCustomer) {
//            enterPurchaseAmtTV.editText?.doAfterTextChanged { text ->
//                Log.d("CalculateCashBackActivity", "initOnClick: ${text.toString()}")
//                if (text.toString().isNotEmpty()) {
//                    getCashBackAmount()
//                } else {
//                    setCashBackAmount()
//                }
//            }
//        }

        Timber.d("requestFromCustomer -> $requestFromCustomer")
        if (!requestFromCustomer) {
//            mBinding.etCashbackAmount.doAfterTextChanged { text ->
//                Timber.d("initOnClick: ${mBinding.etCashbackAmount.getAmount()}")
//                if (mBinding.etCashbackAmount.getAmount().isNaN()) {
//                    setCashBackAmount()
//                } else {
//                    getCashBackAmount()
//                }
//            }
        }
    }

    private fun getCashBackAmount() {

//        if (enterPurchaseAmtTV.editText?.text.toString().isNotEmpty()) {
//
//            val userParams = HashMap<String, Any>()
//            userParams["shop_id"] = qrResponse.merchantId
//            userParams["customer"] = qrResponse.customerId
//            userParams["cashback_settings"] = qrResponse.activeCashBackSetting.id
//            userParams["amount_spent"] =
//                "%.2f".format(enterPurchaseAmtTV.editText?.text.toString().toDouble())
//
//            val token = sessionManager.fetchAuthToken().toString()
//            viewModel.getCashBackInfo(token, userParams)
//        } else {
//            setCashBackAmount()
//
//        }

        if (mBinding.etCashbackAmount.getAmount().toString().isNotEmpty()) {

            val userParams = HashMap<String, Any>()
            userParams["shop_id"] = qrCodeResponse?.merchantId.getZeroIfNull()
            userParams["customer"] = qrCodeResponse?.customerId.getZeroIfNull()
            userParams["cashback_settings"] =
                qrCodeResponse?.activeCashbackSettingsId.getZeroIfNull()
            userParams["amount_spent"] = mBinding.etCashbackAmount.getAmount()

            val token = sessionManager.fetchAuthToken().toString()

            Timber.d("Request -> $userParams")
            viewModel.getCashBackInfo(token, userParams)
        } else {
            setCashBackAmount()

        }
    }

    private fun setCashBackAmount(amount: String = "0") {
        mBinding.cashBackValueTV.setValue(amount.toDoubleOrNull().toLong().getZeroIfNull())
    }

}