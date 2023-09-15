package com.kash4me.utils

val Any?.isNull get() = this == null

fun Any?.ifNull(block: () -> Unit) = run {
    if (this == null) {
        block()
    }
}