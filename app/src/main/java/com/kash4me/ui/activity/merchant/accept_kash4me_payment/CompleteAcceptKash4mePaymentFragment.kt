package com.kash4me.ui.activity.merchant.accept_kash4me_payment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.kash4me.R
import com.kash4me.databinding.FragmentCompleteAcceptKash4mePaymentBinding
import com.kash4me.utils.extensions.formatUsingCurrencySystem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class CompleteAcceptKash4mePaymentFragment : Fragment() {

    private var _binding: FragmentCompleteAcceptKash4mePaymentBinding? = null
    private val mBinding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCompleteAcceptKash4mePaymentBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()

        val args = arguments?.let { CompleteAcceptKash4mePaymentFragmentArgs.fromBundle(it) }
        setDataInUi(args)

        mBinding.btnDone.setOnClickListener { activity?.finish() }

        CoroutineScope(Dispatchers.Main).launch {

            delay(500)

            mBinding.ivSuccess.isVisible = true

            val animation = AnimationUtils.loadAnimation(requireContext(), R.anim.zoom_in)
            mBinding.ivSuccess.startAnimation(animation)

        }

    }

    private fun setDataInUi(args: CompleteAcceptKash4mePaymentFragmentArgs?) {

        mBinding.tvTransactionId.text = args?.acceptPaymentResponse?.paymentTransactionId

        val purchaseAmount = "$" + args?.actualAmount?.toDoubleOrNull()?.formatUsingCurrencySystem()
        mBinding.tvPurchaseAmount.text = purchaseAmount

        val transactionFee = "$${args?.acceptPaymentResponse?.txnFee}"
        mBinding.tvTransactionFee.text = transactionFee

        mBinding.tvTransactionFeeCaption.text =
            "Transaction Fee ${args?.acceptPaymentResponse?.comissionPercentage}%"

        val amountProcessed = "$" + args?.acceptPaymentResponse?.transactionAmount?.toDoubleOrNull()
            ?.formatUsingCurrencySystem()
        mBinding.tvAmountProcessed.text = amountProcessed

    }

    private fun setupToolbar() {
        mBinding.toolbar.toolbar.apply {
            navigationIcon = ContextCompat.getDrawable(
                requireContext(), R.drawable.ic_close
            )
            setNavigationOnClickListener { activity?.finish() }
            title = "Payment Complete"
            subtitle = ""
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}