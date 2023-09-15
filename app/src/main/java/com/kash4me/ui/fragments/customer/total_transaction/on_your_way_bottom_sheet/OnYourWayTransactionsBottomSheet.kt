package com.kash4me.ui.fragments.customer.total_transaction.on_your_way_bottom_sheet

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.kash4me.R
import com.kash4me.databinding.BottomSheetOnYourWayTransactionsBinding
import com.kash4me.ui.fragments.customer.total_transaction.CustomerTotalTransactionViewModel
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class OnYourWayTransactionsBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetOnYourWayTransactionsBinding? = null
    private val mBinding get() = _binding!!

    companion object {

        private const val TOTAL_TRANSACTIONS = "total_transactions"

        private var mProcessingAmount: String? = "0.0"

        fun newInstance(processingAmount: String): OnYourWayTransactionsBottomSheet {
            mProcessingAmount = processingAmount
            return OnYourWayTransactionsBottomSheet()
        }

    }

    private val mTransactionsAdapter by lazy { OnYourWayTransactionsAdapter() }

    private var sharedPreferences: SharedPreferences? = null

    private val mViewModel: CustomerTotalTransactionViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetOnYourWayTransactionsBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Timber.d("On Your Way -> $mProcessingAmount")
        mBinding.tvOnYourWay.text = mProcessingAmount

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
        mViewModel.getProcessingTransactionsFromCache().observe(viewLifecycleOwner) {

            Timber.d("Total transaction entity -> $it")

            if (it.isNullOrEmpty()) {
                val isOpenedForFirstTime = sharedPreferences?.getBoolean(TOTAL_TRANSACTIONS, true)
                if (isOpenedForFirstTime == true) {
                    mBinding.emptyState.root.isVisible = false
                    mBinding.rvTransactions.isVisible = false
                } else {
                    showEmptyState(
                        title = getString(R.string.you_don_t_have_any_transactions),
                        subtitle = ""
                    )
                }
                showEmptyState(
                    title = getString(R.string.you_don_t_have_any_transactions),
                    subtitle = ""
                )
            } else {
                mTransactionsAdapter.setData(transactions = it)
                showRecyclerView()
            }

        }
    }

    private fun showEmptyState(title: String, subtitle: String) {
        mBinding.apply {
            emptyState.tvTitle.text = title
            emptyState.tvSubtitle.text = subtitle
            emptyState.root.isVisible = true

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