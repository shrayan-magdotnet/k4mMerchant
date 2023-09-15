package com.kash4me.ui.activity.customer.cashback_code

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.kash4me.merchant.R
import com.kash4me.merchant.databinding.FragmentCashbackReceivedBinding
import com.kash4me.utils.extensions.formatAsCurrency
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class CashbackReceivedFragment : Fragment() {

    private var _binding: FragmentCashbackReceivedBinding? = null
    private val mBinding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCashbackReceivedBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val args = arguments?.let { CashbackReceivedFragmentArgs.fromBundle(it) }
        mBinding.tvCode.text = args?.code
        val purchaseAmount = if (args?.purchaseAmount?.formatAsCurrency().isNullOrBlank()) {
            "N/A"
        } else {
            args?.purchaseAmount?.formatAsCurrency()
        }
        mBinding.tvCashbackAmount.text = "$$purchaseAmount"

        mBinding.btnDone.setOnClickListener { activity?.finish() }

        CoroutineScope(Dispatchers.Main).launch {

            delay(500)

            mBinding.ivSuccess.isVisible = true

            val animation = AnimationUtils.loadAnimation(requireContext(), R.anim.zoom_in)
            mBinding.ivSuccess.startAnimation(animation)

        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}