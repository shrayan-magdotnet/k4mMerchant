package com.kash4me.ui.activity.forget_password

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.textfield.TextInputLayout
import com.kash4me.merchant.R
import com.kash4me.network.ApiServices
import com.kash4me.network.NetworkConnectionInterceptor
import com.kash4me.network.NotFoundInterceptor
import com.kash4me.repository.ForgetPasswordRepository
import com.kash4me.ui.dialog.ErrorDialog
import com.kash4me.ui.dialog.SuccessDialog
import com.kash4me.utils.custom_views.CustomProgressDialog
import com.kash4me.utils.toast

class ForgetPasswordActivity : AppCompatActivity() {


    private lateinit var viewModel: ForgetPasswordViewModel
    private var customDialogClass: CustomProgressDialog = CustomProgressDialog(this)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forget_password)

        val apiInterface =
            ApiServices.invoke(
                NetworkConnectionInterceptor(applicationContext),
                NotFoundInterceptor()
            )

        val forgetPasswordRepository = ForgetPasswordRepository(apiInterface)

        viewModel = ViewModelProvider(
            this,
            ForgetPasswordViewModelFactory(forgetPasswordRepository)
        )[ForgetPasswordViewModel::class.java]

        val sendEmailBtn = findViewById<Button>(R.id.sendEmailBtn)
        val emailET = findViewById<TextInputLayout>(R.id.emailET)
        val backIV = findViewById<ImageView>(R.id.backIV)
        backIV.visibility = View.VISIBLE


        backIV.setOnClickListener {
            onBackPressed()
        }



        sendEmailBtn.setOnClickListener {
            val email = emailET.editText?.text.toString().trim()

            if (email.isEmpty()) {
                toast("Please enter a valid email")
                return@setOnClickListener
            }

            val params = HashMap<String, String>()
            params["email"] = email

            customDialogClass.show()

            viewModel.forgetPassword(params)


        }

        viewModel.forgetPasswordResponse.observe(this) {
            customDialogClass.hide()
            val successDialog = SuccessDialog.getInstance(message = it.detail)
            successDialog.show(supportFragmentManager, successDialog.tag)

        }

        viewModel.errorMessage.observe(this) {
            val errorDialog = ErrorDialog.getInstance(message = it.toString())
            errorDialog.show(supportFragmentManager, errorDialog.tag)
            customDialogClass.hide()
        }

    }
}