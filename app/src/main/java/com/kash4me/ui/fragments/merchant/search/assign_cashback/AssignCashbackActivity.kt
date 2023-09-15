package com.kash4me.ui.fragments.merchant.search.assign_cashback

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.kash4me.data.models.UserDetails
import com.kash4me.merchant.databinding.ActivityAssignCashbackBinding
import com.kash4me.ui.fragments.merchant.search.assign_cashback.fragment.assign_complete.AssignCompleteViewModel
import com.kash4me.utils.SessionManager
import com.kash4me.utils.custom_views.CustomProgressDialog
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class AssignCashbackActivity : AppCompatActivity() {

    private val viewModel: AssignCompleteViewModel by viewModels()

    private var binding: ActivityAssignCashbackBinding? = null
    private val mBinding get() = binding!!

    var customerId: Int = 0
    var customerDetails: UserDetails? = null
    var token: String = ""
    private lateinit var sessionManager: SessionManager

    var customDialogClass: CustomProgressDialog = CustomProgressDialog(this)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        customerId = intent.getIntExtra("customerId", 0)
        customerDetails = intent.getParcelableExtra("customer_details")
        Timber.d("Customer details -> $customerDetails")

        sessionManager = SessionManager(applicationContext)
        token = sessionManager.fetchAuthToken().toString()

        binding = ActivityAssignCashbackBinding.inflate(layoutInflater)
        val view = binding!!.root
        setContentView(view)
    }
}