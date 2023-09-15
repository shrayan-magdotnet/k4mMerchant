package com.kash4me.data.models.staff


import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class StaffDetailsResponse(
    @SerializedName("name")
    val name: String? = "",
    @SerializedName("user_id")
    val userId: String? = ""
) : Parcelable