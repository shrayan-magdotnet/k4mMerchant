package com.kash4me.di

import android.content.Context
import android.os.Build
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.kash4me.BuildConfig
import com.kash4me.network.ApiServices
import com.kash4me.network.EndPoint
import com.kash4me.network.NetworkConnectionInterceptor
import com.kash4me.network.NotFoundInterceptor
import com.kash4me.network.TokenRefreshAuthenticator
import com.readystatesoftware.chuck.ChuckInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object RetrofitModule {

    @Singleton
    @Provides
    fun provideGsonBuilder(): Gson {
        return GsonBuilder().create()
    }

    @Singleton
    @Provides
    fun provideRetrofit(gson: Gson, okHttpClient: OkHttpClient): Retrofit.Builder {
        return Retrofit.Builder()
            .client(okHttpClient)
            .baseUrl(EndPoint.API.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
    }

    @Singleton
    @Provides
    fun provideOkHttpClient(
        @ApplicationContext applicationContext: Context,
        loggingInterceptor: HttpLoggingInterceptor,
        networkConnectionInterceptor: NetworkConnectionInterceptor,
        notFoundInterceptor: NotFoundInterceptor
    ): OkHttpClient {
        val builder = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS) // Set the connection timeout
            .readTimeout(30, TimeUnit.SECONDS) // Set the read timeout
            .writeTimeout(30, TimeUnit.SECONDS) // Set the write timeout

        builder.addInterceptor(loggingInterceptor)
            .addInterceptor(networkConnectionInterceptor)
            .addInterceptor(notFoundInterceptor)
//                .addInterceptor(AuthorizationInterceptor())
            .authenticator(TokenRefreshAuthenticator())

        if (BuildConfig.DEBUG && Build.VERSION.SDK_INT < 31) {
            val chuckInterceptor = ChuckInterceptor(applicationContext)
            builder.addInterceptor(chuckInterceptor)
        }

        return builder.build()
    }

    @Singleton
    @Provides
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        val interceptor = HttpLoggingInterceptor()
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
        return interceptor
    }

    @Singleton
    @Provides
    fun provideNetworkConnectionInterceptor(@ApplicationContext applicationContext: Context): NetworkConnectionInterceptor {
        return NetworkConnectionInterceptor(applicationContext)
    }

    @Singleton
    @Provides
    fun provideNotFoundInterceptor(): NotFoundInterceptor {
        return NotFoundInterceptor()
    }

    @Singleton
    @Provides
    fun provideBlogService(retrofit: Retrofit.Builder): ApiServices {
        return retrofit.build().create(ApiServices::class.java)
    }

}