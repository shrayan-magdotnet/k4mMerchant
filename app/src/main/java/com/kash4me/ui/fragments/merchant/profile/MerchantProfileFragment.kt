package com.kash4me.ui.fragments.merchant.profile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.kash4me.R
import com.kash4me.data.models.merchant.cashback.CashbackType
import com.kash4me.data.models.merchant.profile.MerchantProfileResponse
import com.kash4me.databinding.FragmentMerchantProfileBinding
import com.kash4me.network.ApiServices
import com.kash4me.network.NetworkConnectionInterceptor
import com.kash4me.network.NotFoundInterceptor
import com.kash4me.repository.UserDetailsRepository
import com.kash4me.repository.UserRepository
import com.kash4me.ui.activity.about_app.AboutAppActivity
import com.kash4me.ui.activity.change_password.ChangePasswordActivity
import com.kash4me.ui.activity.common.FeedbackActivity
import com.kash4me.ui.activity.customer.info_box.InfoBoxActivity
import com.kash4me.ui.activity.merchant.cashback_setting.CashBackSettingActivity
import com.kash4me.ui.activity.merchant.image_settings.ImageSettingsActivity
import com.kash4me.ui.activity.merchant.merchant_profile.MerchantProfileActivity
import com.kash4me.ui.activity.merchant.merchant_profile.MerchantProfileViewModel
import com.kash4me.ui.activity.merchant.merchant_profile.MerchantProfileViewModelFactory
import com.kash4me.ui.activity.merchant.send_cashback_code.SendCashbackCodeActivity
import com.kash4me.ui.activity.payment_gateway.PaymentSettingsActivity
import com.kash4me.ui.fragments.merchant.announcement.CreateAnnouncementActivity
import com.kash4me.ui.fragments.merchant.settings.SettingsActivity
import com.kash4me.ui.fragments.merchant.sub_user_settings.SubUserSettingsActivity
import com.kash4me.utils.App
import com.kash4me.utils.ImageUtils
import com.kash4me.utils.SessionManager
import com.kash4me.utils.TextDrawable
import com.kash4me.utils.extensions.copyTextToClipboard
import com.kash4me.utils.extensions.getEmptyIfNull
import com.kash4me.utils.extensions.getNotAvailableIfEmptyOrNull
import com.kash4me.utils.extensions.showToast
import com.kash4me.utils.formatAmount
import com.kash4me.utils.getNameInitials
import com.kash4me.utils.listeners.SingleParamItemClickListener
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class MerchantProfileFragment : Fragment() {

    companion object {
        fun newInstance() = MerchantProfileFragment()
    }

    private val mViewModel by lazy { initViewModel() }

    private fun initViewModel(): MerchantProfileViewModel {
        val apiInterface = ApiServices.invoke(
            NetworkConnectionInterceptor(App.getContext()!!), NotFoundInterceptor()
        )
        val userDetailsRepository = UserDetailsRepository(apiInterface)
        val userRepository = UserRepository(apiInterface)
        return ViewModelProvider(
            this, MerchantProfileViewModelFactory(userDetailsRepository, userRepository)
        )[MerchantProfileViewModel::class.java]
    }

    @Inject
    lateinit var sessionManager: SessionManager

    private var _binding: FragmentMerchantProfileBinding? = null
    private val mBinding get() = _binding!!

    override fun onAttach(context: Context) {
        try {
            super.onAttach(context)
        } catch (ex: Exception) {
            Timber.d("Caught exception -> ${ex.message}")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMerchantProfileBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerview = view.findViewById<RecyclerView>(R.id.profileRV)

        recyclerview.layoutManager = LinearLayoutManager(activity)

        val adapter = MerchantProfileAdapter(
            profileMenuList = MerchantProfileMenu.values().toList(),
            clickListener = getMenuClickListener()
        )

        recyclerview.adapter = adapter

        initView()

    }

    enum class MerchantProfileMenu(
        @DrawableRes val icon: Int,
        @StringRes val title: Int,
        val activity: Class<*>?
    ) {

        ABOUT(
            icon = R.drawable.ic_account_outlined,
            title = R.string.about,
            activity = AboutAppActivity::class.java
        ),
        INFOBOX(
            icon = R.drawable.ic_email,
            title = R.string.infobox,
            activity = InfoBoxActivity::class.java
        ),
        IMAGE_SETTINGS(
            icon = R.drawable.ic_insert_photo,
            title = R.string.image_settings, activity = ImageSettingsActivity::class.java
        ),
        CASHBACK_SETTINGS(
            icon = R.drawable.ic_money_outlined,
            title = R.string.cashback_settings, activity = CashBackSettingActivity::class.java
        ),
        SEND_CASHBACK_CODE(
            icon = R.drawable.ic_money_circled,
            title = R.string.send_cashback_code,
            activity = SendCashbackCodeActivity::class.java
        ),
        ANNOUNCEMENT(
            icon = R.drawable.ic_announcement,
            title = R.string.announcement,
            activity = CreateAnnouncementActivity::class.java
        ),
        SUB_USER_SETTINGS(
            icon = R.drawable.ic_manage_account,
            title = R.string.sub_user_settings,
            activity = SubUserSettingsActivity::class.java
        ),
        PAYMENT_DETAILS(
            icon = R.drawable.ic_money_100_bill,
            title = R.string.payment_details,
            activity = PaymentSettingsActivity::class.java
        ),
        HOW_IT_WORKS(
            icon = R.drawable.ic_contact_support,
            title = R.string.how_it_works,
            activity = null
        ),
        SETTINGS(
            icon = R.drawable.ic_settings,
            title = R.string.settings,
            activity = SettingsActivity::class.java
        ),

        //        LANGUAGE_SETTINGS(title = R.string.language_settings, activity = null),
        CHANGE_PASSWORD(
            icon = R.drawable.ic_change_password,
            title = R.string.change_password,
            activity = ChangePasswordActivity::class.java
        ),
        FEEDBACK(
            icon = R.drawable.ic_email,
            title = R.string.send_feedback,
            activity = FeedbackActivity::class.java
        ),
        LOG_OUT(icon = R.drawable.ic_logout, title = R.string.log_out, activity = null)

    }

    private fun getMenuClickListener() =
        object : SingleParamItemClickListener<MerchantProfileMenu> {
            override fun onClick(item: MerchantProfileMenu) {

                when (item) {
                    MerchantProfileMenu.LOG_OUT -> {
                        lifecycleScope.launch {
                            withContext(Dispatchers.IO) {
                                SessionManager(requireActivity()).logoutUser(packageContext = requireActivity())
                            }
                        }
                    }
//                    MerchantProfileMenu.LANGUAGE_SETTINGS -> {
//                        val dialog =
//                            SelectLanguageDialog.newInstance(languageChangeListener = object :
//                                SelectLanguageDialog.LanguageChangeListener {
//                                override fun onLanguageChange(selectedLanguage: LocaleManager.Language?) {
//                                    val intent = SplashActivity.getNewIntent(
//                                        packageContext = requireActivity()
//                                    )
//                                    startActivity(intent)
//                                }
//                            })
//                        dialog.show(activity?.supportFragmentManager!!, dialog.tag)
//                    }
                    else -> {

                        if (item.activity != null) {
                            val intent = Intent(requireActivity(), item.activity)
                            requireActivity().startActivity(intent)
                        } else {
                            showToast(getString(R.string.coming_soon))
                        }

                    }
                }

            }
        }

    private fun initView() {
//        mBinding.viewProfileTV.setOnClickListener { navigateToMerchantProfileActivity() }
        mBinding.ownMerchantCard.root.setOnClickListener { navigateToMerchantProfileActivity() }
    }

    override fun onResume() {
        super.onResume()
        mBinding.nameTV.text = sessionManager.fetchMerchantDetails()?.name
        setInitials()

        updateMerchantCardDetails()
        Timber.d("Merchant Details -> ${sessionManager.fetchMerchantDetails()}")
        mBinding.tvOfficeId.text =
            sessionManager.fetchMerchantDetails()?.uniqueOfficeId.getNotAvailableIfEmptyOrNull()
        mBinding.cvBusinessId.setOnClickListener {
            copyTextToClipboard(text = mBinding.tvOfficeId.text.toString())
            showToast(message = getString(R.string.copied_to_clipboard))
        }

    }


    private fun updateMerchantCardDetails() {

        val merchantDetails = sessionManager.fetchMerchantDetails()

        mBinding.ownMerchantCard.apply {

            Timber.d("Merchant logo -> ${merchantDetails?.logo}")
            val nameInitials =
                ImageUtils().getNameInitialsImage(merchantDetails?.name.getNotAvailableIfEmptyOrNull())

            if (merchantDetails?.logo.isNullOrBlank()) {
                Timber.d("Merchant name -> ${merchantDetails?.logo}")
                merchantLogo.ivLogo.setImageDrawable(nameInitials)
            } else {
                Glide.with(mBinding.root)
                    .load(merchantDetails?.logo)
                    .placeholder(nameInitials)
                    .error(nameInitials)
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    .into(merchantLogo.ivLogo)
            }

            tvMerchantName.text = merchantDetails?.name
            updateCashbackData(merchantDetails)

            val promotionalText = merchantDetails?.promotionalText
            if (promotionalText.isNullOrBlank()) {
                cvPromotionalBanner.isVisible = false
            } else {
                cvPromotionalBanner.isVisible = true
                tvPromotionalBanner.text = promotionalText
            }

            tvAddress.text = merchantDetails?.address
            tvDistance.text = "XXX.XX km"
        }

    }

    private fun updateCashbackData(merchantDetails: MerchantProfileResponse?) {
        mBinding.ownMerchantCard.apply {
            if (merchantDetails?.activeCashbackSettings?.cashbackType == CashbackType.FLAT.id) {
                if (merchantDetails.activeCashbackSettings.cashbackAmount.isNullOrBlank()) {
                    tvEarnCashbackCaption.visibility = View.GONE
                    tvEarnCashbackAmount.visibility = View.GONE
                } else {
                    val formattedAmount =
                        "$" + merchantDetails.activeCashbackSettings.cashbackAmount.formatAmount.getEmptyIfNull()
                    tvEarnCashbackAmount.text = formattedAmount
                }
            } else {
                if (merchantDetails?.activeCashbackSettings?.cashbackPercentage.isNullOrEmpty()) {
                    tvEarnCashbackCaption.visibility = View.GONE
                    tvEarnCashbackAmount.visibility = View.GONE
                } else {
                    tvEarnCashbackAmount.text =
                        "${merchantDetails?.activeCashbackSettings?.cashbackPercentage}%"
                }

            }

            if (merchantDetails?.activeCashbackSettings?.cashbackType == CashbackType.FLAT.id) {

                if (merchantDetails.activeCashbackSettings.maturityAmount.isNullOrEmpty()) {
                    tvSpendAmount.visibility = View.GONE
                } else {
                    tvSpendAmount.text =
                        "On Every $${merchantDetails?.activeCashbackSettings?.maturityAmount?.formatAmount} Spent"
                }

            } else {

                if (merchantDetails?.activeCashbackSettings?.maturityAmount.isNullOrEmpty()) {
                    tvSpendAmount.visibility = View.GONE
                } else {
                    tvSpendAmount.text = "Earn on Every Purchase"
                }

            }

        }
    }

    private fun setInitials() {
        val drawable = TextDrawable.builder()
            .buildRect(
                getNameInitials(sessionManager.fetchMerchantDetails()?.name ?: "")
            )
        mBinding.profileIV.setImageDrawable(drawable)
    }


    private fun navigateToMerchantProfileActivity() {
//        val intent = Intent(activity, MerchantUserDetailsActivity::class.java)
        val intent = Intent(activity, MerchantProfileActivity::class.java)
        activity?.startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}