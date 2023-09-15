package com.kash4me.utils.extensions

fun Boolean?.getFalseIfNull(): Boolean {
    return this ?: false
}