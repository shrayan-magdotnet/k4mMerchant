package com.kash4me.utils.custom_views

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import com.kash4me.R
import timber.log.Timber

class CustomProgressDialog(val context: Context) {

    private var dialog: Dialog? = null

    fun show() {
        Timber.d("Context -> $context")
        dialog = Dialog(context)
        dialog?.setContentView(R.layout.custom_progress_circular_indicator)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog?.setCancelable(false)
        dialog?.create()
        dialog?.show()
    }

    fun hide() {
        if (dialog?.isShowing == true)
            dialog?.dismiss()
    }

}