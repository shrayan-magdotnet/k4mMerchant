package com.kash4me.utils

enum class PaymentMethod(val identifier: String, val title: String) {

    ALL(identifier = "ALL", title = "ALL"),
    VOPAY_BANK(identifier = "VOPAY_BANK", title = "Vopay Bank"),
    VOPAY_E_TRANSFER(identifier = "VOPAY_E_TRANSFER", title = "Vopay e transfer")

    ;

    override fun toString(): String {
        return identifier
    }

}