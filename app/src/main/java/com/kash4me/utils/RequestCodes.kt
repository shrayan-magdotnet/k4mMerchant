package com.kash4me.utils

enum class RequestCodes {

    LOCATION_PERMISSION

    ;

    fun getCode(): Int {
        return this.ordinal
    }

}