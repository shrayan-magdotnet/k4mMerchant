package com.kash4me.data.local.customer.total_transactions

import androidx.lifecycle.LiveData
import androidx.room.*


@Dao
interface CustomerTotalTransactionDetailsDao {

    @Query("SELECT * FROM customer_total_transactions")
    fun get(): LiveData<CustomerTotalTransactionDetailsEntityV2?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(totalTransactionDetails: CustomerTotalTransactionDetailsEntityV2): Long

    @Query("DELETE FROM customer_total_transactions")
    suspend fun clear()

    @Transaction
    suspend fun update(totalTransactionDetails: CustomerTotalTransactionDetailsEntityV2): Long {
        clear()
        return insert(totalTransactionDetails)
    }

}