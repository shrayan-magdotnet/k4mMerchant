package com.kash4me.utils.custom_views

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.util.AttributeSet
import android.util.TypedValue
import androidx.appcompat.widget.AppCompatTextView
import timber.log.Timber
import java.text.NumberFormat
import java.util.Locale

class AppCurrencyTextView(context: Context, attrs: AttributeSet)

    : AppCompatTextView(context, attrs) {

    init {
        setTypeface(null, Typeface.BOLD)
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
        setTextColor(Color.GREEN)
    }

    fun setAmount(amount: Double) {
        val currentLocale = Locale.US
        Locale(currentLocale?.language, currentLocale?.country)
        val usdFormat = NumberFormat.getCurrencyInstance(currentLocale)
        val formattedAmount = usdFormat.format(amount)
        Timber.d("Formatted amount -> $formattedAmount")
        text = formattedAmount
    }

}