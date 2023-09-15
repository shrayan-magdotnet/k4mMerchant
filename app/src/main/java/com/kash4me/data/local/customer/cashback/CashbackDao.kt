package com.kash4me.data.local.customer.cashback

import androidx.lifecycle.LiveData
import androidx.room.*


@Dao
interface CashbackDao {

    @Query("SELECT * FROM cashback ORDER BY id ASC")
    fun getAll(): LiveData<List<CashbackEntity>>

    @Query("SELECT * FROM cashback ORDER BY id ASC LIMIT :limit OFFSET :offset")
    suspend fun getPagedList(limit: Int, offset: Int): List<CashbackEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(cashbacks: List<CashbackEntity>?): List<Long>

    @Query("DELETE FROM cashback")
    suspend fun clear()

    @Transaction
    suspend fun updateAll(cashbacks: List<CashbackEntity>?): List<Long> {
        clear()
        return insertAll(cashbacks)
    }

}