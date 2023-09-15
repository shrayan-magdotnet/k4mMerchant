package com.kash4me.ui.activity.customer.announcements


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kash4me.data.models.customer.annoucement.AnnouncementsResponse
import com.kash4me.merchant.databinding.ItemAnnouncementBinding
import com.kash4me.utils.listeners.SingleParamItemClickListener

class AnnouncementsAdapter(val clickListener: SingleParamItemClickListener<AnnouncementsResponse.Result?>)

    : RecyclerView.Adapter<AnnouncementsAdapter.AnnouncementViewHolder>() {

    private val mAnnouncements = arrayListOf<AnnouncementsResponse.Result?>()

    fun setData(announcements: List<AnnouncementsResponse.Result?>) {
        mAnnouncements.clear()
        mAnnouncements.addAll(announcements)
        notifyDataSetChanged()
    }

    override fun getItemCount() = mAnnouncements.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnnouncementViewHolder {
        val itemBinding = ItemAnnouncementBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return AnnouncementViewHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: AnnouncementViewHolder, position: Int) {

        val announcement = mAnnouncements[position]
        holder.bind(announcement)

    }

    inner class AnnouncementViewHolder(private val binding: ItemAnnouncementBinding)

        : RecyclerView.ViewHolder(binding.root) {

        fun bind(announcement: AnnouncementsResponse.Result?) {

            binding.apply {
                tvMerchantName.text = announcement?.merchantName
                tvExpiryDate.text = "Expires On: ${announcement?.expireOn}"
                tvDescription.text = announcement?.message
            }

            binding.root.setOnClickListener { clickListener.onClick(item = announcement) }

        }

    }

}