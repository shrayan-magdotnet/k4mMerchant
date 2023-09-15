package com.kash4me.ui.activity.customer.request_cashback

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.kash4me.databinding.FragmentConfirmRequestingCashbackBinding
import com.kash4me.utils.extensions.formatUsingCurrencySystem
import com.kash4me.utils.extensions.showToast

class ConfirmRequestingCashbackFragment : Fragment() {

    private var _binding: FragmentConfirmRequestingCashbackBinding? = null
    private val mBinding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentConfirmRequestingCashbackBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val args = arguments?.let { ConfirmRequestingCashbackFragmentArgs.fromBundle(it) }

        setData(args)
        btnConfirmListener(args)

    }

    private fun setData(args: ConfirmRequestingCashbackFragmentArgs?) {
        mBinding.apply {

            tvMerchantName.text = args?.merchantName
            tvMerchantUniqueId.text = args?.merchantUniqueId
            val purchaseAmount = "$" + args?.request?.amount?.formatUsingCurrencySystem()
            tvPurchaseAmount.text = purchaseAmount

        }
    }

    private fun btnConfirmListener(args: ConfirmRequestingCashbackFragmentArgs?) {
        mBinding.btnConfirm.setOnClickListener {

            val requestCashbackRequest = args?.request
            if (requestCashbackRequest == null) {
                showToast("Couldn't request cashback")
                return@setOnClickListener
            }

            val action = ConfirmRequestingCashbackFragmentDirections
                .actionConfirmRequestingCashbackFragmentToGenerateQrCodeForCashbackFragment(
                    request = requestCashbackRequest
                )
            findNavController().navigate(action)

        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}