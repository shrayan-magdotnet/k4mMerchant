package com.kash4me.data.models.payment_gateway


import com.google.gson.annotations.SerializedName

data class PaymentInformationResponse(
    @SerializedName("account_details")
    val accountDetails: AccountDetails? = AccountDetails(),
    @SerializedName("company_name")
    val companyName: String? = "",
    @SerializedName("email_address")
    val emailAddress: String? = "",
    @SerializedName("first_name")
    val firstName: String? = "",
    @SerializedName("id")
    val id: Int? = 0,
    @SerializedName("is_default")
    val isDefault: Boolean? = false,
    @SerializedName("is_verified")
    val isVerified: Boolean? = false,
    @SerializedName("last_name")
    val lastName: String? = "",
    @SerializedName("payment_method")
    val paymentMethod: String? = ""
) {
    data class AccountDetails(
        @SerializedName("account_number")
        val accountNumber: String? = "",
        @SerializedName("institute_name")
        val instituteName: String? = ""
    )
}