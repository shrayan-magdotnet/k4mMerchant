package com.kash4me.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.kash4me.merchant.databinding.DialogErrorBinding
import com.kash4me.utils.listeners.AfterDismissalListener

class ErrorDialog : DialogFragment() {

    private var _binding: DialogErrorBinding? = null
    private val mBinding get() = _binding!!

    companion object {

        private var message: String = ""
        private var afterDismissalListener: AfterDismissalListener? = null

        fun getInstance(
            message: String, afterDismissClicked: AfterDismissalListener? = null
        ): ErrorDialog {
            this.message = message
            this.afterDismissalListener = afterDismissClicked
            return ErrorDialog()
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogErrorBinding.inflate(inflater, container, false)
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog?.setCancelable(false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mBinding.apply {
            tvMessage.text = message
            btnOk.setOnClickListener {
                dialog?.dismiss()
                afterDismissalListener?.afterDismissed()
            }
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}