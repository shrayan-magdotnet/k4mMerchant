package com.kash4me.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.kash4me.data.models.customer.claim_coupon.ClaimCouponRequest
import com.kash4me.data.models.customer.claim_coupon.ClaimCouponResponse
import com.kash4me.utils.SessionManager
import com.kash4me.utils.extensions.getEmptyIfNull
import com.kash4me.utils.network.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CustomerCashbackViewModel @Inject constructor(
    val sessionManager: SessionManager,
    val repository: CustomerCashBackDetailsRepository
)

    : ViewModel() {

    fun claimCoupon(coupon: String): LiveData<Resource<ClaimCouponResponse>> {

        var result = MutableLiveData<Resource<ClaimCouponResponse>>()

        viewModelScope.launch {

            result = repository.claimCoupon(
                token = sessionManager.fetchAuthToken().getEmptyIfNull(),
                request = ClaimCouponRequest(coupon = coupon)
            ).asLiveData() as MutableLiveData<Resource<ClaimCouponResponse>>

        }

        return result

    }

}