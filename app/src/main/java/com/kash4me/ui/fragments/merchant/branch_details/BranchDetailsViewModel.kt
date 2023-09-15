package com.kash4me.ui.fragments.merchant.branch_details

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.kash4me.data.models.MerchantTransactionSummaryResponse
import com.kash4me.repository.MerchantTransactionSummaryRepository
import com.kash4me.utils.SingleLiveEvent
import com.kash4me.utils.network.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BranchDetailsViewModel
@Inject
constructor(
    private val merchantTransactionSummaryRepository: MerchantTransactionSummaryRepository
) : ViewModel() {

    val merchantTransactionSummaryResponse = SingleLiveEvent<MerchantTransactionSummaryResponse>()
    val errorMessage = SingleLiveEvent<String>()

    fun getTransactionSummary(
        token: String,
        merchantId: Int?
    ): LiveData<Resource<MerchantTransactionSummaryResponse>> {

        var result = MutableLiveData<Resource<MerchantTransactionSummaryResponse>>()

        viewModelScope.launch {
            result = merchantTransactionSummaryRepository.fetchBranchTransactionSummary(
                token, merchantId
            ).asLiveData() as MutableLiveData<Resource<MerchantTransactionSummaryResponse>>
        }

        return result

    }

//    fun getTransactionSummary(token: String, merchantId: Int?) {
//
//        val response = merchantTransactionSummaryRepository.getTransactionSummary(token, merchantId)
//        response.enqueue(object : Callback<MerchantTransactionSummaryResponse> {
//            override fun onResponse(
//                call: Call<MerchantTransactionSummaryResponse>,
//                response: Response<MerchantTransactionSummaryResponse>
//            ) {
//                if (response.isSuccessful) {
//                    merchantTransactionSummaryResponse.postValue(response.body())
//                    return
//                } else {
//                    val gson = GsonBuilder().create()
//                    val mError: ErrorResponse
//                    try {
//                        mError = gson.fromJson(
//                            response.errorBody()!!.string(),
//                            ErrorResponse::class.java
//                        )
//                        onFailure(call = call, Throwable(message = mError.error))
//                    } catch (e: Exception) {
//                        e.printStackTrace()
//                        onFailure(call = call, Throwable(message = e.localizedMessage))
//                    }
//                }
//
//            }
//
//            override fun onFailure(call: Call<MerchantTransactionSummaryResponse>, t: Throwable) {
//                Log.d("TAG", "onFailure: $t")
//                errorMessage.postValue(t.message)
//            }
//        })
//
//    }
}