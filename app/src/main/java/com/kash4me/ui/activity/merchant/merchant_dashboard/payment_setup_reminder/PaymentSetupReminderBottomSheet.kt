package com.kash4me.ui.activity.merchant.merchant_dashboard.payment_setup_reminder

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.kash4me.databinding.BottomSheetSetupPaymentBinding
import com.kash4me.ui.activity.payment_gateway.PaymentSettingsActivity


class PaymentSetupReminderBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetSetupPaymentBinding? = null
    private val mBinding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetSetupPaymentBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mBinding.btnGoToProfile.setOnClickListener {

            val intent = Intent(context, PaymentSettingsActivity::class.java)
            startActivity(intent)

            dialog?.dismiss()

        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}