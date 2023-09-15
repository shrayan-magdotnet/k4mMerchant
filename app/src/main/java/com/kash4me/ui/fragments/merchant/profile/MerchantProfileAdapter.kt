package com.kash4me.ui.fragments.merchant.profile

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kash4me.R
import com.kash4me.utils.listeners.SingleParamItemClickListener


class MerchantProfileAdapter(
    private val profileMenuList: List<MerchantProfileFragment.MerchantProfileMenu>,
    private val clickListener: SingleParamItemClickListener<MerchantProfileFragment.MerchantProfileMenu>
) :
    RecyclerView.Adapter<MerchantProfileAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_profile_menu, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val profileMenu = profileMenuList[position]
        holder.bind(profileMenu)

    }

    override fun getItemCount(): Int {
        return profileMenuList.size
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        private val tvMenu: TextView = view.findViewById(R.id.menuTV)
        private val ivIcon: ImageView = view.findViewById(R.id.iv_icon)

        fun bind(profileMenu: MerchantProfileFragment.MerchantProfileMenu) {
            ivIcon.setImageResource(profileMenu.icon)
            tvMenu.setText(profileMenu.title)
            itemView.setOnClickListener { clickListener.onClick(item = profileMenu) }
        }

    }

}
