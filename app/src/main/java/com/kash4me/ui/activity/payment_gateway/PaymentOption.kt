package com.kash4me.ui.activity.payment_gateway

data class PaymentOption(
    var isChecked: Boolean = false,
    var isDefault: Boolean = false,
    var isLinked: Boolean = false,
    val title: String,
    val identifier: String,
    var linkCompleteDescription: String,
    val description: String
)