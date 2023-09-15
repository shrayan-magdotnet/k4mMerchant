package com.kash4me.ui.activity.customer.cashback_code

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.kash4me.merchant.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class GetCashbackFromCodeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_get_cashback_from_code)
    }

}