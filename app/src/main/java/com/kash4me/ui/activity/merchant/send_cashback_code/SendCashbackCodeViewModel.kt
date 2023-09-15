package com.kash4me.ui.activity.merchant.send_cashback_code

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.kash4me.data.models.merchant.cashback_code.SendCashbackCodeRequest
import com.kash4me.data.models.merchant.cashback_code.SendCashbackCodeResponse
import com.kash4me.repository.CashBackRepository
import com.kash4me.utils.SessionManager
import com.kash4me.utils.extensions.getEmptyIfNull
import com.kash4me.utils.network.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SendCashbackCodeViewModel @Inject constructor(
    val sessionManager: SessionManager,
    val repository: CashBackRepository
)

    : ViewModel() {

    fun sendCashbackCoupon(
        emailAddress: String, purchaseAmount: String
    ): MutableLiveData<Resource<SendCashbackCodeResponse>> {

        val token = sessionManager.fetchAuthToken().getEmptyIfNull()
        val request = SendCashbackCodeRequest(
            amount = purchaseAmount, emailAddress = emailAddress
        )

        var result = MutableLiveData<Resource<SendCashbackCodeResponse>>()

        viewModelScope.launch {
            result = repository.sendCashbackCode(
                token = sessionManager.fetchAuthToken().getEmptyIfNull(),
                request = request
            ).asLiveData() as MutableLiveData<Resource<SendCashbackCodeResponse>>
        }

        return result

    }

}