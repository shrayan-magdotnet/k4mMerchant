package com.kash4me.data.local.merchant

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface TransactionsSummaryDao {

    @Query("SELECT * FROM merchant_transaction_summary")
    fun getTransactionSummary(): LiveData<MerchantTransactionSummaryEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(summary: MerchantTransactionSummaryEntity): Long

    @Query("DELETE FROM merchant_transaction_summary")
    suspend fun clear()

}