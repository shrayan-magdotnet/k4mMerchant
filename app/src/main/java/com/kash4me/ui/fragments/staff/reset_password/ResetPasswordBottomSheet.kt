package com.kash4me.ui.fragments.staff.reset_password

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.kash4me.R
import com.kash4me.data.models.merchant.sub_user_settings.reset_staff_password.ResetStaffPasswordRequest
import com.kash4me.databinding.BottomSheetResetPasswordBinding
import com.kash4me.ui.dialog.ErrorDialog
import com.kash4me.ui.dialog.SuccessDialog
import com.kash4me.ui.fragments.merchant.sub_user_settings.SubUserSettingsViewModel
import com.kash4me.utils.AppConstants
import com.kash4me.utils.custom_views.CustomProgressDialog
import com.kash4me.utils.extensions.getEmptyIfNull
import com.kash4me.utils.listeners.AfterDismissalListener
import com.kash4me.utils.network.Resource
import timber.log.Timber

class ResetPasswordBottomSheet : BottomSheetDialogFragment() {

    companion object {

        private var staffId: Int = AppConstants.MINUS_ONE

        fun newInstance(staffId: Int): ResetPasswordBottomSheet {
            this.staffId = staffId
            return ResetPasswordBottomSheet()
        }

    }

    private val mProgressDialog by lazy { CustomProgressDialog(requireContext()) }

    private var _binding: BottomSheetResetPasswordBinding? = null
    private val mBinding get() = _binding!!

    private val mViewModel by lazy {
        ViewModelProvider(this)[SubUserSettingsViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetResetPasswordBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btnResetListener()

    }

    private fun btnResetListener() {
        mBinding.btnReset.setOnClickListener {

            mBinding.tilPassword.error = ""

            val password = mBinding.tilPassword.editText?.text.toString()
            if (password.length < 6) {
                mBinding.tilPassword.error =
                    getString(R.string.password_should_have_atleast_six_characters)
                return@setOnClickListener
            }

            resetPassword(password)

        }
    }

    private fun resetPassword(password: String) {
        val request = ResetStaffPasswordRequest(
            newPassword = password, staffId = staffId
        )
        mViewModel.resetStaffPassword(request = request)?.observe(viewLifecycleOwner) {

            when (it) {

                is Resource.Failure -> {
                    mProgressDialog.hide()
                    Timber.d("Failure: ${it.errorMsg}")
                    val errorDialog = ErrorDialog.getInstance(message = it.errorMsg)
                    errorDialog.show(childFragmentManager, errorDialog.tag)
                }

                Resource.Loading -> {
                    mProgressDialog.show()
                }

                is Resource.Success -> {
                    mProgressDialog.hide()
                    Timber.d("Success -> ${it.value?.message}")
                    val successDialog = SuccessDialog.getInstance(
                        message = it.value?.message.getEmptyIfNull(),
                        afterDismissClicked = object : AfterDismissalListener {
                            override fun afterDismissed() {
                                dialog?.dismiss()
                            }
                        }
                    )
                    successDialog.show(childFragmentManager, successDialog.tag)
                }

            }

        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}