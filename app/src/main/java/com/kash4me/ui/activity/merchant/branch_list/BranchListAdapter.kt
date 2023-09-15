package com.kash4me.ui.activity.merchant.branch_list

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kash4me.R
import com.kash4me.data.models.Branch
import com.kash4me.ui.activity.merchant.branch_details.BranchDetailsActivity

class BranchListAdapter(private val branchList: List<Branch>) :
    RecyclerView.Adapter<BranchListAdapter.ViewHolder>() {

    // create new views
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // inflates the card_view_design view
        // that is used to hold list item
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_branch, parent, false)

        return ViewHolder(view, branchList)
    }

    // binds the list items to a view
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val branch = branchList[position]
        holder.branchTV.text = branch.name
        holder.tvAddress.text = branch.address

    }

    // return the number of the items in the list
    override fun getItemCount(): Int {
        return branchList.size
    }


    class ViewHolder(view: View, branchList: List<Branch>) : RecyclerView.ViewHolder(view) {

        val branchTV: TextView = view.findViewById(R.id.branchTV)
        val tvAddress: TextView = view.findViewById(R.id.tv_address)

        init {

            view.setOnClickListener {

                val branch = branchList[bindingAdapterPosition]
                val intent = Intent(view.context, BranchDetailsActivity::class.java)
                intent.putExtra("branchId", branch.id)
                view.context.startActivity(intent)

            }
        }

    }
}
