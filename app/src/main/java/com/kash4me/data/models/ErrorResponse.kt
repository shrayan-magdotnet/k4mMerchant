package com.kash4me.data.models

data class ErrorResponse(
    val error: String = "Oops! Something went wrong",
    val error_code: String = "000"
)