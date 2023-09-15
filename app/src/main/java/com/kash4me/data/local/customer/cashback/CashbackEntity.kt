package com.kash4me.data.local.customer.cashback

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "cashback")
data class CashbackEntity(
    @ColumnInfo(name = "amount_left")
    val amountLeft: String?,
    @ColumnInfo(name = "amount_spent")
    val amountSpent: String?,
    @ColumnInfo(name = "cashback_amount")
    val cashbackAmount: String?,
    @ColumnInfo(name = "count")
    val count: Int?,
    @SerializedName("goal_amount")
    val goalAmount: String?,
    @SerializedName("progress_percent")
    val progressPercent: String?,
    @ColumnInfo(name = "processing_amount")
    val processingAmount: String?,
    @ColumnInfo(name = "shop_details")
    val shopDetails: ShopDetails?,
    @SerializedName("total_processing")
    val totalProcessing: String?
) {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var id: Int = 0

}

data class ShopDetails(
    val address: String?,
    val logo: String?,
    val name: String?,
    val shopId: Int?
)

