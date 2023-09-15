package com.kash4me.ui.fragments.merchant.search.assign_cashback.fragment.purchase_amount

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.GsonBuilder
import com.kash4me.data.models.CashBackSuccessResponse
import com.kash4me.data.models.ErrorResponse
import com.kash4me.data.models.merchant.assign_cashback.AssignCashbackRequest
import com.kash4me.data.models.merchant.assign_cashback.AssignCashbackResponse
import com.kash4me.network.ApiServices
import com.kash4me.network.NetworkConnectionInterceptor
import com.kash4me.network.NotFoundInterceptor
import com.kash4me.repository.CashBackRepository
import com.kash4me.utils.App
import com.kash4me.utils.SessionManager
import com.kash4me.utils.extensions.getEmptyIfNull
import com.kash4me.utils.extensions.getZeroIfNull
import com.kash4me.utils.network.Resource
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber

class PurchaseAmountViewModel : ViewModel() {

    private val sessionManager by lazy { SessionManager(context = App.getContext()!!) }

    private val repository by lazy {
        val apiInterface =
            ApiServices.invoke(
                NetworkConnectionInterceptor(App.getContext()!!),
                NotFoundInterceptor()
            )
        CashBackRepository(apiInterface, sessionManager)
    }

    fun calculateCashbackValue(
        amountSpent: String,
        customerId: Int
    ): LiveData<Resource<AssignCashbackResponse>> {

        val result = MutableLiveData<Resource<AssignCashbackResponse>>()

        val token = sessionManager.fetchAuthToken().getEmptyIfNull()
        val shopId = sessionManager.fetchMerchantDetails()?.id
        val cashbackSettingsId = sessionManager.fetchMerchantDetails()?.activeCashbackSettings?.id

        val request = AssignCashbackRequest(
            amountSpent = amountSpent,
            cashbackSettings = cashbackSettingsId,
            customer = customerId,
            shopId = shopId
        )

        result.postValue(Resource.Loading)

        val response = repository.getCashbackValue(token = token, request = request)
        response.enqueue(object : Callback<AssignCashbackResponse> {
            override fun onResponse(
                call: Call<AssignCashbackResponse>,
                response: Response<AssignCashbackResponse>
            ) {

                val responseBody = response.body()
                if (response.isSuccessful && responseBody != null) {

                    result.postValue(Resource.Success(responseBody))
                    return

                } else {

                    val gson = GsonBuilder().create()
                    val mError: ErrorResponse
                    try {
                        mError = gson.fromJson(
                            response.errorBody()!!.string(),
                            ErrorResponse::class.java
                        )
                        result.postValue(Resource.Failure(errorMsg = mError.error))
                    } catch (e: Exception) {
                        e.printStackTrace()
                        result.postValue(
                            Resource.Failure(errorMsg = e.message.getEmptyIfNull(), exception = e)
                        )
                    }

                }

            }

            override fun onFailure(call: Call<AssignCashbackResponse>, t: Throwable) {
                Timber.d("onFailure: $t")
                result.postValue(
                    Resource.Failure(errorMsg = t.message.getEmptyIfNull(), exception = t)
                )
            }
        })

        return result

    }

    fun createCashBackTransaction(
        customerId: Int,
        cashbackSettings: Int,
        amountSpent: String,
        cashbackAmount: String
    ): LiveData<Resource<CashBackSuccessResponse>> {

        val result = MutableLiveData<Resource<CashBackSuccessResponse>>()
        result.value = Resource.Loading

        val shopId = sessionManager.fetchMerchantDetails()?.id.getZeroIfNull()
        val token = sessionManager.fetchAuthToken().getEmptyIfNull()

        val userParams = HashMap<String, Any>()
        userParams["shop_id"] = shopId
        userParams["customer"] = customerId
        userParams["cashback_settings"] = cashbackSettings
        userParams["amount_spent"] = amountSpent
        userParams["cashback_amount"] = cashbackAmount

        val response = repository.createCashBackTransaction(
            merchantShopID = shopId, token = token, params = userParams
        )
        response.enqueue(object : Callback<CashBackSuccessResponse> {
            override fun onResponse(
                call: Call<CashBackSuccessResponse>,
                response: Response<CashBackSuccessResponse>
            ) {
                val responseBody = response.body()
                if (response.isSuccessful && responseBody != null) {

                    result.postValue(Resource.Success(responseBody))
                    return

                } else {

                    val gson = GsonBuilder().create()
                    val errorResponse: ErrorResponse
                    try {
                        errorResponse = gson.fromJson(
                            response.errorBody()!!.string(),
                            ErrorResponse::class.java
                        )
                        result.postValue(Resource.Failure(errorMsg = errorResponse.error))
                    } catch (e: Exception) {
                        e.printStackTrace()
                        onFailure(call = call, Throwable(message = e.localizedMessage))
                        result.postValue(
                            Resource.Failure(
                                errorMsg = e.message.getEmptyIfNull(), exception = e
                            )
                        )
                    }

                }

            }

            override fun onFailure(call: Call<CashBackSuccessResponse>, t: Throwable) {
                Timber.d("onFailure: $t")
                result.postValue(
                    Resource.Failure(errorMsg = t.message.getEmptyIfNull(), exception = t)
                )
            }
        })

        return result

    }

}