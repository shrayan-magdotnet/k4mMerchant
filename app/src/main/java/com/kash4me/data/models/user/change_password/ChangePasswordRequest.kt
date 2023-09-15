package com.kash4me.data.models.user.change_password


import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class ChangePasswordRequest(
    @SerializedName("new_password1")
    val newPassword1: String? = "",
    @SerializedName("new_password2")
    val newPassword2: String? = "",
    @SerializedName("old_password")
    val oldPassword: String? = ""
)