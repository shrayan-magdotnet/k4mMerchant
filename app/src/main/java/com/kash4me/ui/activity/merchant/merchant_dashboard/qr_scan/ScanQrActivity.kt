package com.kash4me.ui.activity.merchant.merchant_dashboard.qr_scan

import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import androidx.appcompat.app.AppCompatActivity
import com.journeyapps.barcodescanner.CaptureManager
import com.kash4me.merchant.databinding.ActivityScanQrBinding

class ScanQrActivity : AppCompatActivity() {

    private var binding: ActivityScanQrBinding? = null
    private val mBinding get() = binding!!

    private lateinit var capture: CaptureManager

    companion object {

        fun getNewIntent(activity: AppCompatActivity): Intent {
            val intent = Intent(activity, ScanQrActivity::class.java)
            return intent
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScanQrBinding.inflate(layoutInflater)
        val view = binding!!.root
        setContentView(view)

        setupToolbar(title = "Scan QR")

        capture = CaptureManager(this, mBinding.zxingBarcodeScanner)
        capture.initializeFromIntent(intent, savedInstanceState)
        capture.decode()

        mBinding.zxingBarcodeScanner.setStatusText("")

    }

    fun setupToolbar(title: String) {
        setSupportActionBar(mBinding.toolbarLayout.root)
        supportActionBar?.title = title
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        mBinding.toolbarLayout.toolbar.setNavigationOnClickListener { onBackPressed() }
    }

    override fun onResume() {
        super.onResume()
        capture.onResume()
    }

    override fun onPause() {
        super.onPause()
        capture.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        capture.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        capture.onSaveInstanceState(outState)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return mBinding.zxingBarcodeScanner.onKeyDown(keyCode, event)
                || super.onKeyDown(keyCode, event)
    }

}
