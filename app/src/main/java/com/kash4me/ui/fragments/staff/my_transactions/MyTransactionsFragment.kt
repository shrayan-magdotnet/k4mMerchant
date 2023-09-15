package com.kash4me.ui.fragments.staff.my_transactions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.kash4me.data.models.staff.StaffTransactionsResponse
import com.kash4me.databinding.FragmentMyTransactionsBinding
import com.kash4me.ui.dialog.ErrorDialog
import com.kash4me.utils.custom_views.CustomProgressDialog
import com.kash4me.utils.network.Resource
import timber.log.Timber

class MyTransactionsFragment : Fragment(), LifecycleObserver {

    private var _binding: FragmentMyTransactionsBinding? = null
    private val mBinding get() = _binding!!

    private lateinit var mProgressBar: CustomProgressDialog

    private val mAdapter by lazy { MyTransactionsAdapter() }

    private val mViewModel by lazy {
        ViewModelProvider(this)[MyTransactionsViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycle.addObserver(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyTransactionsBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mProgressBar = CustomProgressDialog(context = view.context)

        initRvTransactions()
        mBinding.emptyStateWithButton.btnTryAgain.setOnClickListener { getTransactions() }

    }

    override fun onResume() {
        super.onResume()

        Timber.d("onResume")
        getTransactions()

    }

    private fun getTransactions() {
        Timber.d("Let's fetch transactions")
        lifecycleScope.launchWhenResumed {
            mViewModel.getTransactions().observe(viewLifecycleOwner) {

                when (it) {
                    is Resource.Failure -> {
                        Timber.d("Failure")
                        showErrorState(errorMessage = it.errorMsg)
                    }

                    Resource.Loading -> {
                        Timber.d("Loading")
                        showLoadingState()
                    }

                    is Resource.Success -> {
                        Timber.d("Success -> ${it.value}")
                        mProgressBar.hide()
                        if (it.value.results.isNullOrEmpty()) {
                            showEmptyState()
                        } else {
                            showRvTransactions(transactions = it.value.results)
                        }
                    }
                }

            }
        }
    }

    private fun showLoadingState() {
        mProgressBar.show()
        mBinding.apply {
            tvMyTransactions.isVisible = false
            rvTransactions.isVisible = false
            emptyState.root.isVisible = false
            emptyStateWithButton.root.isVisible = false
        }
    }

    private fun showErrorState(errorMessage: String) {
        mProgressBar.hide()
        val errorDialog = ErrorDialog.getInstance(message = errorMessage)
        errorDialog.show(activity?.supportFragmentManager!!, errorDialog.tag)
        mBinding.apply {
            emptyStateWithButton.root.isVisible = true
            emptyStateWithButton.tvDescription.text = errorMessage

            tvMyTransactions.isVisible = false
            rvTransactions.isVisible = false
            emptyState.root.isVisible = false
        }
    }

    private fun showEmptyState() {
        mBinding.apply {
            emptyState.root.isVisible = true
            emptyState.tvTitle.text = "You have no transactions to display"

            tvMyTransactions.isVisible = false
            rvTransactions.isVisible = false
            emptyStateWithButton.root.isVisible = false
        }
    }

    private fun showRvTransactions(transactions: List<StaffTransactionsResponse.Result?>) {
        mBinding.apply {
            tvMyTransactions.isVisible = true
            rvTransactions.isVisible = true
            mAdapter.setData(transactions = transactions)

            emptyStateWithButton.root.isVisible = false
            emptyState.root.isVisible = false
        }
    }

    private fun initRvTransactions() {
        mBinding.rvTransactions.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = mAdapter
        }
    }

    private fun getDummyTransactions(): List<MyTransactionsAdapter.StaffTransaction> {
        val transaction1 = MyTransactionsAdapter.StaffTransaction(
            name = "Transaction 1",
            code = "C-1",
            amount = 1000.00,
            date = "2022-11-13",
            time = "10:00AM",
            cashbackValue = 1500.00
        )

        val transaction2 = MyTransactionsAdapter.StaffTransaction(
            name = "Transaction 2",
            code = "C-2",
            amount = 2000.00,
            date = "2022-12-13",
            time = "10:00AM",
            cashbackValue = 2500.00
        )

        val transaction3 = MyTransactionsAdapter.StaffTransaction(
            name = "Transaction 3",
            code = "C-3",
            amount = 3000.00,
            date = "2022-12-13",
            time = "10:00AM",
            cashbackValue = 3500.00
        )

        return listOf(transaction1, transaction2, transaction3)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}