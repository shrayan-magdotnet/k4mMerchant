package com.kash4me.ui.activity.payment_gateway.payment_information

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.kash4me.R
import com.kash4me.data.models.payment_gateway.PaymentInformationResponse
import com.kash4me.data.models.user.UserType
import com.kash4me.databinding.FragmentPaymentInformationBinding
import com.kash4me.ui.activity.payment_gateway.PaymentOption
import com.kash4me.ui.activity.payment_gateway.PaymentSettingsViewModel
import com.kash4me.ui.dialog.ErrorDialog
import com.kash4me.utils.FeeType
import com.kash4me.utils.PaymentMethod
import com.kash4me.utils.SessionManager
import com.kash4me.utils.custom_views.CustomProgressDialog
import com.kash4me.utils.extensions.formatUsingCurrencySystem
import com.kash4me.utils.extensions.getEmptyIfNull
import com.kash4me.utils.extensions.getZeroIfNull
import com.kash4me.utils.listeners.SingleParamWithPositionItemClickListener
import com.kash4me.utils.network.Resource
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class PaymentInformationFragment : Fragment() {

    private var _binding: FragmentPaymentInformationBinding? = null
    private val mBinding get() = _binding!!

    private val mProgressDialog by lazy { CustomProgressDialog(requireContext()) }

    private val mAdapter: PaymentInformationAdapter by lazy {
        PaymentInformationAdapter(clickListener = mClickListener)
    }

    private val mClickListener
        get() = object : SingleParamWithPositionItemClickListener<PaymentOption> {
            override fun onClick(item: PaymentOption, position: Int) {
                mAdapter.setSelectedItem(position = position)
            }
        }

    private var mHasMultiplePaymentMethodsLinked = false

    @Inject
    lateinit var mSessionManager: SessionManager

    private var transactionFee: Double = 0.0

    private val mViewModel: PaymentSettingsViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPaymentInformationBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as AppCompatActivity).supportActionBar?.setTitle(R.string.payment_information)

        initRvPaymentOptions()
//        getLinkedPaymentMethods()
        getFeeSettings()
        btnNextListener()


    }

    private fun getFeeSettings() {
        mViewModel.getFeeSettings(feeType = FeeType.NORMAL).observe(this) {

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

                    Timber.d("Success -> ${it.value}")

                    transactionFee =
                        if (mSessionManager.fetchUserType() == UserType.CUSTOMER) {
                            it.value.customerFee?.toDoubleOrNull().getZeroIfNull()
                        } else if (mSessionManager.fetchUserType() == UserType.MERCHANT) {
                            it.value.merchantFee?.toDoubleOrNull().getZeroIfNull()
                        } else {
                            it.value.customerFee?.toDoubleOrNull().getZeroIfNull()
                        }

                    fetchAvailablePaymentMethods()

                }
            }

        }
    }

    private fun fetchAvailablePaymentMethods() {
        mViewModel.fetchPaymentMethods().observe(viewLifecycleOwner) {

            when (it) {
                is Resource.Failure -> {
                    mProgressDialog.hide()
                    val errorDialog = ErrorDialog.getInstance(message = it.errorMsg)
                    errorDialog.show(activity?.supportFragmentManager!!, errorDialog.tag)
                    showEmptyState()
                }

                Resource.Loading -> {
                    mProgressDialog.show()
                }

                is Resource.Success -> {
                    mProgressDialog.hide()
                    val paymentGateways = it.value.map { response ->

                        val description = response.description.getEmptyIfNull()
                        val updatedDescription =
                            description.replace("{}", transactionFee.formatUsingCurrencySystem())

                        PaymentOption(
                            isChecked = false,
                            isDefault = false,
                            isLinked = false,
                            title = response.name.getEmptyIfNull(),
                            linkCompleteDescription = "",
                            description = updatedDescription,
                            identifier = response.identifier.getEmptyIfNull()
                        )
                    }
                    mViewModel.paymentMethods.clear()
                    mViewModel.paymentMethods.addAll(paymentGateways)
                    mAdapter.setData(paymentOptions = mViewModel.getPaymentMethods())
                    getLinkedPaymentMethods()
                    makeRvPaymentGatewaysVisible()
                }
            }

        }
    }

    private fun makeRvPaymentGatewaysVisible() {
        mBinding.tvEmptyState.root.isVisible = false
        mBinding.rvPaymentOptions.isVisible = true
        mBinding.btnNext.isVisible = true
    }

    private fun showEmptyState() {
        mBinding.tvEmptyState.root.isVisible = true
        mBinding.tvEmptyState.tvMessage.text = "Couldn't get payment gateways"
        mBinding.rvPaymentOptions.isVisible = false
        mBinding.btnNext.isVisible = false
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
                    Timber.d("Payment methods -> ${it.value}")

                    when (it.value.size) {

                        0 -> {
                            mHasMultiplePaymentMethodsLinked = false
                            Timber.d("None of the payment methods have been created")
                        }

                        1 -> {
                            handleSinglePaymentMethodScenario(it)
                        }

                        2 -> {
                            handleMultiplePaymentMethodsScenario(it)
                        }

                    }

                }
            }

        }
    }

    private fun handleSinglePaymentMethodScenario(it: Resource.Success<List<PaymentInformationResponse>>) {
        mHasMultiplePaymentMethodsLinked = false

        Timber.d("Payment method [0] -> ${it.value[0]}")

        if (it.value[0].paymentMethod == PaymentMethod.VOPAY_BANK.identifier) {
            Timber.d("Vopay Bank selected")
            mViewModel.paymentMethods.forEach { method ->
                if (method.identifier == PaymentMethod.VOPAY_BANK.identifier) {
                    method.isDefault = true
                    method.isChecked = true
                    method.isLinked = true
                    method.linkCompleteDescription = resources.getString(
                        R.string.bank_connected_description
                    )
                } else {
                    method.isDefault = false
                    method.isChecked = false
                    method.isLinked = false
                }
            }
//            mViewModel.bankAccount.apply {
//                isLinked = true
//                isChecked = true
//                linkCompleteDescription = resources.getString(
//                    R.string.bank_connected_description
//                )
//            }

//            bankAccount?.isLinked = true
//            bankAccount?.isChecked = true
//            bankAccount?.linkCompleteDescription = resources.getString(
//                R.string.bank_connected_description
//            )
//            mViewModel.eTransfer.apply {
//                isLinked = false
//                isChecked = false
//            }
            mAdapter.setData(paymentOptions = mViewModel.getPaymentMethods())
        } else if (it.value[0].paymentMethod == PaymentMethod.VOPAY_E_TRANSFER.identifier) {
            Timber.d("Vopay eTransfer selected")
//            mViewModel.bankAccount.apply {
//                isLinked = false
//                isChecked = false
//            }
            mViewModel.paymentMethods.forEach { method ->
                Timber.d("Payment method -> $method")
                if (method.identifier == PaymentMethod.VOPAY_E_TRANSFER.identifier) {
                    method.isDefault = true
                    method.isChecked = true
                    method.isLinked = true
                    method.linkCompleteDescription = resources.getString(
                        R.string.interac_setup_complete_description,
                        it.value[0].emailAddress
                    )
                } else {
                    method.isDefault = false
                    method.isChecked = false
                    method.isLinked = false
                }
            }
//            mViewModel.eTransfer.apply {
//                isLinked = true
//                isChecked = true
//                linkCompleteDescription = resources.getString(
//                    R.string.interac_setup_complete_description,
//                    it.value[0].emailAddress
//                )
//            }
            Timber.d("Payment method after modification")
            mAdapter.setData(paymentOptions = mViewModel.getPaymentMethods())
        }
    }

    private fun handleMultiplePaymentMethodsScenario(it: Resource.Success<List<PaymentInformationResponse>>) {
        mHasMultiplePaymentMethodsLinked = true

        Timber.d("Both payment methods have been created")
        updateDefaultPaymentStatus(it)
        updatePaymentMethodsContent(it)
        mAdapter.setData(paymentOptions = mViewModel.getPaymentMethods())
    }

    private fun updateDefaultPaymentStatus(it: Resource.Success<List<PaymentInformationResponse>>) {
        val defaultPaymentMethod = it.value.find { method ->
            method.isDefault == true
        }
        Timber.d("Default payment method -> $defaultPaymentMethod")
        if (defaultPaymentMethod?.paymentMethod == PaymentMethod.VOPAY_BANK.identifier) {
//            mViewModel.bankAccount.apply {
//                isDefault = true
//                isChecked = true
//            }
//            val bankAccount = mViewModel.paymentMethods.find { method ->
//                method.identifier == PaymentMethod.VOPAY_BANK.identifier
//            }
//            bankAccount?.isDefault = true
//            bankAccount?.isChecked = true
//            Timber.d("bankAccount -> $bankAccount")
//            mViewModel.eTransfer.apply {
//                isDefault = false
//                isChecked = false
//            }
            mViewModel.paymentMethods.forEach { method ->
                if (method.identifier == PaymentMethod.VOPAY_BANK.identifier) {
                    method.isDefault = true
                    method.isChecked = true
                } else {
                    method.isDefault = false
                    method.isChecked = false
                }
            }
        } else if (defaultPaymentMethod?.paymentMethod == PaymentMethod.VOPAY_E_TRANSFER.identifier) {
//            mViewModel.bankAccount.apply {
//                isDefault = false
//                isChecked = false
//            }
//            mViewModel.eTransfer.apply {
//                isDefault = true
//                isChecked = true
//            }
            mViewModel.paymentMethods.forEach { method ->
                if (method.identifier == PaymentMethod.VOPAY_E_TRANSFER.identifier) {
                    method.isDefault = true
                    method.isChecked = true
                } else {
                    method.isDefault = false
                    method.isChecked = false
                }
            }
//            val eTransfer = mViewModel.paymentMethods.find { method ->
//                method.identifier == PaymentMethod.VOPAY_E_TRANSFER.identifier
//            }
//            eTransfer?.isDefault = true
//            eTransfer?.isChecked = true
        }
        Timber.d(
            "Payment methods after updating default status" + mViewModel.getPaymentMethods()
                .toString()
        )
    }

    private fun updatePaymentMethodsContent(it: Resource.Success<List<PaymentInformationResponse>>) {
//        mViewModel.paymentMethods.forEach { method ->
//            if (method.identifier == PaymentMethod.VOPAY_E_TRANSFER.identifier) {
//                method.isDefault = true
//                method.isChecked = true
//            } else {
//                method.isDefault = false
//                method.isChecked = false
//            }
//        }
        val bankAccount = mViewModel.paymentMethods.find { method ->
            method.identifier == PaymentMethod.VOPAY_BANK.identifier
        }
        bankAccount?.apply {
            isLinked = true
            linkCompleteDescription = resources.getString(
                R.string.bank_connected_description
            )
        }
        val eTransfer = mViewModel.paymentMethods.find { method ->
            method.identifier == PaymentMethod.VOPAY_E_TRANSFER.identifier
        }
        eTransfer?.apply {
            isLinked = true
            val interacInformation = it.value.find { method ->
                method.paymentMethod == PaymentMethod.VOPAY_E_TRANSFER.identifier
            }
            linkCompleteDescription = resources.getString(
                R.string.interac_setup_complete_description,
                interacInformation?.emailAddress
            )
        }
//        mViewModel.eTransfer.apply {
//            isLinked = true
//            val interacInformation = it.value.find { method ->
//                method.paymentMethod == PaymentMethod.VOPAY_E_TRANSFER.identifier
//            }
//            linkCompleteDescription = resources.getString(
//                R.string.interac_setup_complete_description,
//                interacInformation?.emailAddress
//            )
//        }
    }

    private fun initRvPaymentOptions() {
        mBinding.rvPaymentOptions.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = mAdapter
        }
    }

    private fun btnNextListener() {
        mBinding.btnNext.setOnClickListener {
            val selectedItem = mAdapter.getSelectedItem()
            Timber.d("Selected item -> $selectedItem")
            if (selectedItem?.identifier == PaymentMethod.VOPAY_BANK.identifier) {
                val navDirections = PaymentInformationFragmentDirections
                    .actionPaymentInformationFragmentToConnectYourBankFragment(
                        hasMultiplePaymentMethodsLinked = mHasMultiplePaymentMethodsLinked
                    )
                findNavController().navigate(directions = navDirections)
            } else if (selectedItem?.identifier == PaymentMethod.VOPAY_E_TRANSFER.identifier) {
                val navDirections = PaymentInformationFragmentDirections
                    .actionPaymentInformationFragmentToInteracInformationFragment(
                        hasMultiplePaymentMethodsLinked = mHasMultiplePaymentMethodsLinked
                    )
                findNavController().navigate(navDirections)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}