package com.kash4me.ui.activity.merchant.accept_kash4me_payment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.kash4me.data.models.ActiveCashbackSettings
import com.kash4me.data.models.customer.pay_by_kash4me.PayByKash4meQr
import com.kash4me.merchant.R
import com.kash4me.merchant.databinding.FragmentAcceptKash4mePaymentBinding
import com.kash4me.ui.activity.payment_gateway.PaymentSettingsViewModel
import com.kash4me.ui.dialog.ErrorDialog
import com.kash4me.utils.FeeType
import com.kash4me.utils.SessionManager
import com.kash4me.utils.custom_views.CustomProgressDialog
import com.kash4me.utils.extensions.formatUsingCurrencySystem
import com.kash4me.utils.extensions.getEmptyIfNull
import com.kash4me.utils.extensions.getZeroIfNull
import com.kash4me.utils.extensions.roundOffToTwoDecimalDigits
import com.kash4me.utils.extensions.showToast
import com.kash4me.utils.network.Resource
import com.kash4me.utils.parcelable
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class AcceptKash4mePaymentFragment : Fragment() {

    private var _binding: FragmentAcceptKash4mePaymentBinding? = null
    private val mBinding get() = _binding!!

    private val payByKash4meQr: PayByKash4meQr? by lazy {
        activity?.intent?.parcelable(AcceptKash4mePaymentActivity.QR_RESPONSE)
    }

    private var cashbackAmount: String? = null
    private var transactionFee: Double = 0.0

    private val mProgressDialog by lazy { CustomProgressDialog(requireContext()) }

    @Inject
    lateinit var sessionManager: SessionManager

    private val mPaymentSettingsViewModel: PaymentSettingsViewModel by viewModels()
    private val mViewModel: AcceptKash4mePaymentViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAcceptKash4mePaymentBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()

        Timber.d("PayByKash4me QR -> $payByKash4meQr")

        getFeeSettings()
        fetchActiveCashbackSettings()

        mBinding.apply {
            tvCustomerName.text = payByKash4meQr?.customerName
            tvCustomerCode.text = payByKash4meQr?.customerUniqueId

            tvPurchaseAmount.text =
                "$" + payByKash4meQr?.purchaseAmount?.toDoubleOrNull()?.formatUsingCurrencySystem()

            tvTransactionDate.text = payByKash4meQr?.transactionDate
        }

        mBinding.btnAccept.setOnClickListener {

            if (cashbackAmount.isNullOrBlank()) {
                showToast("Couldn't retrieve cashback amount, please scan again")
                return@setOnClickListener
            }

            acceptKash4mePayment()

        }

    }

    private fun fetchActiveCashbackSettings() {
        mViewModel.fetchActiveCashbackSettings(
            merchantShopId = sessionManager.fetchMerchantDetails()?.id.getZeroIfNull()
        ).observe(viewLifecycleOwner) {

            when (it) {
                is Resource.Failure -> {
                    val errorDialog = ErrorDialog.getInstance(message = it.errorMsg)
                    errorDialog.show(activity?.supportFragmentManager!!, errorDialog.tag)
                }

                Resource.Loading -> {
                }

                is Resource.Success -> {
                    Timber.d("Success -> ${it.value}")
                    calculateCashbackAmount(it)
                }
            }

        }
    }

    private fun calculateCashbackAmount(it: Resource.Success<ActiveCashbackSettings>) {
        mViewModel.calculateCashbackAmount(
            purchaseAmount = payByKash4meQr?.purchaseAmount.getEmptyIfNull(),
            cashbackSettingsId = it.value.id,
            customerId = payByKash4meQr?.customerId.getZeroIfNull(),
            merchantShopId = sessionManager.fetchMerchantDetails()?.id.getZeroIfNull()
        ).observe(viewLifecycleOwner) { response ->

            when (response) {
                is Resource.Failure -> {
                    mProgressDialog.hide()
                    val errorDialog = ErrorDialog.getInstance(
                        message = response.errorMsg
                    )
                    errorDialog.show(
                        activity?.supportFragmentManager!!, errorDialog.tag
                    )
                }

                Resource.Loading -> {
                    mProgressDialog.show()
                }

                is Resource.Success -> {
                    mProgressDialog.hide()
                    val cashbackAmount = "$" + response.value.total_cashback_amount
                        .toDoubleOrNull()?.formatUsingCurrencySystem()
                    mBinding.tvCashbackAmount.text = cashbackAmount
                    this.cashbackAmount = response.value.total_cashback_amount
                }
            }

        }
    }

//    private fun calculateCashbackAmount(purchaseAmount: Double) {
//
//        val userParams = HashMap<String, Any>()
//
//        val shopId = sessionManager.fetchMerchantDetails()?.id
//        userParams["shop_id"] = shopId
//        userParams["customer"] = payByKash4meQr?.customerId
//        userParams["cashback_settings"] = qrResponse?.activeCashbackSettingsId.getZeroIfNull()
//        userParams["amount_spent"] = purchaseAmount
//
//        val token = mSessionManager.fetchAuthToken().toString()
//
//        Timber.d("Request -> $userParams")
//
//        mProgressDialog.show()
//        viewModel.getCashBackInfo(token, userParams)
//
//    }

    private fun acceptKash4mePayment() {
        mViewModel.acceptKash4mePayment(
            customerId = payByKash4meQr?.customerId,
            cashbackAmount = cashbackAmount.getEmptyIfNull(),
            transactionFee = payByKash4meQr?.transactionFee,
            purchaseAmount = payByKash4meQr?.purchaseAmount.getEmptyIfNull(),
            qrToken = payByKash4meQr?.qrToken
        ).observe(viewLifecycleOwner) {

            when (it) {
                is Resource.Failure -> {
                    mProgressDialog.hide()
                    val errorDialog = ErrorDialog.getInstance(message = it.errorMsg)
                    errorDialog.show(activity?.supportFragmentManager!!, errorDialog.tag)
                }

                Resource.Loading -> {
                    mProgressDialog.show()
                }

                is Resource.Success -> {
                    mProgressDialog.hide()
                    Timber.d("Success -> ${it.value}")
                    val action = AcceptKash4mePaymentFragmentDirections
                        .actionAcceptKash4mePaymentFragmentToCompleteAcceptKash4mePaymentFragment(
                            acceptPaymentResponse = it.value,
                            actualAmount = payByKash4meQr?.purchaseAmount.getEmptyIfNull()
                        )
                    findNavController().navigate(action)
                }
            }

        }
    }

    private fun getFeeSettings() {
        mPaymentSettingsViewModel.getFeeSettings(feeType = FeeType.PAY_BY_KASH4ME)
            .observe(viewLifecycleOwner) {

                when (it) {
                    is Resource.Failure -> {
                        mProgressDialog.hide()
                        val errorDialog = ErrorDialog.getInstance(message = it.errorMsg)
                        errorDialog.show(activity?.supportFragmentManager!!, errorDialog.tag)
                    }

                    Resource.Loading -> {
                        mProgressDialog.show()
                    }

                    is Resource.Success -> {
                        mProgressDialog.hide()
                        Timber.d("Success -> ${it.value}")

                        val commissionPercentage =
                            it.value.commissionPercentage?.toDoubleOrNull().getZeroIfNull()
                        val purchaseAmount =
                            payByKash4meQr?.purchaseAmount?.toDoubleOrNull().getZeroIfNull()
                        transactionFee = ((commissionPercentage / 100.0) * purchaseAmount)
                            .roundOffToTwoDecimalDigits()

                        val transactionFeeDescription =
                            "A transaction fee of ${commissionPercentage}% ($${transactionFee}) will be applied and will be deducted from the purchase amount."

                        mBinding.tvTransactionFeeDescription.text = transactionFeeDescription

                    }
                }

            }
    }

    private fun setupToolbar() {
        mBinding.toolbar.toolbar.apply {
            title = "Payment Detail"
            navigationIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_back)
            setNavigationOnClickListener { activity?.finish() }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}