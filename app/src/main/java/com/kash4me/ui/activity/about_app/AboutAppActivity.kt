package com.kash4me.ui.activity.about_app

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.kash4me.R
import com.kash4me.data.models.user.UserType
import com.kash4me.databinding.ActivityAboutAppBinding
import com.kash4me.ui.activity.payment_gateway.PaymentSettingsViewModel
import com.kash4me.ui.dialog.ErrorDialog
import com.kash4me.utils.AppConstants
import com.kash4me.utils.FeeType
import com.kash4me.utils.SessionManager
import com.kash4me.utils.custom_views.CustomProgressDialog
import com.kash4me.utils.extensions.getZeroIfNull
import com.kash4me.utils.network.Resource
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class AboutAppActivity : AppCompatActivity() {

    private var binding: ActivityAboutAppBinding? = null
    private val mBinding get() = binding!!

    @Inject
    lateinit var sessionManager: SessionManager

    private val mProgressBar by lazy { CustomProgressDialog(this) }

    private val mViewModel: PaymentSettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAboutAppBinding.inflate(layoutInflater)
        val view = binding!!.root
        setContentView(view)

        setupToolbar(title = getString(R.string.about_us))
        Glide.with(this)
            .load(R.drawable.ic_logo)
            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
            .into(mBinding.ivLogo)

        getFeeSettings()

    }

    private fun getFeeSettings() {
        mViewModel.getFeeSettings(feeType = FeeType.NORMAL).observe(this) {

            when (it) {
                is Resource.Failure -> {
                    mProgressBar.hide()
                    val errorDialog = ErrorDialog.getInstance(message = it.errorMsg)
                    errorDialog.show(supportFragmentManager, errorDialog.tag)

                    mBinding.tvAboutFees.isVisible = false
                    mBinding.tvAboutFeesBody.isVisible = false

                }

                Resource.Loading -> {
                    mProgressBar.show()
                }

                is Resource.Success -> {
                    mProgressBar.hide()
                    Timber.d("Success -> ${it.value}")

                    mBinding.tvAboutFees.isVisible = true
                    mBinding.tvAboutFeesBody.isVisible = true


                    val transactionFee =
                        if (sessionManager.fetchUserType() == UserType.CUSTOMER) {
                            it.value.customerFee?.toDoubleOrNull().getZeroIfNull()
                        } else if (sessionManager.fetchUserType() == UserType.MERCHANT) {
                            it.value.merchantFee?.toDoubleOrNull().getZeroIfNull()
                        } else {
                            it.value.customerFee?.toDoubleOrNull().getZeroIfNull()
                        }

                    //                    "You will be charged 15% of your cash back amount plus 95 cents transaction fee. " +
                    //                            "\\nExample: You set, on every \$100 spend, earn \$10. " +
                    //                            "We will withdraw \$10 (will be paid to customer) + \$1.5 (15%) + 95 cents (transaction fee) = \$12.45"

                    val feeDescription =
                        "You will be charged ${it.value.commissionPercentage}% of your cash back amount plus $$transactionFee transaction fee."

                    val spentAmount = 100
                    val earnedAmount = 10
                    val withdrawAmount = 10

                    val commission =
                        withdrawAmount * (it.value.commissionPercentage?.toDoubleOrNull()
                            .getZeroIfNull() / 100.0)
                    Timber.d("Commission -> $commission")

                    val totalAmount = withdrawAmount + commission + transactionFee
                    Timber.d("Total amount -> $totalAmount")

                    val example =
                        "Example: You set, on every $$spentAmount spend, earn $$earnedAmount. We will withdraw $$withdrawAmount (will be paid to customer) + $$commission (${it.value.commissionPercentage}%) + $$transactionFee (transaction fee) = $$totalAmount"

                    val description = feeDescription + AppConstants.NEW_LINE + example
                    mBinding.tvAboutFeesBody.text = description

                }
            }

        }
    }

    fun setupToolbar(title: String) {
        setSupportActionBar(mBinding.toolbarLayout.toolbar)
        supportActionBar?.title = title
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        mBinding.toolbarLayout.toolbar.setNavigationOnClickListener { onBackPressed() }
    }

}