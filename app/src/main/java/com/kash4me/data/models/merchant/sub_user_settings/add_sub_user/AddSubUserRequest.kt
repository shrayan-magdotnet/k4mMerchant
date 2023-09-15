package com.kash4me.data.models.merchant.sub_user_settings.add_sub_user


import com.google.gson.annotations.SerializedName

data class AddSubUserRequest(
    @SerializedName("nick_name")
    val nickName: String? = "",
    @SerializedName("password")
    val password: String? = "",
    @SerializedName("shop_id")
    val shopId: Int? = 0,
    @SerializedName("user_id")
    val userId: String? = ""
)