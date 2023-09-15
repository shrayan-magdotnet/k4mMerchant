package com.kash4me.ui.fragments.merchant.branch_details

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.appbar.AppBarLayout
import com.kash4me.data.models.MerchantTransactionSummaryResponse
import com.kash4me.merchant.R
import com.kash4me.merchant.databinding.FragmentBranchDetailsBinding
import com.kash4me.ui.activity.merchant.branch_details.BranchDetailsActivity
import com.kash4me.ui.activity.merchant.branch_list.BranchListActivity
import com.kash4me.ui.dialog.ErrorDialog
import com.kash4me.utils.SessionManager
import com.kash4me.utils.custom_views.CustomProgressDialog
import com.kash4me.utils.network.Resource
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

@AndroidEntryPoint
class BranchDetailsFragment : Fragment() {

    private val viewModel: BranchDetailsViewModel by viewModels()

    private lateinit var sessionManager: SessionManager

    private lateinit var progressDialog: CustomProgressDialog

    private val shouldFetchData = MutableLiveData<Boolean>()

    private var _binding: FragmentBranchDetailsBinding? = null
    private val mBinding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBranchDetailsBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sessionManager = SessionManager(requireActivity().applicationContext)
        progressDialog = CustomProgressDialog(context = view.context)

        val locale = resources.configuration.locale
        val frCa = Locale("fr", "CA")
        val currency = Currency.getInstance(frCa)
        Timber.d("Locale -> $locale")

        Timber.d("Currency Code: " + currency.currencyCode)
        Timber.d("Display Name: " + currency.displayName)
        Timber.d("Symbol: " + currency.symbol)
        Timber.d("Symbol L1: " + currency.getSymbol(frCa))
        Timber.d("Symbol L2: " + currency.getSymbol(Locale.CANADA_FRENCH))
        Timber.d("Symbol L (trimmed): " + currency.getSymbol(Locale.CANADA_FRENCH).getOrNull(0))
        Timber.d("Default Fraction Digits: " + currency.defaultFractionDigits)

        val cen = NumberFormat.getCurrencyInstance(Locale.CANADA_FRENCH) // 67 889 786,50 $
        val amount = cen.format(123456789)
        Timber.d("Amount -> $amount")

        shouldFetchData.observe(viewLifecycleOwner) { shouldFetch ->

            Timber.d("Fetching data")

            if (shouldFetch) {
                fetchData()
                shouldFetchData.value = false
            }

        }

        initVM()
        initUI()

    }

    override fun onResume() {
        super.onResume()
        val appBar: AppBarLayout? = (activity)?.findViewById(R.id.customAppBar)
        val ivRefresh: ImageButton? = appBar?.findViewById(R.id.iv_refresh)
        ivRefresh?.isVisible = true
        ivRefresh?.setOnClickListener { shouldFetchData.value = true }
    }

    private fun getTransactionSummary(merchantId: Int? = null) {
        val token = sessionManager.fetchAuthToken().toString()
        viewModel.getTransactionSummary(token, merchantId).observe(viewLifecycleOwner) {

            when (it) {

                is Resource.Failure -> {
                    progressDialog.hide()
                    showEmptyState(it.errorMsg)
                    val errorDialog = ErrorDialog.getInstance(message = it.errorMsg)
                    errorDialog.show(activity?.supportFragmentManager!!, errorDialog.tag)
                }

                Resource.Loading -> {
                    progressDialog.show()
                }

                is Resource.Success -> {
                    progressDialog.hide()
                    Timber.d("Success -> $it")
                    initRecyclerView(it.value)
                    makeDataVisible()
                }

            }

        }
    }

    private fun initRecyclerView(transactionSummary: MerchantTransactionSummaryResponse) {

        mBinding.merchantRV.layoutManager = GridLayoutManager(context, 2)

        var branchId = 0
        branchId = ((activity) as BranchDetailsActivity).branchId
        Timber.d("Branch ID -> $branchId")

        Timber.d("data -> $transactionSummary")

        val adapter = BranchDetailsAdapter(transactionSummary, branchId = branchId)
        mBinding.merchantRV.adapter = adapter

    }

    private fun initVM() {

//        viewModel.merchantTransactionSummaryResponse.observe(viewLifecycleOwner) {
//
//            progressDialog.hide()
//            Timber.d("Success -> $it")
//            initRecyclerView(it)
//            makeDataVisible()
//
//        }
//
//        viewModel.errorMessage.observe(viewLifecycleOwner) {
//
//            progressDialog.hide()
//            showEmptyState(it)
//            val errorDialog = ErrorDialog.getInstance(message = it)
//            errorDialog.show(activity?.supportFragmentManager!!, errorDialog.tag)
//
//        }

        fetchData()

    }

    private fun fetchData() {
        getTransactionSummary(merchantId = (activity as BranchDetailsActivity).branchId)
    }

    fun makeDataVisible() {
        Timber.d("Inside branch details")
        mBinding.apply {
            merchantRV.visibility = View.VISIBLE

            viewBranchTV.visibility = View.INVISIBLE
            emptyState.root.isVisible = false
        }
    }

    private fun showEmptyState(it: String?) {
        mBinding.apply {
            emptyState.root.visibility = View.VISIBLE
            emptyState.tvTitle.text = it
            emptyState.btnTryAgain.isVisible = true
            emptyState.tvDescription.isVisible = true

            merchantRV.visibility = View.INVISIBLE
            viewBranchTV.visibility = View.INVISIBLE
        }
    }

    private fun showMinimalEmptyState(message: String?) {
        mBinding.apply {
            emptyState.root.visibility = View.VISIBLE
            emptyState.tvTitle.text = message
            emptyState.btnTryAgain.isVisible = false
            emptyState.tvDescription.isVisible = false

            merchantRV.visibility = View.INVISIBLE
            viewBranchTV.visibility = View.INVISIBLE
        }
    }

    private fun initUI() {

        initOnClick()

    }

    private fun initOnClick() {

        mBinding.viewBranchTV.setOnClickListener {
            val intent = Intent(activity, BranchListActivity::class.java)
            activity?.startActivity(intent)
        }

        mBinding.emptyState.btnTryAgain.setOnClickListener { fetchData() }

    }

    override fun onStop() {
        val appBar: AppBarLayout? = (activity)?.findViewById(R.id.customAppBar)
        val ivRefresh: ImageButton? = appBar?.findViewById(R.id.iv_refresh)
        ivRefresh?.isVisible = false
        super.onStop()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}