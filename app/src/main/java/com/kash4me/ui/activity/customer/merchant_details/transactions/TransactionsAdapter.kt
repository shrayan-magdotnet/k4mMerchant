package com.kash4me.ui.activity.customer.merchant_details.transactions


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kash4me.data.models.customer.transactions_according_to_merchant.Result
import com.kash4me.databinding.ItemTransaction2Binding
import com.kash4me.utils.extensions.formatAsCurrency
import com.kash4me.utils.listeners.SingleParamItemClickListener
import timber.log.Timber

class TransactionsAdapter(private val clickListener: SingleParamItemClickListener<Result>) :
    RecyclerView.Adapter<TransactionsAdapter.TransactionViewHolder>() {

    private val mTransactions = arrayListOf<Result>()

    fun setData(transactions: List<Result>) {
        mTransactions.clear()
        mTransactions.addAll(transactions)
        notifyDataSetChanged()
    }

    override fun getItemCount() = mTransactions.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val itemBinding = ItemTransaction2Binding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return TransactionViewHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {

        val transaction = mTransactions[position]
        holder.bind(transaction)

    }

    inner class TransactionViewHolder(private val binding: ItemTransaction2Binding)

        : RecyclerView.ViewHolder(binding.root) {

        fun bind(transaction: Result) {

            binding.apply {
                tvDate.text = transaction.date
                tvPurchaseAmount.text = "$" + transaction.amount_spent.formatAsCurrency()
                tvCashbackAmount.text = "$" + transaction.cashback_amount.formatAsCurrency()

                tvTransactionId.text = transaction.id.toString()
                Timber.d("Transaction Type -> " + transaction.transactionType)

                root.setOnClickListener {
                    when (transaction.transactionType) {
                        TransactionType.PAYMENT.title -> {
                            clickListener.onClick(item = transaction)
                        }

                        TransactionType.PURCHASE_RETURN.title -> {
                            // Do nothing since we shouldn't have to navigate to the details screen
                        }

                        TransactionType.ROLLBACK.title -> {
                            // Do nothing since we shouldn't have to navigate to the details screen
                        }
                    }
                }
            }

        }

    }

    private enum class TransactionType(val title: String) {
        PURCHASE_RETURN(title = "Purchase Return"),
        ROLLBACK(title = "Rollback"),
        PAYMENT(title = "Payment")
    }

}