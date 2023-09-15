package com.kash4me.ui.activity.merchant.cashback_setting

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.ViewModelProvider
import com.kash4me.R
import com.kash4me.data.models.ActiveCashbackSettings
import com.kash4me.data.models.merchant.cashback.CashbackType
import com.kash4me.databinding.ActivityCashBackSettingBinding
import com.kash4me.network.ApiServices
import com.kash4me.network.NetworkConnectionInterceptor
import com.kash4me.network.NotFoundInterceptor
import com.kash4me.repository.CashBackRepository
import com.kash4me.repository.UserDetailsRepository
import com.kash4me.repository.UserRepository
import com.kash4me.ui.activity.merchant.merchant_profile.MerchantProfileViewModel
import com.kash4me.ui.activity.merchant.merchant_profile.MerchantProfileViewModelFactory
import com.kash4me.ui.dialog.ErrorDialog
import com.kash4me.ui.dialog.SuccessDialog
import com.kash4me.ui.fragments.merchant.registration.cashbackinfo.CashBackViewModel
import com.kash4me.ui.fragments.merchant.registration.cashbackinfo.CashBackViewModelFactory
import com.kash4me.utils.App
import com.kash4me.utils.SessionManager
import com.kash4me.utils.custom_views.CustomProgressDialog
import com.kash4me.utils.extensions.getAmount
import com.kash4me.utils.extensions.getEmptyIfNull
import com.kash4me.utils.extensions.showToast
import com.kash4me.utils.formatAmount
import com.kash4me.utils.listeners.AfterDismissalListener
import com.kash4me.utils.network.Resource
import com.kash4me.utils.toast
import timber.log.Timber

class CashBackSettingActivity : AppCompatActivity() {

    private var selectedRBId: Int = 0

    private lateinit var sessionManager: SessionManager
    private var merchantId: Int = 0

    private lateinit var mViewModel: CashBackViewModel
    private val mMerchantProfileViewModel: MerchantProfileViewModel by lazy {
        val apiInterface = ApiServices.invoke(
            NetworkConnectionInterceptor(App.getContext()!!), NotFoundInterceptor()
        )
        val userDetailsRepository = UserDetailsRepository(apiInterface)
        val userRepository = UserRepository(apiInterface)
        ViewModelProvider(
            this, MerchantProfileViewModelFactory(userDetailsRepository, userRepository)
        )[MerchantProfileViewModel::class.java]
    }

    var customDialogClass: CustomProgressDialog = CustomProgressDialog(this)

    private var binding: ActivityCashBackSettingBinding? = null
    private val mBinding get() = binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCashBackSettingBinding.inflate(layoutInflater)
        val view = binding!!.root
        setContentView(view)

        sessionManager = SessionManager(applicationContext)

        initUI()

        setupToolbar()
    }


    private fun setupToolbar() {
        setSupportActionBar(mBinding.toolbarLayout.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.cashback_settings)
        mBinding.toolbarLayout.toolbar.setNavigationOnClickListener { onBackPressed() }
    }


    private fun fetchActiveCashBackSettings() {
        customDialogClass.show()
        val token = sessionManager.fetchAuthToken().toString()
        mViewModel.getActiveCashBack(sessionManager.fetchMerchantDetails()?.id ?: 0, token)
    }


    private fun initUI() {

        mBinding.inputFieldsTextChangeListener()
        rgCashbackCheckChangeListener()
        updateBtnListener()
        initVM()

    }

    private fun ActivityCashBackSettingBinding.inputFieldsTextChangeListener() {
        etSpend.doAfterTextChanged { spend -> updateTvSpendDescription(spend) }
        etGet.doAfterTextChanged { get -> updateTvGetDescription(get) }
        percentET.editText?.doAfterTextChanged { percent -> updateTvPercentageDescription(percent) }
        etLimit.doAfterTextChanged { limit -> updateTvLimitDescription(limit = limit) }
    }

    private fun updateTvSpendDescription(text: CharSequence?) {
        val spend =
            if (text.isNullOrBlank()) {
                "XX"
            } else {
                mBinding.etSpend.text
            }
        val descriptionForSpend = resources.getString(
            R.string.customer_needs_to_spend_xx_amount_to_get_cash_back, spend
        )
        mBinding.tvSpendDescription.text = descriptionForSpend
    }

    private fun updateTvGetDescription(text: CharSequence?) {
        val get =
            if (text.isNullOrBlank()) {
                "XX"
            } else {
                mBinding.etGet.text
            }
        val descriptionForGet = resources.getString(
            R.string.customer_gets_xx_on_required_amount_spent, get
        )
        mBinding.tvGetDescription.text = descriptionForGet
    }

    private fun updateTvPercentageDescription(percent: CharSequence? = null) {
        val percentage =
            if (percent.isNullOrBlank()) {
                "XX"
            } else {
                percent
            }
        val percentageDescription = resources.getString(
            R.string.customer_earns_1_s_on_each_purchase_made_at_the_store,
            percentage
        )
        mBinding.tvPercentageDescription.text = percentageDescription
    }

    private fun updateTvLimitDescription(limit: CharSequence? = null) {
        val limitAmount =
            if (limit.isNullOrBlank()) {
                "XX"
            } else {
                mBinding.etLimit.text
            }
        val description = resources.getString(
            R.string.customer_need_earn_1_s_amount_to_be_able_to_withdraw_amount,
            limitAmount
        )
        mBinding.tvLimitDescription.text = description
    }

    private fun rgCashbackCheckChangeListener() {
        mBinding.cashBackRG.setOnCheckedChangeListener { _, i ->
            if (i == R.id.flatRadio) {
                mBinding.spendAndGetLL.visibility = View.VISIBLE
                mBinding.percentageAndLimitLL.visibility = View.GONE
            } else {
                mBinding.spendAndGetLL.visibility = View.GONE
                mBinding.percentageAndLimitLL.visibility = View.VISIBLE
            }

            selectedRBId = i

        }
    }

    private fun updateBtnListener() {
        mBinding.updateBtn.setOnClickListener {

            val userParams = HashMap<String, Any>()

            if (selectedRBId == R.id.percentageRadio) {
                userParams["cashback_type"] = CashbackType.PERCENTAGE.id

                if (mBinding.percentET.editText?.text.isNullOrBlank()) {
                    showToast(R.string.please_enter_percentage)
                    return@setOnClickListener
                }

                val cashBackAmount = mBinding.percentET.editText?.text.toString()
                if (cashBackAmount.toDouble() <= 100.0) {
                    userParams["cashback_amount"] = cashBackAmount
                } else {
                    toast(getString(R.string.percentage_must_be_less_than_100_percent))
                    return@setOnClickListener
                }

                if (mBinding.etLimit.getAmount().isNaN()) {
                    showToast(R.string.please_enter_limit_amount)
                    return@setOnClickListener
                }

                val limitAmount = mBinding.etLimit.getAmount()
                userParams["maturity_amount"] = limitAmount

            } else {

                val getAmount = mBinding.etGet.getAmount()
                val spendAmount = mBinding.etSpend.getAmount()
                if (getAmount > spendAmount) {
                    showToast(message = R.string.get_amount_cannot_exceed_spend_amount)
                    return@setOnClickListener
                }

                userParams["cashback_type"] = CashbackType.FLAT.id
                userParams["cashback_amount"] = getAmount.toString()
                userParams["maturity_amount"] = spendAmount.toString()

            }

            customDialogClass.show()

            val token = sessionManager.fetchAuthToken().toString()
            mViewModel.addCashBack(
                sessionManager.fetchMerchantDetails()?.id ?: 0,
                token,
                userParams
            )

        }
    }


    private fun initVM() {
        val apiInterface =
            ApiServices.invoke(
                NetworkConnectionInterceptor(applicationContext),
                NotFoundInterceptor()
            )

        val cashBackRepository = CashBackRepository(apiInterface, SessionManager(this))

        mViewModel = ViewModelProvider(
            this,
            CashBackViewModelFactory(cashBackRepository)
        )[CashBackViewModel::class.java]

        sessionManager = SessionManager(applicationContext)
        merchantId = sessionManager.fetchMerchantId()


        mViewModel.cashBackResponse.observe(this) {
            val successDialog = SuccessDialog.getInstance(
                message = getString(R.string.cash_back_settings_has_been_updated),
                afterDismissClicked = object : AfterDismissalListener {
                    override fun afterDismissed() {
                        updateMerchantDetailsInSharedPreferences()
                    }
                }
            )
            successDialog.show(supportFragmentManager, successDialog.tag)
            sessionManager.saveCBSettings(true)
            customDialogClass.hide()
        }

        mViewModel.errorMessage.observe(this) {
            toast(it.toString())
            val errorDialog = ErrorDialog.getInstance(message = it.toString())
            errorDialog.show(supportFragmentManager, errorDialog.tag)
            customDialogClass.hide()
        }

        mViewModel.activeCashbackSettings.observe(this) {
            setData(it)
            customDialogClass.hide()
        }

        fetchActiveCashBackSettings()

    }


    private fun setData(activeCashbackSettings: ActiveCashbackSettings) {
        Timber.d("Active cashback settings -> $activeCashbackSettings")
        if (activeCashbackSettings.cashback_type == 1) {
            mBinding.apply {
                cashBackRG.check(R.id.flatRadio)
                spendAndGetLL.visibility = View.VISIBLE
                percentageAndLimitLL.visibility = View.GONE
                val spentAmount: Long? = activeCashbackSettings.maturity_amount.toDoubleOrNull()
                    ?.times(100L)?.toLong()
                etSpend.setValue(spentAmount ?: 0L)
                val getAmount: Long? = activeCashbackSettings.cashback_amount.toDoubleOrNull()
                    ?.times(100L)?.toLong()
                etGet.setValue(getAmount ?: 0L)

                updateTvPercentageDescription(percent = null)
                updateTvLimitDescription(limit = null)
            }
        } else {
            mBinding.apply {
                cashBackRG.check(R.id.percentageRadio)
                spendAndGetLL.visibility = View.GONE
                percentageAndLimitLL.visibility = View.VISIBLE
                percentET.editText?.setText(activeCashbackSettings.cashback_percentage.formatAmount)
                val limitAmount: Long? =
                    activeCashbackSettings.maturity_amount.toDoubleOrNull()?.times(100L)?.toLong()
                etLimit.setValue(limitAmount ?: 0L)

                updateTvSpendDescription(text = null)
                updateTvGetDescription(text = null)
            }
        }

    }

    private fun updateMerchantDetailsInSharedPreferences() {

        val token = sessionManager.fetchAuthToken().getEmptyIfNull()
        mMerchantProfileViewModel.fetchMerchantDetails(token = token).observe(this) { resource ->

            when (resource) {

                is Resource.Failure -> {
                    customDialogClass.hide()
                    finish()
                }

                Resource.Loading -> {
                    customDialogClass.show()
                }

                is Resource.Success -> {
                    customDialogClass.hide()
                    sessionManager.saveMerchantDetails(merchantDetails = resource.value)
                    finish()
                }
            }

        }

    }

}