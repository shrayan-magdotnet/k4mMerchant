package com.kash4me.ui.activity.merchant.transaction_details.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.kash4me.merchant.databinding.ViewListItemBinding
import com.kash4me.ui.activity.merchant.transaction_details.model.TransactionStickyModel
import com.kash4me.utils.convertDpToPixel

class StickyAdapter(val activity: AppCompatActivity) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var headerList: List<String> = listOf()

    var itemsList: Map<String, List<TransactionStickyModel>> = emptyMap()
        set(value) {
            field = value
            headerList = itemsList.keys.toList()
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater: LayoutInflater = LayoutInflater.from(parent.context)
        val viewBinding: ViewListItemBinding =
            ViewListItemBinding.inflate(layoutInflater, parent, false)
        return BookViewHolder(viewBinding, activity)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        if (position != 0) {
            val params = holder.itemView.layoutParams as RecyclerView.LayoutParams
            params.topMargin = 16f.convertDpToPixel(holder.itemView.context).toInt()
            holder.itemView.layoutParams = params
        }

        if (position >= 0 && position < headerList.size) {
            (holder as BookViewHolder).bind(headerList[position])
        }
    }

    override fun getItemCount() = headerList.size

    fun getHeaderForCurrentPosition(position: Int) = if (position in headerList.indices) {
        headerList[position]
    } else {
        ""
    }

    inner class BookViewHolder(
        private val viewBinding: ViewListItemBinding,
        val activity: AppCompatActivity
    ) :
        RecyclerView.ViewHolder(viewBinding.root) {

        fun bind(header: String) {
            viewBinding.tvHeader.text = "$header"
            itemsList[header]?.let { items1 ->
                viewBinding.detailsView.items = items1
            }
        }
    }
}