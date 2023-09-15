package com.kash4me.ui.activity.common

import android.os.Bundle
import android.text.Editable
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.kash4me.merchant.R
import com.kash4me.merchant.databinding.ActivityFeedbackBinding
import com.kash4me.ui.dialog.ErrorDialog
import com.kash4me.ui.dialog.SuccessDialog
import com.kash4me.utils.custom_views.CustomProgressDialog
import com.kash4me.utils.extensions.clear
import com.kash4me.utils.extensions.getEmptyIfNull
import com.kash4me.utils.listeners.AfterDismissalListener
import com.kash4me.utils.network.Resource
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class FeedbackActivity : AppCompatActivity() {

    private var binding: ActivityFeedbackBinding? = null
    private val mBinding get() = binding!!

    private val mProgressDialog by lazy { CustomProgressDialog(this) }

    private val mViewModel: FeedbackViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFeedbackBinding.inflate(layoutInflater)
        val view = binding!!.root
        setContentView(view)

        setupToolbar()
        btnSendListener()

    }

    fun setupToolbar() {
        setSupportActionBar(mBinding.toolbarLayout.toolbar)
        supportActionBar?.setTitle(R.string.feedback)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        mBinding.toolbarLayout.toolbar.setNavigationOnClickListener { onBackPressed() }
    }

    private fun btnSendListener() {
        mBinding.btnSend.setOnClickListener {

            val content = mBinding.tilFeedback.editText?.text
            if (content.isNullOrBlank()) {

                mBinding.tilFeedback.error = "Please enter some feedback"
                return@setOnClickListener

            }

            sendFeedback(content)

        }
    }

    private fun sendFeedback(content: Editable) {
        mViewModel.sendFeedback(content = content.toString()).observe(this) {

            when (it) {
                is Resource.Failure -> {
                    mProgressDialog.hide()
                    val errorDialog = ErrorDialog.getInstance(message = it.errorMsg)
                    errorDialog.show(supportFragmentManager, errorDialog.tag)
                }

                Resource.Loading -> {
                    mProgressDialog.show()
                }

                is Resource.Success -> {
                    mProgressDialog.hide()
                    Timber.d("Success -> ${it.value}")
                    val successDialog = SuccessDialog.getInstance(
                        message = it.value.detail.getEmptyIfNull(),
                        afterDismissClicked = object : AfterDismissalListener {
                            override fun afterDismissed() {
                                mBinding.tilFeedback.clear()
                            }
                        }
                    )
                    successDialog.show(supportFragmentManager, successDialog.tag)
                }
            }

        }
    }


}