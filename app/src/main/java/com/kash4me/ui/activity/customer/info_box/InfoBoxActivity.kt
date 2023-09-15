package com.kash4me.ui.activity.customer.info_box

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.kash4me.databinding.ActivityInfoBoxBinding
import com.kash4me.ui.dialog.ErrorDialog
import com.kash4me.utils.custom_views.CustomProgressDialog
import com.kash4me.utils.network.Resource

class InfoBoxActivity : AppCompatActivity() {

    private val notificationsAdapter by lazy { InfoBoxAdapter() }

    private val progressDialog by lazy { CustomProgressDialog(this) }

    private val viewModel by lazy { ViewModelProvider(this)[InfoBoxViewModel::class.java] }

    private var binding: ActivityInfoBoxBinding? = null
    private val mBinding get() = binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInfoBoxBinding.inflate(layoutInflater)
        val view = binding!!.root
        setContentView(view)

        setupToolbar()
        initRvNotifications()
        fetchInfoBox()
        mBinding.emptyState.btnTryAgain.setOnClickListener { fetchInfoBox() }

    }

    private fun setupToolbar() {
        setSupportActionBar(mBinding.toolbarLayout.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "InfoBox"
        mBinding.toolbarLayout.toolbar.setNavigationOnClickListener { onBackPressed() }
    }

    private fun initRvNotifications() {
        mBinding.rvNotifications.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = notificationsAdapter
        }
    }

    private fun fetchInfoBox() {
        viewModel.fetchInfoBox().observe(this) { response ->

            when (response) {

                is Resource.Failure -> {

                    progressDialog.hide()
                    makeEmptyStateVisible(message = response.errorMsg)
                    val errorDialog = ErrorDialog.getInstance(message = response.errorMsg)
                    errorDialog.show(supportFragmentManager, errorDialog.tag)

                }

                Resource.Loading -> {
                    progressDialog.show()
                    makeAllViewsHidden()
                }

                is Resource.Success -> {

                    progressDialog.hide()

                    if (response.value.results.isNullOrEmpty()) {
                        showMinimalEmptyState(message = "No data found")
                        val errorDialog = ErrorDialog.getInstance(message = "No data found")
                        errorDialog.show(supportFragmentManager, errorDialog.tag)
                    } else {
                        makeRvNotificationsVisible()
                        notificationsAdapter.setData(notifications = response.value.results)
                    }

                }
            }

        }
    }

    private fun makeEmptyStateVisible(message: String) {
        mBinding.apply {
            emptyState.tvTitle.text = message
            emptyState.root.isVisible = true
            emptyState.btnTryAgain.isVisible = true
            emptyState.tvDescription.isVisible = true

            rvNotifications.isVisible = false
        }
    }

    private fun makeAllViewsHidden() {
        mBinding.apply {
            emptyState.root.isVisible = false
            rvNotifications.isVisible = false
        }
    }

    private fun showMinimalEmptyState(message: String) {
        mBinding.apply {
            emptyState.tvTitle.text = message
            emptyState.root.isVisible = true
            emptyState.btnTryAgain.isVisible = false
            emptyState.tvDescription.isVisible = false

            rvNotifications.isVisible = false
        }
    }

    private fun makeRvNotificationsVisible() {
        mBinding.apply {
            rvNotifications.isVisible = true

            emptyState.root.isVisible = false
        }
    }

    private fun getDummyNotifications(): List<InfoBoxModel> {
        val notification1 =
            InfoBoxModel(title = "Title 1", date = "2022/05/06", description = "Description 1")
        val notification2 =
            InfoBoxModel(title = "Title 2", date = "2022/05/06", description = "Description 2")
        val notification3 =
            InfoBoxModel(title = "Title 3", date = "2022/05/06", description = "Description 3")
        return listOf(notification1, notification2, notification3)
    }

    data class InfoBoxModel(val title: String, val date: String, val description: String)

}