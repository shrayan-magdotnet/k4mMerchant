package com.kash4me.ui.activity.merchant.branch_details

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.kash4me.merchant.R
import com.kash4me.merchant.databinding.ActivityBranchDetailsBinding
import com.kash4me.ui.fragments.merchant.branch_details.BranchDetailsFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BranchDetailsActivity : AppCompatActivity() {

    var branchId: Int = 0

    private var binding: ActivityBranchDetailsBinding? = null
    private val mBinding get() = binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBranchDetailsBinding.inflate(layoutInflater)
        val view = binding!!.root
        setContentView(view)

        branchId = intent.getIntExtra("branchId", 0)

        loadFragment()
        setupToolbar()
    }


    private fun setupToolbar() {
        setSupportActionBar(mBinding.toolbarLayout.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Branch Details"
        mBinding.toolbarLayout.toolbar.setNavigationOnClickListener { onBackPressed() }
    }

    private fun loadFragment() {
        val transaction = supportFragmentManager.beginTransaction()
        val fragment = BranchDetailsFragment()
        transaction.replace(R.id.fragment_container_view, fragment, fragment.tag)
        transaction.commit()
    }

}