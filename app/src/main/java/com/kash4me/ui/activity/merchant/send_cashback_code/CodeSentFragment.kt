package com.kash4me.ui.activity.merchant.send_cashback_code

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.kash4me.merchant.R
import com.kash4me.merchant.databinding.FragmentCodeSentBinding
import com.kash4me.utils.AppConstants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber

class CodeSentFragment : Fragment() {

    private var _binding: FragmentCodeSentBinding? = null
    private val mBinding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCodeSentBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val args = arguments?.let { CodeSentFragmentArgs.fromBundle(it) }
        Timber.d(
            "Email address -> ${args?.emailAddress} " + AppConstants.NEW_LINE +
                    "Purchase amount -> ${args?.purchaseAmount}" + AppConstants.NEW_LINE +
                    "Cashback code -> ${args?.cashbackCode}"
        )

        val codeGenerateSuccessMessage = resources.getString(
            R.string.code_generate_success, args?.emailAddress
        )

        mBinding.apply {
            tvCodeGenerateSuccess.text = codeGenerateSuccessMessage
            tvCode.text = args?.cashbackCode
            tvCashbackAmount.text = "$" + args?.purchaseAmount
            btnDone.setOnClickListener { activity?.finish() }
        }

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