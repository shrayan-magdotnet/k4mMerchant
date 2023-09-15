package com.kash4me.ui.fragments.merchant.home

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.core.content.edit
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.appbar.AppBarLayout
import com.kash4me.data.local.merchant.MerchantTransactionSummaryEntity
import com.kash4me.merchant.R
import com.kash4me.merchant.databinding.FragmentMerchantHomeBinding
import com.kash4me.ui.activity.merchant.branch_list.BranchListActivity
import com.kash4me.ui.activity.merchant.merchant_dashboard.MerchantDashBoardActivity
import com.kash4me.ui.activity.merchant.merchant_dashboard.payment_setup_reminder.PaymentSetupReminderBottomSheet
import com.kash4me.ui.dialog.ErrorDialog
import com.kash4me.utils.SessionManager
import com.kash4me.utils.custom_views.CustomProgressDialog
import com.kash4me.utils.extensions.getZeroIfNull
import com.kash4me.utils.isNull
import com.kash4me.utils.network.Resource
import timber.log.Timber
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

class MerchantHomeFragment : Fragment() {

    companion object {

        private const val MERCHANT_HOME = "merchant_home"

    }

    private lateinit var viewModel: MerchantHomeViewModel

    private lateinit var sessionManager: SessionManager
    private var sharedPreferences: SharedPreferences? = null

    private lateinit var progressDialog: CustomProgressDialog

    private val shouldFetchData = MutableLiveData<Boolean>()

    private var _binding: FragmentMerchantHomeBinding? = null
    private val mBinding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMerchantHomeBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sessionManager = SessionManager(requireActivity().applicationContext)
        progressDialog = CustomProgressDialog(context = view.context)
        sharedPreferences = context?.getSharedPreferences(
            requireActivity().getString(R.string.first_start_status), Context.MODE_PRIVATE
        )

        val isOpenedForFirstTime = sharedPreferences?.getBoolean(MERCHANT_HOME, true)
        Timber.d("isFreshLogin -> $isOpenedForFirstTime")
        if (isOpenedForFirstTime == true) {
            Timber.d("Let's fetch from API")
            shouldFetchData.value = true
        }

        Timber.d("onViewCreated")

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

        initVM()

        val isFreshLogin = activity?.intent?.getBooleanExtra(
            MerchantDashBoardActivity.IS_FRESH_LOGIN, true
        )
        Timber.d("isFreshLogin -> $isFreshLogin")
//        if (isFreshLogin == true) {
//            shouldFetchData.value = true
//        }

//        sampleDetectCountry()

        shouldFetchData.observe(viewLifecycleOwner) { shouldFetch ->

            Timber.d("Fetching data")

            if (shouldFetch) {
                updateTransactionSummary()
                shouldFetchData.value = false
            }

        }

        initUI()

        observeTransactionSummary()

    }

    private fun sampleDetectCountry() {
        viewModel.detectCountry(latitude = -31.9505, longitude = 115.8605)
            .observe(viewLifecycleOwner) { resource ->

                when (resource) {
                    is Resource.Failure -> {
                        Timber.d("Failure")
                    }

                    Resource.Loading -> {
                        Timber.d("Loading")
                    }

                    is Resource.Success -> {
                        Timber.d("Success")
                        if (resource.value.results?.size.getZeroIfNull() > 0) {
                            val firstResult = resource.value.results?.firstOrNull()
                            for (i in 0 until firstResult?.addressComponents?.size.getZeroIfNull()) {
                                val component = firstResult?.addressComponents?.get(i)
                                val type = component?.types
                                if (type?.contains("country") == true) {
                                    val countryName = component.longName
                                    Timber.d("Country -> $countryName")
                                    break
                                }
                            }
                        }

                    }
                }

            }
    }

    private fun observeTransactionSummary() {
        viewModel.getTransactionSummaryFromDb().observe(viewLifecycleOwner) {

            Timber.d("Data from db -> $it")
            when (it) {
                null -> {
                    val isOpenedForFirstTime = sharedPreferences?.getBoolean(MERCHANT_HOME, true)
                    Timber.d("Is opened for the first time -> $isOpenedForFirstTime")
                    if (isOpenedForFirstTime == true) {
                        mBinding.emptyState.root.isVisible = false
                        mBinding.merchantRV.isVisible = false
                        mBinding.viewBranchTV.isVisible = false
                    } else {
                        showMinimalEmptyState(message = getString(R.string.no_transactions_found))
                    }
                }

                else -> {
                    initRecyclerView(it)
                    makeDataVisible()
                }
            }

        }
    }

    override fun onResume() {
        super.onResume()
        Timber.d("onResume")
        val appBar: AppBarLayout? = (activity)?.findViewById(R.id.customAppBar)
        val ivRefresh: ImageButton? = appBar?.findViewById(R.id.iv_refresh)
        ivRefresh?.isVisible = true
        ivRefresh?.setOnClickListener {
            Timber.d("let's refresh")
            shouldFetchData.value = true
        }
    }

    private fun updateTransactionSummary() {
        val token = sessionManager.fetchAuthToken().toString()
        viewModel.updateTransactionSummaryCache(token).observe(viewLifecycleOwner) {

            when (it) {

                is Resource.Failure -> {
                    progressDialog.hide()
//                    showEmptyState(it.errorMsg)
                    val errorDialog = ErrorDialog.getInstance(message = it.errorMsg)
                    errorDialog.show(activity?.supportFragmentManager!!, errorDialog.tag)
//                    mBinding.emptyState.root.isVisible = true
//                    mBinding.merchantRV.isVisible = false
//                    mBinding.viewBranchTV.isVisible = false
                }

                Resource.Loading -> {
                    progressDialog.show()
//                    hideBothEmptyStateAndRecyclerView()
//                    mBinding.emptyState.root.isVisible = false
//                    mBinding.merchantRV.isVisible = false
//                    mBinding.viewBranchTV.isVisible = false
                }

                is Resource.Success -> {
                    progressDialog.hide()
                    Timber.d("Success -> $it")

                    val isPaymentSetupNotComplete =
                        it.value.isPaymentSetupComplete == false || it.value.isPaymentSetupComplete.isNull
                    if (isPaymentSetupNotComplete) {
                        val bottomSheet = PaymentSetupReminderBottomSheet()
                        bottomSheet.show(activity?.supportFragmentManager!!, bottomSheet.tag)
                    }

//                    mBinding.emptyState.root.isVisible = false
//                    mBinding.merchantRV.isVisible = true
//                    mBinding.viewBranchTV.isVisible = true
                }

            }

        }
    }

    private fun initRecyclerView(transactionSummary: MerchantTransactionSummaryEntity) {

        mBinding.merchantRV.layoutManager = GridLayoutManager(context, 2)

        Timber.d("data -> $transactionSummary")

        val adapter = MerchantHomeAdapter(transactionSummary)
        mBinding.merchantRV.adapter = adapter

    }

    private fun initVM() {

        viewModel = (activity as MerchantDashBoardActivity).merchantHomeViewModel

    }

    fun makeDataVisible() {
        mBinding.apply {
            merchantRV.visibility = View.VISIBLE
            viewBranchTV.visibility = View.VISIBLE

            emptyState.root.isVisible = false
        }
    }

    private fun hideBothEmptyStateAndRecyclerView() {
        mBinding.root.isVisible = false
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

        mBinding.emptyState.btnTryAgain.setOnClickListener { updateTransactionSummary() }

    }

    override fun onStop() {
        val appBar: AppBarLayout? = (activity)?.findViewById(R.id.customAppBar)
        val ivRefresh: ImageButton? = appBar?.findViewById(R.id.iv_refresh)
        ivRefresh?.isVisible = false
        super.onStop()
        val isOpenedForFirstTime = sharedPreferences?.getBoolean(MERCHANT_HOME, true)
        if (isOpenedForFirstTime == true) {
            sharedPreferences?.edit { putBoolean(MERCHANT_HOME, false) }
        }
        Timber.d("isFreshLogin -> $isOpenedForFirstTime")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}