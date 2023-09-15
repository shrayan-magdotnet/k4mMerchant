package com.kash4me.ui.fragments.customer.total_transaction

import android.content.Context
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
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.appbar.AppBarLayout
import com.kash4me.merchant.R
import com.kash4me.merchant.databinding.FragmentCustomerTotalTransactionBinding
import com.kash4me.ui.activity.customer.pay.PayByKash4meActivity
import com.kash4me.ui.activity.merchant.withdraw_cash.WithdrawCashActivity
import com.kash4me.ui.dialog.ErrorDialog
import com.kash4me.utils.AppConstants
import com.kash4me.utils.custom_views.CustomProgressDialog
import com.kash4me.utils.extensions.formatAsCurrency
import com.kash4me.utils.extensions.getZeroIfNull
import com.kash4me.utils.network.Resource
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class CustomerTotalTransactionFragment : Fragment() {

    companion object {

        private const val TOTAL_TRANSACTIONS = "total_transactions"

        fun newInstance() = CustomerTotalTransactionFragment()
    }

    private val mTransactionsAdapter by lazy { CustomerTotalTransactionAdapter() }

    private var mAvailableBalance: Double? = null

    private val mProgressDialog by lazy { CustomProgressDialog(requireContext()) }

    private var sharedPreferences: SharedPreferences? = null

    private val mViewModel: CustomerTotalTransactionViewModel by viewModels()

    private var _binding: FragmentCustomerTotalTransactionBinding? = null
    private val mBinding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCustomerTotalTransactionBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedPreferences = context?.getSharedPreferences(
            requireActivity().getString(R.string.first_start_status), Context.MODE_PRIVATE
        )

        val isOpenedForFirstTime = sharedPreferences?.getBoolean(TOTAL_TRANSACTIONS, true)
        Timber.d("isOpenedForFirstTime -> $isOpenedForFirstTime")
        if (isOpenedForFirstTime == true) {
            updateCustomerTotalTransactionsDetailsInCache()
            Timber.d("Let's fetch total transactions")
        }

        initRvTransactions()
        observeTotalTransactionDetails()
        btnWithdrawCashListener()
        btnPayListener()

    }

    private fun btnPayListener() {
        mBinding.btnPay.setOnClickListener {
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

            if (it?.results.isNullOrEmpty()) {
                val isOpenedForFirstTime = sharedPreferences?.getBoolean(TOTAL_TRANSACTIONS, true)
                if (isOpenedForFirstTime == true) {
                    mBinding.tvEmptyStateTitle.isVisible = false
                    mBinding.tvEmptyStateSubTitle.isVisible = false
                    mBinding.cashBackRv.isVisible = false
                } else {
                    showEmptyState(getString(R.string.no_data_found))
                }
                showEmptyState(message = getString(R.string.you_don_t_have_any_transactions))
            } else {
                mTransactionsAdapter.setData(cashbacks = it?.results ?: listOf())
                showRecyclerView()
            }

            val cashbackBalance =
                if (it?.transactionDetails?.cashbackBalance != null) {
                    "$" + it.transactionDetails.cashbackBalance.formatAsCurrency()
                } else {
                    "$0.0"
                }
            Timber.d("Cashback -> $cashbackBalance")
            mBinding.cashBackTV.text = cashbackBalance

            val balance = it?.transactionDetails?.cashbackBalance?.toDoubleOrNull().getZeroIfNull()
            mBinding.withdrawCashBtn.isEnabled = balance >= AppConstants.MIN_WITHDRAW_AMOUNT
            mBinding.btnPay.isEnabled = balance >= AppConstants.MIN_AMOUNT_PAY_BY_KASH4ME

            mAvailableBalance =
                it?.transactionDetails?.cashbackBalance?.toDoubleOrNull()

            val processingBalance =
                if (it?.transactionDetails?.processing != null) {
                    "$" + it.transactionDetails.processing.formatAsCurrency()
                } else {
                    "$0.0"
                }
            Timber.d("Processing -> $processingBalance")
            mBinding.processingTV.text = processingBalance

        }
    }

    override fun onStart() {
        super.onStart()
        Timber.d("onStart")
        val appBar: AppBarLayout? = (activity)?.findViewById(R.id.customAppBar)
        val ivRefresh: ImageButton? = appBar?.findViewById(R.id.iv_refresh)
        ivRefresh?.visibility = View.VISIBLE
        ivRefresh?.setOnClickListener {
            Timber.d("Let's refresh")
            updateCustomerTotalTransactionsDetailsInCache()
        }
        val ivInfo: ImageButton? = appBar?.findViewById(R.id.iv_info)
        ivInfo?.visibility = View.VISIBLE
        ivInfo?.setOnClickListener {
            Timber.d("Let's refresh")
            val bottomSheet = CustomerTotalTransactionsBottomSheet()
            bottomSheet.show(activity?.supportFragmentManager!!, bottomSheet.tag)
        }
    }

    override fun onStop() {
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

    }

    private fun btnWithdrawCashListener() {
        mBinding.withdrawCashBtn.setOnClickListener {
            val intent = WithdrawCashActivity.getNewIntent(
                packageContext = requireActivity(),
                availableBalance = mAvailableBalance.getZeroIfNull()
            )
            startActivity(intent)
        }
    }

    private fun initRvTransactions() {
        mBinding.cashBackRv.apply {
            layoutManager = LinearLayoutManager(context)
            val decoration = DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
            addItemDecoration(decoration)
            adapter = mTransactionsAdapter
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
                    showRecyclerView()
                }
            }

        }

    }

    private fun showEmptyState(message: String) {
        mBinding.apply {
            tvEmptyStateTitle.text = message
            tvEmptyStateTitle.isVisible = true
            tvEmptyStateSubTitle.isVisible = true

            cashBackRv.isVisible = false
        }
    }

    private fun showRecyclerView() {
        mBinding.apply {
            cashBackRv.isVisible = true

            tvEmptyStateTitle.isVisible = false
            tvEmptyStateSubTitle.isVisible = false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}