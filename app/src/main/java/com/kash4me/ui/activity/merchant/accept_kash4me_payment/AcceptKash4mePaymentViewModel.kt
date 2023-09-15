package com.kash4me.ui.activity.merchant.accept_kash4me_payment

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.kash4me.data.models.ActiveCashbackSettings
import com.kash4me.data.models.CashBackAmountResponse
import com.kash4me.data.models.merchant.accept_kash4me_payment.AcceptKash4mePaymentRequest
import com.kash4me.data.models.merchant.accept_kash4me_payment.AcceptKash4mePaymentResponse
import com.kash4me.data.models.merchant.calculate_cashback.CalculateCashbackRequest
import com.kash4me.repository.CashBackRepository
import com.kash4me.utils.SessionManager
import com.kash4me.utils.extensions.getEmptyIfNull
import com.kash4me.utils.network.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class AcceptKash4mePaymentViewModel @Inject constructor(
    val sessionManager: SessionManager,
    val cashBackRepository: CashBackRepository
) : ViewModel() {

    fun acceptKash4mePayment(
        customerId: Int?,
        cashbackAmount: String,
        transactionFee: String?,
        purchaseAmount: String,
        qrToken: String?
    ): LiveData<Resource<AcceptKash4mePaymentResponse>> {

        val customerTransaction = AcceptKash4mePaymentRequest.CustomerTransaction(
            cashbackAmount = cashbackAmount,
            transactionFee = transactionFee,
            withdrawAmount = purchaseAmount
        )
        val request = AcceptKash4mePaymentRequest(
            customer = customerId,
            customerTransaction = customerTransaction,
            purchaseAmount = purchaseAmount,
            qrToken = qrToken
        )

        Timber.d("Request -> $request")

        var result = MutableLiveData<Resource<AcceptKash4mePaymentResponse>>()

        viewModelScope.launch {
            result = cashBackRepository.acceptKash4mePayment(
                token = sessionManager.fetchAuthToken().getEmptyIfNull(),
                request = request
            ).asLiveData() as MutableLiveData<Resource<AcceptKash4mePaymentResponse>>
        }

        return result

    }

    fun fetchActiveCashbackSettings(merchantShopId: Int): LiveData<Resource<ActiveCashbackSettings>> {

        var result = MutableLiveData<Resource<ActiveCashbackSettings>>()

        viewModelScope.launch {
            result = cashBackRepository.fetchActiveCashbackSettings(merchantShopId = merchantShopId)
                .asLiveData() as MutableLiveData<Resource<ActiveCashbackSettings>>
        }

        return result

    }

    fun calculateCashbackAmount(
        purchaseAmount: String,
        cashbackSettingsId: Int,
        customerId: Int,
        merchantShopId: Int
    ): LiveData<Resource<CashBackAmountResponse>> {

        var result = MutableLiveData<Resource<CashBackAmountResponse>>()

        viewModelScope.launch {
            val request = CalculateCashbackRequest(
                amountSpent = purchaseAmount,
                cashbackSettings = cashbackSettingsId,
                customer = customerId,
                shopId = merchantShopId
            )
            result = cashBackRepository.calculateCashbackAmount(request = request).asLiveData()
                    as MutableLiveData<Resource<CashBackAmountResponse>>
        }

        return result

    }

}