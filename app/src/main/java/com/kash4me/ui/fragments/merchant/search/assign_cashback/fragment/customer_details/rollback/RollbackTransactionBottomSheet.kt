package com.kash4me.ui.fragments.merchant.search.assign_cashback.fragment.customer_details.rollback

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.kash4me.R
import com.kash4me.network.ApiServices
import com.kash4me.network.NetworkConnectionInterceptor
import com.kash4me.network.NotFoundInterceptor
import com.kash4me.repository.CustomerDetailsFromMerchantRepository
import com.kash4me.ui.dialog.ErrorDialog
import com.kash4me.ui.dialog.SuccessDialog
import com.kash4me.ui.fragments.merchant.search.assign_cashback.fragment.customer_details.CustomerDetailsFromMerchantViewModel
import com.kash4me.ui.fragments.merchant.search.assign_cashback.fragment.customer_details.CustomerDetailsFromMerchantViewModelFactory
import com.kash4me.utils.custom_views.CustomProgressDialog
import com.kash4me.utils.extensions.getEmptyIfNull
import com.kash4me.utils.extensions.getMinusOneIfNull
import com.kash4me.utils.network.Resource
import timber.log.Timber

class RollbackTransactionBottomSheet : BottomSheetDialogFragment() {

    companion object {

        private var token: String = ""
        private var merchantId: Int? = null
        private var customerId: Int? = null

        fun newInstance(token: String, merchantId: Int?, customerId: Int?)

                : RollbackTransactionBottomSheet {

            this.token = token
            this.merchantId = merchantId
            this.customerId = customerId

            return RollbackTransactionBottomSheet()

        }
    }

    private val progressDialog by lazy { CustomProgressDialog(requireContext()) }

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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bottom_sheet_rollback_transaction, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnRollback: Button = view.findViewById(R.id.btn_rollback)
        val etAmount: EditText = view.findViewById(R.id.et_amount)

        btnRollback.setOnClickListener {

            val amount = etAmount.text.toString()
            Timber.d("Amount -> $amount")
            rollbackTransaction(amount = amount)

        }

    }

    private fun rollbackTransaction(amount: String) {
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
                    val successDialog =
                        SuccessDialog.getInstance(message = it.value.message.getEmptyIfNull())
                    successDialog.show(activity?.supportFragmentManager!!, successDialog.tag)
                    Timber.d("Success -> ${it.value}")
                }
            }

        }
    }

}