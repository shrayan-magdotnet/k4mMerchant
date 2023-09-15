package com.kash4me.ui.fragments.merchant.withdraw_cash

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.kash4me.merchant.databinding.BottomSheetPaymentMethodsBinding
import com.kash4me.ui.activity.payment_gateway.PaymentSettingsViewModel
import com.kash4me.ui.dialog.ErrorDialog
import com.kash4me.utils.custom_views.CustomProgressDialog
import com.kash4me.utils.network.Resource
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PaymentMethodsBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetPaymentMethodsBinding? = null
    private val mBinding get() = _binding!!

    private val mProgressDialog by lazy { CustomProgressDialog(requireContext()) }

    private val mPaymentMethodsAdapter by lazy { PaymentMethodsAdapter() }

    private val mViewModel: PaymentSettingsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetPaymentMethodsBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initRvPaymentMethods()
        getLinkedPaymentMethods()

    }

    private fun initRvPaymentMethods() {
        mBinding.rvPaymentOptions.apply {
            layoutManager = LinearLayoutManager(context)
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
            adapter = mPaymentMethodsAdapter
        }
    }

    private fun getLinkedPaymentMethods() {
        mViewModel.getLinkedPaymentMethods().observe(viewLifecycleOwner) {

            when (it) {
                is Resource.Failure -> {
                    mProgressDialog.hide()
                    val errorDialog = ErrorDialog.getInstance(message = it.errorMsg)
                    errorDialog.show(activity?.supportFragmentManager!!, errorDialog.tag)
                }

                Resource.Loading -> {
                    mProgressDialog.show()
                }

                is Resource.Success -> {
                    mProgressDialog.hide()
                    mPaymentMethodsAdapter.setData(paymentMethods = it.value)
                }
            }

        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}