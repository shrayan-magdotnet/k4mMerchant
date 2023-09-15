package com.kash4me.ui.fragments.merchant.search.assign_cashback.fragment.confirm_assign_cashback

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.kash4me.data.models.CashBackSuccessResponse
import com.kash4me.merchant.databinding.FragmentConfirmAssignCashbackBinding
import com.kash4me.ui.dialog.ErrorDialog
import com.kash4me.ui.fragments.merchant.search.assign_cashback.AssignCashbackActivity
import com.kash4me.ui.fragments.merchant.search.assign_cashback.fragment.purchase_amount.PurchaseAmountViewModel
import com.kash4me.utils.App
import com.kash4me.utils.SessionManager
import com.kash4me.utils.custom_views.CustomProgressDialog
import com.kash4me.utils.extensions.formatAsCurrency
import com.kash4me.utils.extensions.formatUsingCurrencySystem
import com.kash4me.utils.extensions.getEmptyIfNull
import com.kash4me.utils.extensions.getZeroIfNull
import com.kash4me.utils.extensions.roundOffToTwoDecimalDigits
import com.kash4me.utils.extensions.toDoubleOrNull
import com.kash4me.utils.network.Resource
import timber.log.Timber

class ConfirmAssignCashbackFragment : Fragment() {

    private var _binding: FragmentConfirmAssignCashbackBinding? = null
    private val mBinding get() = _binding!!

    private var mCashbackValue: String? = null
    private val progressDialog by lazy { CustomProgressDialog(requireContext()) }

    private val customerDetails by lazy { (activity as AssignCashbackActivity).customerDetails }

    private val sessionManager by lazy { SessionManager(context = App.getContext()!!) }

    private val viewModel by lazy {
        ViewModelProvider(this)[PurchaseAmountViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentConfirmAssignCashbackBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()

        mBinding.tvDescription.text =
            "Please confirm the below information are correct for ${customerDetails?.nick_name} (${customerDetails?.unique_id})"

        val args = arguments?.let { ConfirmAssignCashbackFragmentArgs.fromBundle(it) }
        val purchaseAmount = args?.purchaseAmount?.toDoubleOrNull()?.roundOffToTwoDecimalDigits()
        mBinding.tvPurchaseAmount.text = "$" + purchaseAmount?.formatUsingCurrencySystem()
        btnSendListener(args)

        calculateCashbackValue(customerDetails?.id.getZeroIfNull())

    }

    private fun setupToolbar() {
        (activity as AssignCashbackActivity).apply {
            setSupportActionBar(mBinding.toolbarLayout.toolbar)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.title = "Confirm Assigning CashBack"
        }
        mBinding.toolbarLayout.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun calculateCashbackValue(customerId: Int) {
        val args = arguments?.let { ConfirmAssignCashbackFragmentArgs.fromBundle(it) }
        val purchaseAmount = args?.purchaseAmount?.toDoubleOrNull()?.roundOffToTwoDecimalDigits()

        viewModel.calculateCashbackValue(
            amountSpent = purchaseAmount.toString(),
            customerId = customerId
        )
            .observe(viewLifecycleOwner) { resource ->

                when (resource) {

                    is Resource.Failure -> {
                        progressDialog.hide()
                        val errorDialog = ErrorDialog.getInstance(message = resource.errorMsg)
                        errorDialog.show(activity?.supportFragmentManager!!, errorDialog.tag)
                        mCashbackValue = null
                    }

                    Resource.Loading -> {
                        progressDialog.show()
                    }

                    is Resource.Success -> {
                        progressDialog.hide()
                        Timber.d("Result -> ${resource.value}")
                        mBinding.tvCashbackAmount.text =
                            "$" + resource.value.totalCashbackAmount?.formatAsCurrency()
                        mCashbackValue = resource.value.totalCashbackAmount
                    }

                }

            }
    }

    private fun assignCashback(cashbackSettings: Int, purchaseAmount: Double?) {
        viewModel.createCashBackTransaction(
            customerId = customerDetails?.id.getZeroIfNull(),
            cashbackSettings = cashbackSettings,
            amountSpent = purchaseAmount.toString(),
            cashbackAmount = mCashbackValue.getEmptyIfNull()
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
                    Timber.d("response -> ${it.value}")
                    mBinding.tvCashbackAmount.text =
                        "$" + it.value.totalCashbackAmount?.formatAsCurrency()
                }

            }

        }
    }

    fun btnSendListener(args: ConfirmAssignCashbackFragmentArgs?) {

        mBinding.btnSend.setOnClickListener {
            val cashbackSettings =
                sessionManager.fetchMerchantDetails()?.activeCashbackSettings?.id.getZeroIfNull()

            viewModel.createCashBackTransaction(
                customerId = customerDetails?.id.getZeroIfNull(),
                cashbackSettings = cashbackSettings,
                amountSpent = args?.purchaseAmount.toString(),
                cashbackAmount = mCashbackValue.getEmptyIfNull()
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
                        Timber.d("response -> ${it.value}")
                        navigateToAssignSuccessFragment(it)
                    }

                }

            }
        }

    }

    private fun navigateToAssignSuccessFragment(it: Resource.Success<CashBackSuccessResponse>) {
        val action = ConfirmAssignCashbackFragmentDirections
            .actionConfirmAssignCashbackFragmentToAssignCompleteFragment(
                cashbackAmount = it.value.totalCashbackAmount.getEmptyIfNull(),
                customerName = customerDetails?.nick_name.getEmptyIfNull(),
                customerCode = customerDetails?.unique_id.getEmptyIfNull(),
                purchaseAmount = it.value.amount_spent.getEmptyIfNull()
            )
        findNavController().navigate(action)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}