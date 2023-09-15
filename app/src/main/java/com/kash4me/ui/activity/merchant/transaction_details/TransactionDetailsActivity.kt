package com.kash4me.ui.activity.merchant.transaction_details

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.inputmethod.EditorInfo
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kash4me.data.models.merchant.transaction_by_time.ViewTransactionByTimeResponse
import com.kash4me.merchant.R
import com.kash4me.merchant.databinding.ActivityTransactionDetailsBinding
import com.kash4me.network.ApiServices
import com.kash4me.network.NetworkConnectionInterceptor
import com.kash4me.network.NotFoundInterceptor
import com.kash4me.repository.MerchantTransactionDetailsRepository
import com.kash4me.ui.activity.merchant.transaction_details.adapter.StickyAdapter
import com.kash4me.ui.activity.merchant.transaction_details.model.TransactionStickyModel
import com.kash4me.ui.activity.merchant.transaction_details.utils.StickyHeaderDecoration
import com.kash4me.ui.dialog.ErrorDialog
import com.kash4me.utils.SessionManager
import com.kash4me.utils.custom_views.CurrencyTextView
import com.kash4me.utils.custom_views.CustomProgressDialog
import com.kash4me.utils.extensions.getEmptyIfNull
import com.kash4me.utils.extensions.getNotAvailableIfEmptyOrNull
import com.kash4me.utils.extensions.getZeroIfNull
import com.kash4me.utils.extensions.toLong
import timber.log.Timber

class TransactionDetailsActivity : AppCompatActivity() {

    private lateinit var backIV: ImageView
    private lateinit var transactionsRV: RecyclerView
    private lateinit var totalSellAmountTV: CurrencyTextView

    private lateinit var viewModel: TransactionDetailsViewModel

    private lateinit var sessionManager: SessionManager

    private lateinit var mode: String
    private lateinit var transactionType: String
    private lateinit var title: String
    private lateinit var amount: String
    private var branchId: Int = 0

    private lateinit var stickyAdapter: StickyAdapter
    private var customProgressDialog: CustomProgressDialog = CustomProgressDialog(this)

    private var binding: ActivityTransactionDetailsBinding? = null
    private val mBinding get() = binding!!
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTransactionDetailsBinding.inflate(layoutInflater)
        val view = binding!!.root
        setContentView(view)

        mode = intent.getStringExtra("mode") ?: ""
        transactionType = intent.getStringExtra("transaction_type") ?: ""
        title = intent.getStringExtra("title") ?: ""
        amount = intent.getStringExtra("totalAmount") ?: ""
        branchId = intent.getIntExtra("branchId", 0)

        Timber.d("Amount -> $amount")
        Timber.d("Transaction type -> $transactionType")

        if (transactionType == "sell_data") {
            mBinding.textView.setText(R.string.total_sell)
        } else if (transactionType == "cashback_data") {
            mBinding.textView.setText(R.string.total_cashback)
        }

        sessionManager = SessionManager(applicationContext)

        initViewModel()
        initUI()

        setupToolbar()
    }

    private fun setupToolbar() {
        setSupportActionBar(mBinding.toolbarLayout.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = title
        mBinding.toolbarLayout.toolbar.setNavigationOnClickListener { onBackPressed() }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_transactions_by_timeframe, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == R.id.action_info) {
            Timber.d("Need to show bottom sheet here")
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }


    private fun initViewModel() {
        val apiInterface =
            ApiServices.invoke(
                NetworkConnectionInterceptor(applicationContext),
                NotFoundInterceptor()
            )


        val merchantTransactionDetailsRepository =
            MerchantTransactionDetailsRepository(apiInterface)

        viewModel = ViewModelProvider(
            this,
            TransactionDetailsViewModelFactory(merchantTransactionDetailsRepository)
        )[TransactionDetailsViewModel::class.java]
        getMerchantBranchList()


        viewModel.viewTransactionByTimeResponse.observe(this) {
            initRecyclerView(it.results)
            if (it.results.isNullOrEmpty()) {
                mBinding.apply {
                    emptyState.root.isVisible = true
                    emptyState.tvMessage.setText(R.string.we_have_no_transactions_to_display)

                    transactionsRV.isVisible = false
                }
            } else {
                mBinding.apply {
                    transactionsRV.isVisible = true

                    emptyState.root.isVisible = false
                }

                tilSearchTransactionsListener(originalTransactions = it.results)

            }
        }

        viewModel.errorMessage.observe(this) {
            val errorDialog = ErrorDialog.getInstance(message = it.error)
            errorDialog.show(supportFragmentManager, errorDialog.tag)
            customProgressDialog.hide()
        }
    }

    private fun tilSearchTransactionsListener(originalTransactions: List<ViewTransactionByTimeResponse.Result?>) {

        mBinding.tilSearchTransactions.editText?.setOnEditorActionListener { textView, actionId, keyEvent ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val userQuery = mBinding.tilSearchTransactions.editText?.text
                if (userQuery.isNullOrBlank()) {
                    initRecyclerView(transactionList = originalTransactions)
                } else {

                    val filteredTransactions = originalTransactions.filter { result ->

                        val containsAssignedBy = result?.assignedBy?.contains(
                            userQuery.toString(), ignoreCase = true
                        ) == true
                        val containsNickname = result?.customerDetails?.nickName?.contains(
                            userQuery.toString(), ignoreCase = true
                        ) == true
                        val containsAmountSpent = result?.amountSpent?.contains(
                            userQuery.toString(), ignoreCase = true
                        ) == true

                        containsAssignedBy || containsNickname || containsAmountSpent

                    }
                    initRecyclerView(transactionList = filteredTransactions)
                }
                true
            } else false
        }

    }

    private fun initUI() {

        transactionsRV = findViewById(R.id.transactionsRV)
        totalSellAmountTV = findViewById(R.id.totalSellAmountTV)
        totalSellAmountTV.setValue(amount.toDoubleOrNull().toLong().getZeroIfNull())

//        backIV = findViewById(R.id.backIV)
//        backIV.visibility = View.VISIBLE

        initOnClick()
    }

    private fun getMerchantBranchList() {
        customProgressDialog.show()
        val token = sessionManager.fetchAuthToken().toString()
        val map = HashMap<String, Any>()
        if (branchId != 0) {
            map["merchant_shop_id"] = branchId
        }
        map["mode"] = mode
        map["transaction_type"] = transactionType
        viewModel.getMerchantTransactionDetails(token, map)
    }

    private fun initOnClick() {
//        backIV.setOnClickListener {
//            onBackPressed()
//        }
    }


    private fun initRecyclerView(transactionList: List<ViewTransactionByTimeResponse.Result?>?) {

        val requiredList = arrayListOf<TransactionStickyModel>()
        transactionList?.forEach { a ->
            requiredList.add(
                TransactionStickyModel(
                    date = a?.date.getNotAvailableIfEmptyOrNull(),
                    id = a?.customerDetails?.uniqueId.getEmptyIfNull(),
                    createdAt = a?.time.getNotAvailableIfEmptyOrNull(),
                    amount = if (transactionType == "cashback_data")
                        a?.cashbackAmount.getNotAvailableIfEmptyOrNull()
                    else
                        a?.amountSpent.getNotAvailableIfEmptyOrNull(),
                    nickName = a?.customerDetails?.nickName.getNotAvailableIfEmptyOrNull(),
                    assignedBy = a?.assignedBy.getNotAvailableIfEmptyOrNull()
                )
            )
        }

        stickyAdapter = StickyAdapter(this)
        val groupedBooks: Map<String, List<TransactionStickyModel>> =
            requiredList.groupBy { book -> book.date }
        stickyAdapter.itemsList = groupedBooks.toSortedMap()

        transactionsRV.addItemDecoration(
            StickyHeaderDecoration(stickyAdapter, binding!!.root)
        )
        transactionsRV.layoutManager = LinearLayoutManager(this)
        transactionsRV.adapter = stickyAdapter
        customProgressDialog.hide()
    }
}