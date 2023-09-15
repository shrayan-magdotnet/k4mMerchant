package com.kash4me.ui.activity.merchant.branch_list

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kash4me.data.models.Branch
import com.kash4me.merchant.R
import com.kash4me.merchant.databinding.ActivityBranchListBinding
import com.kash4me.network.ApiServices
import com.kash4me.network.NetworkConnectionInterceptor
import com.kash4me.network.NotFoundInterceptor
import com.kash4me.repository.MerchantBranchListRepository
import com.kash4me.ui.dialog.ErrorDialog
import com.kash4me.utils.SessionManager
import com.kash4me.utils.custom_views.CustomProgressDialog

class BranchListActivity : AppCompatActivity() {

    private lateinit var backIV: ImageView
    private lateinit var branchRV: RecyclerView

    private lateinit var viewModel: BranchListViewModel

    private lateinit var sessionManager: SessionManager

    private val customProgressDialog: CustomProgressDialog by lazy { CustomProgressDialog(this) }

    private var binding: ActivityBranchListBinding? = null
    private val mBinding get() = binding!!
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBranchListBinding.inflate(layoutInflater)
        val view = binding!!.root
        setContentView(view)

        sessionManager = SessionManager(applicationContext)

        initViewModel()
        initUI()
        setupToolbar()
    }

    private fun setupToolbar() {
        setSupportActionBar(mBinding.toolbarLayout.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Branches"
        mBinding.toolbarLayout.toolbar.setNavigationOnClickListener { onBackPressed() }
    }


    private fun initViewModel() {
        val apiInterface =
            ApiServices.invoke(
                NetworkConnectionInterceptor(applicationContext),
                NotFoundInterceptor()
            )


        val merchantBranchListRepository =
            MerchantBranchListRepository(apiInterface)

        viewModel = ViewModelProvider(
            this,
            BranchListViewModelFactory(merchantBranchListRepository)
        )[BranchListViewModel::class.java]

        getMerchantBranchList()

        mBinding.emptyState.btnTryAgain.setOnClickListener { getMerchantBranchList() }

        viewModel.branchListResponse.observe(this) {

            customProgressDialog.hide()

            if (it.results.isNullOrEmpty()) {
                showMinimalEmptyState(message = "No data found")
            } else {
                showRecyclerView(it.results)
            }

        }

        viewModel.errorMessage.observe(this) {
            val errorDialog = ErrorDialog.getInstance(message = it.error)
            errorDialog.show(supportFragmentManager, errorDialog.tag)
            showEmptyState(message = it.error)
            customProgressDialog.hide()

        }
    }

    private fun showEmptyState(message: String) {
        mBinding.apply {
            emptyState.tvTitle.text = message
            emptyState.root.isVisible = true
            emptyState.tvDescription.isVisible = true
            emptyState.btnTryAgain.isVisible = true

            branchRV.isVisible = false
        }
    }

    private fun showMinimalEmptyState(message: String) {
        mBinding.apply {
            emptyState.tvTitle.text = message
            emptyState.root.isVisible = true
            emptyState.tvDescription.isVisible = false
            emptyState.btnTryAgain.isVisible = false

            branchRV.isVisible = false
        }
    }

    private fun showRecyclerView(results: List<Branch>) {
        mBinding.apply {
            initRecyclerView(results)
            branchRV.isVisible = true

            emptyState.root.isVisible = false
        }
    }

    private fun initUI() {

        branchRV = findViewById(R.id.branchRV)

//        backIV = findViewById(R.id.backIV)
//        backIV.visibility = View.VISIBLE

        initOnClick()
    }

    private fun getMerchantBranchList() {
        customProgressDialog.show()
        val token = sessionManager.fetchAuthToken().toString()
        viewModel.getMerchantDetailsWithCustomerInfo(token, HashMap())
    }

    private fun initOnClick() {
//        backIV.setOnClickListener {
//            onBackPressed()
//        }
    }


    private fun initRecyclerView(branchList: List<Branch>) {

        // this creates a vertical layout Manager
        branchRV.layoutManager = LinearLayoutManager(this)

        // This will pass the ArrayList to our Adapter
        val adapter = BranchListAdapter(branchList)

        // Setting the Adapter with the recyclerview
        branchRV.adapter = adapter
    }
}