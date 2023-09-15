package com.kash4me.ui.fragments.customer.total_transaction.your_balance_bottom_sheet


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kash4me.data.local.customer.total_transactions.CustomerTotalTransactionDetailsEntityV2.Result
import com.kash4me.merchant.databinding.ItemYourBalanceTransactionBinding
import com.kash4me.utils.extensions.formatAsCurrency
import com.kash4me.utils.extensions.formatUsingCurrencySystem

class YourBalanceTransactionsAdapter :
    RecyclerView.Adapter<YourBalanceTransactionsAdapter.TransactionViewHolder>() {

    private val mTransactions = arrayListOf<Result?>()

    fun setData(transactions: List<Result?>) {
        mTransactions.clear()
        mTransactions.addAll(transactions)
        notifyDataSetChanged()
    }

    override fun getItemCount() = mTransactions.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val itemBinding = ItemYourBalanceTransactionBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return TransactionViewHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {

        val transaction = mTransactions[position]
        holder.bind(transaction)

    }

    class TransactionViewHolder(private val binding: ItemYourBalanceTransactionBinding)

        : RecyclerView.ViewHolder(binding.root) {

        fun bind(cashBack: Result?) {
            binding.apply {

                when (cashBack?.checkTransactionType()) {
                    Result.TransactionType.WITHDRAW -> {
                        val amount =
                            "- $" + cashBack.params?.cashbackAmount?.formatUsingCurrencySystem()
                        tvTransactionAmount.text = amount
                        tvShopName.text = Result.TransactionType.WITHDRAW.value
                    }

                    Result.TransactionType.DEPOSIT -> {
                        val amount = "$" + cashBack.amount?.formatAsCurrency()
                        tvTransactionAmount.text = amount
                        tvShopName.text =
                            if (cashBack.shopName.isNullOrBlank()) "Deposit" else cashBack.shopName
                    }

                    Result.TransactionType.REFUND -> {
                        val amount =
                            "$" + cashBack.params?.cashbackAmount?.formatUsingCurrencySystem()
                        tvTransactionAmount.text = amount
                        tvShopName.text = Result.TransactionType.REFUND.value
                    }

                    else -> {
                        // Do nothing
                    }
                }

                tvTransactionDate.text = cashBack?.createdAt
            }
        }

    }

}