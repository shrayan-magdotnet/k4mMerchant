package com.kash4me.ui.fragments.merchant.search.assign_cashback.fragment.customer_details.rollback

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.kash4me.data.models.UserDetails
import com.kash4me.merchant.databinding.FragmentRevertCashbackBinding
import com.kash4me.network.ApiServices
import com.kash4me.network.NetworkConnectionInterceptor
import com.kash4me.network.NotFoundInterceptor
import com.kash4me.repository.CustomerDetailsFromMerchantRepository
import com.kash4me.ui.dialog.ErrorDialog
import com.kash4me.ui.dialog.SuccessDialog
import com.kash4me.ui.fragments.merchant.search.assign_cashback.AssignCashbackActivity
import com.kash4me.ui.fragments.merchant.search.assign_cashback.fragment.customer_details.CustomerDetailsFromMerchantViewModel
import com.kash4me.ui.fragments.merchant.search.assign_cashback.fragment.customer_details.CustomerDetailsFromMerchantViewModelFactory
import com.kash4me.utils.SessionManager
import com.kash4me.utils.custom_views.CustomProgressDialog
import com.kash4me.utils.extensions.getAmount
import com.kash4me.utils.extensions.getEmptyIfNull
import com.kash4me.utils.extensions.getMinusOneIfNull
import com.kash4me.utils.extensions.showToast
import com.kash4me.utils.listeners.AfterDismissalListener
import com.kash4me.utils.network.Resource
import timber.log.Timber

class RevertCashbackFragment : Fragment() {

    private var mAmountSpent: Double = 0.0

    var customerId: Int? = null
    var merchantId: Int? = null

    private val progressDialog by lazy { CustomProgressDialog(requireContext()) }

    val sessionManager by lazy { SessionManager(requireContext()) }

    private val viewModel: CustomerDetailsFromMerchantViewModel by lazy {
        val apiInterface =
            ApiServices.invoke(
                NetworkConnectionInterceptor(requireContext().applicationContext),
                NotFoundInterceptor()
            )
        val merchantCustomerListRepository = CustomerDetailsFromMerchantRepository(apiInterface)
        ViewModelProvider(
            this,
            CustomerDetailsFromMerchantViewModelFactory(merchantCustomerListRepository)
        )[CustomerDetailsFromMerchantViewModel::class.java]
    }

    private var _binding: FragmentRevertCashbackBinding? = null
    private val mBinding get() = _binding!!

    companion object {
        fun newInstance() = RevertCashbackFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRevertCashbackBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()

        customerId = (activity as AssignCashbackActivity).intent.getIntExtra("customerId", 0)
        merchantId = sessionManager.fetchMerchantDetails()?.id
        val customerDetails: UserDetails? =
            (activity as AssignCashbackActivity).intent.getParcelableExtra("customer_details")
        mBinding.tvCustomerName.text = customerDetails?.nick_name
        mBinding.tvCode.text = customerDetails?.unique_id.toString()

        etPurchaseAmountListener()

        Timber.d("Merchant ID: $merchantId | Customer ID: $customerId")

        mBinding.btnSend.setOnClickListener {

            if (mAmountSpent < 0.1) {
                showToast("Ensure this value is greater than or equal to 0.1")
                return@setOnClickListener
            }

            rollbackTransaction(amount = mAmountSpent.toString())

        }

    }

    private fun setupToolbar() {
        (activity as AssignCashbackActivity).apply {
            setSupportActionBar(mBinding.toolbarLayout.toolbar)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.title = "Revert Cash Back"
        }
        mBinding.toolbarLayout.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun etPurchaseAmountListener() {
        mBinding.etPurchaseAmount.doAfterTextChanged {
            mAmountSpent = mBinding.etPurchaseAmount.getAmount()
            Timber.d("Amount -> $mAmountSpent")
        }
    }

    private fun rollbackTransaction(amount: String) {
        val token = sessionManager.fetchAuthToken().getEmptyIfNull()
        viewModel.rollbackTransaction(
            token = token,
            amountSpent = amount,
            customerId = customerId.getMinusOneIfNull(),
            merchantId = merchantId.getMinusOneIfNull()
        ).observe(viewLifecycleOwner) {

            when (it) {
                is Resource.Failure -> {
                    progressDialog.hide()
                    val errorDialog = ErrorDialog.getInstance(message = it.errorMsg)
                    errorDialog.show(activity?.supportFragmentManager!!, errorDialog.tag)
                }

                Resource.Loading -> {
                    progressDialog.show()
                }

                is Resource.Success -> {
                    progressDialog.hide()
                    val successDialog = SuccessDialog.getInstance(
                        message = it.value.message.getEmptyIfNull(),
                        afterDismissClicked = object : AfterDismissalListener {
                            override fun afterDismissed() {
                                findNavController().popBackStack()
                            }
                        }
                    )
                    successDialog.show(activity?.supportFragmentManager!!, successDialog.tag)
                    Timber.d("Success -> ${it.value}")
                }
            }

        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}