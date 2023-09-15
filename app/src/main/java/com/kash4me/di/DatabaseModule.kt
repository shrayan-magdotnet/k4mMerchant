package com.kash4me.di

import android.content.Context
import androidx.room.Room
import com.kash4me.data.local.AppDatabase
import com.kash4me.data.local.merchant.TransactionsSummaryDao
import com.kash4me.utils.SessionManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Singleton
    @Provides
    fun provideSessionManager(@ApplicationContext context: Context): SessionManager {
        return SessionManager(context)
    }

    @Singleton
    @Provides
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {

        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        ).fallbackToDestructiveMigration().build()

    }

    @Singleton
    @Provides
    fun provideTransactionSummaryDao(appDatabase: AppDatabase): TransactionsSummaryDao {

        return appDatabase.transactionSummaryDao()

    }

}