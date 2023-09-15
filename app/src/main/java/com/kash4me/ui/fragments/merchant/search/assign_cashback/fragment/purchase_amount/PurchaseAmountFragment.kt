package com.kash4me.ui.fragments.merchant.search.assign_cashback.fragment.purchase_amount

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.kash4me.R
import com.kash4me.data.models.UserDetails
import com.kash4me.databinding.FragmentPurchaseAmountBinding
import com.kash4me.ui.dialog.ErrorDialog
import com.kash4me.ui.fragments.merchant.search.assign_cashback.AssignCashbackActivity
import com.kash4me.utils.App
import com.kash4me.utils.SessionManager
import com.kash4me.utils.custom_views.CustomProgressDialog
import com.kash4me.utils.extensions.getAmount
import com.kash4me.utils.extensions.getZeroIfNull
import com.kash4me.utils.extensions.toLong
import com.kash4me.utils.network.Resource
import timber.log.Timber

class PurchaseAmountFragment : Fragment() {

    private var mAmountSpent: Double = 0.0
    private var mCashbackValue: String? = null

    private val customerDetails by lazy { (activity as AssignCashbackActivity).customerDetails }

    private val sessionManager by lazy { SessionManager(context = App.getContext()!!) }

    private val viewModel by lazy {
        ViewModelProvider(this)[PurchaseAmountViewModel::class.java]
    }

    private var _binding: FragmentPurchaseAmountBinding? = null
    private val mBinding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPurchaseAmountBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()

        mBinding.etPurchaseAmount.hint = "$99.99"
        val yellow = ContextCompat.getColor(requireContext(), R.color.yellow)
        mBinding.etPurchaseAmount.setHintTextColor(yellow)

        mBinding.tvCode.text = customerDetails?.unique_id.toString()
        mBinding.tvCustomerName.text = customerDetails?.nick_name

        etPurchaseAmountListener()
        btnSendListener(customerDetails)

    }

    private fun etPurchaseAmountListener() {
        mBinding.etPurchaseAmount.doAfterTextChanged {
            mAmountSpent = mBinding.etPurchaseAmount.getAmount()
            Timber.d("Amount -> $mAmountSpent")
            val customerId =
                (activity as AssignCashbackActivity).intent.getIntExtra("customerId", 0)
//            calculateCashbackValue(customerId)
        }
    }

    private fun btnSendListener(customerDetails: UserDetails?) {
        mBinding.btnSend.setOnClickListener {

//            if (mCashbackValue == null) {
//                val errorDialog = ErrorDialog.getInstance(message = "No cashback value found")
//                errorDialog.show(activity?.supportFragmentManager!!, errorDialog.tag)
//                return@setOnClickListener
//            }

            val progressDialog = CustomProgressDialog(context = requireContext())

            navigateToAssignCompleteFragment(customerDetails)

        }
    }

    private fun navigateToAssignCompleteFragment(customerDetails: UserDetails?) {
        val action = PurchaseAmountFragmentDirections
            .actionPurchaseAmountFragmentToConfirmAssignCashbackFragment(
                purchaseAmount = mAmountSpent.toFloat(),
                cashbackSettings = customerDetails?.id.getZeroIfNull()
            )
        findNavController().navigate(action)
    }

    private fun setupToolbar() {
        (activity as AssignCashbackActivity).apply {
            setSupportActionBar(mBinding.toolbarLayout.toolbar)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.title = "Assign Cash Back"
        }
        mBinding.toolbarLayout.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun calculateCashbackValue(customerId: Int) {
        viewModel.calculateCashbackValue(
            amountSpent = mAmountSpent.toString(),
            customerId = customerId
        )
            .observe(viewLifecycleOwner) { resource ->

                when (resource) {

                    is Resource.Failure -> {
                        val errorDialog = ErrorDialog.getInstance(message = resource.errorMsg)
                        errorDialog.show(activity?.supportFragmentManager!!, errorDialog.tag)
                        mCashbackValue = null

                    }

                    Resource.Loading -> {
                        // Do nothing
                    }

                    is Resource.Success -> {
                        Timber.d("Result -> ${resource.value}")
                        val cashbackAmount = resource.value.totalCashbackAmount?.toDoubleOrNull()
                        mBinding.tvCashbackValueBody.setValue(
                            cashbackAmount.toLong().getZeroIfNull()
                        )
                        mCashbackValue = resource.value.totalCashbackAmount
                    }

                }

            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}