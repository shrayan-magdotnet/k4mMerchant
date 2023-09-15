package com.kash4me.utils.extensions

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.View
import android.widget.Toast
import androidx.annotation.StringRes
import com.google.android.material.snackbar.Snackbar

fun Activity.showToast(message: String, length: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, length).show()
}

fun Activity.showToast(@StringRes message: Int, length: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, length).show()
}

fun Activity.showSnackbar(
    rootView: View, @StringRes message: Int, length: Int = Toast.LENGTH_SHORT
) {
    Snackbar.make(rootView, message, length).show()
}

fun Activity.showSnackbar(rootView: View, message: String, length: Int = Toast.LENGTH_SHORT) {
    Snackbar.make(rootView, message, length).show()
}

fun Activity.copyTextToClipboard(text: String) {
    val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clipData = ClipData.newPlainText("label", text)
    clipboardManager.setPrimaryClip(clipData)
}


