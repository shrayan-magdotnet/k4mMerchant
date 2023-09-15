package com.kash4me.data.local.customer

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.kash4me.data.local.customer.cashback.ShopDetails
import com.kash4me.data.local.customer.total_transactions.CustomerTotalTransactionDetailsEntity
import com.kash4me.data.local.customer.total_transactions.CustomerTotalTransactionDetailsEntityV2
import com.kash4me.data.local.customer.total_transactions.ProcessingTransactionEntity

class CustomerTypeConverters {

    @TypeConverter
    fun jsonToShopDetails(value: String?): ShopDetails? {
        val objType = object : TypeToken<ShopDetails?>() {}.type
        return Gson().fromJson(value, objType)
    }

    @TypeConverter
    fun shopDetailsToJson(obj: ShopDetails?): String? {
        val gson = Gson()
        return gson.toJson(obj)
    }

    @TypeConverter
    fun jsonToTransactionDetailsResult(value: String?): List<CustomerTotalTransactionDetailsEntity.Result?> {
        val objType =
            object : TypeToken<List<CustomerTotalTransactionDetailsEntity.Result?>>() {}.type
        return Gson().fromJson(value, objType)
    }

    @TypeConverter
    fun transactionDetailsResultToJson(obj: List<CustomerTotalTransactionDetailsEntity.Result?>): String? {
        val gson = Gson()
        return gson.toJson(obj)
    }

    @TypeConverter
    fun jsonToTransactionDetails(value: String?): CustomerTotalTransactionDetailsEntity.TransactionDetails? {
        val objType =
            object : TypeToken<CustomerTotalTransactionDetailsEntity.TransactionDetails?>() {}.type
        return Gson().fromJson(value, objType)
    }

    @TypeConverter
    fun transactionDetailsToJson(obj: CustomerTotalTransactionDetailsEntity.TransactionDetails?): String? {
        val gson = Gson()
        return gson.toJson(obj)
    }

    @TypeConverter
    fun jsonToTransactionResults(value: String?): List<CustomerTotalTransactionDetailsEntityV2.Result?> {
        val objType =
            object : TypeToken<List<CustomerTotalTransactionDetailsEntityV2.Result?>>() {}.type
        return Gson().fromJson(value, objType)
    }

    @TypeConverter
    fun transactionResultsToJson(obj: List<CustomerTotalTransactionDetailsEntityV2.Result?>): String? {
        val gson = Gson()
        return gson.toJson(obj)
    }

    @TypeConverter
    fun jsonToTransactionDetailsV2(value: String?): CustomerTotalTransactionDetailsEntityV2.TransactionDetails? {
        val objType =
            object :
                TypeToken<CustomerTotalTransactionDetailsEntityV2.TransactionDetails?>() {}.type
        return Gson().fromJson(value, objType)
    }

    @TypeConverter
    fun transactionDetailsV2ToJson(obj: CustomerTotalTransactionDetailsEntityV2.TransactionDetails?): String? {
        val gson = Gson()
        return gson.toJson(obj)
    }

    @TypeConverter
    fun jsonToProcessingTransactionsParams(value: String?): ProcessingTransactionEntity.Params? {
        val objType = object : TypeToken<ProcessingTransactionEntity.Params?>() {}.type
        return Gson().fromJson(value, objType)
    }

    @TypeConverter
    fun processingTransactionsParamsToJson(obj: ProcessingTransactionEntity.Params?): String? {
        val gson = Gson()
        return gson.toJson(obj)
    }

}