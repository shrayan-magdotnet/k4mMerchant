package com.kash4me.ui.activity.login

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.Observer
import com.kash4me.BuildConfig
import com.kash4me.data.models.user.UserType
import com.kash4me.databinding.ActivityLoginBinding
import com.kash4me.ui.activity.RegistrationActivity
import com.kash4me.ui.activity.customer.CustomerRegistrationActivity
import com.kash4me.ui.activity.customer.customer_dashboard.CustomerDashboardActivity
import com.kash4me.ui.activity.forget_password.ForgetPasswordActivity
import com.kash4me.ui.activity.merchant.MerchantRegistrationActivity
import com.kash4me.ui.activity.merchant.merchant_dashboard.MerchantDashBoardActivity
import com.kash4me.ui.activity.staff.StaffDashboardActivity
import com.kash4me.ui.common.RequestLocationPermissionActivity
import com.kash4me.ui.dialog.ErrorDialog
import com.kash4me.utils.AppConstants
import com.kash4me.utils.AppConstants.goToVerifyScreen
import com.kash4me.utils.SessionManager
import com.kash4me.utils.custom_views.CustomProgressDialog
import com.kash4me.utils.extensions.clearError
import com.kash4me.utils.extensions.getEmptyIfNull
import com.kash4me.utils.extensions.getFalseIfNull
import com.kash4me.utils.extensions.getMinusOneIfNull
import dagger.hilt.android.AndroidEntryPoint
import pub.devrel.easypermissions.EasyPermissions
import timber.log.Timber

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

    companion object {

        fun getNewIntent(packageContext: Context): Intent {
            val intent = Intent(packageContext, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            return intent
        }

    }

    private val viewModel: LoginViewModel by viewModels()

    private lateinit var sessionManager: SessionManager
    private lateinit var binding: ActivityLoginBinding

    private var customDialogClass: CustomProgressDialog = CustomProgressDialog(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        val view = binding.root
        setContentView(view)

        sessionManager = SessionManager(this)

        val emailET = binding.emailET
        val passwordET = binding.passwordET
        val loginBtn = binding.loginBtn
        val businessUserCB = binding.businessUserCB
        val forgetPasswordBtn = binding.forgetPasswordBtn
        forgetPasswordBtn.setOnClickListener {
            val intent = Intent(applicationContext, ForgetPasswordActivity::class.java)
            startActivity(intent)
        }

        loginBtn.setOnClickListener {

            val email = emailET.editText?.text.toString().trim()
            val password = passwordET.editText?.text.toString()

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

            if (email.length < AppConstants.MIN_USERNAME_LENGTH) {
                emailET.error =
                    "Username should be atleast ${AppConstants.MIN_USERNAME_LENGTH} characters"
                isValid = false
            }

            if (password.length < AppConstants.MIN_PASSWORD_LENGTH) {
                passwordET.error =
                    "Password should be atleast ${AppConstants.MIN_PASSWORD_LENGTH} characters"
                isValid = false
            } else {
                passwordET.clearError()
            }

            if (!isValid) {
                return@setOnClickListener
            }

            val request = HashMap<String, String>()
            request["password"] = password
            if (email.contains("@")) {
                request["email"] = email
            } else {
                request["user_name"] = email
            }

            customDialogClass.show()
            Timber.d("Request -> $request")

            if (businessUserCB.isChecked) {
                viewModel.loginMerchant(request = request)
            } else {
                viewModel.loginCustomer(request = request)
            }
        }


        binding.registerBtn.setOnClickListener {
            val intent = Intent(applicationContext, RegistrationActivity::class.java)
            startActivity(intent)
        }

        viewModel.loginResponse.observe(this, Observer {

            Timber.d("Login information -> $it")

            sessionManager.saveAuthToken("Bearer ${it.accessToken}")
            sessionManager.saveRefreshToken(it.refreshToken.getEmptyIfNull())
            sessionManager.saveUserType(it.userType.getMinusOneIfNull())
            sessionManager.saveUserProfile(it.userProfile.getFalseIfNull())
            sessionManager.saveCBSettings(it.cbSettings.getFalseIfNull())

            customDialogClass.hide()

            val isMerchantUnverified = it.userProfile == false && it.isMerchant()
            if (isMerchantUnverified) {
                Timber.d("Merchant is unverified")
                val intent = Intent(this, MerchantRegistrationActivity::class.java)
                startActivity(intent)
                return@Observer
            }

            val isCashbackSettingsIncomplete = it.isMerchant() && it.cbSettings?.not() == true
            if (isCashbackSettingsIncomplete) {
                Timber.d("Merchant has incomplete cashback settings")
                viewModel.fetchMerchantDetailsForCashBack("Bearer ${it.accessToken}")
                return@Observer
            }

            val isCustomerUnverified = it.userProfile == false && it.isCustomer()
            if (isCustomerUnverified) {
                Timber.d("Customer is unverified")
                val intent = Intent(this, CustomerRegistrationActivity::class.java)
                startActivity(intent)
                return@Observer
            }

            if (it.isMerchant()) {
                goToMerchantDashBoard()
                return@Observer
            }

            if (it.isStaff()) {
                goToStaffDashboard()
                return@Observer
            }

            if (it.isCustomer()) {

                if (isLocationPermissionGranted()) {
                    goToCustomerDashBoard()
                } else {
                    goToRequestLocationPermissionActivity()
                }
                return@Observer

            }

        })

        viewModel.errorMessage.observe(this) {

            customDialogClass.hide()

            if (it.error_code == "004") {
                goToCustomerDashBoard()
                return@observe
            }

            if (it.error_code == "001") {
                val intent = Intent(this, RegistrationActivity::class.java)
                intent.putExtra(goToVerifyScreen, true)
                intent.putExtra("email", emailET.editText?.text.toString().trim())
                startActivity(intent)
            } else {
                val errorDialog = ErrorDialog.getInstance(message = it.error)
                errorDialog.show(supportFragmentManager, errorDialog.tag)
            }

        }


        viewModel.merchantResponse.observe(this) {
            sessionManager.saveMerchantId(it.id)

            customDialogClass.hide()

            val intent = Intent(this, MerchantRegistrationActivity::class.java)
            intent.putExtra("goToCashBack", true)
            startActivity(intent)

        }

        binding.tvSkipLogin.setOnClickListener {
            val intent =
                CustomerDashboardActivity.getNewIntent(packageContext = this, isFreshLogin = false)
            sessionManager.saveUserType(userType = UserType.ANONYMOUS.id)
            startActivity(intent)
        }

    }

    private fun goToRequestLocationPermissionActivity() {
        val intent = RequestLocationPermissionActivity.getNewIntent(activity = this)
        startActivity(intent)
    }

    private fun goToStaffDashboard() {
        val intent = StaffDashboardActivity.getNewIntent(packageContext = this)
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()

        if (BuildConfig.DEBUG) {
            val randomNumber = (1..2).random()
            if (randomNumber == 1) {
                binding.emailET.editText?.setText(AppConstants.testCustomerEmail)
                binding.passwordET.editText?.setText(AppConstants.testCustomerPassword)
            } else {
                binding.emailET.editText?.setText(AppConstants.testMerchantEmail)
                binding.passwordET.editText?.setText(AppConstants.testMerchantPassword)
            }
        }

    }

    override fun onBackPressed() {
        super.onBackPressed()
        finishAffinity()
    }


    private fun goToCustomerDashBoard() {
        val intent =
            CustomerDashboardActivity.getNewIntent(packageContext = this, isFreshLogin = true)
        startActivity(intent)
    }

    private fun goToMerchantDashBoard() {
        val intent =
            MerchantDashBoardActivity.getNewIntent(packageContext = this, isFreshLogin = true)
        startActivity(intent)
    }

    private fun isLocationPermissionGranted(): Boolean {
        val permissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        return EasyPermissions.hasPermissions(this, *permissions)
    }

}