package com.kash4me.data.models.merchant.sub_user_settings

import com.google.gson.annotations.SerializedName

data class StaffLists(
    @SerializedName("id")
    val id: Int? = 0,
    @SerializedName("name")
    val name: String? = "",
    @SerializedName("password")
    val password: String? = "",
    @SerializedName("user_id")
    val userId: String? = ""
)