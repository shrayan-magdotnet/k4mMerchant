package com.kash4me.network

import android.util.Log
import com.kash4me.data.models.request.RefreshAccessTokenRequest
import com.kash4me.repository.UserDetailsRepository
import com.kash4me.utils.App
import com.kash4me.utils.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

class TokenRefreshAuthenticator : Authenticator {

    companion object {
        private const val TAG = "TokenAuthenticator"
    }

    val repository by lazy {
        val apiInterface =
            ApiServices.invoke(
                networkConnectionInterceptor = NetworkConnectionInterceptor(App.getContext()!!),
                notFoundInterceptor = NotFoundInterceptor()
            )
        UserDetailsRepository(apiInterface)
    }

    var sessionManager: SessionManager? = null

    override fun authenticate(route: Route?, response: Response): Request? {

        val url = response.request.url.toString()
        Log.d(TAG, "authenticate: url -> $url")

        val refreshTokenEndPoint = EndPoint.API.BASE_URL + EndPoint.API.REFRESH_ACCESS_TOKEN

        val isRefreshTokenApiCalled = url == refreshTokenEndPoint
        Log.d(TAG, "authenticate: is refresh token api called -> $isRefreshTokenApiCalled")

        // When 401 is encountered for any API, it triggers this authenticate() of Authenticator
        // We are calling refresh token API for fetching new access token
        // If refresh token is invalid or has expired, then server returns 401
        // then it would again trigger authenticate() of Authenticator
        // since refresh token is invalid it will again return 401
        // this will lead to infinite loop of 401 -> refresh token API call -> 401 and on and on
        // so we should check if refresh token API is called and navigate to Login screen
        if (isRefreshTokenApiCalled) {
            logOutUser()
            return null // returning null to stop retry
        }

        val retryCount = response.retryCount
        Log.d(TAG, "authenticate: retry count -> $retryCount")

        return refreshAccessToken(response)

    }

    private fun refreshAccessToken(authResponse: Response): Request? {

        val refreshToken = sessionManager?.fetchRefreshToken()
        Log.d(TAG, "authenticate: refresh token -> $refreshToken")

//        val brokenRefreshToken = refreshToken + "a"
        val request = RefreshAccessTokenRequest(refresh = refreshToken)
        val response = repository.refreshAccessToken(request = request).execute()
        if (response.isSuccessful) {
            Log.d(TAG, "onResponse: success -> ${response.body()}")
            val newAccessToken = response.body()?.access
            if (newAccessToken == null) {
                return null
            } else {
                sessionManager?.saveAuthToken(token = "Bearer $newAccessToken")
                Log.d(TAG, "onResponse: let's update access token")
                return authResponse.request.newBuilder()
                    .header("Authorization", "Bearer $newAccessToken")
                    .build()
            }
        } else {
//            val gson = GsonBuilder().create()
//            val errorResponse: ErrorResponse
            return try {
//                errorResponse = gson.fromJson(
//                    response.errorBody()!!.string(),
//                    ErrorResponse::class.java
//                )
                logOutUser()
                null
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    private val Response.retryCount: Int
        get() {
            var currentResponse = priorResponse
            var result = 0
            while (currentResponse != null) {
                result++
                currentResponse = currentResponse.priorResponse
            }
            return result
        }

    private fun logOutUser() {
        CoroutineScope(Dispatchers.IO).launch {
            sessionManager?.logoutUser(packageContext = App().applicationContext)
        }
//        if (!App.getContext()?.javaClass?.simpleName.equals("LoginActivity", true)) {
//            val intent = Intent(App.getContext(), LoginActivity::class.java)
//            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//            App.getContext()?.startActivity(intent)
//        }
    }

}