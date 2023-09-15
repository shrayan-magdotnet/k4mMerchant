package com.kash4me.ui.activity.merchant.return_purchase

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.kash4me.data.models.CashBackAmountResponse
import com.kash4me.data.models.merchant.calculate_cashback.CalculateCashbackRequest
import com.kash4me.data.models.merchant.purchase_return.ReturnPurchaseRequest
import com.kash4me.data.models.merchant.purchase_return.ReturnPurchaseResponse
import com.kash4me.repository.CashBackRepository
import com.kash4me.utils.SessionManager
import com.kash4me.utils.extensions.getEmptyIfNull
import com.kash4me.utils.network.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PurchaseReturnViewModel @Inject constructor(
    private val sessionManager: SessionManager,
    private val repository: CashBackRepository
) : ViewModel() {

    fun returnPurchase(transactionId: Int, amountToReturn: String, qrToken: String)

            : MutableLiveData<Resource<ReturnPurchaseResponse>> {

        val request = ReturnPurchaseRequest(
            transactionId = transactionId,
            amountSpent = amountToReturn,
            qrToken = qrToken
        )

        var result = MutableLiveData<Resource<ReturnPurchaseResponse>>()

        viewModelScope.launch {
            result = repository.returnPurchase(
                token = sessionManager.fetchAuthToken().getEmptyIfNull(),
                request = request
            ).asLiveData() as MutableLiveData<Resource<ReturnPurchaseResponse>>
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
            result = repository.calculateCashbackAmount(request = request).asLiveData()
                    as MutableLiveData<Resource<CashBackAmountResponse>>
        }

        return result

    }

}