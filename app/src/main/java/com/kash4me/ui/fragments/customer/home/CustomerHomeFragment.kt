package com.kash4me.ui.fragments.customer.home

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.appbar.AppBarLayout
import com.kash4me.data.local.customer.cashback.CashbackEntity
import com.kash4me.data.local.customer.cashback.ShopDetails
import com.kash4me.data.models.user.UserType
import com.kash4me.merchant.R
import com.kash4me.merchant.databinding.FragmentCustomerHomeBinding
import com.kash4me.ui.activity.customer.merchant_details.MerchantDetailsActivity
import com.kash4me.ui.dialog.ErrorDialog
import com.kash4me.utils.SessionManager
import com.kash4me.utils.custom_views.CustomProgressDialog
import com.kash4me.utils.extensions.formatUsingCurrencySystem
import com.kash4me.utils.extensions.getEmptyIfNull
import com.kash4me.utils.extensions.getMinusOneIfNull
import com.kash4me.utils.extensions.showToast
import com.kash4me.utils.listeners.SingleParamItemClickListener
import com.kash4me.utils.network.Resource
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
@AndroidEntryPoint
class CustomerHomeFragment : Fragment() {

    companion object {

        const val CUSTOMER_HOME = "customer_home"

        fun newInstance() = CustomerHomeFragment()

    }

    private val cashbacksAdapter =
        CustomerHomeAdapter(object : SingleParamItemClickListener<ShopDetails?> {
            override fun onClick(shopDetails: ShopDetails?) {

                if (shopDetails?.shopId == null) {
                    showToast(R.string.user_has_been_deleted_cant_view_details)
                    return
                }

                val intent = MerchantDetailsActivity.getNewIntent(
                    packageContext = requireActivity(),
                    merchantId = shopDetails.shopId.getMinusOneIfNull(),
                    merchantName = shopDetails.name.getEmptyIfNull()
                )

                activity?.startActivity(intent)
            }
        })

    private val originalCashbacksList = arrayListOf<CashbackEntity>()
    private val viewModel: CustomerHomeViewModel by viewModels()

    private lateinit var sessionManager: SessionManager

    private var selectedTab = TABS.ALL

    private var allBtnClicked: Boolean = false
    private var leftBtnClicked: Boolean = false
    private var earnedBtnClicked: Boolean = false
    private var processingBtnClicked: Boolean = false

    private val customProgressDialog by lazy { CustomProgressDialog(requireContext()) }

    private var sharedPreferences: SharedPreferences? = null

    private val shouldRefresh = MutableLiveData<Boolean>()

    override fun onAttach(context: Context) {
        try {
            super.onAttach(context)
        } catch (ex: Exception) {
            Timber.d("Caught exception -> ${ex.message}")
        }
    }

    private var _binding: FragmentCustomerHomeBinding? = null
    private val mBinding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCustomerHomeBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sessionManager = SessionManager(requireActivity().applicationContext)
        sharedPreferences = context?.getSharedPreferences(
            requireActivity().getString(R.string.first_start_status), Context.MODE_PRIVATE
        )

        initRvCashbacks()

        shouldRefresh.observe(viewLifecycleOwner) { shouldRefresh ->

            Timber.d("value: ${viewModel.value} | search: ${viewModel.searchQuery}")

            if (shouldRefresh) {
//                makeFilterOptions(ordering = viewModel.value, search = viewModel.searchQuery)
                cashbacksAdapter.filter.filter(viewModel.searchQuery)
                this.shouldRefresh.value = false
            }

        }

        viewModel.getCashbacks().observe(viewLifecycleOwner) {

            Timber.d("Cashbacks from database -> $it")

            when {

                it.isEmpty() -> {
                    val isOpenedForFirstTime = sharedPreferences?.getBoolean(CUSTOMER_HOME, true)
                    if (isOpenedForFirstTime == true) {
                        mBinding.emptyState.root.isVisible = false
                        mBinding.customerRV.isVisible = false
                    } else {
                        showEmptyState(
                            title = "No Cash Back Cards Available",
                            subtitle = "Start searching the store of your choice and get cash back cards accumulated"
                        )
                    }
                }

                else -> {

                    showMerchantCardsCount(it)

                    showRecyclerView()
                    originalCashbacksList.clear()
                    originalCashbacksList.addAll(it)
                    cashbacksAdapter.setData(it)
                }

            }

        }

        mBinding.findCashBackET.editText?.doOnTextChanged { text, _, _, _ ->
            when (mBinding.filterButtonLL.checkedButtonId) {
                R.id.allBtn -> {
                    viewModel.value = ""
                    viewModel.searchQuery = text.toString()
                    shouldRefresh.value = true
                }

                R.id.leftBtn -> {
                    viewModel.value = updateLeftBtn()
                    viewModel.searchQuery = text.toString()
                    shouldRefresh.value = true
                }

                R.id.earnedBtn -> {
                    viewModel.value = updateEarnedBtn()
                    viewModel.searchQuery = text.toString()
                    shouldRefresh.value = true
                }

                R.id.processingBtn -> {
                    viewModel.value = updateProcessingBtn()
                    viewModel.searchQuery = text.toString()
                    shouldRefresh.value = true
                }
            }

        }

        mBinding.allBtn.setOnClickListener {

            if (selectedTab == TABS.ALL) return@setOnClickListener

            viewModel.value = ""
//            shouldRefresh.value = true
            Timber.d("All cashbacks -> $originalCashbacksList")
            cashbacksAdapter.setData(cashbacks = originalCashbacksList)
            allBtnClicked = !allBtnClicked
            selectedTab = TABS.ALL
            clearSearchField()

        }

        mBinding.leftBtn.setOnClickListener {

            if (selectedTab == TABS.LEFT) return@setOnClickListener

            viewModel.value = updateLeftBtn()
            val sortedCashbacks = cashbacksAdapter.getCashbacks().sortedBy { cashback ->
                cashback.amountLeft?.toDoubleOrNull()
            }
            cashbacksAdapter.setData(cashbacks = sortedCashbacks)
//            shouldRefresh.value = true
            leftBtnClicked = !leftBtnClicked
            selectedTab = TABS.LEFT
            clearSearchField()

        }

        mBinding.earnedBtn.setOnClickListener {

            if (selectedTab == TABS.EARNED) return@setOnClickListener

            viewModel.value = updateEarnedBtn()
            val sortedCashbacks = cashbacksAdapter.getCashbacks().sortedBy { cashback ->
                cashback.cashbackAmount?.toDoubleOrNull()
            }
            cashbacksAdapter.setData(cashbacks = sortedCashbacks)
//            shouldRefresh.value = true
            earnedBtnClicked = !earnedBtnClicked
            selectedTab = TABS.EARNED
            clearSearchField()
        }

        mBinding.processingBtn.setOnClickListener {

            if (selectedTab == TABS.PROCESSING) return@setOnClickListener

            viewModel.value = updateProcessingBtn()
            val sortedCashbacks = cashbacksAdapter.getCashbacks().sortedBy { cashback ->
                cashback.processingAmount?.toDoubleOrNull()
            }
            cashbacksAdapter.setData(cashbacks = sortedCashbacks)

//            shouldRefresh.value = true
            selectedTab = TABS.PROCESSING
            processingBtnClicked = !processingBtnClicked
            clearSearchField()
        }


//        value = "cashback_amount"
        shouldRefresh.value = true

    }

    private fun showMerchantCardsCount(it: List<CashbackEntity>) {
        val count = it.getOrNull(0)?.count
        val amount = "$" + it.getOrNull(0)?.totalProcessing?.toDoubleOrNull()
            ?.formatUsingCurrencySystem()
        val fullText = "You have accumulated $count cards with $amount on your way."

        val spannableString = SpannableString(fullText)

        // Set red color for all other words
        spannableString.setSpan(
            ForegroundColorSpan(
                ContextCompat.getColor(requireContext(), R.color.blue)
            ),
            0, fullText.length, 0
        )

        // Set blue color for the number
        val numberIndex = fullText.indexOf(count.toString())
        if (numberIndex != -1) {
            spannableString.setSpan(
                ForegroundColorSpan(
                    ContextCompat.getColor(requireContext(), R.color.orange)
                ),
                numberIndex, numberIndex + count.toString().length, 0
            )
        }

        // Set blue color for the amount
        val amountIndex = fullText.indexOf(amount)
        if (amountIndex != -1) {
            spannableString.setSpan(
                ForegroundColorSpan(
                    ContextCompat.getColor(requireContext(), R.color.orange)
                ),
                amountIndex, amountIndex + amount.length, 0
            )
        }

        mBinding.tvCashbackDetails.text = spannableString
    }

    private fun initRvCashbacks() {
        mBinding.customerRV.layoutManager = LinearLayoutManager(mBinding.root.context)
        mBinding.customerRV.adapter = cashbacksAdapter
    }

    private fun fetchCashbacks() {
        viewModel.updateCashbacksInCache().observe(viewLifecycleOwner) {

            when (it) {
                is Resource.Failure -> {
                    customProgressDialog.hide()
                    val errorDialog = ErrorDialog.getInstance(message = it.errorMsg)
                    errorDialog.show(activity?.supportFragmentManager!!, errorDialog.tag)
//                    mBinding.customerRV.isVisible = false
//                    mBinding.emptyState.root.isVisible = true
                }

                Resource.Loading -> {
                    customProgressDialog.show()
//                    mBinding.customerRV.isVisible = false
//                    mBinding.emptyState.root.isVisible = false
                }

                is Resource.Success -> {
                    showRecyclerView()
                    customProgressDialog.hide()
//                    mBinding.customerRV.isVisible = true
//                    mBinding.emptyState.root.isVisible = false
                }
            }

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

            if (sessionManager.fetchUserType() == UserType.ANONYMOUS) {
                val errorDialog = ErrorDialog.getInstance(
                    message = "Please login to fetch cashback cards"
                )
                errorDialog.show(activity?.supportFragmentManager!!, errorDialog.tag)
            } else {
                fetchCashbacks()
            }

        }
        val ivInfo: ImageButton? = appBar?.findViewById(R.id.iv_info)
        ivInfo?.visibility = View.VISIBLE
        ivInfo?.setOnClickListener {
            Timber.d("Let's show info bottom sheet")
            val bottomSheet = CustomerHomeBottomSheet()
            bottomSheet.show(activity?.supportFragmentManager!!, bottomSheet.tag)
        }
    }

    override fun onResume() {
        super.onResume()

        val isOpenedForFirstTime = sharedPreferences?.getBoolean(CUSTOMER_HOME, true)
        Timber.d("onResume: isFreshLogin -> $isOpenedForFirstTime")

        if (isOpenedForFirstTime == true) {
            if (sessionManager.fetchUserType() == UserType.CUSTOMER) {
                fetchCashbacks()
            }
        }

    }

    private fun updateLeftBtn(): String {
        return "amount_left"
//        return if (leftBtnClicked) {
//            leftBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_up, 0, 0, 0)
//            "-amount_left"
//        } else {
//            leftBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_down, 0, 0, 0)
//            "amount_left"
//        }
    }

    private fun updateAllBtn() {
        return if (allBtnClicked) {
            mBinding.allBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_up, 0, 0, 0)
        } else {
            mBinding.allBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_down, 0, 0, 0)
        }
    }

    private fun updateEarnedBtn(): String {
        return "cashback_amount"
//        return if (earnedBtnClicked) {
//            earnedBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_up, 0, 0, 0)
//            "-cashback_amount"
//        } else {
//            earnedBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_down, 0, 0, 0)
//            "cashback_amount"
//        }
    }

    private fun updateProcessingBtn(): String {
        return "processing_amount"
//        return if (processingBtnClicked) {
//            processingBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_up, 0, 0, 0)
//            "-processing"
//        } else {
//            processingBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_down, 0, 0, 0)
//            "processing"
//        }
    }

    private fun makeFilterOptions(
        ordering: String = "",
        search: String = ""
    ) {

        val filterOptions = HashMap<String, String>()

        if (ordering.isNotEmpty()) {
            filterOptions["ordering"] = ordering
        }

        if (search.isNotEmpty()) {
            filterOptions["search"] = search
        }
        fetchCustomerCashBackDetails(filterOptions)

        Timber.d("filter options -> $filterOptions")

    }


    private fun fetchCustomerCashBackDetails(filterOptions: Map<String, String>) {

        if (!filterOptions.containsKey("search")) {

            customProgressDialog.show()

        }

        val token = sessionManager.fetchAuthToken().toString()
        viewModel.fetchCustomerCashBackDetails(token, filterOptions = filterOptions)

//        viewModel.customerCashBackResponse.observe(viewLifecycleOwner) {
//
//            val cashbackList = when (selectedTab) {
//                TABS.ALL -> {
//                    it.results
//                }
//                TABS.LEFT -> {
//                    it.results.sortedBy { cashback -> cashback.amount_left }
//                }
//                TABS.EARNED -> {
//                    it.results.sortedBy { cashback -> cashback.cashback_amount }
//                }
//                TABS.PROCESSING -> {
//                    it.results.sortedBy { cashback -> cashback.processing_amount }
//                }
//            }
//
//            Timber.d("data -> $it")
//
//            if (it.results.isEmpty()) {
//                showEmptyState(message = "No data found")
//            } else {
//                showRecyclerView(results = cashbackList)
//            }
//            customProgressDialog.hide()
//
//
//        }

        viewModel.errorMessage.observe(viewLifecycleOwner) {
            val errorDialog = ErrorDialog.getInstance(message = it)
            errorDialog.show(activity?.supportFragmentManager!!, errorDialog.tag)
            showEmptyState(title = it, subtitle = "")
            customProgressDialog.hide()
        }
    }

    private fun showEmptyState(title: String, subtitle: String) {
        mBinding.apply {
            emptyState.tvTitle.text = title
            emptyState.tvSubtitle.text = subtitle
            emptyState.root.isVisible = true

            customerRV.isVisible = false
        }
    }

    private fun showRecyclerView() {
        mBinding.apply {
            customerRV.isVisible = true

            emptyState.root.isVisible = false
        }
    }

    private fun clearSearchField() {
        mBinding.findCashBackET.editText?.text?.clear()
    }

    override fun onStop() {
        val appBar: AppBarLayout? = (activity)?.findViewById(R.id.customAppBar)
        val ivRefresh: ImageButton? = appBar?.findViewById(R.id.iv_refresh)
        ivRefresh?.visibility = View.INVISIBLE
        val ivInfo: ImageButton? = appBar?.findViewById(R.id.iv_info)
        ivInfo?.visibility = View.GONE
        super.onStop()

        val isOpenedForFirstTime = sharedPreferences?.getBoolean(CUSTOMER_HOME, true)
        if (isOpenedForFirstTime == true) {
            sharedPreferences?.edit { putBoolean(CUSTOMER_HOME, false) }
        }
        Timber.d("isFreshLogin -> $isOpenedForFirstTime")

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private enum class TABS {
        ALL, LEFT, EARNED, PROCESSING
    }


}