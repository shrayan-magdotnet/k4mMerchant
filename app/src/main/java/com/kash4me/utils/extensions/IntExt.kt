package com.kash4me.utils.extensions

import android.content.res.Resources

fun Int?.getZeroIfNull() = this ?: 0

fun Int?.getMinusOneIfNull() = this ?: -1

fun Int.isEven() = this % 2 == 0

// Convert px to dp
val Int.dp: Int
    get() = (this / Resources.getSystem().displayMetrics.density).toInt()

//Convert dp to px
val Int.px: Int
    get() = (this * Resources.getSystem().displayMetrics.density).toInt()