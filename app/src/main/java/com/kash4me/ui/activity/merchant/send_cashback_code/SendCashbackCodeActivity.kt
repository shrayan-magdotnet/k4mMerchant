package com.kash4me.ui.activity.merchant.send_cashback_code

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.kash4me.merchant.databinding.ActivitySendCashbackCodeBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SendCashbackCodeActivity : AppCompatActivity() {

    private var binding: ActivitySendCashbackCodeBinding? = null
    private val mBinding get() = binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySendCashbackCodeBinding.inflate(layoutInflater)
        val view = binding!!.root
        setContentView(view)

    }

}