package com.kash4me.data.models.merchant.sub_user_settings.reset_staff_password


import com.google.gson.annotations.SerializedName

data class ResetStaffPasswordRequest(
    @SerializedName("new_password")
    val newPassword: String? = "",
    @SerializedName("staff_id")
    val staffId: Int? = 0
)