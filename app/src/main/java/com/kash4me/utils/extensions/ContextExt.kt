package com.kash4me.utils.extensions

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent

fun Context.openCustomTab(uri: Uri) {
    val customTabsIntent = CustomTabsIntent.Builder()
    val customTabs = customTabsIntent.build()
    try {
        customTabs.intent.setPackage("com.android.chrome")
        customTabs.launchUrl(this, uri)
    } catch (ex: Exception) {
        this.startActivity(Intent(Intent.ACTION_VIEW, uri))
    }
}