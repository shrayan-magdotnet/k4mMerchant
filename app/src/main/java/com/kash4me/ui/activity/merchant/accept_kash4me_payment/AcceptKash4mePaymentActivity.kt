package com.kash4me.ui.activity.merchant.accept_kash4me_payment

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.kash4me.data.models.customer.pay_by_kash4me.PayByKash4meQr
import com.kash4me.databinding.ActivityAcceptKash4mePaymentBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AcceptKash4mePaymentActivity : AppCompatActivity() {

    private var binding: ActivityAcceptKash4mePaymentBinding? = null
    private val mBinding get() = binding!!

    companion object {

        const val QR_RESPONSE = "qr_response"

        fun getNewIntent(activity: AppCompatActivity, qrResponse: PayByKash4meQr): Intent {
            val intent = Intent(activity, AcceptKash4mePaymentActivity::class.java)
            intent.putExtra(QR_RESPONSE, qrResponse)
            return intent
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAcceptKash4mePaymentBinding.inflate(layoutInflater)
        val view = binding!!.root
        setContentView(view)

    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

}