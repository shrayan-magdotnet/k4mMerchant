package com.kash4me.ui.fragments.customer.search

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.kash4me.data.models.Merchant
import com.kash4me.data.models.merchant.cashback.CashbackType
import com.kash4me.merchant.databinding.ItemMerchantStoreBinding
import com.kash4me.utils.ImageUtils
import com.kash4me.utils.formatAmount
import com.kash4me.utils.listeners.SingleParamItemClickListener

class CustomerSearchAdapter(
    private val clickListener: SingleParamItemClickListener<Merchant>
)

    : RecyclerView.Adapter<CustomerSearchAdapter.MerchantStoreViewHolder>() {

    private val mMerchantStores = arrayListOf<Merchant>()

    fun setData(stores: List<Merchant>) {
        mMerchantStores.clear()
        mMerchantStores.addAll(stores)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MerchantStoreViewHolder {
        val itemBinding = ItemMerchantStoreBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return MerchantStoreViewHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: MerchantStoreViewHolder, position: Int) {

        val store = mMerchantStores[position]
        holder.bind(store)

    }

    override fun getItemCount(): Int {
        return mMerchantStores.size
    }


    inner class MerchantStoreViewHolder(private val binding: ItemMerchantStoreBinding)

        : RecyclerView.ViewHolder(binding.root) {

        fun bind(store: Merchant) {

            binding.apply {

                val nameInitials = ImageUtils().getNameInitialsImage(store.name)

                if (!store.logo.isNullOrBlank()) {
                    Glide.with(binding.root.context)
                        .load(store.logo)
                        .placeholder(nameInitials)
                        .error(nameInitials)
                        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                        .into(merchantLogo.ivLogo)
                } else {
                    merchantLogo.ivLogo.setImageDrawable(nameInitials)
                }

                tvMerchantName.text = store.name
                tvAddress.text = store.address

                if (store.promotionalText.isNullOrBlank()) {
                    cvPromotionalBanner.isVisible = false
                } else {
                    cvPromotionalBanner.isVisible = true
                    tvPromotionalBanner.text = store.promotionalText
                }

                if (store.active_cashback_settings.cashback_type == CashbackType.FLAT.id) {

                    if (store.active_cashback_settings.maturity_amount.isEmpty()) {
                        tvSpendAmount.visibility = View.GONE
                    } else {
                        tvSpendAmount.text =
                            "On Every $${store.active_cashback_settings.maturity_amount?.formatAmount} Spent"
                    }

                } else {

                    if (store.active_cashback_settings.maturity_amount.isNullOrEmpty()) {
                        tvSpendAmount.visibility = View.GONE
                    } else {
                        tvSpendAmount.text = "Earn on Every Purchase"
                    }

                }


                if (store.active_cashback_settings.cashback_type == 1) {
                    if (store.active_cashback_settings.cashback_amount.isEmpty()) {
                        tvEarnCashbackCaption.visibility = View.GONE
                        tvEarnCashbackAmount.visibility = View.GONE
                    } else {
                        tvEarnCashbackAmount.text =
                            "$${store.active_cashback_settings.cashback_amount.formatAmount}"
                    }

                } else {
                    if (store.active_cashback_settings.cashback_percentage.isEmpty()) {
                        tvEarnCashbackCaption.visibility = View.GONE
                        tvEarnCashbackAmount.visibility = View.GONE
                    } else {
                        tvEarnCashbackAmount.text =
                            "${store.active_cashback_settings.cashback_percentage}%"
                    }
                }

                if (store.distance.isNotBlank()) {
                    tvDistance.text = store.distance
                    tvDistance.isVisible = true
                    ivLocationMarker.isVisible = true
                } else {
                    tvDistance.isVisible = false
                    ivLocationMarker.isVisible = false
                }

                root.setOnClickListener { clickListener.onClick(item = store) }

            }

        }

    }
}
