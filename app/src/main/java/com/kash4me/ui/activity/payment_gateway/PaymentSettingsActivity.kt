package com.kash4me.ui.activity.payment_gateway

import android.os.Bundle
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.Navigation
import com.kash4me.merchant.R
import com.kash4me.merchant.databinding.ActivityPaymentSettingsBinding
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class PaymentSettingsActivity : AppCompatActivity() {

    private var binding: ActivityPaymentSettingsBinding? = null
    private val mBinding get() = binding!!

    private val navController by lazy { Navigation.findNavController(this, R.id.nav_host_fragment) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPaymentSettingsBinding.inflate(layoutInflater)
        val view = binding!!.root
        setContentView(view)

//        setupActionBarWithNavController(navController)
        setupToolbar(title = R.string.payment_information)


    }

    fun setupToolbar(@StringRes title: Int) {
        setSupportActionBar(mBinding.toolbar.root)
        supportActionBar?.setTitle(title)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        mBinding.toolbar.root.setNavigationOnClickListener {
            val wasNavigationSuccessful = navController.navigateUp()
            Timber.d("Navigate up result -> $wasNavigationSuccessful")
            if (!wasNavigationSuccessful) {
                finish()
            }
        }
    }

}