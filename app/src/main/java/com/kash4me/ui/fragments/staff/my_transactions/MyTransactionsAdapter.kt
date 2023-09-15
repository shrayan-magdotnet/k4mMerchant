package com.kash4me.ui.fragments.staff.my_transactions


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kash4me.data.models.staff.StaffTransactionsResponse
import com.kash4me.merchant.databinding.ItemStaffTransactionBinding
import com.kash4me.utils.extensions.getZeroIfNull
import com.kash4me.utils.extensions.toLong

class MyTransactionsAdapter : RecyclerView.Adapter<MyTransactionsAdapter.TransactionViewHolder>() {

    data class StaffTransaction(
        val name: String,
        val code: String,
        val amount: Double,
        val date: String,
        val time: String,
        val cashbackValue: Double
    )

    private val mTransactions = arrayListOf<StaffTransactionsResponse.Result?>()

    fun setData(transactions: List<StaffTransactionsResponse.Result?>) {
        mTransactions.clear()
        mTransactions.addAll(transactions)
        notifyDataSetChanged()
    }

    override fun getItemCount() = mTransactions.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val itemBinding = ItemStaffTransactionBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return TransactionViewHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {

        val transaction = mTransactions[position]
        holder.bind(transaction)

    }

    inner class TransactionViewHolder(private val binding: ItemStaffTransactionBinding)

        : RecyclerView.ViewHolder(binding.root) {

        fun bind(transaction: StaffTransactionsResponse.Result?) {

            binding.apply {
                tvName.text = transaction?.customerDetails?.nickName
                tvCode.text = transaction?.customerDetails?.uniqueId.toString()
                tvAmount.setValue(
                    transaction?.amountSpent?.toDoubleOrNull().toLong().getZeroIfNull()
                )
                tvDateTime.text = transaction?.createdAt
                tvCashbackBody.setValue(
                    transaction?.cashbackAmount?.toDoubleOrNull().toLong().getZeroIfNull()
                )
            }

        }

    }

}