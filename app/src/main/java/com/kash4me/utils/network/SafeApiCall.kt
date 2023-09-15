package com.kash4me.utils.network

import com.google.gson.Gson
import com.kash4me.data.models.ErrorResponse
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import retrofit2.Response
import timber.log.Timber

object SafeApiCall {

    suspend fun <T> makeNetworkCall(apiCall: suspend () -> Response<T>) = flow<Resource<T>> {
        emit(Resource.Loading)
        try {
            val response = apiCall.invoke()
            Timber.d("Response code -> ${response.code()}")
            val responseBody = response.body()
            when (response.code()) {

                in 200..299 -> {
                    Timber.d("Inside 200s ${response.code()}")
                    emit(Resource.Success(responseBody!!))
                }

                in 400..499 -> {
                    Timber.d("Inside 400s ${response.code()}")
                    val error = Gson().fromJson(
                        response.errorBody()?.charStream(),
                        ErrorResponse::class.java
                    )
                    emit(Resource.Failure(error?.error ?: "Client error", null))
                }

                in 500..599 -> {
                    Timber.d("Inside 500s ${response.code()} | ${response.errorBody()}")
                    val error = Gson().fromJson(
                        response.errorBody()?.charStream(),
                        ErrorResponse::class.java
                    )
                    emit(Resource.Failure(error?.error ?: "Server error", null))
                }
            }
        } catch (e: Exception) {
            Timber.d("Caught exception ${e.message}")
            emit(Resource.Failure(e.message ?: "Unknown error", e))
        }
    }.catch {
        Timber.d("Inside flow catch block: ${it.message}")
        emit(Resource.Failure(it.message ?: "Unknown error", it))
    }

}