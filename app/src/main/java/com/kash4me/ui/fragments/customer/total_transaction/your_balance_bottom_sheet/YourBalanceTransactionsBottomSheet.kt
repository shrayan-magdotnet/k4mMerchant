package com.kash4me.ui.fragments.customer.total_transaction.your_balance_bottom_sheet

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.kash4me.merchant.R
import com.kash4me.merchant.databinding.BottomSheetYourBalanceTransactionsBinding
import com.kash4me.ui.fragments.customer.total_transaction.CustomerTotalTransactionViewModel
import com.kash4me.utils.extensions.formatAsCurrency
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class YourBalanceTransactionsBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetYourBalanceTransactionsBinding? = null
    private val mBinding get() = _binding!!

    companion object {

        private const val TOTAL_TRANSACTIONS = "total_transactions"

    }

    private val mTransactionsAdapter by lazy { YourBalanceTransactionsAdapter() }

    private var sharedPreferences: SharedPreferences? = null

    private val mViewModel: CustomerTotalTransactionViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetYourBalanceTransactionsBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initRvTransactions()
        observeTotalTransactionDetails()


    }

    private fun initRvTransactions() {
        mBinding.rvTransactions.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = mTransactionsAdapter
        }
    }

    private fun observeTotalTransactionDetails() {
        mViewModel.getTotalTransactionDetails().observe(viewLifecycleOwner) {

            Timber.d("Total transaction entity -> $it")

            if (it?.results.isNullOrEmpty()) {
                val isOpenedForFirstTime = sharedPreferences?.getBoolean(TOTAL_TRANSACTIONS, true)
                if (isOpenedForFirstTime == true) {
                    mBinding.emptyState.root.isVisible = false
                    mBinding.rvTransactions.isVisible = false
                } else {
                    showEmptyState(getString(R.string.you_don_t_have_any_transactions))
                }
                showEmptyState(title = getString(R.string.you_don_t_have_any_transactions))
            } else {
                mTransactionsAdapter.setData(transactions = it?.results ?: listOf())
                showRecyclerView()
            }

            val cashbackBalance =
                if (it?.transactionDetails?.cashbackBalance != null) {
                    "$" + it.transactionDetails.cashbackBalance.formatAsCurrency()
                } else {
                    "$0.0"
                }
            Timber.d("Cashback -> $cashbackBalance")
            mBinding.tvYourBalance.text = cashbackBalance

        }
    }

    private fun showEmptyState(title: String) {
        mBinding.apply {
            emptyState.root.isVisible = true
            emptyState.tvTitle.text = title
            emptyState.tvSubtitle.text = ""

            rvTransactions.isVisible = false
        }
    }

    private fun showRecyclerView() {
        mBinding.apply {
            rvTransactions.isVisible = true

            emptyState.root.isVisible = false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}