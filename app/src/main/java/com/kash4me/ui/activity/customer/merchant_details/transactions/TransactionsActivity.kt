package com.kash4me.ui.activity.customer.merchant_details.transactions

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.inputmethod.EditorInfo
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.kash4me.data.models.customer.transactions_according_to_merchant.Result
import com.kash4me.data.models.customer.transactions_according_to_merchant.TransactionsAccordingToMerchantResponse
import com.kash4me.merchant.R
import com.kash4me.merchant.databinding.ActivityTransactionsBinding
import com.kash4me.ui.dialog.ErrorDialog
import com.kash4me.utils.AppConstants
import com.kash4me.utils.custom_views.CustomProgressDialog
import com.kash4me.utils.extensions.getEmptyIfNull
import com.kash4me.utils.extensions.getZeroIfNull
import com.kash4me.utils.extensions.showToast
import com.kash4me.utils.extensions.toLong
import com.kash4me.utils.listeners.SingleParamItemClickListener
import com.kash4me.utils.network.Resource
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber


@AndroidEntryPoint
class TransactionsActivity : AppCompatActivity() {

    private var binding: ActivityTransactionsBinding? = null
    private val mBinding get() = binding!!

    companion object {

        private const val MERCHANT_ID = "merchant_id"
        private const val MERCHANT_NAME = "merchant_name"

        fun getNewIntent(
            activity: AppCompatActivity, merchantId: Int, merchantName: String
        ): Intent {

            val intent = Intent(activity, TransactionsActivity::class.java)
            intent.putExtra(MERCHANT_ID, merchantId)
            intent.putExtra(MERCHANT_NAME, merchantName)
            return intent

        }

    }

    private val mProgressDialog by lazy { CustomProgressDialog(context = this) }

    private val mTransactionsAdapter by lazy {
        TransactionsAdapter(clickListener = object : SingleParamItemClickListener<Result> {
            override fun onClick(item: Result) {

                val intent = TransactionDetainForReturnPurchaseActivity.getNewIntent(
                    activity = this@TransactionsActivity, transactionId = item.id
                )
                startActivity(intent)

            }
        })
    }

    private val mViewModel: TransactionsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTransactionsBinding.inflate(layoutInflater)
        val view = binding!!.root
        setContentView(view)

        setupToolbar(title = intent.getStringExtra(MERCHANT_NAME).getEmptyIfNull())

        initRvTransactions()

    }

    override fun onResume() {
        super.onResume()
        getTransactionsByMerchant()
    }

    private fun getTransactionsByMerchant() {
        mViewModel.getTransactionsAccordingToMerchant(
            merchantId = intent.getIntExtra(MERCHANT_ID, AppConstants.MINUS_ONE)
        ).observe(this) {

            when (it) {
                is Resource.Failure -> {
                    mProgressDialog.hide()
                    val errorDialog = ErrorDialog.getInstance(message = it.errorMsg)
                    errorDialog.show(supportFragmentManager, errorDialog.tag)
                }

                Resource.Loading -> {
                    mProgressDialog.show()
                }

                is Resource.Success -> {
                    mProgressDialog.hide()
                    Timber.d("Success -> ${it.value}")

                    if (it.value.results.isEmpty()) {
                        showEmptyState()
                    } else {
                        showRvTransactions(it)
                    }

                    tilSearchTransactionsListener(originalTransactions = it.value.results)
                    val cashBalance =
                        it.value.cashbackBalance.toDoubleOrNull().toLong().getZeroIfNull()
                    mBinding.ctvCashBalance.setValue(cashBalance)
                }
            }

        }
    }

    private fun showEmptyState() {
        mBinding.emptyState.root.isVisible = true

        mBinding.rvTransactions.isVisible = false
    }

    private fun showRvTransactions(it: Resource.Success<TransactionsAccordingToMerchantResponse>) {
        mBinding.emptyState.root.isVisible = false

        mBinding.rvTransactions.isVisible = true
        mTransactionsAdapter.setData(transactions = it.value.results)
    }

    private fun tilSearchTransactionsListener(originalTransactions: List<Result>) {

        mBinding.tilSearchTransactions.editText?.setOnEditorActionListener { textView, actionId, keyEvent ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val userQuery = mBinding.tilSearchTransactions.editText?.text
                if (userQuery.isNullOrBlank()) {
                    mTransactionsAdapter.setData(transactions = originalTransactions)
                } else {
                    val filteredTransactions = originalTransactions.filter { result ->
                        result.amount_spent.contains(userQuery.toString())
                                || result.cashback_amount.contains(userQuery.toString())
                                || result.date.contains(userQuery.toString())
                    }
                    mTransactionsAdapter.setData(transactions = filteredTransactions)
                }
                true
            } else false
        }

    }

    fun setupToolbar(title: String) {
        setSupportActionBar(mBinding.toolbar.root)
        supportActionBar?.title = title
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        mBinding.toolbar.root.setNavigationOnClickListener { onBackPressed() }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_transactions, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if (item.itemId == R.id.action_info) {
            showToast("We need to show info bottom sheet")
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    private fun initRvTransactions() {
        mBinding.rvTransactions.apply {
            layoutManager = LinearLayoutManager(context)
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
            adapter = mTransactionsAdapter
        }
    }

}