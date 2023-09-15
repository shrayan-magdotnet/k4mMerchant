package com.kash4me.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import com.kash4me.utils.NoInternetException
import okhttp3.Interceptor
import okhttp3.Response

class NetworkConnectionInterceptor(
    context: Context
) : Interceptor {

    private val applicationContext = context.applicationContext

    override fun intercept(chain: Interceptor.Chain): Response {
        if (!isInternetAvailable()) {
            throw NoInternetException("Please check your internet connection")
        }

        return chain.proceed(chain.request())
    }

    private fun isInternetAvailable(): Boolean {
        var result = false
        val connectivityManager =
            applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
        connectivityManager?.let {
            it.getNetworkCapabilities(connectivityManager.activeNetwork)?.apply {
                result = when {
                    hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                    hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                    else -> false
                }
            }
        }
        return result
    }

}

class NotFoundInterceptor() : Interceptor {

    companion object {
        private const val TAG = "NetworkInterceptor"
    }


    override fun intercept(chain: Interceptor.Chain): Response {

        val request = chain.request()
        val response = chain.proceed(request)
        Log.d(TAG, "Response code: ${response.code}")
        if (response.code == 401) {
            Log.d(TAG, "intercept: 401 caught in interceptor")
        }

//        when (response.code) {
//
//            401 -> {
//                response.close()
//                throw ApiException(AppConstants.ERROR_401)
//            }
//
//            403 -> {
//                response.close()
//                throw ApiException(AppConstants.ERROR_403)
//            }
//
//            404 -> {
//                response.close()
//                throw ApiException(AppConstants.ERROR_404)
//            }
//
//            409 -> {
//                response.close()
//                throw ApiException(AppConstants.ERROR_409)
//            }
//
//            422 -> {
//                response.close()
//                throw ApiException(AppConstants.ERROR_422)
//            }
//
//            500 -> {
//                response.close()
//                throw ApiException(AppConstants.ERROR_500)
//            }
//
//            501 -> {
//                response.close()
//                throw ApiException(AppConstants.ERROR_501)
//            }
//
//            503 -> {
//                response.close()
//                throw ApiException(AppConstants.ERROR_503)
//            }
//
//            504 -> {
//                response.close()
//                throw ApiException(AppConstants.ERROR_504)
//            }
//
//            else -> {
//                "Do nothing"
//            }
//
//        }

        return response
    }


}