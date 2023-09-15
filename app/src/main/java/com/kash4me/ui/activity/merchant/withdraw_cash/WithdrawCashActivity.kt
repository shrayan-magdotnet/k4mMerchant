package com.kash4me.ui.activity.merchant.withdraw_cash

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.kash4me.merchant.R
import com.kash4me.merchant.databinding.ActivityWithdrawCashBinding
import com.kash4me.ui.fragments.merchant.withdraw_cash.WithdrawCashFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WithdrawCashActivity : AppCompatActivity() {

    private var binding: ActivityWithdrawCashBinding? = null
    private val mBinding get() = binding!!

    companion object {

        const val AVAILABLE_BALANCE = "available_balance"

        fun getNewIntent(packageContext: Context, availableBalance: Double?): Intent {
            val intent = Intent(packageContext, WithdrawCashActivity::class.java)
            intent.putExtra(AVAILABLE_BALANCE, availableBalance)
            return intent
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWithdrawCashBinding.inflate(layoutInflater)
        val view = binding!!.root
        setContentView(view)

        setupToolbar()

        val fragment = WithdrawCashFragment.getNewInstance(
            availableBalance = intent.getDoubleExtra(AVAILABLE_BALANCE, 0.0)
        )
        addFragment(fragment)


    }

    fun setupToolbar() {
        setSupportActionBar(mBinding.toolbarLayout.root)
        supportActionBar?.setTitle(R.string.withdraw_cash_back)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        mBinding.toolbarLayout.root.setNavigationOnClickListener { onBackPressed() }
    }

    fun addFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .add(R.id.fl_withdraw_cash, fragment, fragment.tag)
            .commit()
    }

    fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fl_withdraw_cash, fragment, fragment.tag)
            .addToBackStack(fragment.tag)
            .commit()
    }

}