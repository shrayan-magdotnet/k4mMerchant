package com.kash4me.ui.fragments.merchant.settings

import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.kash4me.R
import com.kash4me.data.models.user.UserType
import com.kash4me.databinding.ActivitySettingsBinding
import com.kash4me.ui.dialog.ErrorDialog
import com.kash4me.ui.dialog.SuccessDialog
import com.kash4me.utils.SessionManager
import com.kash4me.utils.custom_views.CustomProgressDialog
import com.kash4me.utils.extensions.getFalseIfNull
import com.kash4me.utils.listeners.AfterDismissalListener
import com.kash4me.utils.network.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class SettingsActivity : AppCompatActivity() {

    private var binding: ActivitySettingsBinding? = null
    private val mBinding get() = binding!!

    private val mProgressDialog by lazy { CustomProgressDialog(this) }

    @Inject
    lateinit var mSessionManager: SessionManager

    private val mViewModel by lazy { ViewModelProvider(this)[SettingsViewModel::class.java] }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        val view = binding!!.root
        setContentView(view)
        setupToolbar(title = getString(R.string.settings))

        btnDeleteMyAccountListener()
        fetchNotificationsSettings()

    }

    private fun btnDeleteMyAccountListener() {
        mBinding.btnDeleteMyAccount.setOnClickListener { showDeletionConfirmationDialog() }
    }

    private fun fetchNotificationsSettings() {
        mViewModel.getNotificationSettings().observe(this) {

            when (it) {
                is Resource.Failure -> {
                    mProgressDialog.hide()
                    val errorDialog = ErrorDialog.getInstance(message = it.errorMsg)
                    errorDialog.show(supportFragmentManager, errorDialog.tag)
                    mViewModel.mSavedEmailSettings = null
                }

                Resource.Loading -> {
                    mProgressDialog.show()
                }

                is Resource.Success -> {
                    mProgressDialog.hide()
                    Timber.d("Success -> ${it.value}")
                    mBinding.switchEmailSubscription.isChecked =
                        it.value.emailSettings.getFalseIfNull()
                    mViewModel.mSavedEmailSettings = it.value.emailSettings
                }
            }

        }
    }

    fun setupToolbar(title: String) {
        setSupportActionBar(mBinding.toolbar.root)
        supportActionBar?.title = title
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        mBinding.toolbar.root.setNavigationOnClickListener { finish() }
    }

    override fun finish() {

        if (mViewModel.mSavedEmailSettings == null) {
            super.finish()
            return
        }

        val isSwitchUnchanged =
            mViewModel.mSavedEmailSettings == mBinding.switchEmailSubscription.isChecked
        if (isSwitchUnchanged) {
            super.finish()
            return
        } else {
            showConfirmationDialog()
        }

    }

    private fun showConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.save_settings_question_mark))
            .setPositiveButton(R.string.yes) { dialog, which ->
                dialog.dismiss()
                updateEmailNotificationSettings()
            }
            .setNegativeButton(R.string.no) { dialog, which ->
                dialog.dismiss()
                super.finish()
            }
            .show()
    }

    private fun updateEmailNotificationSettings() {
        mViewModel.updateNotificationSettings(isChecked = mBinding.switchEmailSubscription.isChecked)
            .observe(this) {

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
                        super.finish()
                    }
                }

            }
    }

    private fun showDeletionConfirmationDialog() {

        mBinding.btnDeleteMyAccount.isEnabled = false

        AlertDialog.Builder(this)
            .setTitle(R.string.delete_user)
            .setMessage(R.string.are_you_sure_you_want_to_delete_your_account)
            .setPositiveButton(R.string.ok) { dialog, _ ->

                when (mSessionManager.fetchUserType()) {
                    UserType.CUSTOMER -> {
                        deleteCustomerAccount()
                    }

                    UserType.MERCHANT -> {
                        deleteMerchantAccount()
                    }

                    UserType.STAFF -> {
                        // Do nothing since, staff doesn't have this screen
                    }

                    UserType.ANONYMOUS -> {
                        // Do nothing since, anonymous doesn't have this screen
                    }

                    null -> {
                        // Do nothing since, staff doesn't have this screen
                    }
                }

                dialog.dismiss()
            }
            .setNegativeButton(R.string.cancel) { dialog, _ ->
                dialog.dismiss()
            }
            .setOnDismissListener {
                mBinding.btnDeleteMyAccount.isEnabled = true
            }
            .setCancelable(false)
            .show()

    }

    private fun deleteMerchantAccount() {
        mViewModel.deleteMerchantAccount().observe(this) {

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
                    val successMessage = it.value.message ?: "Account has been successfully deleted"
                    val successDialog = SuccessDialog.getInstance(
                        message = successMessage,
                        afterDismissClicked = object : AfterDismissalListener {
                            override fun afterDismissed() {
                                lifecycleScope.launch {
                                    withContext(Dispatchers.IO) {
                                        mSessionManager.logoutUser(packageContext = this@SettingsActivity)
                                    }
                                }
                            }
                        }
                    )
                    successDialog.show(supportFragmentManager, successDialog.tag)
                }
            }

        }
    }

    private fun deleteCustomerAccount() {
        mViewModel.deleteCustomerAccount().observe(this) {

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
                    val successMessage = it.value.message ?: "Account has been successfully deleted"
                    val successDialog = SuccessDialog.getInstance(
                        message = successMessage,
                        afterDismissClicked = object : AfterDismissalListener {
                            override fun afterDismissed() {
                                lifecycleScope.launch {
                                    withContext(Dispatchers.IO) {
                                        mSessionManager.logoutUser(packageContext = this@SettingsActivity)
                                    }
                                }
                            }
                        }
                    )
                    successDialog.show(supportFragmentManager, successDialog.tag)
                }
            }

        }
    }

}