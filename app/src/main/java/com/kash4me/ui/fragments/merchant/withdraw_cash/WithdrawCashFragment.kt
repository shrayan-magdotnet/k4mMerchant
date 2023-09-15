package com.kash4me.ui.fragments.merchant.withdraw_cash

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.kash4me.R
import com.kash4me.data.models.payment_gateway.PaymentInformationResponse
import com.kash4me.databinding.FragmentWithdrawCashBinding
import com.kash4me.ui.activity.customer.customer_dashboard.CustomerDashboardActivity
import com.kash4me.ui.activity.merchant.withdraw_cash.WithdrawCashActivity
import com.kash4me.ui.activity.payment_gateway.PaymentSettingsViewModel
import com.kash4me.ui.dialog.ErrorDialog
import com.kash4me.ui.dialog.SuccessDialog
import com.kash4me.ui.fragments.customer.total_transaction.CustomerTotalTransactionViewModel
import com.kash4me.utils.AppConstants
import com.kash4me.utils.SessionManager
import com.kash4me.utils.custom_views.CustomProgressDialog
import com.kash4me.utils.extensions.formatUsingCurrencySystem
import com.kash4me.utils.extensions.getAmount
import com.kash4me.utils.extensions.getEmptyIfNull
import com.kash4me.utils.extensions.getZeroIfNull
import com.kash4me.utils.extensions.showToast
import com.kash4me.utils.network.Resource
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class WithdrawCashFragment : Fragment() {

    private var _binding: FragmentWithdrawCashBinding? = null
    private val mBinding get() = _binding!!

    private var mRemainingBalance: Double? = null

    companion object {

        private var availableBalance: Double? = null

        fun getNewInstance(availableBalance: Double?): WithdrawCashFragment {
            this.availableBalance = availableBalance
            return WithdrawCashFragment()
        }

    }

    private var mDefaultPaymentMethod: PaymentMethod? = null

    private val mSessionManager by lazy { SessionManager(requireActivity().applicationContext) }

    private val mProgressDialog: CustomProgressDialog by lazy {
        CustomProgressDialog(requireContext())
    }

    private val mPaymentMethodsAdapter by lazy {
        ArrayAdapter(
            requireContext(), android.R.layout.simple_list_item_1, PaymentMethod.values()
        )
    }

    private val mSecurityQuestionsAdapter by lazy {
        ArrayAdapter(
            requireContext(), android.R.layout.simple_list_item_1, SecurityQuestions.values()
        )
    }

    private val viewModel: CustomerTotalTransactionViewModel by viewModels()
    private val mPaymentSettingsViewModel: PaymentSettingsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWithdrawCashBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mBinding.tvAmount.text = "$$availableBalance"

        getLinkedPaymentMethods()

        mBinding.apply {
            spSecurityQuestion.adapter = mSecurityQuestionsAdapter

            btnGoToSettings.setOnClickListener {
                activity?.finish()
                val intent = CustomerDashboardActivity.getNewIntent(
                    packageContext = requireActivity(),
                    fragment = CustomerDashboardActivity.NavigationViewFragment.PERSON,
                    isFreshLogin = false
                )
                startActivity(intent)
            }
            btnWithdrawListener()
        }

        etWithdrawAmountListener()
//        btnSendListener()
        errorMessageObserver()

    }

    private fun getLinkedPaymentMethods() {
        mPaymentSettingsViewModel.getLinkedPaymentMethods().observe(viewLifecycleOwner) {

            when (it) {
                is Resource.Failure -> {
                    mProgressDialog.hide()
                    val errorDialog = ErrorDialog.getInstance(message = it.errorMsg)
                    errorDialog.show(activity?.supportFragmentManager!!, errorDialog.tag)
                }

                Resource.Loading -> {
                    mProgressDialog.show()
                }

                is Resource.Success -> {
                    mProgressDialog.hide()

                    val defaultPaymentMethod = it.value.find { paymentInfo ->
                        paymentInfo.isDefault == true
                    }
                    Timber.d("Default payment method -> $defaultPaymentMethod")
                    when (defaultPaymentMethod?.paymentMethod) {
                        null -> {
                            mBinding.setStateToNoDefaultPaymentSelected()
                        }

                        PaymentMethod.E_TRANSFER.value -> {
                            mBinding.setStateToETransfer(defaultPaymentMethod)
                        }

                        PaymentMethod.BANK.value -> {
                            mBinding.setStateToBankTransfer(defaultPaymentMethod)
                        }
                    }

                }
            }

        }
    }

    private fun FragmentWithdrawCashBinding.setStateToNoDefaultPaymentSelected() {

        mDefaultPaymentMethod = null

        cvDefaultPayment.isVisible = true
        val bgColor = ContextCompat.getColor(requireContext(), R.color.bg_orange)
        cvDefaultPayment.setCardBackgroundColor(bgColor)

        tvDefaultPaymentMethod.isVisible = false
        val noDefaultPaymentSelected =
            resources.getString(R.string.default_payment_method_not_set_please_go_to_settings_and_setup_your_payment_method)
        tvUpdatePaymentSettingsDescriptions.text = noDefaultPaymentSelected

        tvSecurityQuestionCaption.isVisible = false
        spSecurityQuestion.isVisible = false
        ivDropDownIcon.isVisible = false

        tvYourAnswerCaption.isVisible = false
        etYourAnswer.isVisible = false

        val transactionFeeDescription = resources.getString(
            R.string.withdraw_amount_description, AppConstants.MIN_WITHDRAW_AMOUNT
        )
        tvTransactionFeeDescription.text = transactionFeeDescription

        btnGoToSettings.isVisible = true
        btnWithdraw.isVisible = false

    }

    private fun FragmentWithdrawCashBinding.setStateToETransfer(
        defaultPaymentMethod: PaymentInformationResponse
    ) {

        mDefaultPaymentMethod = PaymentMethod.E_TRANSFER

        cvDefaultPayment.isVisible = true
        val bgColor = ContextCompat.getColor(requireContext(), android.R.color.transparent)
        cvDefaultPayment.setCardBackgroundColor(bgColor)

        tvDefaultPaymentMethod.isVisible = true
        tvDefaultPaymentMethod.text = resources.getString(
            R.string.you_are_all_set_to_use_e_transfer_with_email_id_x,
            defaultPaymentMethod.emailAddress
        )

        tvSecurityQuestionCaption.isVisible = true
        spSecurityQuestion.isVisible = true
        ivDropDownIcon.isVisible = true

        tvYourAnswerCaption.isVisible = true
        etYourAnswer.isVisible = true

        val transactionFeeDescription = resources.getString(
            R.string.interac_withdraw_amount_description
        )
        tvTransactionFeeDescription.text = transactionFeeDescription

        btnGoToSettings.isVisible = false
        btnWithdraw.isVisible = true


    }

    private fun FragmentWithdrawCashBinding.setStateToBankTransfer(
        defaultPaymentMethod: PaymentInformationResponse
    ) {

        mDefaultPaymentMethod = PaymentMethod.BANK

        cvDefaultPayment.isVisible = true
        val bgColor = ContextCompat.getColor(requireContext(), android.R.color.transparent)
        cvDefaultPayment.setCardBackgroundColor(bgColor)

        tvDefaultPaymentMethod.isVisible = true
        tvDefaultPaymentMethod.text = resources.getString(
            R.string.you_are_all_set_to_use_x_bank_with_account_number_y,
            defaultPaymentMethod.accountDetails?.instituteName,
            defaultPaymentMethod.accountDetails?.accountNumber
        )

        tvSecurityQuestionCaption.isVisible = false
        spSecurityQuestion.isVisible = false
        ivDropDownIcon.isVisible = false

        tvYourAnswerCaption.isVisible = false
        etYourAnswer.isVisible = false

        val transactionFeeDescription = resources.getString(
            R.string.withdraw_amount_description, AppConstants.MIN_WITHDRAW_AMOUNT
        )
        tvTransactionFeeDescription.text = transactionFeeDescription

        btnGoToSettings.isVisible = false
        btnWithdraw.isVisible = true

    }

    private fun FragmentWithdrawCashBinding.btnWithdrawListener() {
        btnWithdraw.setOnClickListener {

            val request = HashMap<String, Any>()

            val amount = etWithdrawAmount.getAmount()
            if (amount < 0.1) {
                showToast(R.string.ensure_this_value_is_greater_than_or_equal_to_0_point_1)
                return@setOnClickListener
            }

            if (amount > availableBalance.getZeroIfNull()) {
                showToast(R.string.amount_cannot_be_greater_than_available_balance)
                return@setOnClickListener
            }

            if (amount < AppConstants.MIN_WITHDRAW_AMOUNT) {
                val minimumAmount = "$${AppConstants.MIN_WITHDRAW_AMOUNT}"
                val minimumAmountMessage = getString(
                    R.string.withdraw_amount_should_be_greater_than_x_amount,
                    minimumAmount
                )
                showToast(minimumAmountMessage)
                return@setOnClickListener
            }

            request["withdraw_amount"] = amount

            if (mDefaultPaymentMethod == null) {
                showToast("Please select a payment method")
                return@setOnClickListener
            }
            request["payment_type"] = mDefaultPaymentMethod?.value.getEmptyIfNull()

            if (spSecurityQuestion.isVisible) {
                val securityQuestion = spSecurityQuestion.selectedItem as SecurityQuestions?
                if (securityQuestion == null) {
                    showToast("Please select a security question")
                    return@setOnClickListener
                }
                request["question"] = securityQuestion.index
            }

            if (etYourAnswer.isVisible) {
                val answer = etYourAnswer.text
                if (answer.isNullOrBlank()) {
                    showToast("Please write your answer")
                    return@setOnClickListener
                }
                if (answer.length < 6) {
                    showToast("Answer should have at-least 6 characters")
                    return@setOnClickListener
                }
                request["answer"] = answer.toString()
            }

            Timber.d("Request -> $request")

            val fragment = ConfirmWithdrawFragment.getNewInstance(
                amountToBeWithdrawn = amount,
                remainingBalance = mRemainingBalance,
                request = request
            )
            (activity as WithdrawCashActivity).replaceFragment(fragment)

//            viewModel.withdrawAmount(request = request).observe(viewLifecycleOwner) {
//
//                when (it) {
//                    is Resource.Failure -> {
//                        mProgressDialog.hide()
//                        val errorDialog = ErrorDialog.getInstance(message = it.errorMsg)
//                        errorDialog.show(activity?.supportFragmentManager!!, errorDialog.tag)
//                    }
//                    Resource.Loading -> {
//                        mProgressDialog.show()
//                    }
//                    is Resource.Success -> {
//                        mProgressDialog.hide()
//                        val successDialog = SuccessDialog.getInstance(
//                            message = getString(R.string.cash_successfully_withdrawn),
//                            afterDismissClicked = object : AfterDismissalListener {
//                                override fun afterDismissed() {
//                                    activity?.finish()
//                                }
//                            }
//                        )
//                        successDialog.show(activity?.supportFragmentManager!!, successDialog.tag)
//                    }
//                }
//
//            }

        }
    }

    private fun etWithdrawAmountListener() {
        mBinding.etWithdrawAmount.doOnTextChanged { _, _, _, _ ->

            val amount = mBinding.etWithdrawAmount.getAmount()
            mRemainingBalance = availableBalance.getZeroIfNull() - amount
            mBinding.tvAmount.text = "$" + mRemainingBalance?.formatUsingCurrencySystem()

        }
    }

    private fun btnSendListener() {
        mBinding.btnWithdraw.setOnClickListener {

            val amount = mBinding.etWithdrawAmount.getAmount()
            if (amount < 0.1) {
                showToast(R.string.ensure_this_value_is_greater_than_or_equal_to_0_point_1)
                return@setOnClickListener
            }

            if (amount > availableBalance.getZeroIfNull()) {
                showToast(R.string.amount_cannot_be_greater_than_available_balance)
                return@setOnClickListener
            }

            val token = mSessionManager.fetchAuthToken()
            if (token != null) {
                mProgressDialog.show()
                viewModel.createTransaction(token = token, amountSpent = amount.toString())
                    .observe(viewLifecycleOwner) {

                        val successDialog = SuccessDialog.getInstance(
                            message = getString(R.string.cash_successfully_withdrawn)
                        )
                        successDialog.show(activity?.supportFragmentManager!!, successDialog.tag)
                        Timber.d(it.toString())
                        mProgressDialog.hide()
                        mBinding.tvAmount.text = it.balanceAmount

                        AlertDialog.Builder(requireContext())
                            .setTitle(R.string.success)
                            .setMessage(getString(R.string.cash_successfully_withdrawn))
                            .setPositiveButton(R.string.ok) { dialog, which ->
                                dialog.dismiss()
                                activity?.finish()
                            }

                    }
            }

        }
    }

    private fun errorMessageObserver() {
        viewModel.errorMessage.observe(viewLifecycleOwner) {

            val errorDialog = ErrorDialog.getInstance(message = it)
            errorDialog.show(activity?.supportFragmentManager!!, errorDialog.tag)
            mProgressDialog.hide()

        }
    }

    private enum class PaymentMethod(val value: String, val title: String) {

        BANK(value = "VOPAY_BANK", title = "Electronic Fund Transfer"),
        E_TRANSFER(value = "VOPAY_E_TRANSFER", title = "Interac Fund Transfer"),

        ;

        override fun toString(): String {
            return title
        }

    }

    private enum class SecurityQuestions(val index: String, val question: String) {

        QUESTION_A(index = "a", question = "What is your mother's maiden name?"),
        QUESTION_B(index = "b", question = "What is your favorite sports team?"),
        QUESTION_C(index = "c", question = "What street did you grow up on?"),
        QUESTION_D(index = "d", question = "What was the name of your first grade teacher?"),
        QUESTION_E(index = "e", question = "What is your favorite food?")

        ;

        override fun toString(): String {
            return question
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}