package com.kash4me.ui.activity.change_password

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.kash4me.databinding.ActivityChangePasswordBinding
import com.kash4me.network.ApiServices
import com.kash4me.network.NetworkConnectionInterceptor
import com.kash4me.network.NotFoundInterceptor
import com.kash4me.repository.ChangePasswordRepository
import com.kash4me.ui.dialog.ErrorDialog
import com.kash4me.ui.dialog.SuccessDialog
import com.kash4me.utils.PasswordUtils
import com.kash4me.utils.SessionManager
import com.kash4me.utils.custom_views.CustomProgressDialog
import com.kash4me.utils.listeners.AfterDismissalListener
import com.kash4me.utils.network.Resource
import com.kash4me.utils.toast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class ChangePasswordActivity : AppCompatActivity() {

    private var binding: ActivityChangePasswordBinding? = null
    private val mBinding get() = binding!!

    private val viewModel: ChangePasswordViewModel by lazy {
        val apiInterface =
            ApiServices.invoke(
                NetworkConnectionInterceptor(context = applicationContext),
                NotFoundInterceptor()
            )

        val changePasswordRepository = ChangePasswordRepository(apiInterface)

        ViewModelProvider(
            this,
            ChangePasswordViewModelFactory(changePasswordRepository)
        )[ChangePasswordViewModel::class.java]
    }

    @Inject
    lateinit var sessionManager: SessionManager

    private val mProgressDialog: CustomProgressDialog by lazy { CustomProgressDialog(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChangePasswordBinding.inflate(layoutInflater)
        val view = binding!!.root
        setContentView(view)
        setupToolbar()

        btnChangePasswordListener()

    }

    private fun btnChangePasswordListener() {
        mBinding.changePasswordBtn.setOnClickListener {

            val oldPassword = mBinding.oldPasswordET.editText?.text.toString().trim()
            val newPassword = mBinding.newPasswordET.editText?.text.toString().trim()
            val confirmNewPassword = mBinding.cNewPasswordET.editText?.text.toString().trim()

            if (oldPassword.isBlank()) {
                toast("Old password field is blank")
                return@setOnClickListener
            }
            if (newPassword.isBlank()) {
                toast("New password field is blank")
                return@setOnClickListener
            }
            if (confirmNewPassword.isBlank()) {
                toast("Confirm new password field is blank")
                return@setOnClickListener
            }
            if (newPassword != confirmNewPassword) {
                toast("New passwords do not match")
                return@setOnClickListener
            }

            Timber.d("Old password -> $oldPassword")
            Timber.d("New password -> $newPassword")

            val isPasswordInvalid = !PasswordUtils().isPasswordValid(input = newPassword)
            if (isPasswordInvalid) {
                val errorDialog =
                    ErrorDialog.getInstance(message = "Password does not meet the required criteria")
                errorDialog.show(supportFragmentManager, errorDialog.tag)
                return@setOnClickListener
            }

            val token = sessionManager.fetchAuthToken().toString()
            changePassword(token, oldPassword, newPassword, confirmNewPassword)

        }
    }

    private fun changePassword(
        token: String,
        oldPassword: String,
        newPassword: String,
        cNewPassword: String
    ) {
        viewModel.changePassword(
            token = token,
            oldPassword = oldPassword,
            newPassword = newPassword,
            confirmNewPassword = cNewPassword,
        ).observe(this) {

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
                    val successDialog = SuccessDialog.getInstance(
                        message = it.value.detail,
                        afterDismissClicked = object : AfterDismissalListener {
                            override fun afterDismissed() {
                                lifecycleScope.launch {
                                    withContext(Dispatchers.IO) {
                                        sessionManager.logoutUser(packageContext = this@ChangePasswordActivity)
                                    }
                                }
                            }
                        })
                    successDialog.show(supportFragmentManager, successDialog.tag)
                }
            }

        }
    }


    private fun setupToolbar() {
        setSupportActionBar(mBinding.toolbarLayout.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Change Password"
        mBinding.toolbarLayout.toolbar.setNavigationOnClickListener { onBackPressed() }
    }


}