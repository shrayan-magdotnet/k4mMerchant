package com.kash4me.ui.activity.merchant.send_cashback_code

import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.kash4me.R
import com.kash4me.databinding.FragmentSendCashbackCodeBinding
import com.kash4me.ui.dialog.ErrorDialog
import com.kash4me.utils.custom_views.CustomProgressDialog
import com.kash4me.utils.extensions.clearError
import com.kash4me.utils.extensions.getEmptyIfNull
import com.kash4me.utils.network.Resource
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SendCashbackCodeFragment : Fragment() {

    private var _binding: FragmentSendCashbackCodeBinding? = null
    private val mBinding get() = _binding!!

    private val mProgressBar by lazy { CustomProgressDialog(requireContext()) }

    private val mViewModel: SendCashbackCodeViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSendCashbackCodeBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar(title = R.string.send_cash_back_code)

        mBinding.btnSend.setOnClickListener {

            val emailAddress = mBinding.tilEmailAddress.editText?.text
            mBinding.tilEmailAddress.clearError()
            if (emailAddress.isNullOrBlank()) {
                mBinding.tilEmailAddress.error = "Please enter an email address"
                return@setOnClickListener
            }

            val purchaseAmount = mBinding.tilPurchaseAmount.editText?.text
            mBinding.tilPurchaseAmount.clearError()
            if (purchaseAmount.isNullOrBlank()) {
                mBinding.tilPurchaseAmount.error = "Please input purchase amount"
                return@setOnClickListener
            }

            val amount = "$$purchaseAmount"
            val message = "Are you sure you want to send cash back code to $emailAddress"

            AlertDialog.Builder(requireContext())
                .setTitle(R.string.send_cash_back)
                .setMessage(message)
                .setPositiveButton(R.string.ok) { dialog, _ ->
                    dialog.dismiss()
                    sendCashbackCode(emailAddress, purchaseAmount)
                }
                .setNegativeButton(R.string.cancel) { dialog, _ -> dialog.dismiss() }
                .show()
        }


    }

    private fun sendCashbackCode(
        emailAddress: Editable,
        purchaseAmount: Editable
    ) {
        mViewModel.sendCashbackCoupon(
            emailAddress = emailAddress.toString(),
            purchaseAmount = purchaseAmount.toString()
        ).observe(viewLifecycleOwner) {

            when (it) {
                is Resource.Failure -> {
                    mProgressBar.hide()
                    val errorDialog = ErrorDialog.getInstance(message = it.errorMsg)
                    errorDialog.show(
                        activity?.supportFragmentManager!!, errorDialog.tag
                    )
                }

                Resource.Loading -> {
                    mProgressBar.show()
                }

                is Resource.Success -> {
                    mProgressBar.hide()
                    val action = SendCashbackCodeFragmentDirections
                        .actionSendCashbackCodeFragmentToCodeSentFragment(
                            emailAddress = emailAddress.toString(),
                            purchaseAmount = it.value.amount.getEmptyIfNull(),
                            cashbackCode = it.value.token.getEmptyIfNull()
                        )
                    findNavController().navigate(action)
                }
            }

        }
    }

    fun setupToolbar(@StringRes title: Int) {
        mBinding.toolbar.toolbar.apply {
            setTitle(title)
            navigationIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_back)
            setNavigationOnClickListener { (activity as SendCashbackCodeActivity).finish() }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}