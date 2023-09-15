package com.kash4me.network

import com.kash4me.utils.App
import com.kash4me.utils.SessionManager
import okhttp3.Interceptor
import okhttp3.Response

class AuthorizationInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {

        val sessionManager = SessionManager(App.getContext()!!)
        val accessToken = sessionManager.fetchAuthToken()

        val requestBuilder = chain.request().newBuilder()
        if (accessToken != null) {
            requestBuilder.addHeader("Authorization", accessToken)

        }
        val request = requestBuilder.build()
        return chain.proceed(request)

    }

}