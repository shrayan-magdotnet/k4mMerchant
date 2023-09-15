package com.kash4me.ui.fragments.customer.total_transaction

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.appbar.AppBarLayout
import com.kash4me.R
import com.kash4me.data.models.user.UserType
import com.kash4me.databinding.FragmentCustomerTotalTransactions2Binding
import com.kash4me.ui.activity.RegistrationActivity
import com.kash4me.ui.activity.customer.pay.PayByKash4meActivity
import com.kash4me.ui.activity.login.LoginActivity
import com.kash4me.ui.activity.merchant.withdraw_cash.WithdrawCashActivity
import com.kash4me.ui.dialog.ErrorDialog
import com.kash4me.ui.fragments.customer.total_transaction.on_your_way_bottom_sheet.OnYourWayTransactionsBottomSheet
import com.kash4me.ui.fragments.customer.total_transaction.your_balance_bottom_sheet.YourBalanceTransactionsBottomSheet
import com.kash4me.utils.AppConstants
import com.kash4me.utils.SessionManager
import com.kash4me.utils.custom_views.CustomProgressDialog
import com.kash4me.utils.extensions.formatAsCurrency
import com.kash4me.utils.extensions.getEmptyIfNull
import com.kash4me.utils.extensions.getZeroIfNull
import com.kash4me.utils.extensions.showToast
import com.kash4me.utils.network.Resource
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class CustomerTotalTransactions2Fragment : Fragment() {

    private var _binding: FragmentCustomerTotalTransactions2Binding? = null
    private val mBinding get() = _binding!!

    companion object {

        private const val TOTAL_TRANSACTIONS = "total_transactions"

    }

    private val mTransactionsAdapter by lazy { CustomerTotalTransactionAdapter() }

    private var mAvailableBalance: Double? = null
    private var mProcessingAmount: String? = null

    private val mProgressDialog by lazy { CustomProgressDialog(requireContext()) }

    private var sharedPreferences: SharedPreferences? = null

    @Inject
    lateinit var mSessionManager: SessionManager

    private val mViewModel: CustomerTotalTransactionViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCustomerTotalTransactions2Binding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        showViewAccordingToUserType()

        sharedPreferences = context?.getSharedPreferences(
            requireActivity().getString(R.string.first_start_status), Context.MODE_PRIVATE
        )

        if (mSessionManager.fetchUserType() == UserType.CUSTOMER) {
            val isOpenedForFirstTime = sharedPreferences?.getBoolean(TOTAL_TRANSACTIONS, true)
            Timber.d("isOpenedForFirstTime -> $isOpenedForFirstTime")
            if (isOpenedForFirstTime == true) {
                updateCache()
                Timber.d("Let's fetch total transactions")
            }

            observeTotalTransactionDetails()
        }
        btnWithdrawCashListener()
        btnPayListener()
        mBinding.ivViewTransactions.setOnClickListener {
            val bottomSheet = YourBalanceTransactionsBottomSheet()
            bottomSheet.show(activity?.supportFragmentManager!!, bottomSheet.tag)
        }
        mBinding.ivViewProcessingTransactions.setOnClickListener {
            val bottomSheet = OnYourWayTransactionsBottomSheet.newInstance(
                processingAmount = mProcessingAmount.getEmptyIfNull()
            )
            bottomSheet.show(activity?.supportFragmentManager!!, bottomSheet.tag)
        }

    }

    private fun showViewAccordingToUserType() {
        val userType = mSessionManager.fetchUserType()
        if (userType == UserType.ANONYMOUS) {
            mBinding.apply {

                Timber.d("User type -> ${userType.id}")

                layoutAccountCreationPrompt.root.isVisible = true
                llMainLayout.isVisible = false

                layoutAccountCreationPrompt.apply {

                    tvTitle.text = "Want to Check your Balance?"
                    tvDescription.text =
                        "By signing up with us, you can check how much balance you have which you can withdraw and redeem anytime you want."

                    btnLogin.setOnClickListener {
                        val intent = Intent(requireActivity(), LoginActivity::class.java)
                        startActivity(intent)
                    }

                    btnSignup.setOnClickListener {
                        val intent = Intent(requireActivity(), RegistrationActivity::class.java)
                        startActivity(intent)
                    }

                }

            }
        } else {
            mBinding.layoutAccountCreationPrompt.root.isVisible = false
            mBinding.llMainLayout.isVisible = true
        }
    }


    private fun btnPayListener() {
        mBinding.ivPay.setOnClickListener {

            if (mAvailableBalance.getZeroIfNull() < AppConstants.MIN_AMOUNT_PAY_BY_KASH4ME) {
                showToast("Balance should be minimum $${AppConstants.MIN_AMOUNT_PAY_BY_KASH4ME} to pay by Kash4me")
                return@setOnClickListener
            }

            val intent = PayByKash4meActivity.getNewIntent(
                activity = requireActivity() as AppCompatActivity,
                remainingBalance = mAvailableBalance.getZeroIfNull()
            )
            startActivity(intent)
        }
    }

    private fun observeTotalTransactionDetails() {
        mViewModel.getTotalTransactionDetails().observe(viewLifecycleOwner) {

            Timber.d("Total transaction entity -> $it")

//            if (it?.results.isNullOrEmpty()) {
//                val isOpenedForFirstTime = sharedPreferences?.getBoolean(TOTAL_TRANSACTIONS, true)
//                if (isOpenedForFirstTime == true) {
//                    mBinding.tvEmptyStateTitle.isVisible = false
//                    mBinding.tvEmptyStateSubTitle.isVisible = false
//                    mBinding.cashBackRv.isVisible = false
//                } else {
//                    showEmptyState(getString(R.string.no_data_found))
//                }
//                showEmptyState(message = getString(R.string.you_don_t_have_any_transactions))
//            } else {
//                mTransactionsAdapter.setData(cashbacks = it?.results ?: listOf())
//                showRecyclerView()
//            }

            val cashbackBalance =
                if (it?.transactionDetails?.cashbackBalance != null) {
                    "$" + it.transactionDetails.cashbackBalance.formatAsCurrency()
                } else {
                    "$0.0"
                }
            Timber.d("Cashback -> $cashbackBalance")
            mBinding.tvYourBalance.text = cashbackBalance

            val balance = it?.transactionDetails?.cashbackBalance?.toDoubleOrNull().getZeroIfNull()
//            mBinding.ivWithdraw.isEnabled = balance >= AppConstants.MIN_WITHDRAW_AMOUNT
//            mBinding.ivPay.isEnabled = balance >= AppConstants.MIN_AMOUNT_PAY_BY_KASH4ME

            mAvailableBalance = it?.transactionDetails?.cashbackBalance?.toDoubleOrNull()

            mProcessingAmount =
                if (it?.transactionDetails?.processing != null) {
                    "$" + it.transactionDetails.processing.formatAsCurrency()
                } else {
                    "$0.0"
                }
            Timber.d("Processing -> $mProcessingAmount")
            mBinding.tvOnYourWay.text = mProcessingAmount

        }
    }

    override fun onStart() {
        super.onStart()
        Timber.d("onStart")

        if (mSessionManager.fetchUserType() == UserType.CUSTOMER) {
            val appBar: AppBarLayout? = (activity)?.findViewById(R.id.customAppBar)
            val ivRefresh: ImageButton? = appBar?.findViewById(R.id.iv_refresh)
            ivRefresh?.visibility = View.VISIBLE
            ivRefresh?.setOnClickListener {
                Timber.d("Let's refresh")
                updateCache()
            }
            val ivInfo: ImageButton? = appBar?.findViewById(R.id.iv_info)
            ivInfo?.visibility = View.VISIBLE
            ivInfo?.setOnClickListener {
                Timber.d("Let's refresh")
                val bottomSheet = CustomerTotalTransactionsBottomSheet()
                bottomSheet.show(activity?.supportFragmentManager!!, bottomSheet.tag)
            }
        }

    }

    private fun updateCache() {
        updateCustomerTotalTransactionsDetailsInCache()
        updateProcessingTransactionsInCache()
    }

    override fun onStop() {

        if (mSessionManager.fetchUserType() == UserType.CUSTOMER) {
            val appBar: AppBarLayout? = (activity)?.findViewById(R.id.customAppBar)
            val ivRefresh: ImageButton? = appBar?.findViewById(R.id.iv_refresh)
            ivRefresh?.visibility = View.INVISIBLE
            val ivInfo: ImageButton? = appBar?.findViewById(R.id.iv_info)
            ivInfo?.visibility = View.GONE
            super.onStop()

            val isOpenedForFirstTime = sharedPreferences?.getBoolean(TOTAL_TRANSACTIONS, true)
            if (isOpenedForFirstTime == true) {
                sharedPreferences?.edit { putBoolean(TOTAL_TRANSACTIONS, false) }
            }
        } else {
            super.onStop()
        }

    }

    private fun btnWithdrawCashListener() {
        mBinding.ivWithdraw.setOnClickListener {

            if (mAvailableBalance.getZeroIfNull() < AppConstants.MIN_WITHDRAW_AMOUNT) {
                showToast("Balance should be minimum $${AppConstants.MIN_WITHDRAW_AMOUNT} to withdraw amount")
                return@setOnClickListener
            }
            val intent = WithdrawCashActivity.getNewIntent(
                packageContext = requireActivity(),
                availableBalance = mAvailableBalance.getZeroIfNull()
            )
            startActivity(intent)
        }
    }

    private fun updateCustomerTotalTransactionsDetailsInCache() {

        mViewModel.updateCustomerTotalTransactionsDetailsInCache().observe(viewLifecycleOwner) {

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
//                    showRecyclerView()
                }
            }

        }

    }

    private fun updateProcessingTransactionsInCache() {

        mViewModel.updateProcessingTransactionsInCache().observe(viewLifecycleOwner) {

            when (it) {
                is Resource.Failure -> {
                    val errorDialog = ErrorDialog.getInstance(message = it.errorMsg)
                    errorDialog.show(activity?.supportFragmentManager!!, errorDialog.tag)
                }

                Resource.Loading -> {
                    /* Do nothing */
                }

                is Resource.Success -> {
                    /* Do nothing */
                }
            }

        }

    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}