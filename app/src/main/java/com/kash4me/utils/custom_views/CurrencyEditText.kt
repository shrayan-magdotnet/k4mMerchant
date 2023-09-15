package com.kash4me.utils.custom_views

import android.content.Context
import android.graphics.Rect
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.widget.Toast
import androidx.appcompat.widget.AppCompatEditText

class CurrencyEditText @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = android.R.attr.editTextStyle
) : AppCompatEditText(context, attrs, defStyleAttr) {

//    private var currencySymbol: String
//    private val decimalFormat: DecimalFormat

    init {
        // Set the default currency symbol based on the device's locale
//        currencySymbol = Currency.getInstance(Locale.getDefault()).symbol

        // Create a decimal format with the appropriate currency symbol
//        decimalFormat = NumberFormat.getNumberInstance(Locale.getDefault()) as DecimalFormat
//        decimalFormat.isParseBigDecimal = true

        // Add a text watcher to format the entered text as currency
//        addTextChangedListener(CurrencyTextWatcher())
    }

    override fun onFocusChanged(focused: Boolean, direction: Int, previouslyFocusedRect: Rect?) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect)

        if (focused) {

            Toast.makeText(context, "Has focus", Toast.LENGTH_SHORT).show()

        } else {

            Toast.makeText(context, "Lost focus", Toast.LENGTH_SHORT).show()

        }

    }

    private inner class CurrencyTextWatcher : TextWatcher {

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            // Not used
        }

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            // Not used
        }

        override fun afterTextChanged(s: Editable) {
            // Remove the previous formatting
//            removeTextChangedListener(this)

            // Parse the current value to a BigDecimal
            val cleanString = s.toString().replace("[^\\d]".toRegex(), "")
//            val parsed = BigDecimal(cleanString)
//                .setScale(2, BigDecimal.ROUND_FLOOR)
//                .divide(BigDecimal(100), BigDecimal.ROUND_FLOOR)
//
//            // Format the value with currency symbol and thousand separators
//            val formatted = currencySymbol + decimalFormat.format(parsed)
//
//            // Set the formatted text
//            setText(formatted)
//            setSelection(formatted.length)
//
//            // Restore the text watcher
//            addTextChangedListener(this)
        }
    }
}
