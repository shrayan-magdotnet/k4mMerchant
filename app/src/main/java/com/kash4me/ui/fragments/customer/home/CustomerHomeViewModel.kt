package com.kash4me.ui.fragments.customer.home

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.GsonBuilder
import com.kash4me.data.local.customer.cashback.CashbackEntity
import com.kash4me.data.models.CustomerCashBackDetailsResponse
import com.kash4me.data.models.ErrorResponse
import com.kash4me.data.models.request.RequestCashbackQrRequest
import com.kash4me.data.models.response.RequestCashbackQrResponse
import com.kash4me.repository.CustomerCashBackDetailsRepository
import com.kash4me.utils.SessionManager
import com.kash4me.utils.SingleLiveEvent
import com.kash4me.utils.extensions.getEmptyIfNull
import com.kash4me.utils.network.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject

@HiltViewModel
class CustomerHomeViewModel
@Inject
constructor(
    private val sessionManager: SessionManager,
    private val customerCashBackDetailsRepository: CustomerCashBackDetailsRepository
) : ViewModel() {

    var value: String = ""
    var searchQuery: String = ""

    val customerCashBackResponse = SingleLiveEvent<CustomerCashBackDetailsResponse>()
    val errorMessage = SingleLiveEvent<String>()

    fun fetchCustomerCashBackDetails(token: String, filterOptions: Map<String, String>) {

//        val dummyToken = dummyAccessToken
        val response = customerCashBackDetailsRepository.fetchCustomerCashBackDetails(
            token,
            filterOptions = filterOptions
        )
        response.enqueue(object : Callback<CustomerCashBackDetailsResponse> {
            override fun onResponse(
                call: Call<CustomerCashBackDetailsResponse>,
                response: Response<CustomerCashBackDetailsResponse>
            ) {
                if (response.isSuccessful) {
                    customerCashBackResponse.postValue(response.body())
                    return

                } else {
                    val gson = GsonBuilder().create()
                    val mError: ErrorResponse
                    try {
                        mError = gson.fromJson(
                            response.errorBody()!!.string(),
                            ErrorResponse::class.java
                        )
                        onFailure(call = call, Throwable(message = mError.error))
//                        errorMessage.postValue(mError.error)
                    } catch (e: Exception) {
                        // handle failure at error parse
                        e.printStackTrace()
                        onFailure(call = call, Throwable(message = e.localizedMessage))
                    }
                }

            }

            override fun onFailure(call: Call<CustomerCashBackDetailsResponse>, t: Throwable) {
                Log.d("TAG", "onFailure: $t")
                errorMessage.postValue(t.message)
            }
        })

    }

    fun getCashbacks(): LiveData<List<CashbackEntity>> {
        return customerCashBackDetailsRepository.getCashbacks()
    }

//    fun getCashbacks(): LiveData<List<CashbackEntity>> {
//
//        val randomCashbacksLiveData = MediatorLiveData<List<CashbackEntity>>()
//        randomCashbacksLiveData.addSource(customerCashBackDetailsRepository.getCashbacks()) {
//
//            val randomData = arrayListOf<CashbackEntity>()
//            for (i in 1..100) {
//
//                val randomNumber = (1..20).random()
//                val randomCashback = if (i.isEven()) it.first() else it.last()
//                val cashback = CashbackEntity(
//                    amountLeft = (randomCashback.amountLeft.toDoubleOrNull()?.times(randomNumber)
//                        .getZeroIfNull()).toString(),
//                    amountSpent = (randomCashback.amountSpent.toDoubleOrNull()?.times(randomNumber)
//                        .getZeroIfNull()).toString(),
//                    cashbackAmount = (randomCashback.cashbackAmount.toDoubleOrNull()
//                        ?.times(randomNumber)
//                        .getZeroIfNull()).toString(),
//                    processingAmount = (randomCashback.processingAmount.toDoubleOrNull()
//                        ?.times(randomNumber)
//                        .getZeroIfNull()).toString(),
//                    shopDetails = ShopDetails(
//                        address = randomCashback.shopDetails.address,
//                        name = randomCashback.shopDetails.name,
//                        shopId = randomCashback.shopDetails.shopId
//                    )
//                )
//                cashback.id = cashback.id * randomNumber
//                randomData.add(cashback)
//
//            }
//
//            randomCashbacksLiveData.value = randomData
//
//        }
//
//        return randomCashbacksLiveData
//
//    }

    fun updateCashbacksInCache(): MutableLiveData<Resource<Unit>> {

        var result = MutableLiveData<Resource<Unit>>()

        viewModelScope.launch {

            val token = sessionManager.fetchAuthToken().getEmptyIfNull()
            result = customerCashBackDetailsRepository.updateCashbacksInCache(token = token)
                .asLiveData() as MutableLiveData<Resource<Unit>>

        }

        return result

    }

    fun requestQrCode(
        token: String,
        amount: Double,
        shopId: Int
    ): LiveData<RequestCashbackQrResponse> {

        val liveData = MutableLiveData<RequestCashbackQrResponse>()

        val request = RequestCashbackQrRequest(amount = amount, shopId = shopId)
        val response = customerCashBackDetailsRepository.requestCashbackQr(
            token = token, request = request
        )
        response.enqueue(object : Callback<RequestCashbackQrResponse> {
            override fun onResponse(
                call: Call<RequestCashbackQrResponse>,
                response: Response<RequestCashbackQrResponse>
            ) {
                if (response.isSuccessful) {

                    liveData.postValue(response.body())
                    return

                } else {

                    val gson = GsonBuilder().create()
                    val mError: ErrorResponse
                    try {
                        mError = gson.fromJson(
                            response.errorBody()!!.string(),
                            ErrorResponse::class.java
                        )
                        onFailure(call = call, Throwable(message = mError.error))
//                        errorMessage.postValue(mError.error)
                    } catch (e: Exception) {
                        // handle failure at error parse
                        e.printStackTrace()
                        onFailure(call = call, Throwable(message = e.localizedMessage))
                    }

                }

            }

            override fun onFailure(call: Call<RequestCashbackQrResponse>, t: Throwable) {
                Log.d("TAG", "onFailure: $t")
                errorMessage.postValue(t.message)
            }
        })

        return liveData

    }

    fun requestCashback(
        request: RequestCashbackQrRequest
    ): LiveData<Resource<RequestCashbackQrResponse>> {

        var result = MutableLiveData<Resource<RequestCashbackQrResponse>>()

        viewModelScope.launch {

            result = customerCashBackDetailsRepository.generateQrCode(
                token = sessionManager.fetchAuthToken().getEmptyIfNull(),
                request = request
            ).asLiveData() as MutableLiveData<Resource<RequestCashbackQrResponse>>

        }

        return result

    }

}