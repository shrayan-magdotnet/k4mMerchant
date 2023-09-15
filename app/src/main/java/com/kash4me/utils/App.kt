package com.kash4me.utils

import android.app.Application
import android.content.Context
import com.google.android.libraries.places.api.Places
import com.kash4me.BuildConfig
import com.kash4me.R
import dagger.hilt.android.HiltAndroidApp
import io.sentry.Sentry
import timber.log.Timber
import timber.log.Timber.Forest.plant

@HiltAndroidApp
class App : Application() {

    companion object {
        private var instance: App? = null

        fun getInstance(): App? {
            return instance
        }

        fun getContext(): Context? {
            return instance
        }
    }

    override fun onCreate() {
        instance = this
        super.onCreate()
        initPlacesAPI()

        if (BuildConfig.DEBUG) {
            plant(Timber.DebugTree())
        }

        // Transaction can be started by providing, at minimum, the name and the operation
        val transaction =
            Sentry.startTransaction("test-transaction-name", "test-transaction-operation")

        // Transactions can have child spans (and those spans can have child spans as well)
        val span = transaction.startChild("test-child-operation")

        // ...
        // (Perform the operation represented by the span/transaction)
        // ...

        span.finish() // Mark the span as finished
        transaction.finish() // Mark the transaction as finished and send it to Sentry


    }

    private fun initPlacesAPI() {
        if (!Places.isInitialized()) {
            val apiKey = getString(R.string.places_api_key)
            Places.initialize(applicationContext, apiKey)
        }
    }

}