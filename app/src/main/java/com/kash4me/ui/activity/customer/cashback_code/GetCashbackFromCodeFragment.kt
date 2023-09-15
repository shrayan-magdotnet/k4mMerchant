package com.kash4me.ui.activity.customer.cashback_code

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.kash4me.R
import com.kash4me.databinding.FragmentGetCashbackFromCodeBinding
import com.kash4me.repository.CustomerCashbackViewModel
import com.kash4me.ui.dialog.ErrorDialog
import com.kash4me.utils.custom_views.CustomProgressDialog
import com.kash4me.utils.extensions.getEmptyIfNull
import com.kash4me.utils.extensions.showToast
import com.kash4me.utils.network.Resource
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class GetCashbackFromCodeFragment : Fragment() {

    private var _binding: FragmentGetCashbackFromCodeBinding? = null
    private val mBinding get() = _binding!!

    private val mProgressBar by lazy { CustomProgressDialog(requireContext()) }

    private val mViewModel: CustomerCashbackViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGetCashbackFromCodeBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar(title = R.string.cash_back_code)

        btnGetCashbackListener()

    }

    private fun btnGetCashbackListener() {
        mBinding.btnGetCashback.setOnClickListener {

            val code = mBinding.etCode.text
            if (code.isNullOrBlank()) {
                showToast("Please enter code")
                return@setOnClickListener
            }

            claimCoupon(code = code.toString())

        }
    }

    private fun claimCoupon(code: String) {
        mViewModel.claimCoupon(coupon = code).observe(viewLifecycleOwner) {

            when (it) {
                is Resource.Failure -> {
                    mProgressBar.hide()
                    val errorDialog = ErrorDialog.getInstance(message = it.errorMsg)
                    errorDialog.show(activity?.supportFragmentManager!!, errorDialog.tag)
                }

                Resource.Loading -> {
                    mProgressBar.show()
                }

                is Resource.Success -> {
                    mProgressBar.hide()
                    // TODO: Send merchant name and purchase amount when navigating
                    val action = GetCashbackFromCodeFragmentDirections
                        .actionGetCashbackFromCodeFragmentToCashbackReceivedFragment(
                            code = code,
                            purchaseAmount = it.value.data?.purchaseAmount.getEmptyIfNull()
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
            setNavigationOnClickListener { (activity)?.finish() }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}