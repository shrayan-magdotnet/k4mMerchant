package com.kash4me.ui.activity.customer.pay

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.kash4me.data.models.customer.pay_by_kash4me.PayByKash4meRequest
import com.kash4me.data.models.customer.pay_by_kash4me.PayByKash4meResponse
import com.kash4me.repository.CashBackRepository
import com.kash4me.repository.CustomerTotalCashBackRepository
import com.kash4me.utils.network.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PayByKash4meViewModel @Inject constructor(
    val repository: CustomerTotalCashBackRepository,
    val cashBackRepository: CashBackRepository
) : ViewModel() {

    fun payByKash4me(withdrawAmount: String): LiveData<Resource<PayByKash4meResponse>> {

        var result = MutableLiveData<Resource<PayByKash4meResponse>>()

        viewModelScope.launch {
            val request = PayByKash4meRequest(withdrawAmount = withdrawAmount)
            result = repository.payByKash4me(request = request)
                .asLiveData() as MutableLiveData<Resource<PayByKash4meResponse>>
        }

        return result

    }

}