package com.kash4me.ui.activity.payment_gateway

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
import com.kash4me.R
import com.kash4me.databinding.FragmentInteracInformationBinding
import com.kash4me.ui.dialog.ErrorDialog
import com.kash4me.ui.dialog.SuccessDialog
import com.kash4me.utils.PaymentMethod
import com.kash4me.utils.custom_views.CustomProgressDialog
import com.kash4me.utils.extensions.clearError
import com.kash4me.utils.network.Resource
import timber.log.Timber

class InteracInformationFragment : Fragment() {

    private var _binding: FragmentInteracInformationBinding? = null
    private val mBinding get() = _binding!!

    private var mPaymentId: Int? = null
    private val mIsVerified by lazy { MutableLiveData<Boolean>().apply { value = false } }

    private val mProgressDialog by lazy { CustomProgressDialog(requireContext()) }

    private val mViewModel: PaymentSettingsViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInteracInformationBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val args = arguments?.let { InteracInformationFragmentArgs.fromBundle(it) }
        Timber.d("Multiple accounts linked -> ${args?.hasMultiplePaymentMethodsLinked}")

        if (args?.hasMultiplePaymentMethodsLinked == true) {
            mBinding.sSetDefaultPaymentOption.isVisible = true
            mBinding.tvSetDefaultPaymentOptionDescription.isVisible = true
        } else {
            mBinding.sSetDefaultPaymentOption.isVisible = false
            mBinding.tvSetDefaultPaymentOptionDescription.isVisible = false
        }

        mBinding.tvSetDefaultPaymentOptionDescription.setOnClickListener {
            mBinding.sSetDefaultPaymentOption.performClick()
        }

        (activity as AppCompatActivity).supportActionBar?.setTitle(R.string.interac_information)
        mIsVerified.observe(viewLifecycleOwner) { isVerified ->
            updateConnectedStatus(isVerified = isVerified)
        }

        getPaymentInformation()
        btnSaveListener()

    }

    private fun updateConnectedStatus(isVerified: Boolean?) {
        if (isVerified == true) {
            mBinding.apply {
                tvDescription.setText(R.string.interac_information_saved_description)
                val darkGreen = ContextCompat.getColor(requireContext(), R.color.success_green)
                tvDescription.setTextColor(darkGreen)
                sSetDefaultPaymentOption.isVisible = true
                tvSetDefaultPaymentOptionDescription.isVisible = true
            }
        } else {
            mBinding.apply {
                sSetDefaultPaymentOption.isVisible = false
                tvSetDefaultPaymentOptionDescription.isVisible = false
                tvDescription.setText(R.string.interac_information_description)
            }
        }
    }

    private fun btnSaveListener() {
        mBinding.btnSave.setOnClickListener {

            val emailAddress = mBinding.tilEmailAddress.editText?.text
            mBinding.tilEmailAddress.clearError()

            if (emailAddress.isNullOrBlank()) {
                mBinding.tilEmailAddress.error = "Please enter email address"
                // TODO: Also check if it is an valid email address
                return@setOnClickListener
            }

            val request = HashMap<String, Any>()
            request["email_address"] = emailAddress.toString()
            request["payment_gateway"] = PaymentMethod.VOPAY_E_TRANSFER.identifier

            Timber.d("Request -> $request")

            val paymentId = mPaymentId
            if (paymentId == null) {
                Timber.d("We need to create payment information")
                createPaymentInformation(request)
            } else {
                Timber.d("We need to update payment information")
                request["is_default"] = mBinding.sSetDefaultPaymentOption.isChecked
                updatePaymentInformation(paymentId = paymentId, request)
            }

        }
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
                        SuccessDialog.getInstance(message = "Payment information has been successfully updated")
                    successDialog.show(activity?.supportFragmentManager!!, successDialog.tag)
                    val interactInformation = it.value.find { paymentInfo ->
                        paymentInfo.paymentMethod == PaymentMethod.VOPAY_E_TRANSFER.identifier
                    }
                    mIsVerified.value = interactInformation?.isVerified
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

                        val eTransfer = it.value.find { paymentInfo ->
                            paymentInfo.paymentMethod == PaymentMethod.VOPAY_E_TRANSFER.identifier
                        }
                        mIsVerified.value = eTransfer?.isVerified
                        mBinding.sSetDefaultPaymentOption.isChecked = eTransfer?.isDefault == true

                    }
                }

            }
    }

    private fun getPaymentInformation() {
        mViewModel.getPaymentInformation(paymentMethod = PaymentMethod.VOPAY_E_TRANSFER)
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
                        Timber.d("Success -> ${it.value}")
                        it.value.getOrNull(0)?.let { paymentInfo ->
                            mBinding.tilEmailAddress.editText?.setText(paymentInfo.emailAddress)
                            mPaymentId = paymentInfo.id
                            mIsVerified.value = paymentInfo.isVerified
                            mBinding.sSetDefaultPaymentOption.isChecked =
                                paymentInfo.isDefault == true
                        }
                    }
                }

            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}