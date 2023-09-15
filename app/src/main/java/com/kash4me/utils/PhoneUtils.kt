package com.kash4me.utils

class PhoneUtils {

    fun sanitizePhoneNumber(phoneNumber: String): String {
        return phoneNumber
            .replace("(", "")
            .replace(")", "")
            .replace("-", "")
            .replace(" ", "")
    }

}