package com.kash4me.utils

import android.content.Context
import android.util.DisplayMetrics

/**
 * This method converts dp unit to equivalent pixels, depending on device density.
 *
 * @param this A value in dp (density independent pixels) unit. Which we need to convert into pixels
 * @param context Context to get resources and device specific display metrics
 * @return A float value to represent px equivalent to dp depending on device density
 */
fun Float.convertDpToPixel(context: Context): Float {
    return this * (context.resources.displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
}

/**
 * This method converts device specific pixels to density independent pixels.
 *
 * @param this A value in px (pixels) unit. Which we need to convert into dp
 * @param context Context to get resources and device specific display metrics
 * @return A float value to represent dp equivalent to px value
 */
fun Float.convertPixelsToDp(context: Context): Float {
    return this / (context.resources.displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
}