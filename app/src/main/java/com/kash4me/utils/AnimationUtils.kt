package com.kash4me.utils

import android.animation.ValueAnimator
import com.google.android.material.progressindicator.LinearProgressIndicator

fun LinearProgressIndicator.setProgressValue(progress: Int, shouldAnimate: Boolean = true) {

    if (shouldAnimate) {
        val progressAnimator = ValueAnimator.ofInt(0, progress)
        progressAnimator.duration = 100
        progressAnimator.addUpdateListener { animation: ValueAnimator ->
            val animatedValue = animation.animatedValue as Int
            this.setProgressCompat(animatedValue, true)
        }
        progressAnimator.start()
    } else {
        this.progress = progress
    }

}