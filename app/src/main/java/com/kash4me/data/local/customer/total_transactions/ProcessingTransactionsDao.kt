package com.kash4me.data.local.customer.total_transactions

import androidx.lifecycle.LiveData
import androidx.room.*


@Dao
interface ProcessingTransactionsDao {

    @Query("SELECT * FROM processing_transactions")
    fun getAll(): LiveData<List<ProcessingTransactionEntity>?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(processingTransactions: List<ProcessingTransactionEntity>)

    @Query("DELETE FROM processing_transactions")
    suspend fun clear()

    @Transaction
    suspend fun update(processingTransactions: List<ProcessingTransactionEntity>) {
        clear()
        return insert(processingTransactions)
    }

}