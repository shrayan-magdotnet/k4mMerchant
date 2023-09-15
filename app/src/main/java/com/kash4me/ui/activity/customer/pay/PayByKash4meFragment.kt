package com.kash4me.ui.activity.customer.pay

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.kash4me.databinding.FragmentPayByKash4meBinding
import com.kash4me.ui.activity.payment_gateway.PaymentSettingsViewModel
import com.kash4me.ui.dialog.ErrorDialog
import com.kash4me.utils.AppConstants
import com.kash4me.utils.FeeType
import com.kash4me.utils.SessionManager
import com.kash4me.utils.custom_views.CustomProgressDialog
import com.kash4me.utils.extensions.formatUsingCurrencySystem
import com.kash4me.utils.extensions.formatUsingNepaliCurrencySystem
import com.kash4me.utils.extensions.getAmount
import com.kash4me.utils.extensions.getZeroIfNull
import com.kash4me.utils.extensions.showToast
import com.kash4me.utils.network.Resource
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class PayByKash4meFragment : Fragment() {

    private var _binding: FragmentPayByKash4meBinding? = null
    private val mBinding get() = _binding!!

    private val remainingBalance: Double by lazy {
        activity?.intent?.getDoubleExtra(PayByKash4meActivity.REMAINING_BALANCE, 0.0)
            .getZeroIfNull()
    }
    private var availableBalance: Double = 0.0
    private var processingFee = 0.00

    private val mProgressDialog by lazy { CustomProgressDialog(requireContext()) }

    @Inject
    lateinit var sessionManager: SessionManager

    private val mViewModel: PaymentSettingsViewModel by viewModels()
    private val mPayByKash4meViewModel: PayByKash4meViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPayByKash4meBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as PayByKash4meActivity).supportActionBar?.title = ""

        getFeeSettings()

        mBinding.tvRemainingBalance.text = "$" + remainingBalance.formatUsingCurrencySystem()

        etPurchaseAmountListener()
        btnProceedToPayListener()

    }

    private fun etPurchaseAmountListener() {
        mBinding.etPurchaseAmount.doOnTextChanged { _, _, _, _ ->

            val amount = mBinding.etPurchaseAmount.getAmount()
            availableBalance = remainingBalance.getZeroIfNull() - amount
            mBinding.tvRemainingBalance.text =
                "$" + availableBalance?.formatUsingNepaliCurrencySystem()

        }
    }

    private fun btnProceedToPayListener() {
        mBinding.btnProceedToPay.setOnClickListener {
            val amountToPay = mBinding.etPurchaseAmount.getAmount()
            Timber.d("Amount to pay -> $amountToPay")
            val minimumAmount = AppConstants.MIN_AMOUNT_PAY_BY_KASH4ME + processingFee
            if (amountToPay < minimumAmount) {
                showToast("Amount should be greater than $${minimumAmount}")
            } else if (amountToPay > remainingBalance) {
                showToast("Amount should not be greater than $$remainingBalance")
            } else {
                payByKash4me(amountToPay)
            }
        }
    }

    private fun payByKash4me(amountToPay: Double) {
        mPayByKash4meViewModel.payByKash4me(withdrawAmount = amountToPay.toString())
            .observe(viewLifecycleOwner) {

                when (it) {
                    is Resource.Failure -> {
                        val errorDialog = ErrorDialog.getInstance(message = it.errorMsg)
                        errorDialog.show(activity?.supportFragmentManager!!, errorDialog.tag)
                        mProgressDialog.hide()
                    }

                    Resource.Loading -> {
                        mProgressDialog.show()
                    }

                    is Resource.Success -> {
                        mProgressDialog.hide()
                        Timber.d("Success -> ${it.value}")
                        val action = PayByKash4meFragmentDirections
                            .actionPayByKash4meFragmentToConfirmPayByKash4meFragment(
                                arg = it.value, purchaseAmount = amountToPay.toFloat()
                            )
                        findNavController().navigate(action)
                    }
                }

            }
    }

    private fun getFeeSettings() {
        mViewModel.getFeeSettings(feeType = FeeType.PAY_BY_KASH4ME).observe(viewLifecycleOwner) {

            when (it) {
                is Resource.Failure -> {
                    val errorDialog = ErrorDialog.getInstance(message = it.errorMsg)
                    errorDialog.show(activity?.supportFragmentManager!!, errorDialog.tag)
                    mProgressDialog.hide()
                    processingFee = 0.00
                    mBinding.tvProcessingFeeDescription.text =
                        "You will be charged processing fee of $$processingFee"
                }

                Resource.Loading -> {
                    mProgressDialog.show()
                }

                is Resource.Success -> {
                    mProgressDialog.hide()
                    processingFee = it.value.customerFee?.toDoubleOrNull().getZeroIfNull()
                    mBinding.tvProcessingFeeDescription.text =
                        "You will be charged processing fee of $$processingFee"
                }
            }

        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}