package com.kash4me.ui.fragments.merchant.sub_user_settings


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kash4me.data.models.merchant.sub_user_settings.SubUserResponse
import com.kash4me.merchant.databinding.ItemSubUserBinding

class SubUserAdapter(private val listener: SubUserClickListener)

    : RecyclerView.Adapter<SubUserAdapter.SubUserViewHolder>() {

    private val mSubUsers = arrayListOf<SubUserResponse?>()

    fun setData(subUsers: List<SubUserResponse?>) {
        mSubUsers.clear()
        mSubUsers.addAll(subUsers)
        notifyDataSetChanged()
    }

    fun addUser(subUser: SubUserResponse) {
        mSubUsers.add(subUser)
        notifyItemInserted(mSubUsers.lastIndex)
    }

    fun removeUser(position: Int) {
        mSubUsers.removeAt(position)
        notifyItemRemoved(position)
    }

    override fun getItemCount() = mSubUsers.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubUserViewHolder {
        val itemBinding = ItemSubUserBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return SubUserViewHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: SubUserViewHolder, position: Int) {

        val subUser = mSubUsers[position]
        holder.bind(subUser)

    }

    inner class SubUserViewHolder(private val binding: ItemSubUserBinding)

        : RecyclerView.ViewHolder(binding.root) {

        fun bind(subUser: SubUserResponse?) {

            binding.apply {
                tvName.text = subUser?.nickName
                tvCode.text = subUser?.id.toString()
                tvUserIdBody.text = subUser?.userId
//                tvPasswordBody.text = subUser?.password
            }

            binding.ivDelete.setOnClickListener {
                listener.onDelete(subUser = subUser, position = layoutPosition)
            }

            binding.tvResetPassword.setOnClickListener {
                listener.onResetPassword(subUser = subUser)
            }

        }

    }

    interface SubUserClickListener {

        fun onDelete(subUser: SubUserResponse?, position: Int)
        fun onResetPassword(subUser: SubUserResponse?)

    }

}