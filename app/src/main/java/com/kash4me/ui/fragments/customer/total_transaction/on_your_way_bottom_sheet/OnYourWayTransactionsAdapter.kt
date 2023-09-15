package com.kash4me.ui.fragments.customer.total_transaction.on_your_way_bottom_sheet


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kash4me.data.local.customer.total_transactions.ProcessingTransactionEntity
import com.kash4me.merchant.databinding.ItemOnYourWayTransactionBinding

class OnYourWayTransactionsAdapter

    : RecyclerView.Adapter<OnYourWayTransactionsAdapter.TransactionViewHolder>() {

    private val mTransactions = arrayListOf<ProcessingTransactionEntity>()

    fun setData(transactions: List<ProcessingTransactionEntity>) {
        mTransactions.clear()
        mTransactions.addAll(transactions)
        notifyDataSetChanged()
    }

    override fun getItemCount() = mTransactions.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val itemBinding = ItemOnYourWayTransactionBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return TransactionViewHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {

        val transaction = mTransactions[position]
        holder.bind(transaction)

    }

    class TransactionViewHolder(private val binding: ItemOnYourWayTransactionBinding)

        : RecyclerView.ViewHolder(binding.root) {

        fun bind(cashBack: ProcessingTransactionEntity) {
            binding.apply {
                tvShopName.text = cashBack.shopName
                tvTransactionDate.text = cashBack.createdAt
                tvTransactionAmount.text = cashBack.params?.cashbackAmount
            }
        }

    }

}