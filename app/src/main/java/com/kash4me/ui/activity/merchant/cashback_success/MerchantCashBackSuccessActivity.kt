package com.kash4me.ui.activity.merchant.cashback_success

import android.content.Intent
import android.os.Bundle
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.kash4me.R
import com.kash4me.data.models.CashBackSuccessResponse
import com.kash4me.data.models.user.UserType
import com.kash4me.ui.activity.merchant.merchant_dashboard.MerchantDashBoardActivity
import com.kash4me.ui.activity.staff.StaffDashboardActivity
import com.kash4me.utils.SessionManager
import com.kash4me.utils.custom_views.CurrencyTextView
import com.kash4me.utils.extensions.getZeroIfNull
import com.kash4me.utils.extensions.toLong
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MerchantCashBackSuccessActivity : AppCompatActivity() {

    private lateinit var cashBackAmountTV: CurrencyTextView
    private lateinit var closeBtn: Button

    @Inject
    lateinit var sessionManager: SessionManager

    private lateinit var cashBackSuccessResponse: CashBackSuccessResponse

    companion object {

        const val SUCCESS_RESPONSE = "cashBackSuccessResponse"

        fun getNewIntent(
            activity: AppCompatActivity,
            successResponse: CashBackSuccessResponse
        ): Intent {
            val intent = Intent(activity, MerchantCashBackSuccessActivity::class.java)
            intent.putExtra(SUCCESS_RESPONSE, successResponse)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            return intent
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_merchant_cash_back_success)

        val bundle = intent?.extras
        if (bundle != null) {
            cashBackSuccessResponse = bundle.getParcelable("cashBackSuccessResponse")!!
        }

        initUI()

        val ivSuccess: ImageView = findViewById(R.id.successIV)

        CoroutineScope(Dispatchers.Main).launch {

            delay(500)

            ivSuccess.isVisible = true

            val animation =
                AnimationUtils.loadAnimation(this@MerchantCashBackSuccessActivity, R.anim.zoom_in)
            ivSuccess.startAnimation(animation)

        }

    }


    private fun initUI() {
        cashBackAmountTV = findViewById(R.id.cashBackAmountTV)
        cashBackAmountTV.setValue(
            cashBackSuccessResponse.totalCashbackAmount?.toDoubleOrNull().toLong().getZeroIfNull()
        )
        closeBtn = findViewById(R.id.closeBtn)


        initOnClick()
    }

    private fun initOnClick() {
        closeBtn.setOnClickListener { closeActivity() }
    }

    private fun closeActivity() {
        if (sessionManager.fetchUserType() == UserType.MERCHANT) {
            val intent = MerchantDashBoardActivity.getNewIntent(
                packageContext = this,
                isFreshLogin = false
            )
            startActivity(intent)
        } else if (sessionManager.fetchUserType() == UserType.STAFF) {
            val intent = StaffDashboardActivity.getNewIntent(packageContext = this)
            startActivity(intent)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        closeActivity()
    }

}