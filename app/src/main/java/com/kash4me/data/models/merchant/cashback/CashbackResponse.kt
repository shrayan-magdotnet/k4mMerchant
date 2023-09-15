package com.kash4me.data.models.merchant.cashback


import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class CashbackResponse(
    @SerializedName("allow_usr_flag")
    val allowUsrFlag: Boolean? = false,
    @SerializedName("archive_flag")
    val archiveFlag: Boolean? = false,
    @SerializedName("cashback_amount")
    val cashbackAmount: String? = "",
    @SerializedName("cashback_percentage")
    val cashbackPercentage: String? = "",
    @SerializedName("cashback_type")
    val cashbackType: Int? = 0,
    @SerializedName("cashback_type_display")
    val cashbackTypeDisplay: String? = "",
    @SerializedName("id")
    val id: Int? = 0,
    @SerializedName("maturity_amount")
    val maturityAmount: String? = "",
    @SerializedName("merchant_details")
    val merchantDetails: MerchantDetails? = MerchantDetails()
) : Parcelable {

    @Parcelize
    data class MerchantDetails(
        @SerializedName("address")
        val address: String? = "",
        @SerializedName("contact_person")
        val contactPerson: String? = "",
        @SerializedName("description")
        val description: String? = "",
        @SerializedName("head_office_id")
        val headOfficeId: String? = "",
        @SerializedName("id")
        val id: Int? = 0,
        @SerializedName("mobile_no")
        val mobileNo: String? = "",
        @SerializedName("name")
        val name: String? = "",
        @SerializedName("tags")
        val tags: List<Tag?>? = listOf(),
        @SerializedName("zip_code")
        val zipCode: String? = ""
    ) : Parcelable {

        @Parcelize
        data class Tag(
            @SerializedName("name")
            val name: String? = "",
            @SerializedName("tag_id")
            val tagId: String? = ""
        ) : Parcelable

        fun getTagNames(): String? {
            return tags?.map { it?.name }?.joinToString()
        }

    }
}