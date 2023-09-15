package com.kash4me.data.local.customer.total_transactions


import androidx.annotation.Keep
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.kash4me.utils.AppConstants

@Entity(tableName = "customer_total_transactions")
data class CustomerTotalTransactionDetailsEntityV2(
    @ColumnInfo("results")
    val results: List<Result?>? = listOf(),
    @ColumnInfo("transaction_details")
    val transactionDetails: TransactionDetails? = TransactionDetails()
) {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var id: Int = AppConstants.DEFAULT_PRIMARY_KEY

    @Keep
    data class Result(
        @ColumnInfo("after_amount")
        val afterAmount: String? = "",
        @ColumnInfo("amount")
        val amount: String? = "",
        @ColumnInfo("created_at")
        val createdAt: String? = "",
        @ColumnInfo("params")
        val params: Params? = Params(),
        @ColumnInfo("shop_name")
        val shopName: String? = "",
        @ColumnInfo("transaction_type")
        val transactionType: String? = ""
    ) {

        @Keep
        data class Params(
            @ColumnInfo("cashback_amount")
            val cashbackAmount: Double? = 0.0,
            @ColumnInfo("fee_amount")
            val feeAmount: Double? = 0.0
        )

        enum class TransactionType(val value: String) {
            WITHDRAW(value = "Withdraw"),
            DEPOSIT(value = "Deposit"),
            REFUND(value = "Refund"),
        }

        fun checkTransactionType(): TransactionType? {
            return if (transactionType.equals(TransactionType.WITHDRAW.value, ignoreCase = true)) {
                TransactionType.WITHDRAW
            } else if (transactionType.equals(TransactionType.DEPOSIT.value, ignoreCase = true)) {
                TransactionType.DEPOSIT
            } else if (transactionType.equals(TransactionType.REFUND.value, ignoreCase = true)) {
                TransactionType.REFUND
            } else {
                null
            }
        }

    }

    @Keep
    data class TransactionDetails(
        @ColumnInfo("cashback_balance")
        val cashbackBalance: String? = "",
        @ColumnInfo("processing")
        val processing: String? = ""
    )
}