package com.kash4me.ui.activity.customer.merchant_details

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.AbsoluteSizeSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.DisplayMetrics
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.google.gson.Gson
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.journeyapps.barcodescanner.BarcodeEncoder
import com.kash4me.R
import com.kash4me.data.models.QRResponse
import com.kash4me.data.models.customer.view_merchant_details.MerchantDetailsResponse
import com.kash4me.data.models.user.UserType
import com.kash4me.databinding.ActivityMerchantDetails2Binding
import com.kash4me.network.ApiServices
import com.kash4me.network.NetworkConnectionInterceptor
import com.kash4me.network.NotFoundInterceptor
import com.kash4me.repository.MerchantDetailsWithCustomerInfoRepository
import com.kash4me.security.AES
import com.kash4me.ui.activity.RegistrationActivity
import com.kash4me.ui.activity.customer.merchant_details.announcement.AnnouncementBottomSheet
import com.kash4me.ui.activity.customer.merchant_details.merchant_details_info.MerchantDetailsInfoBottomSheet
import com.kash4me.ui.activity.customer.merchant_details.transactions.TransactionsActivity
import com.kash4me.ui.activity.customer.request_cashback.RequestCashbackActivity
import com.kash4me.ui.activity.login.LoginActivity
import com.kash4me.ui.dialog.ErrorDialog
import com.kash4me.utils.AppConstants
import com.kash4me.utils.CurrentLocation
import com.kash4me.utils.ImageUtils
import com.kash4me.utils.SessionManager
import com.kash4me.utils.convertDpToPixel
import com.kash4me.utils.custom_views.CustomProgressDialog
import com.kash4me.utils.extensions.formatAsCurrency
import com.kash4me.utils.extensions.formatUsingCurrencySystem
import com.kash4me.utils.extensions.getEmptyIfNull
import com.kash4me.utils.extensions.getNotAvailableIfEmptyOrNull
import com.kash4me.utils.extensions.getZeroIfNull
import com.kash4me.utils.extensions.openCustomTab
import com.kash4me.utils.extensions.showToast
import com.kash4me.utils.isNull
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import kotlin.math.roundToInt

@AndroidEntryPoint
class MerchantDetailsActivity : AppCompatActivity() {

    companion object {

        private const val MERCHANT_ID = "merchant_id"
        private const val MERCHANT_NAME = "merchant_name"

        fun getNewIntent(packageContext: Context, merchantId: Int, merchantName: String): Intent {
            val intent = Intent(packageContext, MerchantDetailsActivity::class.java)
            intent.putExtra(MERCHANT_ID, merchantId)
            intent.putExtra(MERCHANT_NAME, merchantName)
            return intent
        }

    }

    private var merchantId: Int = 0

    private lateinit var merchantDetailsResponse: MerchantDetailsResponse

    private val viewModel: MerchantDetailsViewModel by viewModels()

    @Inject
    lateinit var sessionManager: SessionManager

    private lateinit var lat: String
    private lateinit var lng: String

    private var customProgressDialog: CustomProgressDialog = CustomProgressDialog(this)

    private var binding: ActivityMerchantDetails2Binding? = null
    private val mBinding get() = binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMerchantDetails2Binding.inflate(layoutInflater)
        val view = binding!!.root
        setContentView(view)

        customProgressDialog.show()
        setupToolbar()

        if (sessionManager.fetchUserType() == UserType.ANONYMOUS) {

            mBinding.apply {

                clSpendProgressSection.isVisible = false
                clQrSection.isVisible = false
                mBinding.tvProfileDescription.isVisible = false
                ivGoThere.isVisible = false
                ivRequestCashback.isVisible = false

                layoutAccountCreationPrompt.root.isVisible = true
                layoutAccountCreationPrompt.apply {

                    layoutAccountCreationPrompt.tvTitle.isVisible = true
                    tvTitle.text = "Want to get Cash backs?"
                    layoutAccountCreationPrompt.tvDescription.isVisible = true
                    layoutAccountCreationPrompt.tvDescription.text =
                        "By signing up with us, you can earn cash backs from different places which you can withdraw anytime."

                    btnLogin.setOnClickListener {
                        val intent = Intent(this@MerchantDetailsActivity, LoginActivity::class.java)
                        startActivity(intent)
                    }

                    btnSignup.setOnClickListener {
                        val intent =
                            Intent(this@MerchantDetailsActivity, RegistrationActivity::class.java)
                        startActivity(intent)
                    }

                }

            }

        }

        merchantId = intent.getIntExtra(MERCHANT_ID, 0)
        initViewModel()

        mBinding.ivViewTransactions.setOnClickListener { openTransactionsActivity() }
        mBinding.ivViewAnnouncement.setOnClickListener { openAnnouncementBottomSheet() }

    }

    private fun openAnnouncementBottomSheet() {
        val bottomSheet = AnnouncementBottomSheet.newInstance(
            title = merchantDetailsResponse.announcement?.message.getEmptyIfNull()
        )
        bottomSheet.show(supportFragmentManager, bottomSheet.tag)
    }

    private fun setupHalfPieChart() {
        val display = windowManager?.defaultDisplay
        val metrics = DisplayMetrics()
        windowManager?.defaultDisplay?.getMetrics(metrics)

        val height = metrics.heightPixels
        val offset = (height * 0.5).toInt()

//        val params = mBinding.halfPieChart.layoutParams as ConstraintLayout.LayoutParams
//        params.setMargins(0, 0, 0, -offset)
        //        mBinding.halfPieChart.scaleX = 1.4f
        //        mBinding.halfPieChart.scaleY = 1.4f
//        mBinding.halfPieChart.layoutParams = params

        mBinding.halfPieChart.apply {

//            rotationAngle = 0f
            isRotationEnabled = false
            isHighlightPerTapEnabled = false

            legend.isEnabled = false

            val cashRewards = getSpannableForCashRewardsAmount()
            val cashRewardsCaption = getSpannableForCashRewardsCaption()
            val onYourWay = getSpannableForOnYourWayAmount()
            val onYourWayCaption = getSpannableForOnYourWayCaption()

            // Concatenate the SpannableStrings using SpannableStringBuilder with a new line separator
            val stringBuilder = SpannableStringBuilder()
            stringBuilder.append("\n")
            stringBuilder.append("\n")
            stringBuilder.append("\n")
            stringBuilder.append(cashRewards)
            stringBuilder.append("\n")
            stringBuilder.append(cashRewardsCaption)
            stringBuilder.append("\n")
            stringBuilder.append("\n")
            stringBuilder.append(onYourWay)
            stringBuilder.append("\n")
            stringBuilder.append(onYourWayCaption)

            // Use the spannableString as needed (e.g., set it to a TextView)
            centerText = stringBuilder

            setHoleRadius(50f)
            setTransparentCircleRadius(55f)

            setUsePercentValues(true)
            description.isEnabled = false
            isDrawHoleEnabled = true

            maxAngle = 180f
            rotationAngle = 180f
            setCenterTextOffset(0f, -20f)
        }

        setDataInHalfPieChart()

        mBinding.halfPieChart.apply {
            animateY(1000, Easing.EaseInOutCubic)

            setDrawEntryLabels(false)
            setHoleColor(Color.WHITE)
            setTransparentCircleColor(Color.GRAY)
            setBackgroundColor(Color.TRANSPARENT)

        }
    }

    private fun getSpannableForCashRewardsAmount(): SpannableString {
        val cashRewards = getYourCashRewardsData()

        val cashRewardsSpannable = SpannableString(cashRewards)

        // Apply bold style to the entire text
        cashRewardsSpannable.setSpan(
            StyleSpan(Typeface.BOLD),
            0,
            cashRewards.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        // Apply black text color to the entire text
        cashRewardsSpannable.setSpan(
            ForegroundColorSpan(Color.BLACK),
            0,
            cashRewards.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        // Apply desired text size to the entire text
        cashRewardsSpannable.setSpan(
            AbsoluteSizeSpan(16f.convertDpToPixel(context = this).roundToInt()),
            0,
            cashRewards.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        return cashRewardsSpannable
    }

    private fun getSpannableForCashRewardsCaption(): SpannableString {
        val cashRewardsCaption = "YOUR CASH REWARDS"
        val cashRewardsCaptionSpannable = SpannableString(cashRewardsCaption)

        // Setting text size
        cashRewardsCaptionSpannable.setSpan(
            AbsoluteSizeSpan(11f.convertDpToPixel(context = this).roundToInt()),
            0,
            cashRewardsCaption.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        // Setting text color
        cashRewardsCaptionSpannable.setSpan(
            ForegroundColorSpan(ContextCompat.getColor(this, R.color.dark_gray)),
            0,
            cashRewardsCaption.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        return cashRewardsCaptionSpannable
    }

    private fun getSpannableForOnYourWayAmount(): SpannableString {
        val cashRewards = getOnYourWayData()

        val cashRewardsSpannable = SpannableString(cashRewards)

        // Apply bold style to the entire text
        cashRewardsSpannable.setSpan(
            StyleSpan(Typeface.BOLD),
            0,
            cashRewards.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        // Apply text color to the entire text
        cashRewardsSpannable.setSpan(
            ForegroundColorSpan(Color.parseColor("#ffaf2e")),
            0,
            cashRewards.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        // Apply desired text size to the entire text
        cashRewardsSpannable.setSpan(
            AbsoluteSizeSpan(16f.convertDpToPixel(context = this).roundToInt()),
            0,
            cashRewards.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        return cashRewardsSpannable
    }

    private fun getSpannableForOnYourWayCaption(): SpannableString {
        val onYourWayCaption = "ON YOUR WAY"
        val cashRewardsCaptionSpannable = SpannableString(onYourWayCaption)

        // Setting text size
        cashRewardsCaptionSpannable.setSpan(
            AbsoluteSizeSpan(11f.convertDpToPixel(context = this).roundToInt()),
            0,
            onYourWayCaption.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        // Setting text color
        cashRewardsCaptionSpannable.setSpan(
            ForegroundColorSpan(Color.parseColor("#ffaf2e")),
            0,
            onYourWayCaption.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        return cashRewardsCaptionSpannable
    }

    private fun setDataInHalfPieChart() {

        val pieEntries = arrayListOf<PieEntry>()

        val spentProgress =
            merchantDetailsResponse.latestTxn?.progressPercent?.toDoubleOrNull().getZeroIfNull()
        Timber.d("Spent progress -> $spentProgress")
        pieEntries.add(PieEntry(spentProgress.toFloat(), "Spent"))

        val remainingProgress = 100.00 - spentProgress
        Timber.d("Goal progress -> $remainingProgress")
        pieEntries.add(PieEntry(remainingProgress.toFloat(), "Goal"))

        val dataSet = PieDataSet(pieEntries, "Partner")
        dataSet.selectionShift = 5f
        dataSet.sliceSpace = 0f
        dataSet.colors = mutableListOf(
            ContextCompat.getColor(this, R.color.blue_progress_made),
            ContextCompat.getColor(this, R.color.blue_progress_remaining)
        )


        val data = PieData(dataSet)
        data.setValueFormatter(PercentFormatter())
        data.setValueTextSize(15f)
        data.setValueTextColor(Color.TRANSPARENT)

        mBinding.halfPieChart.data = data

    }

    private fun openTransactionsActivity() {
        val intent = TransactionsActivity.getNewIntent(
            activity = this,
            merchantId = merchantId,
            merchantName = intent.getStringExtra(MERCHANT_NAME).getEmptyIfNull()
        )
        startActivity(intent)
    }

    private fun openInfoBottomSheet() {
        val bottomSheet = MerchantDetailsInfoBottomSheet()
        bottomSheet.show(supportFragmentManager, bottomSheet.tag)
    }

    private fun setupToolbar() {
        setSupportActionBar(mBinding.toolbarLayout.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        mBinding.toolbarLayout.toolbar.setNavigationOnClickListener { onBackPressed() }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_merchant_details, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        return when (item.itemId) {
            R.id.action_info -> {
                openInfoBottomSheet()
                true
            }

            R.id.action_refresh -> {
                val token = sessionManager.fetchAuthToken().toString()
                viewModel.getMerchantDetailsWithCustomerInfo(
                    merchantId,
                    token,
                    CurrentLocation.lat,
                    CurrentLocation.lng
                )
                true
            }

            else -> {
                super.onOptionsItemSelected(item)
            }
        }

    }

    private fun initViewModel() {
        val apiInterface = ApiServices.invoke(
            NetworkConnectionInterceptor(applicationContext), NotFoundInterceptor()
        )

        val repository = MerchantDetailsWithCustomerInfoRepository(apiInterface)

        fetchMerchantDetails()
        initUI()
    }

    private fun fetchMerchantDetails() {
        val token = sessionManager.fetchAuthToken().toString()
        viewModel.getMerchantDetailsWithCustomerInfo(
            merchantId,
            token,
            CurrentLocation.lat,
            CurrentLocation.lng
        )

        viewModel.merchantDetailsResponse.observe(this) {
            Timber.d("Fetching merchant details -> $it")
            merchantDetailsResponse = it
            setData()
            if (!it.qrImage.isNullOrBlank()) {
                generateQR(it.qrImage)
            }
        }

        viewModel.errorMessage.observe(this) {

            if (it != null) {
                val errorDialog = ErrorDialog.getInstance(message = it.error)
                errorDialog.show(supportFragmentManager, errorDialog.tag)
                customProgressDialog.hide()
            }

        }

    }

    private fun setData() {
        Timber.d("Data -> $merchantDetailsResponse")

        supportActionBar?.title = merchantDetailsResponse.name
//        mBinding.merchantNameTV.text = merchantDetailsWithCustomerInfoResponse.name

        setCoverPhoto()
        setImage()
        setAddressAndDistanceData()
        setEarnCashbackData()
        setSpendLimitData()
        getYourCashRewardsData()
        getOnYourWayData()
        setSpentAndGoalAmountData()
        setPromotionalBannerData()
        setDescription()
        setWebsiteUrl()

        val hasNoAnnouncement =
            merchantDetailsResponse.announcement.isNull || merchantDetailsResponse.announcement == MerchantDetailsResponse.Announcement()
        mBinding.ivViewAnnouncement.isVisible = !hasNoAnnouncement
        mBinding.tvViewAnnouncement.isVisible = !hasNoAnnouncement

        setupHalfPieChart()

        if (merchantDetailsResponse.activeCashbackSettings?.cashbackType == 1) {
            if (merchantDetailsResponse.activeCashbackSettings?.cashbackAmount.isNullOrEmpty()) {
//                mBinding.tvEarnCashbackCaption.visibility = View.GONE
//                mBinding.tvEarnCashbackAmount.visibility = View.GONE
            } else {
//                mBinding.tvEarnCashbackAmount.text =
//                    "$${merchantDetailsWithCustomerInfoResponse.active_cashback_settings.cashback_amount.formatAmount}"
            }


        } else {
            if (merchantDetailsResponse.activeCashbackSettings?.cashbackPercentage.isNullOrEmpty()) {
//                mBinding.tvEarnCashbackCaption.visibility = View.GONE
//                mBinding.tvEarnCashbackAmount.visibility = View.GONE
            } else {
//                mBinding.tvEarnCashbackAmount.text =
//                    "$${merchantDetailsWithCustomerInfoResponse.active_cashback_settings.cashback_percentage.formatAmount}"
            }
        }

//        mBinding.leftTV.setValue(
//            merchantDetailsWithCustomerInfoResponse.latest_txn?.amount_left?.toDoubleOrNull()
//                .toLong().getZeroIfNull()
//        )
//        mBinding.earnedCashTV.setValue(
//            merchantDetailsWithCustomerInfoResponse.latest_txn?.cashback_amount?.toDoubleOrNull()
//                .toLong().getZeroIfNull()
//        )

        lat = merchantDetailsResponse.latitude.getEmptyIfNull()
        lng = merchantDetailsResponse.longitude.getEmptyIfNull()

//        if (merchantDetailsWithCustomerInfoResponse.latest_txn.processing_amount < 0) {
//            statusTV.text =
//                "Processing: $${merchantDetailsWithCustomerInfoResponse.latest_txn.processing_amount}"
//            statusTV.visibility = View.VISIBLE
//
//        }

//        mBinding.mainCL.visibility = View.VISIBLE

        customProgressDialog.hide()


    }

    private fun setWebsiteUrl() {
        if (merchantDetailsResponse.website.isNullOrBlank()) {
            mBinding.ivWebsite.isVisible = false
        } else {
            mBinding.ivWebsite.isVisible = true
            mBinding.ivWebsite.setOnClickListener {
                openCustomTab(uri = Uri.parse(merchantDetailsResponse.website))
            }
        }
    }

    private fun setDescription() {
        mBinding.tvProfileDescription.text = merchantDetailsResponse.description
    }

    private fun setCoverPhoto() {

        if (!merchantDetailsResponse.mainImage.isNullOrBlank()) {
            Glide.with(this)
                .load(merchantDetailsResponse.mainImage)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .into(mBinding.ivCoverPhoto)
        } else {
            showToast("Hide cover photo")
        }
    }

    private fun setImage() {

        val nameInitials =
            ImageUtils().getNameInitialsImage(merchantDetailsResponse.name.getNotAvailableIfEmptyOrNull())

        if (!merchantDetailsResponse.logo.isNullOrBlank()) {
            Glide.with(this)
                .load(merchantDetailsResponse.logo)
                .placeholder(nameInitials)
                .error(nameInitials)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .into(mBinding.merchantLogo.ivLogo)
        } else {
            mBinding.merchantLogo.ivLogo.setImageDrawable(nameInitials)
        }
    }

    private fun setAddressAndDistanceData() {
        mBinding.tvAddress.text = merchantDetailsResponse.address
        Timber.d("Distance -> ${merchantDetailsResponse.distance}")
        mBinding.tvDistance.text = merchantDetailsResponse.distance
    }

    private fun setPromotionalBannerData() {
        if (merchantDetailsResponse.promotionalText.isNullOrBlank()) {
            mBinding.cvPromotionalBanner.isVisible = false
        } else {
            mBinding.cvPromotionalBanner.isVisible = true
            mBinding.tvPromotionalBanner.text = merchantDetailsResponse.promotionalText
        }
    }

    private fun setSpentAndGoalAmountData() {
//        mBinding.tvSpentAmount.text =
//            "YOU SPENT: " + "$" + merchantDetailsResponse.latestTxn?.amountSpent?.formatAsCurrency()
        mBinding.tvYouSpentAmount.text =
            "$" + merchantDetailsResponse.latestTxn?.amountSpent?.formatAsCurrency()

        var goalAmount = merchantDetailsResponse.latestTxn?.goalAmount?.toDoubleOrNull()
//        mBinding.tvGoal.text = "GOAL: " + "$" + goalAmount?.formatUsingCurrencySystem()
        mBinding.tvYourGoalAmount.text = "$" + goalAmount?.formatUsingCurrencySystem()

//        mBinding.progressBar.progress =
//            merchantDetailsResponse.latestTxn?.progressPercent?.toDoubleOrNull()?.roundToInt()
//                .getZeroIfNull()
    }

    private fun getOnYourWayData(): String {
        return if (merchantDetailsResponse.latestTxn?.processingAmount.isNullOrBlank()) {
//            mBinding.tvOnYourWay.text = "ON YOUR WAY: $0.00"
            "$0.00"
        } else {
//            mBinding.tvOnYourWay.text =
//                "ON YOUR WAY: " + "$" + merchantDetailsResponse.latestTxn?.processingAmount?.toDoubleOrNull()
//                    ?.formatUsingCurrencySystem()
            "$" + merchantDetailsResponse.latestTxn?.processingAmount?.toDoubleOrNull()
                ?.formatUsingCurrencySystem()
        }
    }

    private fun getYourCashRewardsData(): String {
        return if (merchantDetailsResponse.latestTxn?.cashbackAmount.isNullOrEmpty()) {
//            mBinding.tvYourCashRewards.text = "$0.00"
            "$0.00"
        } else {
//            mBinding.tvYourCashRewards.text =
//                "$" + merchantDetailsResponse.latestTxn?.cashbackAmount?.formatAsCurrency()
            "$" + merchantDetailsResponse.latestTxn?.cashbackAmount?.formatAsCurrency()
        }
    }

    private fun setSpendLimitData() {
        if (merchantDetailsResponse.activeCashbackSettings?.maturityAmount.isNullOrEmpty()) {
            mBinding.cvSpendLimit.isVisible = false
        } else {
            mBinding.cvSpendLimit.isVisible = true
            mBinding.tvSpendLimit.text =
                "$" + "${merchantDetailsResponse.activeCashbackSettings?.maturityAmount?.formatAsCurrency()}"
        }
    }

    private fun setEarnCashbackData() {
        if (merchantDetailsResponse.activeCashbackSettings?.cashbackType == 1) {
            if (merchantDetailsResponse.activeCashbackSettings?.cashbackAmount?.isEmpty() == true) {
                mBinding.tvCashbackAmount.text = "$0.00"
            } else {
                mBinding.tvCashbackAmount.text =
                    "$" + "${
                        merchantDetailsResponse.activeCashbackSettings?.cashbackAmount?.formatAsCurrency()
                            .getEmptyIfNull()
                    }"
            }
        } else {
            if (merchantDetailsResponse.activeCashbackSettings?.cashbackPercentage.isNullOrEmpty()) {
                mBinding.tvCashbackAmount.text = "$0.00"
            } else {
                mBinding.tvCashbackAmount.text =
                    "${merchantDetailsResponse.activeCashbackSettings?.cashbackPercentage}%"
            }

        }
    }


    private fun initUI() {
//        mBinding.mainCL.visibility = View.GONE
//        statusTV = findViewById(R.id.statusTV)
//        mBinding.backIV.visibility = View.VISIBLE

        mBinding.initOnClicks()
    }

    private fun ActivityMerchantDetails2Binding.initOnClicks() {
//        backIV.setOnClickListener {
//            onBackPressed()
//        }

        ivGoThere.setOnClickListener {
            openGoogleMap()
        }

        ivRequestCashback.setOnClickListener {

            // TODO: Do check qrToken here (right now it is not required here)
            val qrResponse = QRResponse(
                activeCashbackSettingsId = merchantDetailsResponse.activeCashbackSettings?.id,
//                activeCashBackSetting = merchantDetailsWithCustomerInfoResponse.active_cashback_settings,
                customerId = merchantDetailsResponse.customerId,
                merchantId = merchantDetailsResponse.id,
                qrCodeInBase64 = merchantDetailsResponse.qrImage,
                qrToken = ""
            )

//            val intent = Intent(applicationContext, CalculateCashBackActivity::class.java)
//            intent.putExtra("qrResponse", qrResponse)
//            intent.putExtra("requestFromCustomer", true)
//            startActivity(intent)
            Timber.d("Merchant details -> $merchantDetailsResponse")
            navigateToRequestCashbackActivity(qrResponse)

        }


    }

    private fun navigateToRequestCashbackActivity(qrResponse: QRResponse) {
        val intent = RequestCashbackActivity.getNewIntent(
            packageContext = this,
            qrResponse = qrResponse,
            merchantId = merchantDetailsResponse.id.getZeroIfNull(),
            merchantName = merchantDetailsResponse.name.getEmptyIfNull(),
            merchantUniqueId = merchantDetailsResponse.uniqueId.getEmptyIfNull()
        )
        startActivity(intent)
    }

    private fun openGoogleMap() {
        val gmmIntentUri = Uri.parse("google.navigation:q=$lat,$lng")
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
        mapIntent.setPackage("com.google.android.apps.maps")
        mapIntent.resolveActivity(packageManager)?.let {
            startActivity(mapIntent)
        }
    }

    private fun generateQR(encryptedContentsForQrCode: String?) {
        val gson = Gson()
        val userParams = HashMap<String, Any?>()
        userParams["customerId"] = merchantDetailsResponse.customerId
        userParams["activeCashBackSetting"] =
            merchantDetailsResponse.activeCashbackSettings
        userParams["merchantId"] = merchantDetailsResponse.id
        userParams["purchaseAmount"] = "0.00"
        userParams["finalQR"] = false

        val mWriter = MultiFormatWriter()
        try {
            val mMatrix =
                mWriter.encode(encryptedContentsForQrCode, BarcodeFormat.QR_CODE, 1000, 1000)
            val mEncoder = BarcodeEncoder()
            val mBitmap = mEncoder.createBitmap(mMatrix)
            mBinding.ivQrCode.setImageBitmap(mBitmap)
        } catch (e: WriterException) {
            e.printStackTrace()
        }

        Timber.d("Encrypted data -> $encryptedContentsForQrCode")

        val decryptedData = try {
            AES().decryptString(
                encryptedString = encryptedContentsForQrCode.getEmptyIfNull(),
                key = AppConstants.keyValue.toByteArray()
            )
        } catch (ex: Exception) {
            ""
        }
        Timber.d("Decrypted data -> $decryptedData")

        lifecycleScope.launch {

            val qrCode = ImageUtils().decodeImageFromBase64(
                context = this@MerchantDetailsActivity,
                base64String = encryptedContentsForQrCode
            )

        }
//        mBinding.qrIV.setImageBitmap(qrCode)

    }

}