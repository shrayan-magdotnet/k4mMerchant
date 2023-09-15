package com.kash4me.data.local.merchant

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.kash4me.data.models.Monthly
import com.kash4me.data.models.Today
import com.kash4me.data.models.Weekly

class MerchantTypeConverters {

    @TypeConverter
    fun jsonToToday(value: String?): Today? {
        val objType = object : TypeToken<Today?>() {}.type
        return Gson().fromJson(value, objType)
    }

    @TypeConverter
    fun todayToJson(obj: Today?): String? {
        val gson = Gson()
        return gson.toJson(obj)
    }

    @TypeConverter
    fun jsonToWeekly(value: String?): Weekly? {
        val objType = object : TypeToken<Weekly?>() {}.type
        return Gson().fromJson(value, objType)
    }

    @TypeConverter
    fun weeklyToJson(obj: Weekly?): String? {
        val gson = Gson()
        return gson.toJson(obj)
    }

    @TypeConverter
    fun jsonToMonthly(value: String?): Monthly? {
        val objType = object : TypeToken<Monthly?>() {}.type
        return Gson().fromJson(value, objType)
    }

    @TypeConverter
    fun monthlyToJson(obj: Monthly?): String? {
        val gson = Gson()
        return gson.toJson(obj)
    }

}