package com.kash4me.data.models.merchant.sub_user_settings.add_sub_user


import com.google.gson.annotations.SerializedName
import com.kash4me.data.models.merchant.sub_user_settings.SubUserResponse

data class AddSubUserResponse(
    @SerializedName("staff_lists")
    val staffLists: List<SubUserResponse?>? = listOf()
)