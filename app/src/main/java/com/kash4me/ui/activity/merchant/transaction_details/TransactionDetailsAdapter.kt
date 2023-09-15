package com.kash4me.ui.activity.merchant.transaction_details

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kash4me.data.models.Transaction
import com.kash4me.merchant.R
import com.kash4me.utils.formatAmount

class TransactionDetailsAdapter(private val todaySellList: List<Transaction>) :
    RecyclerView.Adapter<TransactionDetailsAdapter.ViewHolder>() {

    // create new views
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // inflates the card_view_design view
        // that is used to hold list item
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_today_sell, parent, false)

        return ViewHolder(view)
    }

    // binds the list items to a view
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val dashboardTransaction = todaySellList[position]
//
//        // sets the text to the textview from our itemHolder class
        holder.customerIDTV.text = dashboardTransaction.customer?.id.toString().uppercase()
        holder.earnedTV.text = "$${dashboardTransaction.cashback_amount.formatAmount}"
        holder.amountTV.text = "$${dashboardTransaction.amount_spent.formatAmount}"

    }

    // return the number of the items in the list
    override fun getItemCount(): Int {
        return todaySellList.size
    }

    // Holds the views for adding it to image and text
//    class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
//        val titleTV: TextView = itemView.findViewById(R.id.titleTV)
//        val amountTV: TextView = itemView.findViewById(R.id.amountTV)
//        val transactionTV: TextView = itemView.findViewById(R.id.transactionsTV)
//
//    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val customerIDTV: TextView
        val earnedTV: TextView
        val amountTV: TextView

        init {
//            // Define click listener for the ViewHolder's View.
            customerIDTV = view.findViewById(R.id.customerIDTV)
            earnedTV = view.findViewById(R.id.earnedTV)
            amountTV = view.findViewById(R.id.amountTV)
//
//            view.setOnClickListener {
//                val intent = Intent(view.context, TodaySellActivity::class.java)
//                view.context.startActivity(intent)
//            }

        }

    }
}
