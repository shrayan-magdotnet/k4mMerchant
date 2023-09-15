package com.kash4me.ui.activity.merchant.return_purchase

import android.content.Intent
import android.os.Bundle
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.kash4me.merchant.R
import com.kash4me.merchant.databinding.ActivityPurchaseReturnSuccessBinding
import com.kash4me.ui.activity.merchant.merchant_dashboard.MerchantDashBoardActivity
import com.kash4me.utils.extensions.formatUsingCurrencySystem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class PurchaseReturnSuccessActivity : AppCompatActivity() {

    private var binding: ActivityPurchaseReturnSuccessBinding? = null
    private val mBinding get() = binding!!

    companion object {

        private const val RETURNED_AMOUNT = "returned_amount"
        private const val MESSAGE = "message"

        fun getNewIntent(
            activity: AppCompatActivity, returnedAmount: Double, message: String
        ): Intent {
            val intent = Intent(activity, PurchaseReturnSuccessActivity::class.java)
            intent.putExtra(RETURNED_AMOUNT, returnedAmount)
            intent.putExtra(MESSAGE, message)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            return intent
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPurchaseReturnSuccessBinding.inflate(layoutInflater)
        val view = binding!!.root
        setContentView(view)
        setupToolbar()

        mBinding.tvAmount.text =
            "$" + intent.getDoubleExtra(RETURNED_AMOUNT, 0.0).formatUsingCurrencySystem()

        val message = intent.getStringExtra(MESSAGE)
        mBinding.tvMessage.text = message

        btnDoneListener()

        CoroutineScope(Dispatchers.Main).launch {

            delay(500)

            mBinding.ivSuccess.isVisible = true

            val animation =
                AnimationUtils.loadAnimation(this@PurchaseReturnSuccessActivity, R.anim.zoom_in)
            mBinding.ivSuccess.startAnimation(animation)

        }

    }

    fun setupToolbar() {
        setSupportActionBar(mBinding.toolbarLayout.root)
        supportActionBar?.setTitle(R.string.purchase_return)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        mBinding.toolbarLayout.root.setNavigationOnClickListener { navigateToMerchantDashboard() }
    }

    private fun btnDoneListener() {
        mBinding.btnDone.setOnClickListener { navigateToMerchantDashboard() }
    }

    private fun navigateToMerchantDashboard() {
        val intent = MerchantDashBoardActivity.getNewIntent(
            packageContext = this, isFreshLogin = false
        )
        startActivity(intent)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        navigateToMerchantDashboard()
    }

}