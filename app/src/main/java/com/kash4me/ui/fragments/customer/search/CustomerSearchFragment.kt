package com.kash4me.ui.fragments.customer.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Button
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.kash4me.R
import com.kash4me.data.models.Merchant
import com.kash4me.databinding.FragmentCustomerSearchBinding
import com.kash4me.network.ApiServices
import com.kash4me.network.NetworkConnectionInterceptor
import com.kash4me.network.NotFoundInterceptor
import com.kash4me.repository.MerchantsRepository
import com.kash4me.ui.activity.customer.customer_dashboard.CustomerDashboardActivity
import com.kash4me.ui.activity.customer.merchant_details.MerchantDetailsActivity
import com.kash4me.ui.dialog.ErrorDialog
import com.kash4me.utils.CurrentLocation
import com.kash4me.utils.LocationUtils
import com.kash4me.utils.SessionManager
import com.kash4me.utils.extensions.showToast
import com.kash4me.utils.listeners.SingleParamItemClickListener
import timber.log.Timber


class CustomerSearchFragment : Fragment() {

    companion object {
        fun newInstance() = CustomerSearchFragment()
    }

    private val mAdapter by lazy { CustomerSearchAdapter(clickListener = mMerchantClickListener) }
    private val mMerchantClickListener
        get() = object : SingleParamItemClickListener<Merchant> {
            override fun onClick(item: Merchant) {

                if (item.isDeleted == true) {
                    showToast(R.string.user_has_been_deleted_cant_view_details)
                    return
                }

                Timber.d("Merchant details -> $item")
                val intent = MerchantDetailsActivity.getNewIntent(
                    packageContext = requireActivity(),
                    merchantId = item.id,
                    merchantName = item.name
                )
                requireActivity().startActivity(intent)
            }
        }

    private val mViewModel: CustomerSearchViewModel by lazy {
        val apiInterface = ApiServices.invoke(
            NetworkConnectionInterceptor(requireContext().applicationContext),
            NotFoundInterceptor()
        )

        val merchantsRepository = MerchantsRepository(apiInterface)

        ViewModelProvider(
            this,
            CustomerSearchViewModelFactory(merchantsRepository)
        )[CustomerSearchViewModel::class.java]
    }

    private val shouldFetchData = MutableLiveData<Boolean>()

    private val sessionManager: SessionManager by lazy { SessionManager(requireContext()) }

    private var azBtnClicked: Boolean = false
    private var allBtnClicked: Boolean = false

    private var selectedTab: TABS = TABS.NEAREST

    private var cashBackBtnClicked: Boolean = false

    private var _binding: FragmentCustomerSearchBinding? = null
    private val mBinding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCustomerSearchBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        etFindMerchantTextChangeListener()
        btnAllListener()
        btnAzListener()
        btnNearestListener()
        btnCashbackListener()

        shouldFetchData.observe(viewLifecycleOwner) { shouldFetch ->

            Timber.d("value: ${mViewModel.orderBy} | search: ${mViewModel.searchQuery}")
            Timber.d("lat: ${mViewModel.latitude} | lon: ${mViewModel.longitude}")

            if (shouldFetch) {
                makeFilterOptions(
                    orderBy = mViewModel.orderBy,
                    lat = mViewModel.latitude,
                    lng = mViewModel.longitude,
                    searchQuery = mViewModel.searchQuery
                )
                shouldFetchData.value = false
            }

        }

        initRvCustomers()

        selectedTab = TABS.NEAREST
        mViewModel.latitude = CurrentLocation.lat.toString()
        mViewModel.longitude = CurrentLocation.lng.toString()
//        makeFilterOptions(lat = mViewModel.latitude, lng = mViewModel.longitude)
        tilSearchTransactionsListener()

    }

    private fun initRvCustomers() {
        mBinding.customerRV.layoutManager = LinearLayoutManager(mBinding.root.context)
        mBinding.customerRV.adapter = mAdapter
    }

    override fun onResume() {
        super.onResume()
        shouldFetchData.value = true
    }

    private fun etFindMerchantTextChangeListener() {
        mBinding.findMerchantET.editText?.doOnTextChanged { text, _, _, _ ->
            when (mBinding.filterButtonMBTG.checkedButtonId) {
                R.id.allBtn -> {
                    mViewModel.orderBy = ""
                    mViewModel.latitude = CurrentLocation.lat.toString()
                    mViewModel.longitude = CurrentLocation.lng.toString()
                    mViewModel.searchQuery = text.toString()
                    shouldFetchData.value = true
                }

                R.id.cashBackBtn -> {
                    mViewModel.orderBy = updateCashBackBtn()
                    mViewModel.latitude = CurrentLocation.lat.toString()
                    mViewModel.longitude = CurrentLocation.lng.toString()
                    mViewModel.searchQuery = text.toString()
                    shouldFetchData.value = true
                }

                R.id.azBtn -> {
                    mViewModel.orderBy = updateAZBtn()
                    mViewModel.latitude = CurrentLocation.lat.toString()
                    mViewModel.longitude = CurrentLocation.lng.toString()
                    mViewModel.searchQuery = text.toString()
                    shouldFetchData.value = true
                }

                R.id.nearestBtn -> {
                    mViewModel.orderBy = ""
                    mViewModel.latitude = CurrentLocation.lat.toString()
                    mViewModel.longitude = CurrentLocation.lng.toString()
                    mViewModel.searchQuery = text.toString()
                    shouldFetchData.value = true
                }
            }

        }
    }

    private fun btnAllListener() {
        mBinding.allBtn.setOnClickListener {

            if (selectedTab == TABS.ALL) return@setOnClickListener
            selectedTab = TABS.ALL

            //            val value: String = updateAZBtn()
            mViewModel.orderBy = ""
            shouldFetchData.value = true
            mViewModel.orderBy = ""
            mViewModel.searchQuery = ""
//            makeFilterOptions(ordering = viewModel.value, search = viewModel.searchQuery)
            allBtnClicked = !allBtnClicked
            clearSearchField()
            removeDrawableFromButton(mBinding.azBtn)
            removeDrawableFromButton(mBinding.nearestBtn)
            removeDrawableFromButton(mBinding.cashBackBtn)

        }
    }

    private fun btnAzListener() {
        mBinding.azBtn.setOnClickListener {

            if (selectedTab == TABS.A_Z) return@setOnClickListener
            selectedTab = TABS.A_Z

            mViewModel.orderBy = updateAZBtn()
            shouldFetchData.value = true
            mViewModel.orderBy = ""
            mViewModel.searchQuery = ""
//            makeFilterOptions(ordering = viewModel.value)
            azBtnClicked = !azBtnClicked
            clearSearchField()
            removeDrawableFromButton(mBinding.azBtn)

        }
    }

    private fun btnNearestListener() {
        mBinding.nearestBtn.setOnClickListener {

            if (selectedTab == TABS.NEAREST) return@setOnClickListener
            selectedTab = TABS.NEAREST

            val lastKnownLocation = LocationUtils().getCurrentLocation(context = requireContext())

            if (lastKnownLocation == null) {
                showToast("Couldn't get Nearest Stores")
            } else {
                mViewModel.latitude = lastKnownLocation.lat.toString()
                mViewModel.longitude = lastKnownLocation.lng.toString()
                shouldFetchData.value = true
                mViewModel.orderBy = ""
                mViewModel.searchQuery = ""
            }

            clearSearchField()
            removeDrawableFromButton(mBinding.nearestBtn)
        }
    }

    private fun btnCashbackListener() {
        mBinding.cashBackBtn.setOnClickListener {

            if (selectedTab == TABS.CASHBACK) return@setOnClickListener
            selectedTab = TABS.CASHBACK

            mViewModel.orderBy = updateCashBackBtn()
            shouldFetchData.value = true
            mViewModel.orderBy = ""
            mViewModel.searchQuery = ""
//            makeFilterOptions(ordering = viewModel.value)
            cashBackBtnClicked = !cashBackBtnClicked
            clearSearchField()
            removeDrawableFromButton(mBinding.cashBackBtn)
        }
    }

    private fun updateCashBackBtn(): String {
        return "cashback_amount"
//        return if (cashBackBtnClicked) {
//            cashBackBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_up, 0, 0, 0)
//            "-cashback_amount"
//        } else {
//            cashBackBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_down, 0, 0, 0)
//            "cashback_amount"
//
//        }
    }

    private fun updateAZBtn(): String {
        return "shop_name"
//        return if (azBtnClicked) {
//            azBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_up, 0, 0, 0)
//            "-shop_name"
//        } else {
//            azBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_down, 0, 0, 0)
//            "shop_name"
//        }
    }

    private fun clearSearchField() {
        mBinding.findMerchantET.editText?.text?.clear()
    }

    private fun removeDrawableFromButton(selectedButton: Button) {
        if (selectedButton != mBinding.azBtn) {
            mBinding.azBtn.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
        }

        if (selectedButton != mBinding.cashBackBtn) {
            mBinding.cashBackBtn.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
        }

        if (selectedButton != mBinding.nearestBtn) {
            mBinding.nearestBtn.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
        }

    }


    private fun makeFilterOptions(
        lat: String = "",
        lng: String = "",
        orderBy: String = "",
        searchQuery: String = ""
    ) {

        val filterOptions = HashMap<String, String>()
        if (lat.isNotEmpty() && lat != "0.0" && lng.isNotEmpty() && lng != "0.0") {
            filterOptions["lat"] = lat
            filterOptions["lng"] = lng
        }

        if (orderBy.isNotEmpty()) {
            filterOptions["ordering"] = orderBy
        }

        if (searchQuery.isNotEmpty()) {
            filterOptions["search"] = searchQuery
        }
        fetchNearByMerchantData(filterOptions)

        Timber.d("filter options -> $filterOptions")

    }

    private fun fetchNearByMerchantData(filterOptions: Map<String, String>) {

        if (!filterOptions.containsKey("search")) {
            ((activity) as CustomerDashboardActivity).customDialogClass.show()
        }

        val token = sessionManager.fetchAuthToken().toString()
        mViewModel.getMerchants(token, filterOptions = filterOptions)

        mViewModel.nearByMerchantResponse.observe(viewLifecycleOwner) {

            Timber.d("Success -> ${it.results}")

            val merchants = arrayListOf<Merchant>()

            when (selectedTab) {
                TABS.ALL -> {
                    merchants.clear()
                    merchants.addAll(it.results)
                }

                TABS.A_Z -> {
                    merchants.clear()
                    merchants.addAll(it.results)
                }

                TABS.NEAREST -> {
                    merchants.clear()
                    merchants.addAll(it.results)
                }

                TABS.CASHBACK -> {
                    merchants.clear()
                    merchants.addAll(it.results)
                }
            }

            if (it.results.isEmpty()) {
                showEmptyState(title = "No Merchant Stores found", subtitle = "")
            } else {
                showRecyclerView(results = merchants)
            }
            ((activity) as CustomerDashboardActivity).customDialogClass.hide()

        }


        mViewModel.errorMessage.observe(viewLifecycleOwner) {

            val errorDialog = ErrorDialog.getInstance(message = it)
            errorDialog.show(activity?.supportFragmentManager!!, errorDialog.tag)
            showEmptyState(title = it, subtitle = "")
            ((activity) as CustomerDashboardActivity).customDialogClass.hide()

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

    private fun showRecyclerView(results: List<Merchant>) {
        mBinding.apply {
            mAdapter.setData(stores = results)
            customerRV.isVisible = true

            emptyState.root.isVisible = false
        }
    }

    private fun tilSearchTransactionsListener() {

        mBinding.findMerchantET.editText?.setOnEditorActionListener { textView, actionId, keyEvent ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {

                val userQuery = mBinding.findMerchantET.editText?.text

                when (mBinding.filterButtonMBTG.checkedButtonId) {
                    R.id.allBtn -> {
                        mViewModel.orderBy = ""
                        mViewModel.latitude = CurrentLocation.lat.toString()
                        mViewModel.longitude = CurrentLocation.lng.toString()
                        mViewModel.searchQuery = userQuery.toString()
                        shouldFetchData.value = true
                    }

                    R.id.cashBackBtn -> {
                        mViewModel.orderBy = updateCashBackBtn()
                        mViewModel.latitude = CurrentLocation.lat.toString()
                        mViewModel.longitude = CurrentLocation.lng.toString()
                        mViewModel.searchQuery = userQuery.toString()
                        shouldFetchData.value = true
                    }

                    R.id.azBtn -> {
                        mViewModel.orderBy = updateAZBtn()
                        mViewModel.latitude = CurrentLocation.lat.toString()
                        mViewModel.longitude = CurrentLocation.lng.toString()
                        mViewModel.searchQuery = userQuery.toString()
                        shouldFetchData.value = true
                    }

                    R.id.nearestBtn -> {
                        mViewModel.orderBy = ""
                        mViewModel.latitude = CurrentLocation.lat.toString()
                        mViewModel.longitude = CurrentLocation.lng.toString()
                        mViewModel.searchQuery = userQuery.toString()
                        shouldFetchData.value = true
                    }
                }

                true

            } else
                false
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private enum class TABS {
        ALL, A_Z, NEAREST, CASHBACK
    }


}