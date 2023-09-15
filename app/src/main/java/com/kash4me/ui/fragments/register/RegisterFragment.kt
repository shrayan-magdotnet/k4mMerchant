package com.kash4me.ui.fragments.register

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.textfield.TextInputLayout
import com.kash4me.merchant.R
import com.kash4me.merchant.databinding.FragmentRegisterBinding
import com.kash4me.network.ApiServices
import com.kash4me.network.NetworkConnectionInterceptor
import com.kash4me.network.NotFoundInterceptor
import com.kash4me.repository.RegisterRepository
import com.kash4me.ui.activity.RegistrationActivity
import com.kash4me.ui.activity.login.LoginActivity
import com.kash4me.ui.dialog.ErrorDialog
import com.kash4me.utils.AppConstants
import com.kash4me.utils.PasswordUtils
import com.kash4me.utils.SessionManager
import com.kash4me.utils.extensions.clearError
import com.kash4me.utils.extensions.openCustomTab
import com.kash4me.utils.extensions.showToast
import com.kash4me.utils.toast
import timber.log.Timber


class RegisterFragment : Fragment() {

    companion object {
        fun newInstance() = RegisterFragment()
    }

    private lateinit var viewModel: RegisterViewModel
    var email: String = ""


    private var _binding: FragmentRegisterBinding? = null
    private val mBinding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val apiInterface =
            ApiServices.invoke(
                NetworkConnectionInterceptor(requireContext().applicationContext),
                NotFoundInterceptor()
            )


        val registerRepository = RegisterRepository(apiInterface)

        viewModel = ViewModelProvider(
            this,
            RegisterViewModelFactory(registerRepository)
        )[RegisterViewModel::class.java]

        val sessionManager = SessionManager(requireActivity().applicationContext)
        sessionManager.clearMerchantBasicInfo()
        sessionManager.clearCashbackSettings()

        if ((activity as RegistrationActivity).goToVerify) {
            email = (activity as RegistrationActivity).email
            goToVerifyScreen(email, view)
        }

        val businessUserCB = view.findViewById<CheckBox>(R.id.businessUserCB)
        val emailET = view.findViewById<TextInputLayout>(R.id.emailET)
        val passwordET = view.findViewById<TextInputLayout>(R.id.passwordET)
        val cPasswordET = view.findViewById<TextInputLayout>(R.id.cPasswordET)
        val loginBtn = view.findViewById<Button>(R.id.loginBtn)

        loginBtn.setOnClickListener {
            val intent = Intent(activity, LoginActivity::class.java)
            activity?.startActivity(intent)
        }

        val joinBtn = view.findViewById<Button>(R.id.joinBtn)
        joinBtn.setOnClickListener {

            email = emailET.editText?.text.toString().trim()
            val password = passwordET.editText?.text.toString()
            val confirmPassword = cPasswordET.editText?.text.toString()

            var isValid = true

            if (email.isEmpty()) {
                emailET.error = "Please enter a valid email"
                isValid = false
            } else {
                emailET.clearError()
            }

            if (password.isEmpty()) {
                passwordET.error = "Please enter password"
                isValid = false
            } else {
                passwordET.clearError()
            }

            if (confirmPassword.isEmpty()) {
                cPasswordET.error = "Please enter confirm password"
                isValid = false
            } else {
                cPasswordET.clearError()
            }

            if (password != confirmPassword) {
                cPasswordET.error = "Password and confirm password doesn't match"
                isValid = false
            } else {
                cPasswordET.clearError()
            }

            if (!isValid) {
                return@setOnClickListener
            }

            val isPasswordInvalid = !PasswordUtils().isPasswordValid(input = confirmPassword)
            if (isPasswordInvalid) {
                val errorDialog =
                    ErrorDialog.getInstance(message = "Password does not meet the required criteria")
                errorDialog.show(activity?.supportFragmentManager!!, errorDialog.tag)
                return@setOnClickListener
            }

            ((activity) as RegistrationActivity).customDialogClass.show()

            val params = HashMap<String, Any>()

            params["email"] = email.lowercase()
            params["password"] = password
            params["confirm_password"] = confirmPassword
            params["is_merchant"] = businessUserCB.isChecked

            Timber.d("Request -> $params")
            viewModel.registerUser(params)

        }

        tvTocClickListener()

        viewModel.registerResponse.observe(viewLifecycleOwner) {
            showToast(it.detail)
            ((activity) as RegistrationActivity).customDialogClass.hide()
            (activity as RegistrationActivity).isBusinessSelected = businessUserCB.isChecked
            goToVerifyScreen(email, view)

        }

        viewModel.errorMessage.observe(viewLifecycleOwner) {
            requireContext().toast(it.toString())
            ((activity) as RegistrationActivity).customDialogClass.hide()
        }

    }

    private fun tvTocClickListener() {
        val spannableString = SpannableString(getString(R.string.toc))
        val spanForTermsAndConditions: ClickableSpan = getSpanForTermsAndConditions()
        spannableString.setSpan(spanForTermsAndConditions, 29, 49, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        val spanForPrivacyPolicy: ClickableSpan = getSpanForPrivacyPolicy()
        spannableString.setSpan(spanForPrivacyPolicy, 54, 68, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        mBinding.apply {
            tocTV.text = spannableString
            tocTV.movementMethod = LinkMovementMethod.getInstance()
            tocTV.highlightColor = Color.TRANSPARENT
        }
    }

    private fun getSpanForTermsAndConditions(): ClickableSpan {
        val spanForTermsAndConditions: ClickableSpan = object : ClickableSpan() {
            override fun onClick(textView: View) {
                context?.openCustomTab(uri = Uri.parse(AppConstants.TERMS_AND_CONDITIONS_URL))
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = true
            }
        }
        return spanForTermsAndConditions
    }

    private fun getSpanForPrivacyPolicy(): ClickableSpan {
        val spanForPrivacyPolicy: ClickableSpan = object : ClickableSpan() {
            override fun onClick(textView: View) {
                context?.openCustomTab(uri = Uri.parse(AppConstants.PRIVACY_POLICY_URL))
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = true
            }
        }
        return spanForPrivacyPolicy
    }


    private fun goToVerifyScreen(email: String, view: View) {
        val bundle = bundleOf("email" to email)
        findNavController().navigate(R.id.navigateToVerifyFragment, bundle)
    }
}