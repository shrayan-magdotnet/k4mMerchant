package com.kash4me.ui.activity.customer.announcements

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.kash4me.data.models.customer.annoucement.AnnouncementsResponse
import com.kash4me.data.models.user.UserType
import com.kash4me.databinding.FragmentAnnouncementsBinding
import com.kash4me.ui.activity.RegistrationActivity
import com.kash4me.ui.activity.customer.merchant_details.MerchantDetailsActivity
import com.kash4me.ui.activity.login.LoginActivity
import com.kash4me.utils.SessionManager
import com.kash4me.utils.custom_views.CustomProgressDialog
import com.kash4me.utils.extensions.getEmptyIfNull
import com.kash4me.utils.extensions.getZeroIfNull
import com.kash4me.utils.listeners.SingleParamItemClickListener
import com.kash4me.utils.network.Resource
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class AnnouncementsFragment : Fragment() {

    private var _binding: FragmentAnnouncementsBinding? = null
    private val mBinding get() = _binding!!

    private val mAnnouncementAdapter by lazy { AnnouncementsAdapter(clickListener = mAnnouncementClickListener) }
    private val mAnnouncementClickListener
        get() = object : SingleParamItemClickListener<AnnouncementsResponse.Result?> {

            override fun onClick(item: AnnouncementsResponse.Result?) {
                val intent = MerchantDetailsActivity.getNewIntent(
                    packageContext = requireActivity(),
                    merchantId = item?.merchantId.getZeroIfNull(),
                    merchantName = item?.merchantName.getEmptyIfNull()
                )
                startActivity(intent)
            }

        }

    private val mProgressDialog by lazy { CustomProgressDialog(requireContext()) }

    @Inject
    lateinit var mSessionManager: SessionManager

    private val mViewModel: AnnouncementsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAnnouncementsBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        showViewAccordingToUserType()

        initRvAnnouncements()
        if (mSessionManager.fetchUserType() == UserType.CUSTOMER) {
            fetchAnnouncements(searchQuery = "")
        }
        tilSearchTransactionsListener()

    }

    private fun showViewAccordingToUserType() {
        val userType = mSessionManager.fetchUserType()
        if (userType == UserType.ANONYMOUS) {
            mBinding.apply {

                layoutAccountCreationPrompt.root.isVisible = true
                clMainLayout.isVisible = false

                layoutAccountCreationPrompt.apply {

                    tvTitle.text = "Want to Check the Announcements?"
                    tvDescription.text =
                        "By signing up with us, you can find out which store has what announcements"

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


    private fun initRvAnnouncements() {
        mBinding.rvAnnouncements.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = mAnnouncementAdapter
        }
    }

    private fun fetchAnnouncements(searchQuery: String) {
        mViewModel.getAnnouncements(searchQuery = searchQuery).observe(viewLifecycleOwner) {

            when (it) {
                is Resource.Failure -> {
                    mProgressDialog.hide()
                    showEmptyState(message = it.errorMsg)
                }

                Resource.Loading -> {
                    mProgressDialog.show()
                }

                is Resource.Success -> {
                    mProgressDialog.hide()
                    Timber.d("Success -> ${it.value.results}")
                    if (it.value.results.isNullOrEmpty()) {

                        showEmptyState(message = "No Announcements Found")

                    } else {

                        makeRvVisible(announcements = it.value.results)

                    }
                }
            }

        }
    }

    private fun showEmptyState(message: String) {
        mBinding.apply {
            rvAnnouncements.isVisible = false

            emptyState.root.isVisible = true
            emptyState.tvTitle.text = message
        }
    }

    private fun makeRvVisible(announcements: List<AnnouncementsResponse.Result?>) {
        mBinding.apply {
            rvAnnouncements.isVisible = true
            mAnnouncementAdapter.setData(announcements = announcements)

            emptyState.root.isVisible = false
        }
    }

    private fun tilSearchTransactionsListener() {

        mBinding.tilFindAnnouncement.editText?.doAfterTextChanged {
            if (it.isNullOrBlank()) {
                fetchAnnouncements(searchQuery = "")
            }
        }

        mBinding.tilFindAnnouncement.editText?.setOnEditorActionListener { textView, actionId, keyEvent ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val userQuery = mBinding.tilFindAnnouncement.editText?.text
                if (userQuery.isNullOrBlank()) {
                    fetchAnnouncements(searchQuery = "")
                } else {
                    fetchAnnouncements(searchQuery = userQuery.toString())
                }
                true
            } else false
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}