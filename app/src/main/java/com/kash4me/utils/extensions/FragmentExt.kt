package com.kash4me.utils.extensions

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar

fun Fragment.showToast(message: String, length: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(context, message, length).show()
}

fun Fragment.showToast(@StringRes message: Int, length: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(context, message, length).show()
}

fun Fragment.showSnackbar(@StringRes message: Int, length: Int = Toast.LENGTH_SHORT) {
    view?.let { Snackbar.make(it, message, length).show() }
}

fun Fragment.showSnackbar(message: String, length: Int = Toast.LENGTH_SHORT) {
    view?.let { Snackbar.make(it, message, length).show() }
}

fun Fragment.copyTextToClipboard(text: String) {
    val clipboardManager = activity?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clipData = ClipData.newPlainText("label", text)
    clipboardManager.setPrimaryClip(clipData)
}