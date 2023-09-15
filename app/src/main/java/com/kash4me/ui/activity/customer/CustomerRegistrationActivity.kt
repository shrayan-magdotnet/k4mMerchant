package com.kash4me.ui.activity.customer

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.kash4me.merchant.R
import com.kash4me.utils.custom_views.CustomProgressDialog

class CustomerRegistrationActivity : AppCompatActivity() {


    var customDialogClass: CustomProgressDialog = CustomProgressDialog(this)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customer_registration)
    }
}