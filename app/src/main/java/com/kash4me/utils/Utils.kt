package com.kash4me.utils

import android.content.Context
import android.content.Intent
import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import android.os.Parcelable
import android.widget.Toast
import com.kash4me.utils.extensions.formatUsingCurrencySystem

//fun Double.formatAmount(digits: Int=2) = "%.${digits}f".format(this)

val String?.formatAmount: String
    get() {
        return try {
            this?.toDouble()?.formatUsingCurrencySystem() ?: "0.0"
        } catch (ex: NumberFormatException) {
            this ?: "0.0"
        }
//        return "%.2f".format(amount)
    }

fun Context.toast(message: String, length: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, length).show()
}

inline fun <reified T : Parcelable> Intent.parcelable(key: String): T? = when {
    SDK_INT >= 33 -> getParcelableExtra(key, T::class.java)
    else -> @Suppress("DEPRECATION") getParcelableExtra(key) as? T
}

inline fun <reified T : Parcelable> Bundle.parcelable(key: String): T? = when {
    SDK_INT >= 33 -> getParcelable(key, T::class.java)
    else -> @Suppress("DEPRECATION") getParcelable(key) as? T
}