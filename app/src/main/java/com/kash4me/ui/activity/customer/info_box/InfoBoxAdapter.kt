package com.kash4me.ui.activity.customer.info_box


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.text.HtmlCompat
import androidx.recyclerview.widget.RecyclerView
import com.kash4me.data.models.user.InfoBoxResponse
import com.kash4me.databinding.ItemInfoboxBinding
import com.kash4me.utils.TimeUtils
import com.kash4me.utils.extensions.getEmptyIfNull

class InfoBoxAdapter : RecyclerView.Adapter<InfoBoxAdapter.InfoBoxViewHolder>() {

    private val mNotifications = arrayListOf<InfoBoxResponse.Result?>()

    fun setData(notifications: List<InfoBoxResponse.Result?>) {
        mNotifications.clear()
        mNotifications.addAll(notifications)
        notifyDataSetChanged()
    }

    override fun getItemCount() = mNotifications.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InfoBoxViewHolder {
        val itemBinding = ItemInfoboxBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return InfoBoxViewHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: InfoBoxViewHolder, position: Int) {

        val notification = mNotifications[position]
        holder.bind(notification)

    }

    class InfoBoxViewHolder(private val binding: ItemInfoboxBinding)

        : RecyclerView.ViewHolder(binding.root) {

        fun bind(notification: InfoBoxResponse.Result?) {

            binding.apply {
                tvTitle.text = notification?.title
                if (notification?.createdAt != null) {
                    tvDate.text = TimeUtils().convertToYyyyMmDd(notification.createdAt)
                } else {
                    tvDate.text = "N/A"
                }
                tvDescription.text = HtmlCompat.fromHtml(
                    notification?.message.getEmptyIfNull(), HtmlCompat.FROM_HTML_MODE_LEGACY
                )
            }

        }

    }

}