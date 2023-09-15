package com.kash4me.ui.fragments.merchant.search

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.textfield.TextInputLayout
import com.kash4me.data.models.CustomerDetails
import com.kash4me.merchant.R
import com.kash4me.merchant.databinding.FragmentMerchantSearchBinding
import com.kash4me.network.ApiServices
import com.kash4me.network.NetworkConnectionInterceptor
import com.kash4me.network.NotFoundInterceptor
import com.kash4me.repository.MerchantCustomerListRepository
import com.kash4me.ui.dialog.ErrorDialog
import com.kash4me.ui.fragments.merchant.search.assign_cashback.AssignCashbackActivity
import com.kash4me.utils.SessionManager
import com.kash4me.utils.custom_views.CustomProgressDialog
import com.kash4me.utils.extensions.showToast
import com.kash4me.utils.listeners.SingleParamItemClickListener
import timber.log.Timber

class MerchantSearchFragment : Fragment() {

    companion object {

        const val MERCHANT_SEARCH = "merchant_search"

        fun newInstance() = MerchantSearchFragment()

    }

    private lateinit var viewModel: MerchantSearchViewModel

    private lateinit var sessionManager: SessionManager

    private lateinit var recyclerview: RecyclerView
    private lateinit var findCustomerET: TextInputLayout

    private lateinit var materialButtonToggleGroup: MaterialButtonToggleGroup

    private lateinit var progressDialog: CustomProgressDialog

    private var selectedTab = TABS.ALL

    private val shouldFetchData = MutableLiveData<Boolean>()

    private var sharedPreferences: SharedPreferences? = null

    private var _binding: FragmentMerchantSearchBinding? = null
    private val mBinding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMerchantSearchBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    private val searchAdapter = MerchantSearchAdapter(
        clickListener = object : SingleParamItemClickListener<CustomerDetails> {
            override fun onClick(item: CustomerDetails) {

                if (item.user_details?.isUserDeleted() == true) {
                    showToast(R.string.user_has_been_deleted_cant_view_details)
                    return
                }

                val intent = Intent(context, AssignCashbackActivity::class.java)
                intent.putExtra("customerId", item.user_details?.id)
                intent.putExtra("customer_details", item.user_details)
                startActivity(intent)

            }
        })

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sessionManager = SessionManager(requireActivity().applicationContext)
        progressDialog = CustomProgressDialog(context = view.context)

        sharedPreferences = context?.getSharedPreferences(
            requireActivity().getString(R.string.first_start_status), Context.MODE_PRIVATE
        )

        initUI(view)
        initVM()

    }

    override fun onResume() {
        super.onResume()

        checkIfWeShouldRefresh()

    }

    private fun checkIfWeShouldRefresh() {
        // Here, we are not using caching mechanism
        // Only time we need to set this value to true
        // Is when we have assigned a cashback to a customer
        // And we are returning back to this screen
        val shouldRefresh = sharedPreferences?.getBoolean(MERCHANT_SEARCH, false)
        Timber.d("shouldRefresh -> $shouldRefresh")
        if (shouldRefresh == true) {
            Timber.d("Let's fetch from API")
            shouldFetchData.value = true
            sharedPreferences?.edit { putBoolean(MERCHANT_SEARCH, false) }
        }
    }

    private fun initUI(view: View) {
        recyclerview = view.findViewById(R.id.customerRV)
        findCustomerET = view.findViewById(R.id.findCustomerET)
        materialButtonToggleGroup = view.findViewById(R.id.filterButtonMBTG)


        mBinding.allBtn.setOnClickListener {

            if (selectedTab == TABS.ALL) return@setOnClickListener

            viewModel.value = updateAllBtn()
            shouldFetchData.value = true
            selectedTab = TABS.ALL
            clearSearchField()
            removeDrawableFromButton(mBinding.allBtn)
        }

        mBinding.azBtn.setOnClickListener {

            if (selectedTab == TABS.A_Z) return@setOnClickListener

            viewModel.value = updateAtoZBtn()
            shouldFetchData.value = true
            selectedTab = TABS.A_Z
            clearSearchField()
            removeDrawableFromButton(mBinding.azBtn)

        }

        mBinding.earnedBtn.setOnClickListener {

            if (selectedTab == TABS.EARNED) return@setOnClickListener

            viewModel.value = updateEarnedBtn()
            shouldFetchData.value = true
            selectedTab = TABS.EARNED
            clearSearchField()
            removeDrawableFromButton(mBinding.earnedBtn)
        }

        mBinding.processingBtn.setOnClickListener {

            if (selectedTab == TABS.PROCESSING) return@setOnClickListener

            viewModel.value = updateProcessingBtn()
            shouldFetchData.value = true
            selectedTab = TABS.PROCESSING
            clearSearchField()
            removeDrawableFromButton(mBinding.processingBtn)
        }

        shouldFetchData.observe(viewLifecycleOwner) { shouldFetch ->

            Timber.d("value: ${viewModel.value} | search: ${viewModel.searchQuery}")

            if (shouldFetch) {
                makeFilterOptions(ordering = viewModel.value, search = viewModel.searchQuery)
                shouldFetchData.value = false
            }

        }

        // This will pass the ArrayList to our Adapter
        recyclerview.layoutManager = LinearLayoutManager(activity)
        recyclerview.adapter = searchAdapter

        findCustomerET.editText?.doOnTextChanged { text, _, _, _ ->
            when (materialButtonToggleGroup.checkedButtonId) {

                R.id.allBtn -> {
                    viewModel.value = updateAllBtn()
                    viewModel.searchQuery = text.toString()
                    shouldFetchData.value = true
                }

                R.id.azBtn -> {
                    viewModel.value = updateAtoZBtn()
                    viewModel.searchQuery = text.toString()
                    shouldFetchData.value = true
                }

                R.id.earnedBtn -> {
                    viewModel.value = updateEarnedBtn()
                    viewModel.searchQuery = text.toString()
                    shouldFetchData.value = true
                }

                R.id.processingBtn -> {
                    viewModel.value = updateProcessingBtn()
                    viewModel.searchQuery = text.toString()
                    shouldFetchData.value = true
                }

            }

        }

    }

    private fun showCustomersCardsCount(count: Int) {

        val fullText = "You have gained $count customer so far."

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

        mBinding.tvCustomerCount.text = spannableString
    }


    private fun updateAllBtn(): String {
        return ""
//        return if (allBtnClicked) {
//            mBinding.allBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_up, 0, 0, 0)
//            ""
//        } else {
//            mBinding.allBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_down, 0, 0, 0)
//            ""
//        }
    }

    private fun updateAtoZBtn(): String {
        return "name"
//        return if (azBtnClicked) {
//            mBinding.azBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_up, 0, 0, 0)
//            "-name"
//        } else {
//            mBinding.azBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_down, 0, 0, 0)
//            "name"
//        }
    }

    private fun updateEarnedBtn(): String {
        return "cashback_amount"
//        return if (earnedBtnClicked) {
//            mBinding.earnedBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_up, 0, 0, 0)
//            "-cashback_amount"
//        } else {
//            mBinding.earnedBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_down, 0, 0, 0)
//            "cashback_amount"
//        }
    }

    private fun updateProcessingBtn(): String {
        return "processing_amount"
//        return if (processingBtnClicked) {
//            mBinding.processingBtn.setCompoundDrawablesWithIntrinsicBounds(
//                R.drawable.ic_up,
//                0,
//                0,
//                0
//            )
//            "-processing_amount"
//        } else {
//            mBinding.processingBtn.setCompoundDrawablesWithIntrinsicBounds(
//                R.drawable.ic_down,
//                0,
//                0,
//                0
//            )
//            "processing_amount"
//        }
    }

    private fun removeDrawableFromButton(selectedButton: Button) {
        if (selectedButton != mBinding.allBtn) {
            mBinding.allBtn.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
        }

        if (selectedButton != mBinding.azBtn) {
            mBinding.azBtn.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
        }

        if (selectedButton != mBinding.earnedBtn) {
            mBinding.earnedBtn.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
        }

        if (selectedButton != mBinding.processingBtn) {
            mBinding.processingBtn.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
        }
    }

    private fun clearSearchField() {
        findCustomerET.editText?.text?.clear()
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
        fetchCustomerList(filterOptions)

    }

    private fun initVM() {
        val apiInterface =
            ApiServices.invoke(
                NetworkConnectionInterceptor(requireContext().applicationContext),
                NotFoundInterceptor()
            )

        val merchantCustomerListRepository = MerchantCustomerListRepository(apiInterface)


        viewModel = ViewModelProvider(
            this,
            MerchantSearchViewModelFactory(merchantCustomerListRepository)
        )[MerchantSearchViewModel::class.java]


//        makeFilterOptions(
//            lat = CurrentLocation.lat.toString(),
//            lng = CurrentLocation.lng.toString(),
//        )

        fetchCustomerList(HashMap())

    }

    private fun fetchCustomerList(filterOptions: Map<String, String>) {

        if (!filterOptions.containsKey("search")) {
            progressDialog.show()
        }

        val token = sessionManager.fetchAuthToken().toString()
        viewModel.getMerchantCustomerList(token, filterOptions)

        viewModel.customerListResponse.observe(viewLifecycleOwner) {

            progressDialog.hide()
            if (it.results.isEmpty()) {
                showEmptyState(message = "No customers found")
                mBinding.tvCustomerCount.isVisible = false
            } else {
                showRecyclerView(it.results)
                mBinding.tvCustomerCount.isVisible = true
                showCustomersCardsCount(count = it.count)
            }

        }

        viewModel.errorMessage.observe(viewLifecycleOwner) {

            val errorDialog = ErrorDialog.getInstance(message = it)
            errorDialog.show(activity?.supportFragmentManager!!, errorDialog.tag)
            showEmptyState(it)
            progressDialog.hide()

        }
    }

    private fun showEmptyState(message: String) {
        mBinding.apply {
            emptyState.tvTitle.text = message
            emptyState.tvSubtitle.text = ""
            emptyState.root.isVisible = true

            recyclerview.isVisible = false
        }
    }

    private fun showRecyclerView(results: List<CustomerDetails>) {
        mBinding.apply {

            searchAdapter.setData(results)
            recyclerview.isVisible = true

            emptyState.root.isVisible = false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private enum class TABS {
        ALL, A_Z, EARNED, PROCESSING
    }

}