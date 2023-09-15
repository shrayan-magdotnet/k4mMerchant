package com.kash4me.utils.extensions

import com.google.android.material.textfield.TextInputLayout

fun TextInputLayout.clearError() {
    this.error = ""
}

fun TextInputLayout.clear() {
    this.editText?.setText("")
}