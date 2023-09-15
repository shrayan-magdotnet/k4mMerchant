package com.kash4me.ui.fragments.customer.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.AppBarLayout
import com.kash4me.R
import com.kash4me.data.models.user.UserType
import com.kash4me.databinding.FragmentCustomerProfileBinding
import com.kash4me.network.ApiServices
import com.kash4me.network.NetworkConnectionInterceptor
import com.kash4me.network.NotFoundInterceptor
import com.kash4me.repository.UserDetailsRepository
import com.kash4me.repository.UserRepository
import com.kash4me.ui.activity.RegistrationActivity
import com.kash4me.ui.activity.about_app.AboutAppActivity
import com.kash4me.ui.activity.change_password.ChangePasswordActivity
import com.kash4me.ui.activity.common.FeedbackActivity
import com.kash4me.ui.activity.customer.cashback_code.GetCashbackFromCodeActivity
import com.kash4me.ui.activity.customer.customer_profile.CustomerProfileActivity
import com.kash4me.ui.activity.customer.info_box.InfoBoxActivity
import com.kash4me.ui.activity.login.LoginActivity
import com.kash4me.ui.activity.payment_gateway.PaymentSettingsActivity
import com.kash4me.ui.fragments.merchant.settings.SettingsActivity
import com.kash4me.utils.SessionManager
import com.kash4me.utils.TextDrawable
import com.kash4me.utils.extensions.showToast
import com.kash4me.utils.getNameInitials
import com.kash4me.utils.listeners.SingleParamItemClickListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber


class CustomerProfileFragment : Fragment() {

    companion object {
        fun newInstance() = CustomerProfileFragment()
    }

    private lateinit var viewModel: CustomerProfileViewModel

    private val sessionManager: SessionManager by lazy { SessionManager(requireContext()) }

    private var _binding: FragmentCustomerProfileBinding? = null
    private val mBinding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCustomerProfileBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    enum class CustomerProfileMenu(
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
        SETTINGS(
            icon = R.drawable.ic_settings,
            title = R.string.settings,
            activity = SettingsActivity::class.java
        ),
        HOW_IT_WORKS(
            icon = R.drawable.ic_contact_support,
            title = R.string.how_it_works,
            activity = null
        ),
        PAYMENT_DETAILS(
            icon = R.drawable.ic_money_100_bill,
            title = R.string.payment_details,
            activity = PaymentSettingsActivity::class.java
        ),

        //        LANGUAGE_SETTINGS(title = R.string.language_settings, activity = null),
        CHANGE_PASSWORD(
            icon = R.drawable.ic_change_password,
            title = R.string.change_password,
            activity = ChangePasswordActivity::class.java
        ),
        DEPOSIT_CASHBACK(
            icon = R.drawable.ic_money_circled,
            title = R.string.deposit_cashback,
            activity = GetCashbackFromCodeActivity::class.java
        ),
        FEEDBACK(
            icon = R.drawable.ic_email,
            title = R.string.send_feedback,
            activity = FeedbackActivity::class.java
        ),

        //        NOTIFICATION(title = R.string.notification, activity = null),
//        WHATS_NEW(title = R.string.whats_new, activity = null),
        LOG_OUT(icon = R.drawable.ic_logout, title = R.string.log_out, activity = null)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        showViewAccordingToUserType()

        val apiInterface =
            ApiServices.invoke(
                NetworkConnectionInterceptor(requireContext()), NotFoundInterceptor()
            )
        val userDetailsRepository = UserDetailsRepository(apiInterface)
        val userRepository = UserRepository(apiInterface)
        viewModel = ViewModelProvider(
            this, CustomerProfileViewModelFactory(userDetailsRepository, userRepository)
        )[CustomerProfileViewModel::class.java]


        // getting the recyclerview by its id
        val recyclerview = view.findViewById<RecyclerView>(R.id.profileRV)

        // this creates a vertical layout Manager
        recyclerview.layoutManager = LinearLayoutManager(activity)
        val adapter = CustomerProfileAdapter(
            profileMenuList = CustomerProfileMenu.values().toList(),
            clickListener = object : SingleParamItemClickListener<CustomerProfileMenu> {
                override fun onClick(item: CustomerProfileMenu) {

                    when (item) {
                        CustomerProfileMenu.LOG_OUT -> {
                            lifecycleScope.launch {
                                withContext(Dispatchers.IO) {
                                    SessionManager(requireContext()).logoutUser(packageContext = requireActivity())
                                }
                            }
                        }
//                        CustomerProfileMenu.LANGUAGE_SETTINGS -> {
//                            val dialog =
//                                SelectLanguageDialog.newInstance(languageChangeListener = object :
//                                    SelectLanguageDialog.LanguageChangeListener {
//                                    override fun onLanguageChange(selectedLanguage: LocaleManager.Language?) {
//
//                                        lifecycleScope.launch {
//                                            if (selectedLanguage != null) {
//                                                LocaleManager.setLocale(language = selectedLanguage)
//                                            }
//                                        }
//
////                                        val intent = SplashActivity.getNewIntent(
////                                            packageContext = requireActivity()
////                                        )
////                                        startActivity(intent)
//                                    }
//                                })
//                            dialog.show(activity?.supportFragmentManager!!, dialog.tag)
//                        }
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
            })

        // Setting the Adapter with the recyclerview
        recyclerview.adapter = adapter

        initView()

    }

    private fun showViewAccordingToUserType() {
        val userType = sessionManager.fetchUserType()
        if (userType == UserType.ANONYMOUS) {
            mBinding.apply {

                layoutAccountCreationPrompt.root.isVisible = true
                clMainLayout.isVisible = false

                layoutAccountCreationPrompt.apply {

                    tvTitle.text = "Want to Create your profile?"
                    tvDescription.text =
                        "Create your profile by signing up with us so that you can start earning from every purchase you make."

                    btnLogin.setOnClickListener {
                        val intent = Intent(requireActivity(), LoginActivity::class.java)
                        startActivity(intent)
                    }

                    btnSignup.setOnClickListener {
                        val intent = Intent(requireActivity(), RegistrationActivity::class.java)
                        startActivity(intent)
                    }

                }

            }
        } else {
            mBinding.layoutAccountCreationPrompt.root.isVisible = false
            mBinding.clMainLayout.isVisible = true
        }
    }

    override fun onStart() {
        super.onStart()
        Timber.d("onStart")
    }

    private fun initView() {
        mBinding.viewProfileTV.setOnClickListener { goToCustomerProfileActivity() }
    }

    override fun onResume() {
        super.onResume()

        Timber.d("Customer details -> ${sessionManager.fetchCustomerDetails()}")
        mBinding.nameTV.text = sessionManager.fetchCustomerDetails()?.nick_name
        setInitials()

        val appBar: AppBarLayout? = (activity)?.findViewById(R.id.customAppBar)
        val ivRefresh: ImageButton? = appBar?.findViewById(R.id.iv_refresh)
        ivRefresh?.visibility = View.INVISIBLE

    }

    private fun setInitials() {
        val drawable = TextDrawable.builder()
            .buildRect(
                getNameInitials(sessionManager.fetchCustomerDetails()?.nick_name ?: "")
            )
        mBinding.profileIV.setImageDrawable(drawable)
    }


    private fun goToCustomerProfileActivity() {
        val intent = Intent(activity, CustomerProfileActivity::class.java)
        activity?.startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}