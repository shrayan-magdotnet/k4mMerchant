package com.kash4me.ui.fragments.customer.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.kash4me.data.local.customer.cashback.CashbackEntity
import com.kash4me.data.local.customer.cashback.ShopDetails
import com.kash4me.merchant.databinding.ItemCashbackCardBinding
import com.kash4me.utils.CustomFilter
import com.kash4me.utils.ImageUtils
import com.kash4me.utils.extensions.formatUsingCurrencySystem
import com.kash4me.utils.extensions.getNotAvailableIfEmptyOrNull
import com.kash4me.utils.extensions.getZeroIfNull
import com.kash4me.utils.formatAmount
import com.kash4me.utils.listeners.SingleParamItemClickListener
import com.kash4me.utils.setProgressValue
import timber.log.Timber


class CustomerHomeAdapter(
    private val clickListener: SingleParamItemClickListener<ShopDetails?>
) : Adapter<CustomerHomeAdapter.CashbackViewHolder>(), Filterable {

    private val originalCashbacks = arrayListOf<CashbackEntity>()
    private val filteredCashbacks = arrayListOf<CashbackEntity>()

    fun getCashbacks(): List<CashbackEntity> {
        return originalCashbacks
    }

    fun setData(cashbacks: List<CashbackEntity>) {
        val diffCallback = CashbackDiffCallback(originalCashbacks, cashbacks)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        originalCashbacks.clear()
        originalCashbacks.addAll(cashbacks)
        setFilteredCashbacks(cashbacks)
        diffResult.dispatchUpdatesTo(this)
    }

    private fun setFilteredCashbacks(cashbacks: List<CashbackEntity>) {
        filteredCashbacks.clear()
        filteredCashbacks.addAll(cashbacks)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CashbackViewHolder {
        return CashbackViewHolder(
            ItemCashbackCardBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(viewHolder: CashbackViewHolder, position: Int) {

        val cashback = filteredCashbacks[position]
        Timber.d("Let's bind -> $cashback")
        viewHolder.bind(cashback)

    }

    override fun getItemCount(): Int {
        return filteredCashbacks.size
    }

    override fun getFilter(): Filter {
        return object : CustomFilter<CashbackEntity>(mItems = originalCashbacks) {
            override fun matchesCriteria(userQuery: String, item: CashbackEntity): Boolean {
                return item.shopDetails?.name?.contains(userQuery, ignoreCase = true) == true
                        || item.shopDetails?.address?.contains(userQuery, ignoreCase = true) == true
            }

            override fun publishResults(userQuery: CharSequence?, results: FilterResults?) {

                if (results?.values != null) {

                    val filteredResult = results.values as List<CashbackEntity>
                    setFilteredCashbacks(filteredResult)

                }

            }
        }
    }

    inner class CashbackViewHolder(val binding: ItemCashbackCardBinding)

        : RecyclerView.ViewHolder(binding.root) {

        fun bind(cashback: CashbackEntity) {

            binding.apply {

                val nameInitials =
                    ImageUtils().getNameInitialsImage(cashback.shopDetails?.name.getNotAvailableIfEmptyOrNull())

                if (!cashback.shopDetails?.logo.isNullOrBlank()) {
                    Glide.with(binding.root.context)
                        .load(cashback.shopDetails?.logo)
                        .placeholder(nameInitials)
                        .error(nameInitials)
                        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                        .into(merchantLogo.ivLogo)
                } else {
                    merchantLogo.ivLogo.setImageDrawable(nameInitials)
                }

//                progressBar.progress = cashback.progressPercent?.toDoubleOrNull()?.toInt().getZeroIfNull()
                progressBar.setProgressValue(
                    progress = cashback.progressPercent?.toDoubleOrNull()?.toInt().getZeroIfNull(),
                    shouldAnimate = true
                )

                if (cashback.processingAmount?.toDoubleOrNull().getZeroIfNull() > 0) {
                    tvOnYourWay.visibility = View.VISIBLE
                    tvOnYourWay.text = "ON YOUR WAY: $" + cashback.processingAmount?.formatAmount
                } else {
                    tvOnYourWay.visibility = View.GONE
                }

                tvShopName.text = cashback.shopDetails?.name
                tvAddress.text = cashback.shopDetails?.address
                tvSpentAmount.text = "YOU SPENT: $" + cashback.amountSpent?.formatAmount
                tvGoal.text =
                    "GOAL: $" + cashback.goalAmount?.toDoubleOrNull()?.formatUsingCurrencySystem()
                tvCashRewards.text = "$" + cashback.cashbackAmount?.formatAmount

            }

            binding.root.setOnClickListener {
                clickListener.onClick(item = cashback.shopDetails)
            }

        }

    }

    inner class CashbackDiffCallback(
        private val oldList: List<CashbackEntity>,
        private val newList: List<CashbackEntity>
    ) : DiffUtil.Callback() {

        override fun getOldListSize(): Int = oldList.size

        override fun getNewListSize(): Int = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].id == newList[newItemPosition].id
        }

        override fun areContentsTheSame(oldPosition: Int, newPosition: Int): Boolean {
            val (_, value, name) = oldList[oldPosition]
            val (_, value1, name1) = newList[newPosition]

            return name == name1 && value == value1
        }

        override fun getChangePayload(oldPosition: Int, newPosition: Int): Any? {
            return super.getChangePayload(oldPosition, newPosition)
        }
    }

}
