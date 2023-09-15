package com.kash4me.ui.activity.customer.request_cashback

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.kash4me.R
import com.kash4me.data.models.request.RequestCashbackQrRequest
import com.kash4me.databinding.FragmentRequestCashbackAmountBinding
import com.kash4me.ui.fragments.customer.home.CustomerHomeViewModel
import com.kash4me.utils.AppConstants
import com.kash4me.utils.extensions.getAmount
import com.kash4me.utils.extensions.getEmptyIfNull
import com.kash4me.utils.extensions.showToast
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber


@AndroidEntryPoint
class RequestCashbackAmountFragment : Fragment() {

    private var _binding: FragmentRequestCashbackAmountBinding? = null
    private val mBinding get() = _binding!!

    val mViewModel: CustomerHomeViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRequestCashbackAmountBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val merchantShopId = (activity)?.intent?.getIntExtra(
            RequestCashbackActivity.MERCHANT_ID, AppConstants.MINUS_ONE
        )

        val merchantUniqueId = (activity)?.intent?.getStringExtra(
            RequestCashbackActivity.MERCHANT_UNIQUE_ID
        ).getEmptyIfNull()

        val merchantName = (activity)?.intent?.getStringExtra(
            RequestCashbackActivity.MERCHANT_NAME
        ).getEmptyIfNull()

        Timber.d("Merchant name -> $merchantName | Merchant Code -> $merchantUniqueId")

        mBinding.tvMerchantName.text = merchantName
        mBinding.tvMerchantCode.text = merchantUniqueId

        mBinding.btnRequest.setOnClickListener {

            val purchaseAmount = mBinding.etTotalPurchaseAmount.getAmount()

            if (purchaseAmount < 0.1) {
                showToast(R.string.ensure_this_value_is_greater_than_or_equal_to_0_point_1)
                return@setOnClickListener
            }

            Timber.d("Let's request cashback for purchase amount -> $purchaseAmount")
            val request = RequestCashbackQrRequest(amount = purchaseAmount, shopId = merchantShopId)

            val action = RequestCashbackAmountFragmentDirections
                .actionRequestCashbackAmountFragmentToConfirmRequestingCashbackFragment(
                    merchantName = merchantName,
                    merchantUniqueId = merchantUniqueId,
                    request = request
                )
            findNavController().navigate(action)

        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}