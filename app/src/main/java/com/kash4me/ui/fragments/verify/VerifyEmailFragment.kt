package com.kash4me.ui.fragments.verify

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.kash4me.data.models.user.UserType
import com.kash4me.merchant.R
import com.kash4me.merchant.databinding.FragmentVerifyBinding
import com.kash4me.network.ApiServices
import com.kash4me.network.NetworkConnectionInterceptor
import com.kash4me.network.NotFoundInterceptor
import com.kash4me.repository.VerifyEmailRepository
import com.kash4me.ui.activity.RegistrationActivity
import com.kash4me.ui.activity.customer.CustomerRegistrationActivity
import com.kash4me.ui.activity.login.LoginActivity
import com.kash4me.ui.activity.merchant.MerchantRegistrationActivity
import com.kash4me.utils.SessionManager
import com.kash4me.utils.extensions.getEmptyIfNull
import com.kash4me.utils.extensions.getMinusOneIfNull
import com.kash4me.utils.extensions.showToast
import com.kash4me.utils.toast
import timber.log.Timber
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone


class VerifyEmailFragment : Fragment() {

    private lateinit var sessionManager: SessionManager

    lateinit var email: String

    private var _binding: FragmentVerifyBinding? = null
    private val mBinding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVerifyBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    companion object {
        fun newInstance() = VerifyEmailFragment()
    }

    private lateinit var viewModel: VerifyEmailViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        updateStepper(view)

        sessionManager = SessionManager(requireActivity().applicationContext)

        email = arguments?.getString("email").toString()

        startTimer()


        mBinding.resendOTPBTn.setOnClickListener {
            val params = HashMap<String, String>()
            params["email"] = email
            ((activity) as RegistrationActivity).customDialogClass.show()

            viewModel.resendOTP(params)

        }

        moveFocusToNextET()


        mBinding.verifyBtn.setOnClickListener {
            verifyBtnClicked()
        }


        val apiInterface =
            ApiServices.invoke(
                NetworkConnectionInterceptor(requireContext().applicationContext),
                NotFoundInterceptor()
            )

        val verifyEmailRepository = VerifyEmailRepository(apiInterface)

        viewModel = ViewModelProvider(
            this,
            VerifyEmailViewModelFactory(verifyEmailRepository)
        )[VerifyEmailViewModel::class.java]


        viewModel.loginResponse.observe(viewLifecycleOwner) {

            sessionManager.saveAuthToken("Bearer ${it.accessToken}")
            sessionManager.saveRefreshToken(it.refreshToken.getEmptyIfNull())
            sessionManager.saveUserType(it.userType.getMinusOneIfNull())
//            sessionManager.saveUserType(it.userType.getMinusOneIfNull())
//            sessionManager.saveUserProfile(it.userProfile.getFalseIfNull())
//            sessionManager.saveCBSettings(it.cbSettings.getFalseIfNull())

            Timber.d("Login response -> $it")

            ((activity) as RegistrationActivity).customDialogClass.hide()

            when (it.userType) {
                UserType.CUSTOMER.id -> {
                    val intent = Intent(activity, CustomerRegistrationActivity::class.java)
                    activity?.startActivity(intent)
                    activity?.finish()
                }

                UserType.MERCHANT.id -> {
                    val intent = Intent(activity, MerchantRegistrationActivity::class.java)
                    activity?.startActivity(intent)
                    activity?.finish()
                }

                UserType.STAFF.id -> {
                    val intent = LoginActivity.getNewIntent(packageContext = requireContext())
                    activity?.startActivity(intent)
                    activity?.finish()
                }

                else -> {
                    showToast("Something went wrong")
                }
            }

        }

        viewModel.errorMessage.observe(viewLifecycleOwner) {
            requireContext().toast(it)
            ((activity) as RegistrationActivity).customDialogClass.hide()
        }

        viewModel.resendOTPResponse.observe(viewLifecycleOwner) {
            requireContext().toast(it.detail)
            ((activity) as RegistrationActivity).customDialogClass.hide()
            startTimer()
        }

    }


    private fun updateStepper(view: View) {
        val firstImageView = view.findViewById<ImageView>(R.id.imageView)
        val secondImageView = view.findViewById<ImageView>(R.id.imageView2)

        firstImageView.setImageDrawable(
            ContextCompat.getDrawable(
                requireContext(),
                R.drawable.ic_check
            )
        )
        secondImageView.setBackgroundResource(R.drawable.green_circle)
    }

    private fun verifyBtnClicked() {

        val userOtp = mBinding.inputotp1.text.toString() +
                mBinding.inputotp2.text.toString() +
                mBinding.inputotp3.text.toString() +
                mBinding.inputotp4.text.toString() +
                mBinding.inputotp5.text.toString()
        if (userOtp.length != 5) {
            showToast("Please enter a valid pin")
            return
        }

        ((activity) as RegistrationActivity).customDialogClass.show()


        val params = HashMap<String, String>()
        params["email"] = email
        params["otp_code"] = userOtp

        Timber.d("Request -> $params")
        viewModel.verifyEmail(params)

    }

    private fun moveFocusToNextET() {
        mBinding.inputotp1.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                if (charSequence.toString().trim { it <= ' ' }.isNotEmpty()) {
                    mBinding.inputotp2.requestFocus()
                }
            }

            override fun afterTextChanged(editable: Editable) {}
        })
        mBinding.inputotp2.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                if (charSequence.toString().trim { it <= ' ' }.isNotEmpty()) {
                    mBinding.inputotp3.requestFocus()
                }
            }

            override fun afterTextChanged(editable: Editable) {}
        })
        mBinding.inputotp3.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                if (charSequence.toString().trim { it <= ' ' }.isNotEmpty()) {
                    mBinding.inputotp4.requestFocus()
                }
            }

            override fun afterTextChanged(editable: Editable) {}
        })

        mBinding.inputotp4.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                if (charSequence.toString().trim { it <= ' ' }.isNotEmpty()) {
                    mBinding.inputotp5.requestFocus()
                }
            }

            override fun afterTextChanged(editable: Editable) {}
        })
    }

    private val resendMinutes = 1L
    private val resendMinutesInSeconds = resendMinutes * 60L
    val resendMinutesInMilliseconds = resendMinutesInSeconds * 1000L

    private val timer
        get() = object : CountDownTimer(resendMinutesInMilliseconds, 1000) {

            // Callback function, fired on regular interval
            override fun onTick(millisUntilFinished: Long) {

                try {

                    mBinding.resendOTPBTn.isEnabled = false

                    val formatter: DateFormat = SimpleDateFormat("mm:ss", Locale.US)
                    formatter.timeZone = TimeZone.getTimeZone("UTC")
                    val text: String = formatter.format(Date(millisUntilFinished))
                    _binding?.resendOTPBTn?.text = getString(R.string.otp_sent)
                    val gray = ResourcesCompat.getColor(resources, R.color.grayText, null)
                    _binding?.resendOTPBTn?.setTextColor(gray)
                    val otpCountdownDescription = resources.getString(
                        R.string.your_verification_code_has_been_sent_please_wait_another_28_seconds_to_resend_the_code_again,
                        text
                    )
                    _binding?.tvResendOtpDescription?.text = otpCountdownDescription
                } catch (ex: IllegalStateException) {
                    Timber.d("Caught exception: " + ex.message)
                } catch (ex: NullPointerException) {
                    Timber.d("Caught exception: " + ex.message)
                }


            }

            // Callback function, fired
            // when the time is up
            override fun onFinish() {
                try {
                    _binding?.resendOTPBTn?.isEnabled = true
                    val green = ResourcesCompat.getColor(resources, R.color.green, null)
                    _binding?.resendOTPBTn?.setTextColor(green)
                    _binding?.resendOTPBTn?.setText(R.string.resend_otp)
                } catch (ex: Exception) {
                    Timber.d("Caught exception: ${ex.message}")
                }
            }
        }

    private fun startTimer() {
        timer.start()
    }

    override fun onDestroyView() {
        timer.cancel()
        super.onDestroyView()
        _binding = null
    }

}