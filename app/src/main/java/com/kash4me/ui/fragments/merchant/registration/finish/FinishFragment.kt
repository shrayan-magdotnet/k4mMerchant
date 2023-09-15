package com.kash4me.ui.fragments.merchant.registration.finish

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.material.appbar.AppBarLayout
import com.kash4me.R
import com.kash4me.data.models.merchant.cashback.CashbackResponseV2
import com.kash4me.data.models.merchant.cashback.CashbackType
import com.kash4me.databinding.FragmentMerchantFinishBinding
import com.kash4me.ui.activity.merchant.MerchantRegistrationActivity
import com.kash4me.ui.activity.merchant.merchant_dashboard.MerchantDashBoardActivity
import com.kash4me.utils.ImageUtils
import com.kash4me.utils.SessionManager
import com.kash4me.utils.extensions.getEmptyIfNull
import com.kash4me.utils.extensions.getNotAvailableIfEmptyOrNull
import com.kash4me.utils.extensions.getZeroIfNull
import com.kash4me.utils.extensions.toLong
import com.kash4me.utils.formatAmount


class FinishFragment : Fragment() {

    companion object {
        fun newInstance() = FinishFragment()
    }

    private val viewModel: FinishViewModel by lazy {
        ViewModelProvider(this)[FinishViewModel::class.java]
    }

    private var _binding: FragmentMerchantFinishBinding? = null
    private val mBinding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMerchantFinishBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val cashbackResponse = arguments?.get("cashBackResponse") as CashbackResponseV2

        setData(cashbackResponse)
        updateStepper(view)

        mBinding.finishBtn.setOnClickListener { completeRegistration() }

    }

    override fun onResume() {
        super.onResume()

        val appBarLayout: AppBarLayout =
            ((activity) as MerchantRegistrationActivity).findViewById(R.id.customAppBar)
        val ivBack: ImageView = appBarLayout.findViewById(R.id.backIV)
        ivBack.isVisible = true
        ivBack.setOnClickListener { ((activity) as MerchantRegistrationActivity).onBackPressed() }

    }

    private fun setData(cashbackResponse: CashbackResponseV2) {
        mBinding.apply {

            mBinding.cvMerchantCardPreview.apply {
                tvMerchantName.text = cashbackResponse.merchantDetails?.name
                updateCashbackData(cashbackResponse)

                val promotionalText = cashbackResponse.merchantDetails?.promotionalText
                if (promotionalText.isNullOrBlank()) {
                    cvPromotionalBanner.isVisible = false
                } else {
                    cvPromotionalBanner.isVisible = true
                    tvPromotionalBanner.text = promotionalText
                }

                tvAddress.text = cashbackResponse.merchantDetails?.address
                tvDistance.text = "XXX.XX km"
            }

            businessNameTV.text = cashbackResponse.merchantDetails?.name
            addressTV.text = cashbackResponse.merchantDetails?.address
            tvCountryBody.text = cashbackResponse.merchantDetails?.countryName
            tvPostalCodeBody.text = cashbackResponse.merchantDetails?.zipCode
            tvWebsite.text =
                cashbackResponse.merchantDetails?.website.getNotAvailableIfEmptyOrNull()
            if (cashbackResponse.merchantDetails?.promotionalText.isNullOrBlank()) {
                tvPromotionalTextBody.setText("N/A")
            } else {
                tvPromotionalTextBody.text = cashbackResponse.merchantDetails?.promotionalText
            }
            if (cashbackResponse.merchantDetails?.description.isNullOrBlank()) {
                tvDescriptionBody.setText("N/A")
            } else {
                tvDescriptionBody.text = cashbackResponse.merchantDetails?.description
            }
            phoneTV.text = cashbackResponse.merchantDetails?.mobileNo
            tvContactPerson.text = cashbackResponse.merchantDetails?.contactPerson
            if (cashbackResponse.merchantDetails?.headOfficeId.isNullOrBlank()) {
                headOfficeTV.setText("N/A")
            } else {
                headOfficeTV.text = cashbackResponse.merchantDetails?.headOfficeId.toString()
            }
            val tags = cashbackResponse.merchantDetails?.getTagNames()
            if (tags?.isEmpty() == true) {
                tvTagsBody.text = "N/A"
            } else {
                tvTagsBody.text = cashbackResponse.merchantDetails?.getTagNames()
            }

            val nameInitials =
                ImageUtils().getNameInitialsImage(cashbackResponse.merchantDetails?.name.getEmptyIfNull())

            if (!cashbackResponse.merchantDetails?.logo.isNullOrBlank()) {
                Glide.with(this@FinishFragment)
                    .load(cashbackResponse.merchantDetails?.logo)
                    .placeholder(nameInitials)
                    .error(nameInitials)
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    .into(mBinding.cvMerchantCardPreview.merchantLogo.ivLogo)
            } else {
                mBinding.cvMerchantCardPreview.merchantLogo.ivLogo.setImageDrawable(nameInitials)
            }

            if (cashbackResponse.cashbackType == CashbackType.FLAT.id) {

                spendLTV.setText(R.string.spend)
                getLTV.setText(R.string.get)
                tvSpend.setValue(
                    cashbackResponse.maturityAmount?.toDoubleOrNull().toLong().getZeroIfNull()
                )
                tvSpend.isVisible = true
                tvPercentage.isVisible = false

                tvGet.setValue(
                    cashbackResponse.cashbackAmount?.toDoubleOrNull().toLong().getZeroIfNull()
                )

            } else if (cashbackResponse.cashbackType == CashbackType.PERCENTAGE.id) {

                spendLTV.setText(R.string.percentage)
                getLTV.setText(R.string.limit)
                tvPercentage.text = cashbackResponse.cashbackPercentage?.formatAmount + "%"
                tvPercentage.isVisible = true
                tvSpend.isVisible = false

                tvGet.setValue(
                    cashbackResponse.maturityAmount?.toDoubleOrNull().toLong().getZeroIfNull()
                )

            }

        }
    }

    private fun updateCashbackData(cashbackResponse: CashbackResponseV2?) {
        mBinding.cvMerchantCardPreview.apply {
            if (cashbackResponse?.cashbackType == CashbackType.FLAT.id) {
                if (cashbackResponse.cashbackAmount.isNullOrBlank()) {
                    tvEarnCashbackCaption.visibility = View.GONE
                    tvEarnCashbackAmount.visibility = View.GONE
                } else {
                    val formattedAmount =
                        "$" + cashbackResponse.cashbackAmount.formatAmount.getEmptyIfNull()
                    tvEarnCashbackAmount.text = formattedAmount
                }
            } else {
                if (cashbackResponse?.cashbackPercentage.isNullOrEmpty()) {
                    tvEarnCashbackCaption.visibility = View.GONE
                    tvEarnCashbackAmount.visibility = View.GONE
                } else {
                    tvEarnCashbackAmount.text =
                        "${cashbackResponse?.cashbackPercentage}%"
                }

            }

            if (cashbackResponse?.cashbackType == CashbackType.FLAT.id) {

                if (cashbackResponse.maturityAmount.isNullOrEmpty()) {
                    tvSpendAmount.visibility = View.GONE
                } else {
                    tvSpendAmount.text =
                        "On Every $${cashbackResponse.maturityAmount?.formatAmount} Spent"
                }

            } else {

                if (cashbackResponse?.maturityAmount.isNullOrEmpty()) {
                    tvSpendAmount.visibility = View.GONE
                } else {
                    tvSpendAmount.text = "Earn on Every Purchase"
                }

            }

        }
    }

    private fun completeRegistration() {
        clearMerchantRegistrationCache()
        navigateToMerchantDashboard()
    }

    private fun navigateToMerchantDashboard() {
        val intent = MerchantDashBoardActivity.getNewIntent(
            packageContext = requireActivity(), isFreshLogin = false
        )
        activity?.startActivity(intent)
    }

    private fun clearMerchantRegistrationCache() {
        val sessionManager = SessionManager(requireActivity().applicationContext)
        sessionManager.clearMerchantBasicInfo()
        sessionManager.clearCashbackSettings()
    }

    private fun updateStepper(view: View) {


        val basicInfo = view.findViewById<ImageView>(R.id.imageView)
        val imageUpload = view.findViewById<ImageView>(R.id.iv_image_upload)
        val cashbackInfo = view.findViewById<ImageView>(R.id.imageView2)
        val finish = view.findViewById<ImageView>(R.id.imageView3)

        basicInfo.setImageDrawable(
            ContextCompat.getDrawable(
                requireContext(),
                R.drawable.ic_check
            )
        )
        imageUpload.setImageDrawable(
            ContextCompat.getDrawable(
                requireContext(),
                R.drawable.ic_check
            )
        )
        cashbackInfo.setImageDrawable(
            ContextCompat.getDrawable(
                requireContext(),
                R.drawable.ic_check
            )
        )
        imageUpload.setBackgroundResource(R.drawable.green_circle)
        cashbackInfo.setBackgroundResource(R.drawable.green_circle)
        finish.setBackgroundResource(R.drawable.green_circle)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}