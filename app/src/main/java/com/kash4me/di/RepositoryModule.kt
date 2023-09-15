package com.kash4me.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
//
//    @Singleton
//    @Provides
//    fun provideMerchantTransactionSummaryRepository(
//        transactionsSummaryDao: TransactionsSummaryDao,
//        apiServices: ApiServices
//    ): MerchantTransactionSummaryRepository {
//
//        return MerchantTransactionSummaryRepository(
//            apiServices = apiServices,
//            transactionsSummaryDao = transactionsSummaryDao
//        )
//
//    }

}