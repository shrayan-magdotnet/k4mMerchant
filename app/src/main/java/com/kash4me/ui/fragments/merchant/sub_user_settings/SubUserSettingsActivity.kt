package com.kash4me.ui.fragments.merchant.sub_user_settings

import android.os.Bundle
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.kash4me.R
import com.kash4me.data.models.merchant.sub_user_settings.SubUserResponse
import com.kash4me.databinding.ActivitySubUserSettingsBinding
import com.kash4me.ui.dialog.ErrorDialog
import com.kash4me.ui.dialog.SuccessDialog
import com.kash4me.ui.fragments.staff.reset_password.ResetPasswordBottomSheet
import com.kash4me.utils.SessionManager
import com.kash4me.utils.custom_views.CustomProgressDialog
import com.kash4me.utils.extensions.getEmptyIfNull
import com.kash4me.utils.extensions.getMinusOneIfNull
import com.kash4me.utils.network.Resource
import timber.log.Timber

class SubUserSettingsActivity : AppCompatActivity() {

    private var binding: ActivitySubUserSettingsBinding? = null
    private val mBinding get() = binding!!

    private val mAdapter by lazy {
        SubUserAdapter(
            listener = object : SubUserAdapter.SubUserClickListener {
                override fun onDelete(subUser: SubUserResponse?, position: Int) {
                    AlertDialog.Builder(this@SubUserSettingsActivity)
                        .setTitle(R.string.delete_user)
                        .setMessage("Are you sure you want to delete ${subUser?.nickName} (${subUser?.id})")
                        .setPositiveButton(R.string.yes) { p0, p1 ->
                            if (subUser?.id == null) {
                                val errorDialog = ErrorDialog.getInstance(
                                    message = getString(R.string.couldnt_delete_selected_user)
                                )
                                errorDialog.show(supportFragmentManager, errorDialog.tag)
                                return@setPositiveButton
                            }
                            deleteSubUser(shopId = subUser.id, position = position)
                            p0.dismiss()
                        }
                        .setNegativeButton(R.string.no) { p0, p1 -> p0.dismiss() }
                        .show()
                }

                override fun onResetPassword(subUser: SubUserResponse?) {
                    val bottomSheet = ResetPasswordBottomSheet.newInstance(
                        staffId = subUser?.id.getMinusOneIfNull()
                    )
                    bottomSheet.show(supportFragmentManager, bottomSheet.tag)
                }
            }
        )
    }

    private val mProgressDialog by lazy { CustomProgressDialog(context = this) }

    private val mViewModel by lazy {
        ViewModelProvider(this)[SubUserSettingsViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySubUserSettingsBinding.inflate(layoutInflater)
        val view = binding!!.root
        setContentView(view)

        setupToolbar(title = R.string.sub_user_settings)
        initRvSubUsers()
        fetchSubUsers()

        btnAddUserListener()

    }

    private fun fetchSubUsers() {
        val shopId = SessionManager(this).fetchMerchantDetails()?.id
        if (shopId == null) {
            val errorDialog = ErrorDialog.getInstance(message = "Couldn't fetch sub users")
            errorDialog.show(supportFragmentManager, errorDialog.tag)
            return
        }
        mViewModel.getSubUsers().observe(this) {

            when (it) {

                is Resource.Failure -> {
                    mProgressDialog.hide()
                    Timber.d("Failure: ${it.errorMsg}")
                    val errorDialog = ErrorDialog.getInstance(message = it.errorMsg)
                    errorDialog.show(supportFragmentManager, errorDialog.tag)
                    showEmptyState(errorMessage = it.errorMsg)
                }

                Resource.Loading -> {
                    mProgressDialog.show()
                }

                is Resource.Success -> {
                    mProgressDialog.hide()
                    Timber.d("Success -> ${it.value}")
                    val staffs = it.value
                    if (staffs.isEmpty()) {
                        showEmptyState(errorMessage = "No staffs found")
                    } else {
                        mAdapter.setData(subUsers = staffs)
                        showRvSubUsers()
                        clearInputFields()
                    }
                }

            }

        }
    }

    private fun btnAddUserListener() {
        mBinding.btnAddUser.setOnClickListener {

            var isInvalid = false

            mBinding.tilNickName.error = null
            if (mBinding.tilNickName.editText?.text.isNullOrBlank()) {
                isInvalid = true
                mBinding.tilNickName.error = getString(R.string.please_enter_a_nick_name)
            }

            mBinding.tilUserId.error = null
            if (mBinding.tilUserId.editText?.text.isNullOrBlank()) {
                isInvalid = true
                mBinding.tilUserId.error = getString(R.string.please_enter_a_user_id)
            }

            mBinding.tilPassword.error = null
            if (mBinding.tilPassword.editText?.text.isNullOrBlank()) {
                isInvalid = true
                mBinding.tilPassword.error = getString(R.string.please_enter_password)
            }

            if (isInvalid) {
                return@setOnClickListener
            }

            addSubUser()

        }
    }

    private fun addSubUser() {
        val nickname = mBinding.tilNickName.editText?.text.toString()
        val userId = mBinding.tilUserId.editText?.text.toString()
        val password = mBinding.tilPassword.editText?.text.toString()
        val shopId = SessionManager(this).fetchMerchantDetails()?.id.getMinusOneIfNull()
        mViewModel.addSubUser(
            nickname = nickname, password = password, shopId = shopId, userId = userId
        ).observe(this) {

            when (it) {

                is Resource.Failure -> {
                    mProgressDialog.hide()
                    Timber.d("Failure: ${it.errorMsg}")
                    val errorDialog = ErrorDialog.getInstance(message = it.errorMsg)
                    errorDialog.show(supportFragmentManager, errorDialog.tag)
                    //                    showEmptyState(errorMessage = it.errorMsg)
                }

                Resource.Loading -> {
                    mProgressDialog.show()
                }

                is Resource.Success -> {
                    mProgressDialog.hide()
                    Timber.d("Success -> ${it.value}")
                    if (it.value == null) {
                        showEmptyState(errorMessage = "An error encountered while creating a new sub user")
                    } else {
                        showRvSubUsers()
                        mAdapter.setData(subUsers = it.value)
                        clearInputFields()
                    }
                }

            }

        }
    }

    private fun deleteSubUser(shopId: Int, position: Int) {
        mViewModel.deleteSubUser(shopId = shopId).observe(this) {

            when (it) {

                is Resource.Failure -> {
                    mProgressDialog.hide()
                    Timber.d("Failure: ${it.errorMsg}")
                    val errorDialog = ErrorDialog.getInstance(message = it.errorMsg)
                    errorDialog.show(supportFragmentManager, errorDialog.tag)
                }

                Resource.Loading -> {
                    mProgressDialog.show()
                }

                is Resource.Success -> {
                    mProgressDialog.hide()
                    Timber.d("Success -> ${it.value?.message}")
                    val successDialog = SuccessDialog.getInstance(
                        message = it.value?.message.getEmptyIfNull()
                    )
                    successDialog.show(supportFragmentManager, successDialog.tag)
                    mAdapter.removeUser(position = position)
                }

            }

        }
    }

    private fun showRvSubUsers() {
        mBinding.apply {
            emptyState.root.isVisible = false
            rvSubUsers.isVisible = true
        }
    }

    private fun showEmptyState(errorMessage: String) {
        mBinding.apply {
            rvSubUsers.isVisible = false

            emptyState.root.isVisible = true
            emptyState.tvTitle.text = errorMessage
            emptyState.tvSubtitle.text = ""
        }
    }

    private fun clearInputFields() {
        mBinding.apply {
            tilNickName.editText?.setText("")
            tilUserId.editText?.setText("")
            tilPassword.editText?.setText("")
        }
    }

    private fun initRvSubUsers() {
        mBinding.rvSubUsers.apply {
            layoutManager = LinearLayoutManager(context)
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
            adapter = mAdapter
        }
    }

    fun setupToolbar(@StringRes title: Int) {
        setSupportActionBar(mBinding.layoutToolbar.toolbar)
        supportActionBar?.setTitle(title)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        mBinding.layoutToolbar.toolbar.setNavigationOnClickListener { onBackPressed() }
    }

}