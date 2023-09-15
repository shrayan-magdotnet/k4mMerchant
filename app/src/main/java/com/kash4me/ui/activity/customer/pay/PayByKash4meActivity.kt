package com.kash4me.ui.activity.customer.pay

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.kash4me.merchant.databinding.ActivityPayByKash4meBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PayByKash4meActivity : AppCompatActivity() {

    private var binding: ActivityPayByKash4meBinding? = null
    private val mBinding get() = binding!!

    companion object {

        const val REMAINING_BALANCE = "remaining_balance"

        fun getNewIntent(activity: AppCompatActivity, remainingBalance: Double): Intent {
            val intent = Intent(activity, PayByKash4meActivity::class.java)
            intent.putExtra(REMAINING_BALANCE, remainingBalance)
            return intent
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPayByKash4meBinding.inflate(layoutInflater)
        val view = binding!!.root
        setContentView(view)
        setupToolbar()

    }

    fun setupToolbar() {
        setSupportActionBar(mBinding.toolbar.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Pay"
        mBinding.toolbar.toolbar.setNavigationOnClickListener { onBackPressed() }
    }

}