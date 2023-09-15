package com.kash4me.ui.fragments.customer.profile

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kash4me.R
import com.kash4me.utils.listeners.SingleParamItemClickListener


class CustomerProfileAdapter(
    private val profileMenuList: List<CustomerProfileFragment.CustomerProfileMenu>,
    private val clickListener: SingleParamItemClickListener<CustomerProfileFragment.CustomerProfileMenu>
) :
    RecyclerView.Adapter<CustomerProfileAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_profile_menu, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val profileMenu = profileMenuList[position]
        holder.bind(item = profileMenu)

    }

    override fun getItemCount(): Int {
        return profileMenuList.size
    }

    inner class ViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {

        private val tvMenu: TextView = view.findViewById(R.id.menuTV)
        private val ivIcon: ImageView = view.findViewById(R.id.iv_icon)

        fun bind(item: CustomerProfileFragment.CustomerProfileMenu) {
            ivIcon.setImageResource(item.icon)
            tvMenu.setText(item.title)
            view.rootView.setOnClickListener { clickListener.onClick(item = item) }
        }

    }
}
