package com.kash4me.ui.fragments.merchant.withdraw_cash


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.kash4me.data.models.payment_gateway.PaymentInformationResponse
import com.kash4me.databinding.ItemPaymentOptionForCashWithdrawBinding
import com.kash4me.utils.PaymentMethod
import com.kash4me.utils.extensions.getFalseIfNull
import com.kash4me.utils.extensions.getNotAvailableIfEmptyOrNull

class PaymentMethodsAdapter

    : RecyclerView.Adapter<PaymentMethodsAdapter.PaymentMethodViewHolder>() {

    private val mPaymentMethods = arrayListOf<PaymentInformationResponse>()

    fun setData(paymentMethods: List<PaymentInformationResponse>) {
        mPaymentMethods.clear()
        mPaymentMethods.addAll(paymentMethods)
        notifyDataSetChanged()
    }

    override fun getItemCount() = mPaymentMethods.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PaymentMethodViewHolder {
        val itemBinding = ItemPaymentOptionForCashWithdrawBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return PaymentMethodViewHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: PaymentMethodViewHolder, position: Int) {

        val paymentMethod = mPaymentMethods[position]
        holder.bind(paymentMethod)

    }

    class PaymentMethodViewHolder(private val binding: ItemPaymentOptionForCashWithdrawBinding)

        : RecyclerView.ViewHolder(binding.root) {

        fun bind(paymentMethod: PaymentInformationResponse) {

            val accountLinkedMessage = when (paymentMethod.paymentMethod) {
                PaymentMethod.VOPAY_BANK.identifier -> {
                    "You are already connected with " +
                            "${paymentMethod.accountDetails?.instituteName.getNotAvailableIfEmptyOrNull()} " +
                            "with account ending with " +
                            "${paymentMethod.accountDetails?.accountNumber}"
                }

                PaymentMethod.VOPAY_E_TRANSFER.identifier -> {
                    "You are all set to use " + "${paymentMethod.emailAddress} " + "to use interac"
                }

                else -> {
                    ""
                }
            }

            val paymentOption = PaymentMethod.values().find {
                paymentMethod.paymentMethod == it.identifier
            }
            binding.apply {
                tvTitle.text = paymentOption?.identifier.getNotAvailableIfEmptyOrNull()
                tvLinkCompleteMessage.isVisible = paymentMethod.isVerified.getFalseIfNull()
                tvLinkCompleteMessage.text = accountLinkedMessage
            }

        }

    }

}