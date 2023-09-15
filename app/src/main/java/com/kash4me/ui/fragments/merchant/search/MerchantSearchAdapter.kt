package com.kash4me.ui.fragments.merchant.search

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.kash4me.data.models.CustomerDetails
import com.kash4me.merchant.databinding.ItemCustomerSearchBinding
import com.kash4me.utils.extensions.formatAsCurrency
import com.kash4me.utils.extensions.getZeroIfNull
import com.kash4me.utils.extensions.toLong
import com.kash4me.utils.listeners.SingleParamItemClickListener
import com.kash4me.utils.setProgressValue
import kotlin.math.roundToInt

class MerchantSearchAdapter(
    private val clickListener: SingleParamItemClickListener<CustomerDetails>
) :
    RecyclerView.Adapter<MerchantSearchAdapter.ViewHolder>() {

    companion object {
        private const val TAG = "MerchantSearchAdapter"
    }

    private val customerDetails = arrayListOf<CustomerDetails>()

    fun setData(customerDetails: List<CustomerDetails>) {
        this.customerDetails.clear()
        this.customerDetails.addAll(customerDetails)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemCustomerSearchBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val customerCashBack = customerDetails[position]
        Log.d(TAG, "onBindViewHolder: $customerCashBack")

        holder.bind(customerCashBack)


    }

    // return the number of the items in the list
    override fun getItemCount(): Int {
        return customerDetails.size
    }


    inner class ViewHolder(val binding: ItemCustomerSearchBinding)

        : RecyclerView.ViewHolder(binding.root) {

        fun bind(customerCashBack: CustomerDetails) {
            // sets the text to the textview from our itemHolder class
            binding.customerNameTV.text = customerCashBack.user_details?.nick_name
            binding.tvCode.text = customerCashBack.user_details?.unique_id.toString()

//        binding.lastVisitDateTV.text = "Last Visit: ${customerCashBack.created_at}"
            binding.earnedTV.setValue(
                customerCashBack.cashback_amount?.toDoubleOrNull().toLong().getZeroIfNull()
            )
//        binding.spentTV.text = "$${customerCashBack.amount_spent.formatAmount}"
//            binding.leftTV.setValue(
//                customerCashBack.amount_left.toDoubleOrNull().toLong().getZeroIfNull()
//            )

            binding.progressBar.setProgressValue(
                progress = customerCashBack.progressPercent?.toDoubleOrNull()?.roundToInt()
                    .getZeroIfNull()
            )

            binding.tvSpentAmount.text =
                "SPENT: " + "$" + customerCashBack.amount_spent?.formatAsCurrency()

            binding.tvGoal.text =
                "GOAL: " + "$" + customerCashBack.goalAmount?.formatAsCurrency()

            val processingAmt = try {
                customerCashBack.processingAmount?.toDoubleOrNull()
            } catch (e: Exception) {
                0.0
            }
            if (processingAmt == null || processingAmt == 0.0) {
                binding.tvProcessing.isVisible = false
            } else {
                binding.tvProcessing.visibility = View.VISIBLE
                binding.tvProcessing.text =
                    "PROCESSING: $" + customerCashBack.processingAmount?.formatAsCurrency()
            }

            binding.root.setOnClickListener {
                clickListener.onClick(item = customerCashBack)
            }
        }

    }
}
