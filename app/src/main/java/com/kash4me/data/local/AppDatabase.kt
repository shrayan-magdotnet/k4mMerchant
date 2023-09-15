package com.kash4me.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.kash4me.data.local.customer.CustomerTypeConverters
import com.kash4me.data.local.customer.cashback.CashbackDao
import com.kash4me.data.local.customer.cashback.CashbackEntity
import com.kash4me.data.local.customer.total_transactions.CustomerTotalTransactionDetailsDao
import com.kash4me.data.local.customer.total_transactions.CustomerTotalTransactionDetailsEntity
import com.kash4me.data.local.customer.total_transactions.CustomerTotalTransactionDetailsEntityV2
import com.kash4me.data.local.customer.total_transactions.ProcessingTransactionEntity
import com.kash4me.data.local.customer.total_transactions.ProcessingTransactionsDao
import com.kash4me.data.local.merchant.MerchantTransactionSummaryEntity
import com.kash4me.data.local.merchant.MerchantTypeConverters
import com.kash4me.data.local.merchant.TransactionsSummaryDao

@Database(
    entities = [
        MerchantTransactionSummaryEntity::class,
        CashbackEntity::class,
        CustomerTotalTransactionDetailsEntity::class,
        CustomerTotalTransactionDetailsEntityV2::class,
        ProcessingTransactionEntity::class
    ],
    version = 7
)
@TypeConverters(value = [MerchantTypeConverters::class, CustomerTypeConverters::class])
abstract class AppDatabase : RoomDatabase() {

    companion object {

        const val DATABASE_NAME = "kash4me_db"

    }

    abstract fun transactionSummaryDao(): TransactionsSummaryDao
    abstract fun cashbackDao(): CashbackDao
    abstract fun customerTotalTransactionDetailsDao(): CustomerTotalTransactionDetailsDao
    abstract fun processingTransactionsDao(): ProcessingTransactionsDao

}