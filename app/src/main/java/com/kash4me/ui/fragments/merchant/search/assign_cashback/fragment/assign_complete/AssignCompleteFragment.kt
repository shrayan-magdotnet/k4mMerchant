package com.kash4me.ui.fragments.merchant.search.assign_cashback.fragment.assign_complete

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.core.content.edit
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.kash4me.R
import com.kash4me.databinding.FragmentAssignCompleteBinding
import com.kash4me.ui.fragments.merchant.search.MerchantSearchFragment
import com.kash4me.ui.fragments.merchant.search.assign_cashback.AssignCashbackActivity
import com.kash4me.utils.SessionManager
import com.kash4me.utils.custom_views.CustomProgressDialog
import com.kash4me.utils.extensions.getEmptyIfNull
import com.kash4me.utils.extensions.getZeroIfNull
import com.kash4me.utils.extensions.toLong
import com.kash4me.utils.network.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber


@AndroidEntryPoint
class AssignCompleteFragment : Fragment() {

    private val progressDialog by lazy { CustomProgressDialog(requireContext()) }

    private var sharedPreferences: SharedPreferences? = null

    private val viewModel: AssignCompleteViewModel by viewModels()

    private var _binding: FragmentAssignCompleteBinding? = null
    private val mBinding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAssignCompleteBinding.inflate(inflater, container, false)
        return mBinding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()

        sharedPreferences = context?.getSharedPreferences(
            requireActivity().getString(R.string.first_start_status), Context.MODE_PRIVATE
        )

        val sessionManager = SessionManager(context = view.context)
        mBinding.btnDone.setOnClickListener {

            val shouldRefreshCustomersList =
                sharedPreferences?.getBoolean(MerchantSearchFragment.MERCHANT_SEARCH, false)
            Timber.d("Should refresh customer list -> $shouldRefreshCustomersList")

            sharedPreferences?.edit {
                putBoolean(MerchantSearchFragment.MERCHANT_SEARCH, true)
                Timber.d("Let's set should refresh Merchant Search to true")
            }

            updateTransactionSummary(sessionManager)
        }

        val args = arguments?.let { AssignCompleteFragmentArgs.fromBundle(it) }
        Timber.d("Details -> $args")

        mBinding.tvPurchaseAmount.setValue(
            args?.purchaseAmount?.toDoubleOrNull().toLong().getZeroIfNull()
        )
        mBinding.tvCashbackAmount.setValue(
            args?.cashbackAmount?.toDoubleOrNull().toLong().getZeroIfNull()
        )
        val message = resources.getString(
            R.string.thank_you_for_using_kash4me_your_cash_back_has_been_successfully_assigned_to_jacky_man_bxt100,
            args?.customerName,
            args?.customerCode
        )
        mBinding.tvMessage.text = message

        CoroutineScope(Dispatchers.Main).launch {

            delay(500)

            mBinding.ivSuccess.isVisible = true

            val animation = AnimationUtils.loadAnimation(requireContext(), R.anim.zoom_in)
            mBinding.ivSuccess.startAnimation(animation)

        }

    }

    private fun updateTransactionSummary(sessionManager: SessionManager) {
        viewModel.updateTransactionSummaryCache(
            token = sessionManager.fetchAuthToken().getEmptyIfNull()
        ).observe(viewLifecycleOwner) {

            when (it) {

                is Resource.Failure -> {
                    progressDialog.hide()
                    (activity as AssignCashbackActivity).finish()
                }

                Resource.Loading -> progressDialog.show()

                is Resource.Success -> {
                    progressDialog.hide()
                    (activity as AssignCashbackActivity).finish()
                }

            }

        }
    }

    private fun setupToolbar() {
        (activity as AssignCashbackActivity).setSupportActionBar(mBinding.toolbarLayout.toolbar)
        (activity as AssignCashbackActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        (activity as AssignCashbackActivity).supportActionBar?.title = "Assign Complete"
        mBinding.toolbarLayout.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}