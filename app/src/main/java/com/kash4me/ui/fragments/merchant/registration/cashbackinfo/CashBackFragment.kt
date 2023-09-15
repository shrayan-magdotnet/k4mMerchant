package com.kash4me.ui.fragments.merchant.registration.cashbackinfo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RadioGroup
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.textfield.TextInputLayout
import com.kash4me.data.models.merchant.cashback.CashbackResponseV2
import com.kash4me.data.models.merchant.cashback.CashbackType
import com.kash4me.merchant.R
import com.kash4me.merchant.databinding.FragmentCashBackBinding
import com.kash4me.network.ApiServices
import com.kash4me.network.NetworkConnectionInterceptor
import com.kash4me.network.NotFoundInterceptor
import com.kash4me.repository.CashBackRepository
import com.kash4me.ui.activity.merchant.MerchantRegistrationActivity
import com.kash4me.utils.SessionManager
import com.kash4me.utils.extensions.getAmount
import com.kash4me.utils.extensions.getAmountInDouble
import com.kash4me.utils.extensions.getZeroIfNull
import com.kash4me.utils.extensions.showToast
import com.kash4me.utils.formatAmount
import com.kash4me.utils.toast


class CashBackFragment : Fragment() {

    private var selectedRBId: Int = 0

    private lateinit var sessionManager: SessionManager
    private var merchantId: Int = 0

    private val selectedTags = arrayListOf<String>()

    private var _binding: FragmentCashBackBinding? = null
    private val mBinding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCashBackBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    private val customDialog by lazy {
        ((activity) as MerchantRegistrationActivity).customDialogClass
    }

    companion object {

        private const val TAG = "CashBackFragment"

        fun newInstance() = CashBackFragment()
    }

    private lateinit var viewModel: CashBackViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        updateStepper(view)
        mBinding.inputFieldsTextChangeListener()

        val apiInterface =
            ApiServices.invoke(
                NetworkConnectionInterceptor(requireContext().applicationContext),
                NotFoundInterceptor()
            )

        val cashBackRepository = CashBackRepository(apiInterface, SessionManager(requireContext()))

        viewModel = ViewModelProvider(
            this,
            CashBackViewModelFactory(cashBackRepository)
        )[CashBackViewModel::class.java]

        sessionManager = SessionManager(requireActivity().applicationContext)
        merchantId = sessionManager.fetchMerchantBasicInfo()?.business_info?.shop_id.getZeroIfNull()


        val cashBackRG = view.findViewById<RadioGroup>(R.id.cashBackRG)

        val spendAndGetLL = view.findViewById<LinearLayout>(R.id.spendAndGetLL)
        val percentageAndLimitLL = view.findViewById<LinearLayout>(R.id.percentageAndLimitLL)
        val percentET = view.findViewById<TextInputLayout>(R.id.percentET)

        cashBackRG.setOnCheckedChangeListener { _, i ->
            if (i == R.id.flatRadio) {
                spendAndGetLL.visibility = View.VISIBLE
                percentageAndLimitLL.visibility = View.GONE
            } else {
                spendAndGetLL.visibility = View.GONE
                percentageAndLimitLL.visibility = View.VISIBLE
            }

            selectedRBId = i

        }

        nextBtnListener(view = view, percentET = percentET)

        viewModel.cashBackResponse.observe(viewLifecycleOwner) {
            sessionManager.saveCashbackSettings(cashbackSettings = it)
            sessionManager.saveCBSettings(true)
            customDialog.hide()
            val cashbackResponse = bundleOf("cashBackResponse" to it)
            mBinding.apply {
                etSpend.setValue(0L)
                etGet.setValue(0L)
                percentET.editText?.setText("")
                etLimit.setValue(0L)
            }
            Navigation.findNavController(view).navigate(R.id.finishFragment, cashbackResponse)
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) {
            requireContext().toast(it.toString())
            customDialog.hide()
        }

        resetInputFields()

    }

    private fun resetInputFields() {
        mBinding.apply {
            etSpend.setValue(0L)
            etGet.setValue(0L)
            percentET.editText?.setText("")
            etLimit.setValue(0L)
        }
    }

    private fun FragmentCashBackBinding.inputFieldsTextChangeListener() {
        etSpend.doAfterTextChanged { spend -> updateTvSpendDescription(spend) }
        etGet.doAfterTextChanged { get -> updateTvGetDescription(get) }
        percentET.editText?.doAfterTextChanged { percent -> updateTvPercentageDescription(percent) }
        etLimit.doAfterTextChanged { limit -> updateTvLimitDescription(limit = limit) }
    }

    private fun updateTvSpendDescription(text: CharSequence?) {
        val spend =
            if (text.isNullOrBlank()) {
                "XX"
            } else {
                mBinding.etSpend.text
            }
        val descriptionForSpend = resources.getString(
            R.string.customer_needs_to_spend_xx_amount_to_get_cash_back, spend
        )
        mBinding.tvSpendDescription.text = descriptionForSpend
    }

    private fun updateTvGetDescription(text: CharSequence?) {
        val get =
            if (text.isNullOrBlank()) {
                "XX"
            } else {
                mBinding.etGet.text
            }
        val descriptionForGet = resources.getString(
            R.string.customer_gets_xx_on_required_amount_spent, get
        )
        mBinding.tvGetDescription.text = descriptionForGet
    }

    private fun updateTvPercentageDescription(percent: CharSequence? = null) {
        val percentage =
            if (percent.isNullOrBlank()) {
                "XX"
            } else {
                percent
            }
        val percentageDescription = resources.getString(
            R.string.customer_earns_1_s_on_each_purchase_made_at_the_store,
            percentage
        )
        mBinding.tvPercentageDescription.text = percentageDescription
    }

    private fun updateTvLimitDescription(limit: CharSequence? = null) {
        val limitAmount =
            if (limit.isNullOrBlank()) {
                "XX"
            } else {
                mBinding.etLimit.text
            }
        val description = resources.getString(
            R.string.customer_need_earn_1_s_amount_to_be_able_to_withdraw_amount,
            limitAmount
        )
        mBinding.tvLimitDescription.text = description
    }

    private fun nextBtnListener(view: View, percentET: TextInputLayout) {

        val nextBtn = view.findViewById<Button>(R.id.nextBtn)
        nextBtn.setOnClickListener {

            //            addCashback()

            val userParams = HashMap<String, Any>()

            if (selectedRBId == R.id.percentageRadio) {

                val cashBackAmountStr = percentET.editText?.text.toString()
                val limitAmount = mBinding.etLimit.getAmount()

                var isValid = true

                if (cashBackAmountStr.isBlank()) {
                    percentET.error = getString(R.string.please_enter_cashback_amount)
                    isValid = false
                } else {
                    percentET.error = null
                }

                if (limitAmount.isNaN()) {
                    mBinding.etLimit.error = getString(R.string.please_enter_limit_amount)
                    isValid = false
                } else {
                    mBinding.etLimit.error = null
                }

                if (!isValid) {
                    return@setOnClickListener
                }

                if (cashBackAmountStr.getAmountInDouble() !in 0.1..100.0) {
                    percentET.error = "Percentage must be in the range of 0.1 to 100"
                    isValid = false
                } else {
                    percentET.error = null
                }

                if (limitAmount <= 0.0) {
                    mBinding.etLimit.error = "Please enter valid limit amount"
                    isValid = false
                } else {
                    mBinding.etLimit.error = null
                }

                if (!isValid) {
                    return@setOnClickListener
                }

                userParams["cashback_type"] = CashbackType.PERCENTAGE.id
                userParams["cashback_amount"] = cashBackAmountStr.getAmountInDouble()
                userParams["maturity_amount"] = limitAmount


            } else {

                var isValid = true

                val cashbackAmount = mBinding.etGet.getAmount()

                if (cashbackAmount.isNaN()) {
                    mBinding.etGet.error = getString(R.string.please_enter_cashback_amount)
                    isValid = false
                } else {
                    mBinding.etGet.error = null
                }

                val spendAmount = mBinding.etSpend.getAmount()

                if (spendAmount.isNaN()) {
                    mBinding.etSpend.error = getString(R.string.please_enter_spend_amount)
                    isValid = false
                } else {
                    mBinding.etSpend.error = null
                }

                if (!isValid) {
                    return@setOnClickListener
                }

                if (cashbackAmount <= 0.0) {
                    mBinding.etGet.error = "Please enter valid get amount"
                    isValid = false
                } else {
                    mBinding.etGet.error = null
                }

                if (spendAmount <= 0.0) {
                    mBinding.etSpend.error = "Please enter valid spend amount"
                    isValid = false
                } else {
                    mBinding.etSpend.error = null
                }

                if (!isValid) {
                    return@setOnClickListener
                }

                if (cashbackAmount > spendAmount) {
                    showToast(message = R.string.get_amount_cannot_exceed_spend_amount)
                    return@setOnClickListener
                }

                userParams["cashback_type"] = CashbackType.FLAT.id
                userParams["cashback_amount"] = cashbackAmount
                userParams["maturity_amount"] = spendAmount

            }

            //            userParams["tags"] = etTags.editText?.text.toString().trim().split("\\s".toRegex())
            //            userParams["tags"] = selectedTags

            //            ((activity) as MerchantRegistrationActivity).customDialogClass.show()
            customDialog.show()

            val token = sessionManager.fetchAuthToken().toString()
            viewModel.addCashBack(merchantId, token, userParams)


        }
    }

    override fun onResume() {
        super.onResume()

        val cashbackSettings = sessionManager.fetchCashbackSettings()
        if (cashbackSettings != null) {
            setData(cashbackSettings)
        }

        val appBarLayout: AppBarLayout =
            ((activity) as MerchantRegistrationActivity).findViewById(R.id.customAppBar)
        val ivBack: ImageView = appBarLayout.findViewById(R.id.backIV)
        ivBack.isVisible = true
        ivBack.setOnClickListener { ((activity) as MerchantRegistrationActivity).onBackPressed() }

    }

    private fun addCashback() {
//        val userParams = HashMap<String, Any>()
//
//
//        if (selectedRBId == R.id.percentageRadio) {
//            userParams["cashback_type"] = 2
//            val cashBackAmount = percentET.editText?.text.toString()
//            if (cashBackAmount.toDouble() <= 100.0) {
//                userParams["cashback_amount"] = cashBackAmount
//            } else {
//                requireContext().toast("Percentage must be less than 100%")
//
//            }
//            val limitAmount = limitET.editText?.text.toString()
//            if (limitAmount.toDouble() <= 100.0) {
//                userParams["maturity_amount"] = limitAmount
//            } else {
//                requireContext().toast("Limit must be less than 100%")
//            }
//
//
//        } else {
//            userParams["cashback_type"] = 1
//            userParams["cashback_amount"] = getET.editText?.text.toString()
//            userParams["maturity_amount"] = spendET.editText?.text.toString()
//
//        }
//
//        userParams["tags"] = etTags.editText?.text.toString().trim().split("\\s".toRegex())
//
//        ((activity) as MerchantRegistrationActivity).customDialogClass.show()
//
//        val token = sessionManager.fetchAuthToken().toString()
//        viewModel.addCashBack(merchantId, token, userParams)
    }


    private fun updateStepper(view: View) {
        val basicInfo = view.findViewById<ImageView>(R.id.imageView)
        val imageUpload = view.findViewById<ImageView>(R.id.iv_image_upload)
        val cashbackInfo = view.findViewById<ImageView>(R.id.imageView2)

        basicInfo.setImageDrawable(
            ContextCompat.getDrawable(requireContext(), R.drawable.ic_check)
        )
        imageUpload.setImageDrawable(
            ContextCompat.getDrawable(requireContext(), R.drawable.ic_check)
        )
        imageUpload.setBackgroundResource(R.drawable.green_circle)
        cashbackInfo.setBackgroundResource(R.drawable.green_circle)
    }

    private fun setData(activeCashbackSettings: CashbackResponseV2?) {
        if (activeCashbackSettings?.cashbackType == 1) {
            mBinding.apply {
                cashBackRG.check(R.id.flatRadio)
                spendAndGetLL.visibility = View.VISIBLE
                percentageAndLimitLL.visibility = View.GONE
                val spendAmount: Long = activeCashbackSettings.maturityAmount?.toDoubleOrNull()
                    ?.times(100L)?.toLong() ?: 0L
                etSpend.setValue(spendAmount)
                val getAmount: Long = activeCashbackSettings.cashbackAmount?.toDoubleOrNull()
                    ?.times(100L)?.toLong() ?: 0L
                etGet.setValue(getAmount)
                cashBackRG.check(R.id.flatRadio)
                selectedRBId = R.id.flatRadio
            }
        } else {
            mBinding.apply {
                cashBackRG.check(R.id.percentageRadio)
                spendAndGetLL.visibility = View.GONE
                percentageAndLimitLL.visibility = View.VISIBLE
                percentET.editText?.setText(activeCashbackSettings?.cashbackPercentage?.formatAmount)
                val limitAmount: Long = activeCashbackSettings?.maturityAmount?.toDoubleOrNull()
                    ?.times(100L)?.toLong() ?: 0L
                etLimit.setValue(limitAmount)
                cashBackRG.check(R.id.percentageRadio)
                selectedRBId = R.id.percentageRadio
            }
        }
    }

}