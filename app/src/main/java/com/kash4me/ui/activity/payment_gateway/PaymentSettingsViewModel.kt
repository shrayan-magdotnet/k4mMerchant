package com.kash4me.ui.activity.payment_gateway

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.kash4me.data.models.payment_gateway.ConnectYourBankResponse
import com.kash4me.data.models.payment_gateway.PaymentGatewayResponse
import com.kash4me.data.models.payment_gateway.PaymentInformationResponse
import com.kash4me.data.models.user.fee_settings.FeeSettingsResponse
import com.kash4me.merchant.R
import com.kash4me.repository.PaymentRepository
import com.kash4me.repository.UserRepository
import com.kash4me.utils.FeeType
import com.kash4me.utils.PaymentMethod
import com.kash4me.utils.network.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class PaymentSettingsViewModel
@Inject constructor(
    @ApplicationContext context: Context,
    private val paymentRepository: PaymentRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    val bankAccount = PaymentOption(
        title = context.getString(R.string.use_my_bank_account),
        linkCompleteDescription = "",
        description = "Connect your Bank Account and use the connected bank account for all the deposit and withdrawal transactions. Your banking information will not be stored with us. Transaction fee of $0.85 will apply per transaction.",
        isChecked = false,
        isDefault = false,
        isLinked = false,
        identifier = PaymentMethod.VOPAY_BANK.identifier
    )

    val eTransfer = PaymentOption(
        title = context.getString(R.string.use_interac_e_transfer),
        linkCompleteDescription = "",
        description = "Use email address for all deposit and withdrawal transactions. For all payables, Payment Request will be sent to the email address and you will have to pay the amount manually each time you receive the email. For all receivables, we will send email to the email address and you can deposit the amount to your Bank Account. Transaction fee of $1.50 will apply per transaction.",
        isChecked = false,
        isDefault = false,
        isLinked = false,
        identifier = PaymentMethod.VOPAY_E_TRANSFER.identifier
    )

    val paymentMethods = arrayListOf<PaymentOption>()

//    fun getPaymentMethods(): List<PaymentOption> {
//        return listOf(bankAccount, eTransfer)
//    }

    fun getPaymentMethods(): List<PaymentOption> {
        Timber.d("Getting payment methods -> $paymentMethods")
        return paymentMethods
    }

    fun fetchPaymentMethods(): LiveData<Resource<List<PaymentGatewayResponse>>> {
        var result = MutableLiveData<Resource<List<PaymentGatewayResponse>>>()

        viewModelScope.launch {
            result = paymentRepository.getPaymentGateways()
                .asLiveData() as MutableLiveData<Resource<List<PaymentGatewayResponse>>>
        }

        return result
    }

    fun getLinkedPaymentMethods(): LiveData<Resource<List<PaymentInformationResponse>>> {

        var result = MutableLiveData<Resource<List<PaymentInformationResponse>>>()

        viewModelScope.launch {
            result = paymentRepository.getPaymentInformation(paymentMethod = PaymentMethod.ALL)
                .asLiveData() as MutableLiveData<Resource<List<PaymentInformationResponse>>>
        }

        return result

    }

    fun getPaymentInformation(paymentMethod: PaymentMethod): LiveData<Resource<List<PaymentInformationResponse>>> {

        var result = MutableLiveData<Resource<List<PaymentInformationResponse>>>()

        viewModelScope.launch {
            result = paymentRepository.getPaymentInformation(paymentMethod = paymentMethod)
                .asLiveData() as MutableLiveData<Resource<List<PaymentInformationResponse>>>
        }

        return result

    }

    fun createPaymentInformation(request: HashMap<String, Any>): LiveData<Resource<List<PaymentInformationResponse>>> {

        var result = MutableLiveData<Resource<List<PaymentInformationResponse>>>()

        viewModelScope.launch {
            result = paymentRepository.createPaymentInformation(request = request)
                .asLiveData() as MutableLiveData<Resource<List<PaymentInformationResponse>>>
        }

        return result

    }

    fun updatePaymentInformation(
        paymentId: Int,
        request: HashMap<String, Any>
    ): LiveData<Resource<List<PaymentInformationResponse>>> {

        var result = MutableLiveData<Resource<List<PaymentInformationResponse>>>()

        viewModelScope.launch {
            result =
                paymentRepository.updatePaymentInformation(paymentId = paymentId, request = request)
                    .asLiveData() as MutableLiveData<Resource<List<PaymentInformationResponse>>>
        }

        return result

    }

    fun connectYourBank(): LiveData<Resource<ConnectYourBankResponse>> {

        var result = MutableLiveData<Resource<ConnectYourBankResponse>>()

        viewModelScope.launch {
            result = paymentRepository.connectBankAccount()
                .asLiveData() as MutableLiveData<Resource<ConnectYourBankResponse>>
        }

        return result

    }

    fun getFeeSettings(feeType: FeeType): LiveData<Resource<FeeSettingsResponse>> {

        var result = MutableLiveData<Resource<FeeSettingsResponse>>()

        viewModelScope.launch {
            result = userRepository.getFeeSettings(feeType = feeType)
                .asLiveData() as MutableLiveData<Resource<FeeSettingsResponse>>
        }

        return result

    }

}