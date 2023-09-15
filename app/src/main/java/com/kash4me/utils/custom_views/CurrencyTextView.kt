package com.kash4me.utils.custom_views

import android.content.Context
import android.util.AttributeSet
import androidx.core.view.updatePadding
import com.blackcat.currencyedittext.CurrencyEditText
import java.util.Locale

class CurrencyTextView(context: Context, attrs: AttributeSet) : CurrencyEditText(context, attrs) {

    init {
        isEnabled = false
        background = null
        val scale = resources.displayMetrics.density
        val dpAsPixels = (0 * scale + 0.5f).toInt()
        locale = Locale.US
        updatePadding(top = dpAsPixels, bottom = dpAsPixels)
    }

}