package com.kash4me.ui.fragments.merchant.branch_details

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kash4me.R
import com.kash4me.data.models.MerchantTransactionSummaryResponse
import com.kash4me.ui.activity.merchant.transaction_details.TransactionDetailsActivity
import com.kash4me.utils.custom_views.CurrencyTextView
import com.kash4me.utils.extensions.getEmptyIfNull
import com.kash4me.utils.extensions.getZeroIfNull
import com.kash4me.utils.extensions.toLong
import timber.log.Timber

class BranchDetailsAdapter(
    private val transactionSummary: MerchantTransactionSummaryResponse,
    val branchId: Int? = null
) :
    RecyclerView.Adapter<BranchDetailsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_merchant_transaction_card, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        Timber.d("onBindViewHolder: position $position")
        Timber.d("onBindViewHolder: $transactionSummary")
        var amount = ""

        if (position == 0) {
            holder.titleTV.text = "Today's Sell"
            amount = transactionSummary.today?.total_amount.getEmptyIfNull()
            holder.amountTV.setValue(
                transactionSummary.today?.total_amount?.toDoubleOrNull().toLong()
                    .getZeroIfNull()
            )
//            holder.amountTV.setTextColor(Color.parseColor("#76A541"))
            holder.transactionTV.text =
                "Transactions: ${transactionSummary.today?.count}"
        }

        if (position == 1) {
            holder.titleTV.text = "Today's CashBack"
            amount = transactionSummary.today?.total_cashback.getEmptyIfNull()
            holder.amountTV.setValue(
                transactionSummary.today?.total_cashback?.toDoubleOrNull().toLong()
                    .getZeroIfNull()
            )
//            holder.amountTV.setTextColor(Color.parseColor("#FF0000"))
            holder.transactionTV.text =
                "Transactions: ${transactionSummary.today?.count}"


        }

        if (position == 2) {
            holder.titleTV.text = "This Week's Sell"
            amount = transactionSummary.weekly?.total_amount.getEmptyIfNull()
            holder.amountTV.setValue(
                transactionSummary.weekly?.total_amount?.toDoubleOrNull().toLong()
                    .getZeroIfNull()
            )
//            holder.amountTV.setTextColor(Color.parseColor("#76A541"))
            holder.transactionTV.text =
                "Transactions: ${transactionSummary.weekly?.count}"

        }

        if (position == 3) {
            holder.titleTV.text = "This Week CashBack"
            amount = transactionSummary.weekly?.total_cashback.getEmptyIfNull()
            holder.amountTV.setValue(
                transactionSummary.weekly?.total_cashback?.toDoubleOrNull().toLong()
                    .getZeroIfNull()
            )
//            holder.amountTV.setTextColor(Color.parseColor("#FF0000"))
            holder.transactionTV.text =
                "Transactions: ${transactionSummary.weekly?.count}"


        }

        if (position == 4) {
            holder.titleTV.text = "This Month Sell"
            amount = transactionSummary.monthly?.total_amount.getEmptyIfNull()
            holder.amountTV.setValue(
                transactionSummary.monthly?.total_amount?.toDoubleOrNull().toLong()
                    .getZeroIfNull()
            )
//            holder.amountTV.setTextColor(Color.parseColor("#76A541"))
            holder.transactionTV.text =
                "Transactions: ${transactionSummary.monthly?.count}"

        }

        if (position == 5) {
            holder.titleTV.text = "This Month CashBack"
            amount = transactionSummary.monthly?.total_cashback.getEmptyIfNull()
            holder.amountTV.setValue(
                transactionSummary.monthly?.total_cashback?.toDoubleOrNull().toLong()
                    .getZeroIfNull()
            )
//            holder.amountTV.setTextColor(Color.parseColor("#FF0000"))
            holder.transactionTV.text =
                "Transactions: ${transactionSummary.monthly?.count}"


        }

        holder.itemView.setOnClickListener {

            Timber.d("")

            val intent = Intent(holder.itemView.context, TransactionDetailsActivity::class.java)
            var mode = ""
            var transactionType = ""

            if (position == 0) {
                mode = "today"
                transactionType = "sell_data"
            }

            if (position == 1) {
                mode = "today"
                transactionType = "cashback_data"
            }

            if (position == 2) {
                mode = "weekly"
                transactionType = "sell_data"
            }

            if (position == 3) {
                mode = "weekly"
                transactionType = "cashback_data"
            }

            if (position == 4) {
                mode = "monthly"
                transactionType = "sell_data"
            }

            if (position == 5) {
                mode = "monthly"
                transactionType = "cashback_data"
            }

            intent.putExtra("mode", mode)
            intent.putExtra("transaction_type", transactionType)
            intent.putExtra("title", holder.titleTV.text)
            intent.putExtra("totalAmount", amount)
            intent.putExtra("branchId", branchId)
            holder.itemView.context.startActivity(intent)
        }


    }

    // return the number of the items in the list
    override fun getItemCount(): Int {
        return 6
    }


    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val titleTV: TextView
        val amountTV: CurrencyTextView
        val transactionTV: TextView

        init {
            // Define click listener for the ViewHolder's View.
            titleTV = view.findViewById(R.id.titleTV)
            amountTV = view.findViewById(R.id.amountTV)
            transactionTV = view.findViewById(R.id.transactionsTV)

        }

    }
}
