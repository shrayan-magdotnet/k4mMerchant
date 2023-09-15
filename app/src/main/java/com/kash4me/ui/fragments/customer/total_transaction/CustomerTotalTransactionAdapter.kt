package com.kash4me.ui.fragments.customer.total_transaction

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kash4me.data.local.customer.total_transactions.CustomerTotalTransactionDetailsEntityV2
import com.kash4me.data.local.customer.total_transactions.CustomerTotalTransactionDetailsEntityV2.Result.TransactionType
import com.kash4me.merchant.databinding.ItemCashbackBinding
import com.kash4me.utils.extensions.formatAsCurrency
import com.kash4me.utils.extensions.formatUsingCurrencySystem


class CustomerTotalTransactionAdapter

    : RecyclerView.Adapter<CustomerTotalTransactionAdapter.TransactionDetailsViewHolder>() {

    private val cashbacks = arrayListOf<CustomerTotalTransactionDetailsEntityV2.Result?>()

    fun setData(cashbacks: List<CustomerTotalTransactionDetailsEntityV2.Result?>) {
        this.cashbacks.clear()
        this.cashbacks.addAll(cashbacks)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): TransactionDetailsViewHolder {
        val itemBinding = ItemCashbackBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return TransactionDetailsViewHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: TransactionDetailsViewHolder, position: Int) {

        val cashback = cashbacks[position]
        holder.bind(cashback)

    }

    override fun getItemCount(): Int {
        return cashbacks.size
    }

    class TransactionDetailsViewHolder(private val binding: ItemCashbackBinding)

        : RecyclerView.ViewHolder(binding.root) {

        fun bind(cashBack: CustomerTotalTransactionDetailsEntityV2.Result?) {
            binding.apply {

                when (cashBack?.checkTransactionType()) {
                    TransactionType.WITHDRAW -> {
                        val amount =
                            "- $" + cashBack.params?.cashbackAmount?.formatUsingCurrencySystem()
                        amountTV.text = amount
                        merchantNameTV.text = TransactionType.WITHDRAW.value
                    }

                    TransactionType.DEPOSIT -> {
                        val amount = "$" + cashBack.amount?.formatAsCurrency()
                        amountTV.text = amount
                        merchantNameTV.text =
                            if (cashBack.shopName.isNullOrBlank()) "Deposit" else cashBack.shopName
                    }

                    TransactionType.REFUND -> {
                        val amount =
                            "$" + cashBack.params?.cashbackAmount?.formatUsingCurrencySystem()
                        amountTV.text = amount
                        merchantNameTV.text = TransactionType.REFUND.value
                    }

                    else -> {
                        // Do nothing
                    }
                }

                dateTV.text = cashBack?.createdAt
            }
        }

    }
}
