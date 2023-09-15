package com.kash4me.ui.activity.payment_gateway.connect_your_bank

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import com.kash4me.data.models.payment_gateway.PaymentInformationResponse
import com.kash4me.merchant.R
import com.kash4me.merchant.databinding.FragmentConnectYourBankBinding
import com.kash4me.ui.activity.payment_gateway.PaymentSettingsViewModel
import com.kash4me.ui.dialog.ErrorDialog
import com.kash4me.ui.dialog.SuccessDialog
import com.kash4me.utils.PaymentMethod
import com.kash4me.utils.custom_views.CustomProgressDialog
import com.kash4me.utils.extensions.clearError
import com.kash4me.utils.extensions.getNotAvailableIfEmptyOrNull
import com.kash4me.utils.extensions.openCustomTab
import com.kash4me.utils.extensions.showToast
import com.kash4me.utils.network.Resource
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber


@AndroidEntryPoint
class ConnectYourBankFragment : Fragment() {

    private var _binding: FragmentConnectYourBankBinding? = null
    private val mBinding get() = _binding!!

    private var mPaymentId: Int? = null
    private val mIsVerified by lazy { MutableLiveData<Boolean>().apply { value = false } }
    private var mPaymentInfo: PaymentInformationResponse? = null

    private val mProgressDialog by lazy { CustomProgressDialog(requireContext()) }

    private val mViewModel: PaymentSettingsViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentConnectYourBankBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val args = arguments?.let { ConnectYourBankFragmentArgs.fromBundle(it) }
        Timber.d("Multiple accounts linked -> ${args?.hasMultiplePaymentMethodsLinked}")

        if (args?.hasMultiplePaymentMethodsLinked == true) {
            mBinding.sSetDefaultPaymentOption.isVisible = true
            mBinding.tvSetDefaultPaymentOptionDescription.isVisible = true
        } else {
            mBinding.sSetDefaultPaymentOption.isVisible = false
            mBinding.tvSetDefaultPaymentOptionDescription.isVisible = false
        }

        (activity as AppCompatActivity).supportActionBar?.setTitle(R.string.connect_your_bank)

        getPaymentInformation()
        mIsVerified.observe(viewLifecycleOwner) { isVerified ->
            updateConnectedState(isVerified = isVerified)
        }

        btnSaveListener()

        mBinding.cvConnectBank.setOnClickListener { connectYourBank() }

    }

    private fun updateConnectedState(isVerified: Boolean?) {
        if (isVerified == true) {
            mBinding.apply {
                tvConnectYourBankAccountDescription.setText(R.string.bank_connected_description)
                val green = ContextCompat.getColor(requireContext(), R.color.success_green)
                tvConnectYourBankAccountDescription.setTextColor(green)
                sSetDefaultPaymentOption.isVisible = true
                tvSetDefaultPaymentOptionDescription.isVisible = true
                val accountLinkedDescription =
                    "You are already connected with " +
                            "${mPaymentInfo?.accountDetails?.instituteName.getNotAvailableIfEmptyOrNull()} " +
                            "with account ending with " +
                            "${mPaymentInfo?.accountDetails?.accountNumber}"
                tvLinkStatus.text = accountLinkedDescription
                tvLinkStatus.setTextColor(green)
                ivLinkStatusIcon.setImageResource(R.drawable.ic_check_mark_filled)
            }
        } else {
            mBinding.apply {
                sSetDefaultPaymentOption.isVisible = false
                tvSetDefaultPaymentOptionDescription.isVisible = false
                tvConnectYourBankAccountDescription.setText(R.string.connect_your_bank_account_description)
                tvLinkStatus.setText(R.string.no_account_linked_yet)
                val gray = ContextCompat.getColor(requireContext(), R.color.graySurface)
                tvLinkStatus.setTextColor(gray)
                ivLinkStatusIcon.setImageResource(R.drawable.ic_exclamation_mark)
            }
        }
    }

    private fun getPaymentInformation() {
        mViewModel.getPaymentInformation(paymentMethod = PaymentMethod.VOPAY_BANK)
            .observe(viewLifecycleOwner) {

                when (it) {
                    is Resource.Failure -> {
                        mProgressDialog.hide()
                        val errorDialog = ErrorDialog.getInstance(message = it.errorMsg)
                        errorDialog.show(activity?.supportFragmentManager!!, errorDialog.tag)
                        mBinding.cvConnectBank.isEnabled = mPaymentId != null
                        mIsVerified.value = false
                    }

                    Resource.Loading -> {
                        mProgressDialog.show()
                    }

                    is Resource.Success -> {
                        mProgressDialog.hide()
                        Timber.d("Success -> ${it.value}")
                        mBinding.apply {
                            val vopayBank = it.value.find { paymentInfo ->
                                paymentInfo.paymentMethod == PaymentMethod.VOPAY_BANK.identifier
                            }
                            vopayBank?.let { paymentInfo ->
                                tilFirstName.editText?.setText(paymentInfo.firstName)
                                tilLastName.editText?.setText(paymentInfo.lastName)
                                tilCompanyName.editText?.setText(paymentInfo.companyName)
                                mPaymentId = paymentInfo.id
                                cvConnectBank.isEnabled = mPaymentId != null
                                mPaymentInfo = paymentInfo
                                mIsVerified.value = paymentInfo.isVerified
                                mBinding.sSetDefaultPaymentOption.isChecked =
                                    vopayBank.isDefault == true
                            }
                        }
                    }
                }

            }
    }

    private fun btnSaveListener() {
        mBinding.btnSave.setOnClickListener {

            val firstName = mBinding.tilFirstName.editText?.text
            mBinding.tilFirstName.clearError()

            val lastName = mBinding.tilLastName.editText?.text
            mBinding.tilLastName.clearError()

            val companyName = mBinding.tilCompanyName.editText?.text
            mBinding.tilCompanyName.clearError()

            val areAllFieldsFilled =
                firstName?.isNotBlank() == true && lastName?.isNotBlank() == true && companyName?.isNotBlank() == true
            if (areAllFieldsFilled) {
                showToast("Please enter either name or company name")
                return@setOnClickListener
            }

            val isNameFilled =
                firstName?.isNotBlank() == true && lastName?.isNotBlank() == true && companyName?.isNotBlank() == false
            val isCompanyNameFilled =
                firstName?.isNotBlank() == false && lastName?.isNotBlank() == false && companyName?.isNotBlank() == true

            val paymentId = mPaymentId
            if (paymentId == null) {
                Timber.d("We need to create payment information")
                val request = getRequestForCreatingPaymentInfo(
                    isNameFilled = isNameFilled,
                    isCompanyNameFilled = isCompanyNameFilled
                ) ?: return@setOnClickListener
                Timber.d("Request -> $request")
                createPaymentInformation(request = request)
            } else {
                Timber.d("We need to update payment information")
                val request = getRequestForUpdatingPaymentInfo(
                    isNameFilled = isNameFilled,
                    isCompanyNameFilled = isCompanyNameFilled
                ) ?: return@setOnClickListener
                Timber.d("Request -> $request")
                updatePaymentInformation(paymentId = paymentId, request = request)
            }

        }
    }

    fun getRequestForCreatingPaymentInfo(
        isNameFilled: Boolean,
        isCompanyNameFilled: Boolean
    ): HashMap<String, Any>? {

        val request = HashMap<String, Any>()
        if (isNameFilled) {

            request["first_name"] = mBinding.tilFirstName.editText?.text.toString()
            request["last_name"] = mBinding.tilLastName.editText?.text.toString()

        } else if (isCompanyNameFilled) {

            request["company_name"] = mBinding.tilCompanyName.editText?.text.toString()

        } else {

            showToast("None of the options match")
            return null

        }

        request["payment_gateway"] = PaymentMethod.VOPAY_BANK.identifier
        return request

    }

    fun getRequestForUpdatingPaymentInfo(
        isNameFilled: Boolean,
        isCompanyNameFilled: Boolean
    ): HashMap<String, Any>? {

        val request = HashMap<String, Any>()
        if (isNameFilled) {

            request["first_name"] = mBinding.tilFirstName.editText?.text.toString()
            request["last_name"] = mBinding.tilLastName.editText?.text.toString()
            request["company_name"] = ""

        } else if (isCompanyNameFilled) {

            request["first_name"] = ""
            request["last_name"] = ""
            request["company_name"] = mBinding.tilCompanyName.editText?.text.toString()

        } else {

            showToast("None of the options match")
            return null

        }

        request["payment_gateway"] = PaymentMethod.VOPAY_BANK.identifier
        request["is_default"] = mBinding.sSetDefaultPaymentOption.isChecked

        return request

    }

    private fun createPaymentInformation(request: HashMap<String, Any>) {
        mViewModel.createPaymentInformation(request = request).observe(viewLifecycleOwner) {

            when (it) {
                is Resource.Failure -> {
                    mProgressDialog.hide()
                    val errorDialog = ErrorDialog.getInstance(message = it.errorMsg)
                    errorDialog.show(activity?.supportFragmentManager!!, errorDialog.tag)
                    mIsVerified.value = false
                }

                Resource.Loading -> {
                    mProgressDialog.show()
                }

                is Resource.Success -> {
                    mProgressDialog.hide()
                    val successDialog =
                        SuccessDialog.getInstance(message = "Payment information has been successfully created")
                    successDialog.show(activity?.supportFragmentManager!!, successDialog.tag)
                    val connectYourBank = it.value.find { paymentInfo ->
                        paymentInfo.paymentMethod == PaymentMethod.VOPAY_BANK.identifier
                    }
                    mPaymentInfo = connectYourBank
                    mIsVerified.value = connectYourBank?.isVerified
                    mBinding.sSetDefaultPaymentOption.isChecked = connectYourBank?.isDefault == true
                }
            }

        }
    }

    private fun updatePaymentInformation(paymentId: Int, request: HashMap<String, Any>) {
        mViewModel.updatePaymentInformation(request = request, paymentId = paymentId)
            .observe(viewLifecycleOwner) {

                when (it) {
                    is Resource.Failure -> {
                        mProgressDialog.hide()
                        val errorDialog = ErrorDialog.getInstance(message = it.errorMsg)
                        errorDialog.show(activity?.supportFragmentManager!!, errorDialog.tag)
                        mIsVerified.value = false
                    }

                    Resource.Loading -> {
                        mProgressDialog.show()
                    }

                    is Resource.Success -> {
                        mProgressDialog.hide()
                        val successDialog =
                            SuccessDialog.getInstance(message = "Payment information has been successfully updated")
                        successDialog.show(activity?.supportFragmentManager!!, successDialog.tag)
                        val vopayBank = it.value.find { paymentInfo ->
                            paymentInfo.paymentMethod == PaymentMethod.VOPAY_BANK.identifier
                        }
                        mPaymentInfo = vopayBank
                        mIsVerified.value = vopayBank?.isVerified
                        mBinding.sSetDefaultPaymentOption.isChecked = vopayBank?.isDefault == true

                    }
                }

            }
    }

    private fun connectYourBank() {
        mViewModel.connectYourBank().observe(viewLifecycleOwner) {

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
                    context?.openCustomTab(uri = Uri.parse(it.value.iframeUrl))
                }
            }

        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}