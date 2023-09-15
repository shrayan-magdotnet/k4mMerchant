package com.kash4me.ui.activity.merchant.transaction_details.utils

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.kash4me.R
import com.kash4me.databinding.ItemTodaySellNewBinding
import com.kash4me.databinding.ViewBookDetailsComponentBinding
import com.kash4me.ui.activity.merchant.transaction_details.model.TransactionStickyModel
import com.kash4me.utils.extensions.getZeroIfNull
import com.kash4me.utils.extensions.toLong
import timber.log.Timber

class DetailComponentView : ConstraintLayout {

    private lateinit var binding: ViewBookDetailsComponentBinding
    private lateinit var itemTodaySellNewBinding: ItemTodaySellNewBinding
    private lateinit var adapter: ItemsAdapter

    var items: List<TransactionStickyModel> = emptyList()
        set(value) {
            field = value
            onItemsUpdated()
        }

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(context)
    }

    private fun init(context: Context) {
        binding = ViewBookDetailsComponentBinding.inflate(LayoutInflater.from(context), this, true)
        adapter = ItemsAdapter(context)
        binding.bookDetailsList.adapter = adapter
    }

    private fun onItemsUpdated() {
        adapter.notifyDataSetChanged()
        binding.bookDetailsList.requestLayout()
    }

    inner class ItemsAdapter(private val context: Context) : BaseAdapter() {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View? {

            val singleItem: TransactionStickyModel = items[position]
            var view: View? = convertView

            if (view == null) {
                view = LayoutInflater.from(context)
                    .inflate(R.layout.item_today_sell_new, parent, false)
                itemTodaySellNewBinding = ItemTodaySellNewBinding.bind(view)
                view.tag = itemTodaySellNewBinding
            } else {
                itemTodaySellNewBinding = view.tag as ItemTodaySellNewBinding
            }

            itemTodaySellNewBinding.apply {
                tvNickname.text = singleItem.nickName
                tvCustomerCode.text = singleItem.id.toString()
                tvTime.text = singleItem.createdAt
                tvAmount.setValue(singleItem.amount.toDoubleOrNull().toLong().getZeroIfNull())
                tvAssignBy.text = "By: " + singleItem.assignedBy
                val a = singleItem.amount.toDoubleOrNull().getZeroIfNull()
                Timber.d("Amount inside detail view -> ${singleItem.amount}")
                if (a < 0.0) {
                    tvAmount.setTextColor(ContextCompat.getColor(context, R.color.negative_numbers))
                    tvAmount.setAllowNegativeValues(true)
                } else {
                    tvAmount.setTextColor(ContextCompat.getColor(context, R.color.black))
                }

                if (position == items.size - 1) {
                    divider.visibility = View.GONE
                } else {
                    divider.visibility = View.VISIBLE
                }

            }

            return itemTodaySellNewBinding.root
        }

        override fun getItem(position: Int): Any {
            return items[position]
        }

        override fun getItemId(position: Int): Long {
            return 0
        }

        override fun getCount(): Int {
            return items.size
        }

        override fun isEnabled(position: Int): Boolean {
            return false
        }
    }
}