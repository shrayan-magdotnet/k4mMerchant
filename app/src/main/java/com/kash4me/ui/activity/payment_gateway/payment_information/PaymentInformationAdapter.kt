package com.kash4me.ui.activity.payment_gateway.payment_information


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.kash4me.merchant.databinding.ItemPaymentOptionBinding
import com.kash4me.ui.activity.payment_gateway.PaymentOption
import com.kash4me.utils.listeners.SingleParamWithPositionItemClickListener
import timber.log.Timber

class PaymentInformationAdapter(private val clickListener: SingleParamWithPositionItemClickListener<PaymentOption>)

    : RecyclerView.Adapter<PaymentInformationAdapter.PaymentOptionViewHolder>() {

    private val mPaymentOptions = arrayListOf<PaymentOption>()

    fun setData(paymentOptions: List<PaymentOption>) {
        Timber.d("Setting payment options -> $paymentOptions")
        mPaymentOptions.clear()
        mPaymentOptions.addAll(paymentOptions)
        notifyDataSetChanged()
    }

    fun setSelectedItem(position: Int) {
        mPaymentOptions.forEach { it.isChecked = false }
        mPaymentOptions[position].isChecked = true
        notifyDataSetChanged()
    }

    fun getSelectedItem(): PaymentOption? {
        return mPaymentOptions.find { it.isChecked }
    }

    override fun getItemCount() = mPaymentOptions.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PaymentOptionViewHolder {
        val itemBinding = ItemPaymentOptionBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return PaymentOptionViewHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: PaymentOptionViewHolder, position: Int) {

        val paymentOption = mPaymentOptions[position]
        holder.bind(paymentOption)

    }

    inner class PaymentOptionViewHolder(private val binding: ItemPaymentOptionBinding)

        : RecyclerView.ViewHolder(binding.root) {

        fun bind(paymentOption: PaymentOption) {

            binding.apply {
                tvTitle.setText(paymentOption.title)
                tvDescription.text = paymentOption.description
                radioButton.isChecked = paymentOption.isChecked
                radioButton.setOnClickListener { updateState(paymentOption) }
                if (paymentOption.isLinked) {
                    tvLinkCompleteMessage.isVisible = true
                    tvLinkCompleteMessage.text = paymentOption.linkCompleteDescription
                } else {
                    tvLinkCompleteMessage.isVisible = false
                }
                tvDefaultIndicator.isVisible = paymentOption.isDefault
                root.setOnClickListener { updateState(paymentOption) }
            }

        }

        private fun updateState(paymentOption: PaymentOption) {
            paymentOption.isChecked = !paymentOption.isChecked
            clickListener.onClick(item = paymentOption, position = bindingAdapterPosition)
        }

    }

}