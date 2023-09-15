package com.kash4me.ui.fragments.merchant.withdraw_cash

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.kash4me.R
import com.kash4me.data.models.user.UserType
import com.kash4me.databinding.FragmentConfirmWithdrawBinding
import com.kash4me.ui.activity.payment_gateway.PaymentSettingsViewModel
import com.kash4me.ui.dialog.ErrorDialog
import com.kash4me.ui.dialog.SuccessDialog
import com.kash4me.ui.fragments.customer.total_transaction.CustomerTotalTransactionViewModel
import com.kash4me.utils.FeeType
import com.kash4me.utils.SessionManager
import com.kash4me.utils.custom_views.CustomProgressDialog
import com.kash4me.utils.extensions.formatUsingCurrencySystem
import com.kash4me.utils.extensions.getZeroIfNull
import com.kash4me.utils.listeners.AfterDismissalListener
import com.kash4me.utils.network.Resource
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class ConfirmWithdrawFragment : Fragment() {

    private var _binding: FragmentConfirmWithdrawBinding? = null
    private val mBinding get() = _binding!!

    private val mProgressDialog by lazy { CustomProgressDialog(requireContext()) }

    @Inject
    lateinit var sessionManager: SessionManager

    companion object {

        private var amountToBeWithdrawn: Double? = null
        private var remainingBalance: Double? = null
        private lateinit var request: HashMap<String, Any>

        fun getNewInstance(
            amountToBeWithdrawn: Double?,
            remainingBalance: Double?,
            request: HashMap<String, Any>
        ): ConfirmWithdrawFragment {
            this.amountToBeWithdrawn = amountToBeWithdrawn
            this.remainingBalance = remainingBalance
            this.request = request
            return ConfirmWithdrawFragment()
        }

    }

    private val mViewModel: PaymentSettingsViewModel by viewModels()
    private val mTotalTransactionsViewModel: CustomerTotalTransactionViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentConfirmWithdrawBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mBinding.tvWithdrawAmount.text = "$${amountToBeWithdrawn?.formatUsingCurrencySystem()}"
        getFeeSettings()
        btnWithdrawListener()

    }

    private fun getFeeSettings() {
        mViewModel.getFeeSettings(feeType = FeeType.NORMAL).observe(viewLifecycleOwner) {

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

                    when (sessionManager.fetchUserType()) {
                        UserType.CUSTOMER -> {
                            Timber.d("Customer")
                            if (it.value.customerFee.isNullOrBlank()) {
                                mBinding.tvTransactionFee.text = "$" + "0.0"
                            } else {
                                mBinding.tvTransactionFee.text = "$" + it.value.customerFee
                            }
                            remainingBalance = remainingBalance.getZeroIfNull() -
                                    it.value.customerFee?.toDoubleOrNull().getZeroIfNull()
                            mBinding.tvRemainingBalance.text =
                                "$${remainingBalance?.formatUsingCurrencySystem()}"
                        }

                        UserType.MERCHANT -> {
                            Timber.d("Merchant")
                            if (it.value.merchantFee.isNullOrBlank()) {
                                mBinding.tvTransactionFee.text = "$" + "0.0"
                            } else {
                                mBinding.tvTransactionFee.text = "$" + it.value.merchantFee
                            }
                        }

                        UserType.STAFF -> {
                            Timber.d("Staff")
                        }

                        else -> {
                            Timber.d(sessionManager.fetchUserType()?.name)
                        }
                    }

                }
            }

        }
    }

    private fun btnWithdrawListener() {
        mBinding.btnWithdraw.setOnClickListener {

            mTotalTransactionsViewModel.withdrawAmount(request = request)
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
                            val successDialog = SuccessDialog.getInstance(
                                message = getString(R.string.cash_successfully_withdrawn),
                                afterDismissClicked = object : AfterDismissalListener {
                                    override fun afterDismissed() {
                                        activity?.finish()
                                    }
                                }
                            )
                            successDialog.show(
                                activity?.supportFragmentManager!!, successDialog.tag
                            )
                        }
                    }

                }

        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}