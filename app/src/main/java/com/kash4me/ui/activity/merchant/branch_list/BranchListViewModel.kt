package com.kash4me.ui.activity.merchant.branch_list

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.gson.GsonBuilder
import com.kash4me.data.models.BranchListResponse
import com.kash4me.data.models.ErrorResponse
import com.kash4me.repository.MerchantBranchListRepository
import com.kash4me.utils.SingleLiveEvent
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class BranchListViewModel(private val merchantBranchListRepository: MerchantBranchListRepository) :
    ViewModel() {

    val branchListResponse =
        SingleLiveEvent<BranchListResponse>()
    val errorMessage = SingleLiveEvent<ErrorResponse>()

    fun getMerchantDetailsWithCustomerInfo(
        token: String,
        filterOptions: Map<String, String>?
    ) {

        val response = merchantBranchListRepository.getMerchantBranchList(token, filterOptions)
        response.enqueue(object : Callback<BranchListResponse> {
            override fun onResponse(
                call: Call<BranchListResponse>,
                response: Response<BranchListResponse>
            ) {
                if (response.isSuccessful) {
                    branchListResponse.postValue(response.body())
                    return


                } else {
                    val gson = GsonBuilder().create()
                    var errorResponse: ErrorResponse
                    try {
                        errorResponse = gson.fromJson(
                            response.errorBody()!!.string(),
                            ErrorResponse::class.java
                        )
                        errorMessage.postValue(errorResponse)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        errorResponse = ErrorResponse(e.localizedMessage ?: "")
                        errorMessage.postValue(errorResponse)
                    }
                }

            }

            override fun onFailure(
                call: Call<BranchListResponse>,
                t: Throwable
            ) {
                Log.d("TAG", "onFailure: $t")
                val errorResponse = ErrorResponse(t.localizedMessage ?: "")
                errorMessage.postValue(errorResponse)
            }
        })

    }
}
