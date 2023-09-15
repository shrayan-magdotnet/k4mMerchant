package com.kash4me.utils

object AppConstants {

    const val algorithm = "CFB"

    // TODO: Store this key to make it secured from reverse engineering
    var keyValue = "H@McQfTjWnZr4u7x!z%C*F-JaNdRgUkX"

    const val NEW_LINE = "\n"

    const val MINUS_ONE = -1
    const val DEFAULT_PRIMARY_KEY = 0

    const val KASH4ME_PREFERENCES = "kash4me_preferences"
    const val APP_LOCALE = "app_locale"

    const val MIN_USERNAME_LENGTH = 6
    const val MIN_PASSWORD_LENGTH = 8
    const val MIN_TRANSACTION_AMOUNT = 0.1
    const val LAT_LON_DECIMAL_DIGITS = 6

    const val MIN_AMOUNT_PAY_BY_KASH4ME = 50.00
    const val MIN_WITHDRAW_AMOUNT = 5.00

    const val IMAGE_SIZE_LIMIT = 10.0

    const val email: String = "email"
    const val goToVerifyScreen: String = "goToVerifyScreen"

    const val testCustomerEmail: String = "customer1@yopmail.com"
    const val testCustomerPassword: String = "Admin@123"

    const val testMerchantEmail: String = "merchant1@yopmail.com"
    const val testMerchantPassword: String = "Admin@123"

    const val STAFF_ERROR: String = "005"

    const val PRIVACY_POLICY_URL = "https://dev.kash4me.com/web/privacy-policy/"
    const val TERMS_AND_CONDITIONS_URL = "https://dev.kash4me.com/web/terms-and-conditions/"

    const val ERROR_400 =
        "The server couldn't understand the request due to malformed syntax or invalid parameters."
    const val ERROR_401 =
        "Authentication is required, and the provided credentials are invalid or missing."
    const val ERROR_403 = "The client does not have permission to access the requested resource."
    const val ERROR_404 = "The requested resource could not be found on the server."
    const val ERROR_409 =
        "The request could not be completed due to a conflict with the current state of the resource."
    const val ERROR_422 =
        "The server understands the request, but it cannot process the request entity (e.g., validation errors)."

    const val ERROR_500 = "An unexpected condition was encountered by the server."
    const val ERROR_501 =
        "The server does not support the functionality required to fulfill the request."
    const val ERROR_503 =
        "The server is currently unable to handle the request due to temporary overloading or maintenance of the server."
    const val ERROR_504 =
        "The server, while acting as a gateway or proxy, did not receive a timely response from the upstream server."


}