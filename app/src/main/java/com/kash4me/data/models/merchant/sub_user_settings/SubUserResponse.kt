package com.kash4me.data.models.merchant.sub_user_settings


import com.google.gson.annotations.SerializedName

data class SubUserResponse(
    @SerializedName("id")
    val id: Int? = 0,
    @SerializedName("nick_name")
    val nickName: String? = "",
    @SerializedName("user_id")
    val userId: String? = ""
)