package com.kash4me.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.navigation.Navigation
import com.kash4me.merchant.R
import com.kash4me.utils.AppConstants.goToVerifyScreen
import com.kash4me.utils.custom_views.CustomProgressDialog


class RegistrationActivity : AppCompatActivity() {

    var isBusinessSelected: Boolean = false

    var customDialogClass: CustomProgressDialog = CustomProgressDialog(this)

    private val navController by lazy {
        Navigation.findNavController(
            this,
            R.id.fragment_container_view
        )
    }

    var goToVerify: Boolean = false
    var email: String = ""

    companion object {

        fun getNewIntent(packageContext: Context): Intent {
            return Intent(packageContext, RegistrationActivity::class.java)
        }

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)

        val ivBack: ImageView = findViewById(R.id.backIV)
        ivBack.isVisible = true
        ivBack.setOnClickListener { onBackPressed() }

//        checkDrawable = ContextCompat.getDrawable(applicationContext, R.drawable.ic_check)!!

        goToVerify = intent.getBooleanExtra(goToVerifyScreen, false)
        email = intent.getStringExtra("email").toString()


        val actionBar: android.app.ActionBar? = actionBar
        actionBar?.setHomeButtonEnabled(true)
        actionBar?.setDisplayHomeAsUpEnabled(true)
        actionBar?.setHomeAsUpIndicator(R.drawable.ic_back)


    }

}